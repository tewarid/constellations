package net.sf.recombo.common.persistence;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.hibernate.CallbackException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.ReplicationMode;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.metadata.ClassMetadata;
import net.sf.hibernate.type.Type;
import net.sf.recombo.common.SystemRuntimeException;

/**
 * This utility class provides a simple framework for using Hibernate
 * by implementing some <a href="http://www.hibernate.org/40.html">well known</a> 
 * design patterns or by being useful with some of these patterns: 
 * <ul>
 * <li>Thread Local session</li>
 * <li>AspectJ Hibernate aspect</li>
 * <li>Root Persistent Class</li>
 * </ul>
 * 
 * In conjunctions with CGLIB and the TransactionConfiguration class it allows
 * any class to be extended so that methods of it's instances are wrapped with
 * appropriate begin and commit transaction invocations. The transaction
 * requirement can specified in an external xml configuration file.
 * 
 * @see net.sf.hibernate.Session
 * @see net.sf.hibernate.SessionFactory
 */
public final class HibernateUtil implements MethodInterceptor, Interceptor {
    private static HibernateUtil instance;
	
	// Private attributes
    
    // Transaction configuration
	private TransactionConfiguration transactions; 
	// Hibernate Session Factory
	private SessionFactory sessionFactory = null;
	// Number of transactions in progress
	private int transactionCount = 0;             
	
	// Private Thread Local attributes
	
	// Session local to current thread
	private ThreadLocal session = new ThreadLocal();
	// Active transaction local to current thread
	private ThreadLocal transaction = new ThreadLocal();
	// Depth of the active database transaction
	private ThreadLocal transactionDepth = new ThreadLocal(); 
	
	private HibernateUtil() {
	}
	
	/**
	 * Recovers the singleton instance of this class.
	 * @return HibernateUtil instance
	 */
	public synchronized static HibernateUtil getInstance() {
	    if (instance == null) {
	        instance = new HibernateUtil();
	        instance.configure("hibernate.cfg.xml");
	    }
	    return instance;
	}
	
	/**
	 * Configure and initialize this class.
	 * @param filename Name of the Hibernate session factory 
	 * configuration file
	 */
	public void configure(String filename) {
		URL urlConfig;
		
		// Load transaction configuration
		transactions = TransactionConfiguration.getInstance();
		
		try {
			// Try System class loader
			urlConfig = ClassLoader.getSystemResource(filename);
			
			// Try Thread specific class loader
			if (urlConfig == null) {
				urlConfig = Thread.currentThread().getContextClassLoader().getResource(filename);
			}
			
			if (urlConfig == null) {
				System.err.println("Failed to load Hibernate configuration file " + filename + ".");
				throw new SystemRuntimeException("Failed to load Hibernate configuration file " + filename + ".", null);
			} else {
				Configuration cfg = new Configuration();
				cfg = cfg.configure(urlConfig);
				cfg.setInterceptor(this);
				sessionFactory = cfg.buildSessionFactory();
			}
			
		} catch (HibernateException e) {
			System.err.println("Exception building SessionFactory.");
			e.printStackTrace(System.err);
			throw new SystemRuntimeException("Exception building SessionFactory.", null, e);
		}
	}
	
	/**
	 * Recovers a Hibernate session associated with the current thread.
	 * Opens a new session if one doesn't already exist and associates
	 * it to the thread. As long as you don't call the closeSession 
	 * or the rollbackTransaction methods the session remains
	 * open and associated with the current thread.
	 * 
	 * @return A Hibernate session
	 * 
	 * @throws DataPersistenceException If the openSession method
	 * of the Hibernate session factory fails.
	 */
	public Session currentSession() throws DataPersistenceException {
		
		Session s = (Session) session.get();
		// Open a new Session, if this Thread has none yet
		if (s == null) {
			try {
				s = sessionFactory.openSession(this);
				session.set(s);
			} catch (HibernateException e) {
				e.printStackTrace();
				throw new DataPersistenceException(e);
			}
		}
		return s;
	}
	
