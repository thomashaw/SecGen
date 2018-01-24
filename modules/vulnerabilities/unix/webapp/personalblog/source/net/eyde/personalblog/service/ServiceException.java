/*
 * Created on 19/06/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

/**
 * @author Administrador
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ServiceException extends Exception {

    /**
     *
     */
    public ServiceException() {
        super();
    }

    /**
     * @param message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

}
