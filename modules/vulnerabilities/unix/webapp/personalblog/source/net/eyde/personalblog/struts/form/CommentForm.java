package net.eyde.personalblog.struts.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


public final class CommentForm extends ActionForm {
    // ---------------------------Properties (Instance Variables)
    // ---------------------------Accessors (getters and setters)
    // ----------------------------- Public Methods
    private String name;
    private String content;
    private String parentId;
    private String email;
	private String url;
    private String emoticon = null;

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
                        
        if ("/editsave".equals(mapping.getPath()))  {
                        
        if ((name == null) || (name.length() < 1))
             errors.add("name", new ActionError("error.name.required"));
                        
        //add validation for checkbox "subscribe" in here
                        
        }
                        
        */
        return errors;
    } // end of the method validate()

    public String getName() {
        return name;
    }

    public void setName(String newTitle) {
        name = newTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String newContent) {
        content = newContent;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String newId) {
        parentId = newId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }

	/**
	 * Returns the emoticon.
	 * @return String[]
	 */
	public String getEmoticon() {
		return emoticon;
	}

	/**
	 * Sets the emoticon.
	 * @param emoticon The emoticon to set
	 */
	public void setEmoticon(String emoticon) {
		this.emoticon = emoticon;
	}

    /**
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param string
     */
    public void setUrl(String string) {
        url = string;
    }

} // end of the class EditPostForm
