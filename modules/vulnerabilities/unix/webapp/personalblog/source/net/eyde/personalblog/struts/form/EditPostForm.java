package net.eyde.personalblog.struts.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


public final class EditPostForm extends ActionForm {
    // ---------------------------Properties (Instance Variables)
    // ---------------------------Accessors (getters and setters)
    // ----------------------------- Public Methods

	// ---------------------------Properties (Instance Variables)
	private String title = null;
	private String id = null;
	private String content = null;
	private String category = null;
	private String emailComments = null;
	private String[] selectedCategories = null;
	// ---------------------------Accessors (getters and setters)
	public String getTitle() {
		return (this.title);
	}

	public void setTitle(String atitle) {
		this.title = atitle;
	}

	public String getContent() {
		return (this.content);
	}

	public void setContent(String acontent) {
		this.content = acontent;
	}

	// ----------------------------- Public Methods

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
                        
		if ("/postsave".equals(mapping.getPath()))  {
                        
		if ((name == null) || (name.length() < 1))
			 errors.add("name", new ActionError("error.name.required"));
                        
		//add validation for checkbox "subscribe" in here
                        
		}
                        
		*/
		return errors;
	} // end of the method validate()
	/**
	 * Returns the category.
	 * @return String[]
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category.
	 * @param category The category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

		/** Getter for property emailComments.
		 * @return Value of property emailComments.
		 *
		 */
		public java.lang.String getEmailComments() {
			return emailComments;
		}
        
		/** Setter for property emailComments.
		 * @param emailComments New value of property emailComments.
		 *
		 */
		public void setEmailComments(java.lang.String emailComments) {
			this.emailComments = emailComments;
		}
        
	/**
	 * @return
	 */
	public String[] getSelectedCategories() {
		return selectedCategories;
	}

	/**
	 * @param strings
	 */
	public void setSelectedCategories(String[] strings) {
		selectedCategories = strings;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

} // end of the class EditPostForm
