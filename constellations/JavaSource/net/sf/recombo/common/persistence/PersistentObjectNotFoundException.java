
package net.sf.recombo.common.persistence;

import java.util.List;
import java.util.Map;

import net.sf.recombo.common.SystemRuntimeException;

/**
 * Exception thrown when a business entity cannot
 * be found in persistent storage.
 */
public class PersistentObjectNotFoundException extends SystemRuntimeException {
	
	/**
	 * Default constructor.
	 */
	public PersistentObjectNotFoundException() {
		super();
	}
	/**
	 * @param messages
	 * @param arguments
	 */
	public PersistentObjectNotFoundException(List messages, List arguments) {
		super(messages, arguments);
	}
	/**
	 * @param message
	 * @param arguments
	 */
	public PersistentObjectNotFoundException(String message, Map arguments) {
		super(message, arguments);
	}
	/**
	 * @param message
	 * @param arguments
	 * @param cause
	 */
	public PersistentObjectNotFoundException(String message, Map arguments,
			Throwable cause) {
		super(message, arguments, cause);
	}
	/**
	 * @param cause
	 */
	public PersistentObjectNotFoundException(Throwable cause) {
		super(cause);
	}
}
