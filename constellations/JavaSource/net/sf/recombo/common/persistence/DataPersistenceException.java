 
package net.sf.recombo.common.persistence;

import java.util.List;
import java.util.Map;

import net.sf.recombo.common.SystemRuntimeException;

/**
 * Exception thrown when there is any problem with the data persistence
 * mechanism.
 */
public class DataPersistenceException extends SystemRuntimeException {
	/**
	 * Default constructor
	 */
	public DataPersistenceException() {
		super();
	}
	/**
	 * @param messages
	 * @param arguments
	 */
	public DataPersistenceException(List messages, List arguments) {
		super(messages, arguments);
	}
	/**
	 * @param message
	 * @param arguments
	 */
	public DataPersistenceException(String message, Map arguments) {
		super(message, arguments);
	}
	/**
	 * @param message
	 * @param arguments
	 * @param cause
	 */
	public DataPersistenceException(String message, Map arguments,
			Throwable cause) {
		super(message, arguments, cause);
	}
	/**
	 * @param cause
	 */
	public DataPersistenceException(Throwable cause) {
		super(cause);
	}
}
