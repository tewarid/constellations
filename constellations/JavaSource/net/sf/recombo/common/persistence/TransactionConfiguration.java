
package net.sf.recombo.common.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.recombo.common.SystemRuntimeException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Utility class used to represent transaction configuration. 
 */
public class TransactionConfiguration extends DefaultHandler {
	private static String className;
	Map classes = new HashMap();
	private static TransactionConfiguration instance;
	
	public static TransactionConfiguration getInstance() {
		if (instance == null) {
			instance = new TransactionConfiguration();
		}
		return instance;
	}
	
	/**
	 * Constructor intitiliazes this object instance by reading the 
	 * specified configuration file.  
	 * @param configFileName Configuration file name
	 */
	private TransactionConfiguration(String configFileName) {
		InputStream in;
		try {
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        SAXParser parser = factory.newSAXParser();
	        
	        // Try System class loader
	        in = ClassLoader.getSystemClassLoader().getResourceAsStream(configFileName);
	        
	        // Try Thread specific class loader
	        if (in == null) {
	        	in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName);
	        }
	        
	        if (in == null) {
				System.err.println("Failed to Load transaction configuration file " + configFileName + ".");
				throw new SystemRuntimeException("Failed to Load transaction configuration file " + configFileName + ".", null);
	        } else {
				parser.parse(in, this);
	        }
		} catch (ParserConfigurationException e) {
			System.err.println(configFileName + ": parser configuration error.");
			e.printStackTrace(System.err);
			throw new SystemRuntimeException(configFileName + ": parser configuration error.", null, e);
		} catch (SAXException e) {
			System.err.println(configFileName + ": parse error.");
			e.printStackTrace(System.err);
			throw new SystemRuntimeException(configFileName + ": parse error.", null, e);
		} catch (IOException e) {
			System.err.println(configFileName + ": IO exception.");
			e.printStackTrace(System.err);
			throw new SystemRuntimeException(configFileName + ": IO exception.", null, e);
		}
	}
	
	/**
	 * Default constructor. The transaction configuration is read from
	 * a file names transaction-config.xml.
	 */
	private TransactionConfiguration() {
		this("transaction-config.xml");
	}
	
	/**
	 * The method returns true if the method of the specified class is
	 * transactional.
	 * 
	 * @param className
	 *            Fully qualified name of the class
	 * @param methodName
	 *            Method name
	 * @return true if the method is specified as transactional.
	 */
	public boolean isTransaction(String className, String methodName) {
		return hasMethod(className, methodName);
	}

	/**
	 * The method returns true if the given Method instance is transactional.
	 * 
	 * @param method
	 *            Method instance
	 * @return true if the Method instance is specified as transactional.
	 */
	public boolean isTransaction(Method method) {
		boolean val;
		val = isTransaction(method.getDeclaringClass().getName(), method
				.getName());
		if (val == false) {
			// let us check if super classes / interfaces are transactions
			for (int i = 0; i < method.getDeclaringClass().getInterfaces().length; i++) {
				val = isTransaction(
						method.getDeclaringClass().getInterfaces()[i].getName(),
						method.getName());
				if (val == true)
					break;
			}
		}
		return val;
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("class")) {
			className = attributes.getValue("type");
			addClass(className);
		}
		if (qName.equals("method")) {
			addMethod(className, attributes.getValue("name"));
		}
	}
	
	/**
	 * Add the given class name to the transaction
	 * configuration.
	 * @param className Fully qualified name of the class
	 */
	public void addClass(String className) {
		if (!classes.containsKey(className)) {
			classes.put(className, new HashSet());
		}
	}

	/**
	 * Add the given Class instance to the transaction
	 * configuration.

	 * @param classObj Class instance
	 */
	public void addClass(Class classObj) {
		addClass(classObj.getName());
	}

	/**
	 * Add method of the specified class to the transaction
	 * configuration. Any calls to isTransaction for the
	 * specified method should then return true. 

	 * @param classObj Class instance
	 * @param methodName Method name
	 */
	public void addMethod(Class classObj, String methodName) {
		addMethod(classObj.getName(), methodName);
	}

	/**
	 * Add method of the specified class to the transaction
	 * configuration. Any calls to isTransaction for the
	 * specified method should then return true.
	 *  
	 * @param className Fully qualified name of the class
	 * @param methodName Method name
	 */
	public void addMethod(String className, String methodName) {
		if (!classes.containsKey(className)) {
			addClass(className);
		}
		HashSet methods = (HashSet)classes.get(className);
		methods.add(methodName);
	}
	
	/**
	 * Add the specified method instance to the transaction
	 * configuration. Any calls to isTransaction for the
	 * specified method should then return true.
	 *  
	 * @param method Method instance
	 */
	public void addMethod(Method method) {
		addMethod(method.getClass(), method.getName());
	}
	
	/**
	 * Check if the transaction configuration has the Method
	 * instance specified.
	 * @param method Method instance
	 * @return true if the specified Method instance exists
	 */
	public boolean hasMethod(Method method) {
		boolean has = false;
		has = hasMethod(method.getClass(), method.getName());
		return has;
	}
	
	/**
	 * Check if the transaction configuration has the method
	 * of the specified Class instance.
	 * 
	 * @param classObj Class instance
	 * @param methodName Method name
	 * @return true if the specified method exists
	 */
	public boolean hasMethod(Class classObj, String methodName) {
		boolean has = false;
		has = hasMethod(classObj.getName(), methodName);
		return has;
	}
	
	/**
	 * Check if the transaction configuration has the method
	 * of the specified Class instance.
	 * 
	 * @param className
	 * @param methodName
	 * @return true if the specified method exists
	 */
	public boolean hasMethod(String className, String methodName) {
		boolean has = false;
		if (classes.containsKey(className)) {
			has = ((HashSet)classes.get(className)).contains(methodName);
		}
		return has;
	}
	
	/**
	 * Check if the transaction configuration has the 
	 * specified class.
	 * 
	 * @param className
	 * @return true if the specified class exists
	 */
	public boolean hasClass(String className) {
		return classes.containsKey(className);
	}

	/**
	 * Check if the transaction configuration has the 
	 * specified Class instance.
	 * 
	 * @param classObj
	 * @return true if the specified class exists
	 */
	public boolean hasClass(Class classObj) {
		return hasClass(classObj.getName());
	}

}
