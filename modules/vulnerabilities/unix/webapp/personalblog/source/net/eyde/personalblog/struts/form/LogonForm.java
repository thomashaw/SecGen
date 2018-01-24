package net.eyde.personalblog.struts.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


public final class LogonForm extends ActionForm {
    // ---------------------------Properties (Instance Variables)
    // ---------------------------Accessors (getters and setters)
    // ----------------------------- Public Methods
    String userName;
    String password;

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    } // end of the method reset()

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an ActionErrors object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * null or an ActionErrors object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping, 
                                 HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        /* Turn these on if you want to validate the request parameters in this ActionForm */
        /*
                        
        if ("/logon".equals(mapping.getPath()))  {
                        
        if ((name == null) || (name.length() < 1))
             errors.add("name", new ActionError("error.name.required"));
                        
        //add validation for checkbox "subscribe" in here
                        
        }
                        
        */
        return errors;
    } // end of the method validate()

    public String getUserName() {
        return userName;
    }

    public void setUserName(String newUserName) {
        userName = newUserName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String newPassword) {
        password = newPassword;
    }
} // end of the class LogonForm/* 