	/**
	 * Closes and dissociates the Hibernate session associated with 
	 * the current thread.
	 * @throws DataPersistenceException If the close method of the
	 * Hibernate session fails.
	 */
	public void closeSession() throws DataPersistenceException {
		Integer depth = (Integer) transactionDepth.get();
		if (depth != null) {
			// We are within a transaction, ignore close request 
		} else {
			try {
				Session s = (Session) session.get();
				session.set(null);
				if (s != null) {
					s.close();
				}
			} catch (HibernateException e) {
				e.printStackTrace();
				throw new DataPersistenceException(e);
			}
		}
	}
	
	/**
	 * Initiates a new database transaction and associates the
	 * Hibernate Transaction instance with the current thread only on the 
	 * first call to this method. Successive calls to this method will
	 * not initiate any transactions and result in incrementing a
	 * stack counter. This stack counter is later used to determine
	 * the outer-most commit so that the transaction can be committed.
	 * 
	 * @throws DataPersistenceException If the beginTransaction method
	 * of hibernate returns a HibernateException.
	 */
	public void beginTransaction() throws DataPersistenceException {
		Integer depth = (Integer) transactionDepth.get();
		if (depth == null) {
			// first call to begin transaction in this thread 
			Session s = (Session) session.get();
			if (s == null) {
				currentSession();
			}
			s = (Session) session.get();
			try {
				Transaction t = (Transaction) s.beginTransaction();
				transaction.set(t);
				transactionDepth.set(new Integer(0));
				incrementTransactionCount();
			} catch (HibernateException e) {
				throw new DataPersistenceException(e);
			}
		} else {
			// increment depth
			transactionDepth.set(new Integer(depth.intValue() + 1));
		}
	}
	
	/**
	 * Commits a database transaction and dissociates the
	 * Hibernate Transaction instance associated with the 
	 * current thread only if this is the outer-most commit request.
	 * 
	 * @throws DataPersistenceException If the commit method
	 * of hibernate returns a HibernateException or there is
	 * no existing transaction to be committed.
	 */
	public void commitTransaction() throws DataPersistenceException {
		Integer depth = (Integer) transactionDepth.get();
		if (depth == null) {
			// no begin transaction issued
		} else {
			if (depth.intValue() == 0) {
				// reached the outer most call to begin transaction
				Transaction t = (Transaction) transaction.get();
				if (t == null) {
					// no begin transaction issued or rolled back
					throw new DataPersistenceException("Assertion Failed: A transaction should be present but was not found.", null);
				} else {
					try {
						t.commit();
					} catch (HibernateException e) {
						throw new DataPersistenceException(e);
					} finally {
						transaction.set(null);
						transactionDepth.set(null);
						decrementTransactionCount();
					}
				}
			} else {
				// decrement depth, ignore commit request
				transactionDepth.set(new Integer(depth.intValue() - 1));
			}
		}
	}
	
	/**
	 * Rolls back a database transaction and dissociates the
	 * Hibernate Transaction instance associated with the 
	 * current thread.
	 * 
	 * @throws DataPersistenceException If the rollback method
	 * of hibernate returns a HibernateException or there is
	 * no existing transaction to be rolled back.
	 */
	public void rollbackTransaction() throws DataPersistenceException {
		Integer depth = (Integer) transactionDepth.get();
		if (depth == null) {
			// no begin transaction issued
		} else {
			Transaction t = (Transaction) transaction.get();
			if (t == null) {
				// no transaction
				throw new DataPersistenceException("Assertion Failed: A transaction should be present but was not found.", null);
			} else {
				try {
					t.rollback();
				} catch (HibernateException e) {
					throw new DataPersistenceException(e);
				} finally {
					transaction.set(null);
					decrementTransactionCount();
					closeSession();
				}
			}
			// decrement depth
			transactionDepth.set(null);
		}
	}
	
