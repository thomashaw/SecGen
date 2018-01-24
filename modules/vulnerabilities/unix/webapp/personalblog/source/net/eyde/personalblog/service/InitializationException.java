/*
 * Created on 18/07/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

/**
 * @author Emerson
 *
 * This Exception is common to all Initialization problems
 * Use errorCode to create more options of different approaches to take
 * 
 */
public class InitializationException extends ServiceException {

	
	


    /**
     * 
     */
    public InitializationException() {
        super();
    }

    /**
     * @param message
     */
    public InitializationException(String message) {
        super(message);
  
    }

    /**
     * @param cause
     */
    public InitializationException(Throwable cause) {
        super(cause);
        
    }

    /**
     * @param message
     * @param cause
     */
    public InitializationException(String message, Throwable cause) {
        super(message, cause);

    }

    


}
