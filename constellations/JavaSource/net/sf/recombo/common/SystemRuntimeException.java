package net.sf.recombo.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class represents a system exception. 
 */
public class SystemRuntimeException extends RuntimeException {

    private List messages = null;  // A list of messages or message identifiers 
    private List arguments = null; // A list of message argument maps

    /**
     * Default constructor
     */
    public SystemRuntimeException() {
        super();
    }

    /**
     * Construct a BusinessException from a single message and it´s arguments.
     * @param message A message.
     * @param arguments Message arguments.
     */
    public SystemRuntimeException(String message, Map arguments) {
        addMessage(message, arguments);
    }

    /**
     * Construct a BusinessException from a message, it's arguments and a
     * root cause.
     * @param message A message.
     * @param arguments Message arguments.
     * @param cause Root cause.
     */
    public SystemRuntimeException(String message, Map arguments, Throwable cause) {
        super(cause);
        addMessage(message, arguments);
    }

    /**
     * Construct a BusinessException given a root cause.
     * @param cause Root cause.
     */
    public SystemRuntimeException(Throwable cause) {
    	super(cause);
    }

    /**
     * Construct a BusinessException from a list of messages and their arguments.
     * @param messages A list of messages
     * @param arguments A list of arguments maps
     */
    public SystemRuntimeException(List messages, List arguments) {
    	addMessages(messages, arguments);
    }

    /**
     * Return a list of messages.
     * @return Message list.
     */
    public List getMessages() {
        return messages;
    }

    /**
     * Read the list of argument maps.
     * @return Arguments map list.
     */
    public List getArguments() {
        return arguments;
    }

    /**
     * Add a message and it's arguments.
     * @param message A message
     * @param arguments Arguments map
     */
    public void addMessage(String message, Map arguments) {
    	if (messages == null) {
            this.messages = new LinkedList();
    	}
    	if (arguments == null) {
    		this.arguments = new LinkedList();
    	}
        this.messages.add(message);
        this.arguments.add(arguments);
    }
    
    /**
     * Add messages and their corresponding arguments. The 
     * number of elements in each of the lists must be the same.
     * This means that the following should return true:
     * <pre>messages.size() == arguments.size().</pre>
     * @param messages A list of messages
     * @param arguments A list of argument maps
     */
    public void addMessages(List messages, List arguments) {
    	if (messages.size() != arguments.size())
			throw new IllegalArgumentException();
     	
    	for (int i = 0; i < messages.size(); i++) {
    		addMessage((String)messages.get(i), (Map)arguments.get(i));
    	}
    }

}
