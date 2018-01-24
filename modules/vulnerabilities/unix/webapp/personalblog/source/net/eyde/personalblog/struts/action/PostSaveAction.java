package net.eyde.personalblog.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eyde.personalblog.beans.Post;
import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.struts.form.NewPostForm;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *  Description of the Class
 *
 *@author     NEyde
 *@created    September 17, 2002
 */
public final class PostSaveAction extends BlogGeneralAction {
	private static Log log = LogFactory.getLog(PostSaveAction.class);

	// ----------------------------------- Public Methods

	/**
	 *  Process the specified HTTP request, and create the corresponding HTTP
	 *  response (or forward to another web component that will create it). Return
	 *  an ActionForward instance describing where and how control should be
	 *  forwarded, or null if the response has already been completed.
	 *
	 *@param  mapping               The ActionMapping used to select this instance
	 *@param  request               The HTTP request we are processing
	 *@param  response              The HTTP response we are creating
	 *@param  form                  Description of the Parameter
	 *@return                       Description of the Return Value
	 *@exception  IOException       if an input/output error occurs
	 *@exception  ServletException  if a servlet exception occurs
	 */
	public ActionForward executeStart(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception {

		if (isSignedIn(request)) {
			
                    PersonalBlogService pblog = PersonalBlogService.getInstance(); 
                    Post newPost = new Post();
                    request.setAttribute("editpost",newPost);
                    request.setAttribute("editorPage", "/weblog/" + pblog.getPropertyManager().getProperty("weblog.editor"));
                    request.setAttribute("availableCategories", pblog.getCategories());
                    NewPostForm newPostForm= (NewPostForm) form;
                     
			return (mapping.findForward("success"));
		} else {
			log.debug("tried to post and Post and was not logged");
			return (mapping.findForward("cancel"));
		}
	}

	/**
	 *  Process the specified HTTP request, and create the corresponding HTTP
	 *  response (or forward to another web component that will create it). Return
	 *  an ActionForward instance describing where and how control should be
	 *  forwarded, or null if the response has already been completed.
	 *
	 *@param  mapping               The ActionMapping used to select this instance
	 *@param  request               The HTTP request we are processing
	 *@param  response              The HTTP response we are creating
	 *@param  form                  Description of the Parameter
	 *@return                       Description of the Return Value
	 *@exception  IOException       if an input/output error occurs
	 *@exception  ServletException  if a servlet exception occurs
	 */
	public ActionForward executeFinish(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception {

		ActionErrors errors = new ActionErrors();

		// Get instance of PersonalBlog Service
		PersonalBlogService pblog = PersonalBlogService.getInstance();

		// Check that user is logged in

		if (!isSignedIn(request)) {
			return (mapping.findForward("success"));
		}

		// Obtain all the request parameters from the NewPostForm
		NewPostForm newPostForm = (NewPostForm) form;
		Post newPost = new Post();
		
		//copy properties
		BeanUtils.copyProperties(newPost, newPostForm);
		String[] selectedCats=newPostForm.getSelectedCategories();
		StringBuffer catTemp=new StringBuffer();
		for (int i = 0; i < selectedCats.length; i++) {
            catTemp.append(selectedCats[i]);
        }
		newPost.setCategory(catTemp.toString());
                                
		pblog.createPost(newPost);

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		return (mapping.findForward("success"));
	}

	/**
	 * @return
	 */
}
