/*
 * Created on 10/02/2005
 */
package net.sf.recombo.common.persistence;

import net.sf.recombo.common.SystemRuntimeException;

/**
 * @author Devendra Tewari
 */
public abstract aspect TransactionAspect {
	
    public abstract pointcut transactedMethods();

    Object around() throws SystemRuntimeException : transactedMethods() {
		boolean useTransaction = false; 
		Object retValFromSuper = null;
		
		String methodName = thisJoinPoint.getSignature().getName();
		String className = thisJoinPoint.getSignature()
				.getDeclaringType().getName();
		
		// The TransactionConfiguration determines if a method
		// should be made transactional or not.
		useTransaction = TransactionConfiguration.getInstance()
				.isTransaction(className, methodName);
		
		if ( useTransaction ) {
			System.out.println("TransactionAspect: Method '" 
					+ className + "." + methodName + "' is transactional.");
		    try {
		    	// begin the transaction.
		        HibernateUtil.getInstance().beginTransaction();
		    		    
		        // "proceed()" allows the method to continue as it normally
		        // would. The return value is saved.
		        retValFromSuper = proceed();
		        
		        HibernateUtil.getInstance().commitTransaction();
		        
				return retValFromSuper;
		    } catch (RuntimeException e) {
				System.out.println("TransactionAspect: Method '" 
						+ className + "." + methodName + "' threw an exception.");
		        HibernateUtil.getInstance().rollbackTransaction();
		        throw e;
		    }
		}

		// This means the method was not transactional.
		System.out.println("TransactionAspect: Method '" 
				+ className + "." + methodName + "' is NOT transactional.");
		
		// Return the result of the action method as normal.
	    return proceed();
    }
}
