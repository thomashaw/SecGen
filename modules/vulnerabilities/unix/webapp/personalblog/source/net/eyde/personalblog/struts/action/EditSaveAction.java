package net.eyde.personalblog.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.eyde.personalblog.beans.Post;
import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.struts.form.EditPostForm;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author
 * @author     <a href="mailto:jorge@bcs.org.uk">Jorge Basto</a>
 * @created
 * @modified   June 29, 2003
 * @todo: implement Open Session in View,
 *        persistence services should be within try/catch block, unless using
 *        declarative handling,
 *        handle error msgs,
 */
public final class EditSaveAction extends BlogGeneralAction {

    private static Log log = LogFactory.getLog(EditSaveAction.class);
    // ----------------------------------- Public Methods

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an ActionForward instance describing where and how
     * control should be forwarded, or null if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward executeStart(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ActionErrors errors = new ActionErrors();

        if (!isSignedIn(request)) {
            if (log.isDebugEnabled()){
               log.debug("Authentication missing");
            }
            errors.add( ActionErrors.GLOBAL_ERROR,
                    new ActionError("authentication.missing") );
            saveErrors(request, errors);
            return forwardCancel( mapping );
        }
        // Get request parameters
        String postId = request.getParameter("editId");
            
        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();
        request.setAttribute("editorPage", "/weblog/" + pblog.getPropertyManager().getProperty("weblog.editor"));
        request.setAttribute("editpost", pblog.getPost(postId));
        request.setAttribute("cat", pblog.getCategories());
        return forwardSuccess( mapping );
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an ActionForward instance describing where and how
     * control should be forwarded, or null if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward executeFinish(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {
        ActionErrors errors = new ActionErrors();

        if (!isSignedIn(request)) {
            if (log.isDebugEnabled()){
               log.debug("Authentication missing");
            }
            errors.add( ActionErrors.GLOBAL_ERROR,
                    new ActionError("authentication.missing") );
        }
        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        // Obtain all the request parameters from the EditPostForm
        EditPostForm editPostForm = (EditPostForm) form;
        Post editPost = pblog.getPost(editPostForm.getId());

        //use copy properties of beanutils
        BeanUtils.copyProperties( editPost, editPostForm );

		String[] selectedCats=editPostForm.getSelectedCategories();
		StringBuffer catTemp=new StringBuffer();
		for (int i = 0; i < selectedCats.length; i++) {
			catTemp.append(selectedCats[i]);
		}
		editPost.setCategory(catTemp.toString());

        pblog.updatePost(editPost);

        // build referer list

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return forwardFailure( mapping );
        }

        return forwardSuccess( mapping );
    }
}
// end of the class EditSaveAction