	/**
	 * Loads an instance of a class from the database given
	 * it's class and identifier. 
	 * 
	 * @param classObj Class
	 * @param id Identifier
	 * @return An instance of the specified class with the identifier specified
	 * @throws DataPersistenceException If the load method of the Hibernate 
	 * session generates an HibernateException which is not ObjectNotFoundException.
	 * @throws PersistentObjectNotFoundException If the object cannot be found
	 * in the database.
	 */
	public Object load(Class classObj, Serializable id)
            throws DataPersistenceException, PersistentObjectNotFoundException {
		try {
			return currentSession().load(classObj, id);
		} catch (ObjectNotFoundException e) {
			throw new PersistentObjectNotFoundException(e); 
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}

	/**
	 * Loads the persistent copy of a transient object. Hibernate
	 * class metadata is used to determine the identifier of the 
	 * transient object and then the load method is called.
	 * @param transientObj A transient object
	 * @return A persistent object if it exists
	 * @throws DataPersistenceException If the identifier of the
	 * given transient object cannot be determined.
	 * @throws PersistentObjectNotFoundException If the 
	 * persistent object does not exist.
	 */
	public Object loadPersistent(PersistentObject transientObj) 
			throws DataPersistenceException, PersistentObjectNotFoundException {
			Class classObj = transientObj.getClass(); 
			ClassMetadata meta = getClassMetadata(classObj);
			Serializable key = null;
            try {
                key = meta.getIdentifier(transientObj);
            } catch (HibernateException e) {
                throw new DataPersistenceException(e);
            }
            Object persistent = load(classObj, key);
			return persistent;
	}

	/**
	 * Calls the replicate method of the Hibernate session object with
	 * the given parameters. 
	 * @param object Persistent object to replicate
	 * @param mode Replication mode
	 * @throws DataPersistenceException Wraps a HibernateException
	 */
	public void replicate(Object object, ReplicationMode mode)
            throws DataPersistenceException {
		try {
			currentSession().replicate(object, mode);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Save a transient object to the database.
	 * @param object A transient object
	 * @throws DataPersistenceException If a HibernateException is thrown
	 */
	public void save(PersistentObject object) throws DataPersistenceException {
		try {
			currentSession().save(object);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Save a transient object to the database with the given identifier.
	 * @param object A transient object
	 * @param id
	 * @throws DataPersistenceException If a HibernateException is thrown
	 */
	public void save(PersistentObject object, Serializable id) throws DataPersistenceException {
		try {
			currentSession().save(object, id);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Update a persistent object with the identifier of the given transient object.
	 * If that persistent object is already in the session an exception is thrown. 
	 * This method is useful if you have a detached or transient object with updated 
	 * state and you want to reassociate it with another session. If the
	 * state of the detached or transient object has been changed an sql UPDATE is 
	 * scheduled. See the lock method if you do not want to schedule an UPDATE. 
	 * 
	 * @param object A transient object with updated state
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public void update(PersistentObject object) throws DataPersistenceException {
		try {
			currentSession().update(object);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Either saves a transient object or updates the state of a persistent
	 * object.
	 * 
	 * @param object A transient object
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public void saveOrUpdate(PersistentObject object) throws DataPersistenceException {
		try {
			currentSession().saveOrUpdate(object);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Detaches a persistent object and schedules a DELETE from the database.
	 * 
	 * @param object A persistent object or a transient object whose identifier can
	 * be used to load the persistent object
	 * 
	 * @throws DataPersistenceException
	 */
	public void delete(PersistentObject object) throws DataPersistenceException {
		try {
			currentSession().delete(object);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Detaches a persistent object from a Hibernate session (cache) without
	 * deleting it from the database.
	 * @param object A persistent object
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public void evict(PersistentObject object) throws DataPersistenceException {
		try {
			currentSession().evict(object);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Executes the specified HQL and returns a List of results.
	 * @param query HQL query
	 * @return A List
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public List find(String query) throws DataPersistenceException {
		try {
			return currentSession().find(query);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Executes the specified HQL and returns a List of results. The
	 * query allows one bind parameter. 
	 * @param query HQL query string
	 * @param object Value to be bound to the parameter 
	 * @param type The type of the value
	 * @return A List
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public List find(String query, Object value, Type type)
            throws DataPersistenceException {
		try {
			return currentSession().find(query, value, type);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Executes the specified HQL and returns a List of results. The
	 * query allows bind parameters. 
	 * @param query HQL query string
	 * @param values Values to be bound to parameters 
	 * @param types The corresponding types of the values 
	 * @return A List
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public List find(String query, Object[] values, Type[] types)
            throws DataPersistenceException {
		try {
			return currentSession().find(query, values, types);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}

	/**
	 * Recover class mapping meta-data.
	 * 
	 * @param classObj A Class instance
	 * @return Meta-data
	 * @throws DataPersistenceException Wraps HibernateException 
	 */
	public ClassMetadata getClassMetadata(Class classObj)
            throws DataPersistenceException {
		try {
			return sessionFactory.getClassMetadata(classObj); 
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}

	/**
	 * Recovers meta-data of all mapped classes.
	 * 
	 * @return Meta-data
	 * @throws DataPersistenceException Wraps HibernateException 
	 */
	public Map getAllClassMetadata() throws DataPersistenceException {
		try {
			return sessionFactory.getAllClassMetadata();
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Recovers meta-data of all mapped collections.
	 * 
	 * @return Meta-data
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public Map getAllCollectionMetadata() throws DataPersistenceException {
		try {
			return sessionFactory.getAllCollectionMetadata();
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Print some information about the current state of
	 * this class / thread.
	 *
	 */
	public void printDebugInformation() {
		Session s;
		Transaction t;
		
		System.out.println("This Class");
		System.out.println("-----------");
		System.out.println("Number of transactions active: " + transactionCount);
		System.out.println();
		
		s = (Session) session.get();
		t = (Transaction) transaction.get();
		System.out.println("This Thread");
		System.out.println("-----------");
		System.out.println("Session open: " +  ((session != null) || s.isOpen()));
		System.out.println("Transaction active: " +  (t != null));
		System.out.println("Transaction depth: " + transactionDepth.get());
	}
	
	/***
	 * This method returns a CGLIB extended object of the
	 * specified class. Each method call on the extended object
	 * results in a callback to the intercept method.
	 * @param c The class to extend.
	 * @return A new extended instance of the specified class
	 */
	public Object extendClass(Class c) {
		Object newObject = null;
		newObject = Enhancer.create(c, null, this);
		return newObject;
	}
	
	/**
	 * Wraps the call of an intercepted method with begin and 
	 * commit transactions if it supports transactions.
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {
		
			boolean useTransaction; 
			Object retValFromSuper = null;
			
			useTransaction = transactions.isTransaction(method);
			try {
				if (useTransaction) beginTransaction();
				
				retValFromSuper = proxy.invokeSuper(obj, args);
				
				if (useTransaction) commitTransaction();
				
			}catch (Throwable e) {
				if (useTransaction) rollbackTransaction();
				throw e;
			}
			return retValFromSuper;
	}
	
	/**
	 * Reassociates a transient or detached object with the current
	 * session.
	 * @param object Detached or transient object
	 * @param mode Lock mode
	 * @throws DataPersistenceException Wraps HibernateException
	 */
	public void lock(Object object, LockMode mode) throws DataPersistenceException {
		try {
			currentSession().lock(object, mode);
		} catch (HibernateException e) {
			throw new DataPersistenceException(e);
		}
	}
	
	/**
	 * Increment the number of active transactions.
	 */
	private synchronized void incrementTransactionCount() {
		transactionCount++;
	}
	
	/**
	 * Decrement the number of active transactions.
	 */
	private synchronized void decrementTransactionCount() {
		transactionCount--;
	}
	
	/**
	 * If the specified object is an instance of PersistentObject 
	 * sets the state of the object to saved.
	 * @see net.sf.hibernate.Interceptor#onLoad(java.lang.Object, 
	 * java.io.Serializable, java.lang.Object[], java.lang.String[], 
	 * net.sf.hibernate.type.Type[])
	 */
	public boolean onLoad(Object object, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) 
			throws CallbackException {
		
	    if (object instanceof PersistentObject) {
	    	( (PersistentObject) object ).onLoad();		
	    }
		return false; // we did not modify state
	}

	/**
	 * If the specified object is an instance of AuditedPersistentObject 
	 * sets the dateLastUpdated attribute.
	 * @see net.sf.hibernate.Interceptor#onFlushDirty(java.lang.Object, 
	 * java.io.Serializable, java.lang.Object[], java.lang.Object[], 
	 * java.lang.String[], net.sf.hibernate.type.Type[])
	 */
	public boolean onFlushDirty(Object object, Serializable id, 
			Object[] currentState, Object[] previousState, 
			String[] propertyNames, Type[] types) 
			throws CallbackException {
	    boolean result = false; // we did not modify state

	    if (object instanceof AuditedPersistentObject) {
	        for (int i = 0; i < propertyNames.length; i++) {
	            if ("dateLastUpdated".equals(propertyNames[i])) {
	            	currentState[i] = new Date();
	                result = true;
	                break;
	            }
	        }
	    }

		return result;
	}

	/**
	 * If the specified object is an instance of AuditedPersistentObject 
	 * sets the dateLastUpdated attribute.
	 * @see net.sf.hibernate.Interceptor#onSave(java.lang.Object, 
	 * java.io.Serializable, java.lang.Object[], java.lang.String[], 
	 * net.sf.hibernate.type.Type[])
	 */
	public boolean onSave(Object object, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) 
			throws CallbackException {
	    boolean result = false;

	    if (object instanceof AuditedPersistentObject) {
	        for (int i = 0; i < propertyNames.length; i++) {
	            if ("dateLastUpdated".equals(propertyNames[i])) {
	                state[i] = new Date();
	                result = true;
	                break;
	            }
	        }
	    }
	    
	    if (object instanceof PersistentObject) {
	    	( (PersistentObject) object ).onSave();		
	    }

		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.hibernate.Interceptor#onDelete(java.lang.Object, 
	 * java.io.Serializable, java.lang.Object[], java.lang.String[], 
	 * net.sf.hibernate.type.Type[])
	 */
	public void onDelete(Object object, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) 
			throws CallbackException {
	}

	/* (non-Javadoc)
	 * @see net.sf.hibernate.Interceptor#preFlush(java.util.Iterator)
	 */
	public void preFlush(Iterator objects) throws CallbackException {

	}

	/* (non-Javadoc)
	 * @see net.sf.hibernate.Interceptor#postFlush(java.util.Iterator)
	 */
	public void postFlush(Iterator objects) throws CallbackException {

	}

	/**
	 * If the object is a PersistentObject returns its saved status.
	 * @return true if the PersistentObject already exists in the database.
	 * @see net.sf.hibernate.Interceptor#isUnsaved(java.lang.Object)
	 */
	public Boolean isUnsaved(Object object) {
	    if (object instanceof PersistentObject) {
	        return new Boolean( !( (PersistentObject) object ).isSaved() );
	    } else {
	        return null; // let Hibernate assume default behavior
	    }
	}

	/**
	 * @see net.sf.hibernate.Interceptor#findDirty(java.lang.Object, 
	 * java.io.Serializable, java.lang.Object[], java.lang.Object[], 
	 * java.lang.String[], net.sf.hibernate.type.Type[])
	 */
	public int[] findDirty(Object object, Serializable id, 
			Object[] currentState, Object[] previousState, 
			String[] propertyNames, Type[] types) {
		return null; // let Hibernate assume default behavior
	}

	/**
	 * @see net.sf.hibernate.Interceptor#instantiate(java.lang.Class, 
	 * java.io.Serializable)
	 */
	public Object instantiate(Class classObj, Serializable id)
			throws CallbackException {
		Object object = null; // let Hibernate assume default behavior
		return object; 
	}
	
}
