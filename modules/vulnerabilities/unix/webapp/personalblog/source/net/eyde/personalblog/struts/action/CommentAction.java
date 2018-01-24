package net.eyde.personalblog.struts.action;

import java.util.Date;

import net.eyde.personalblog.beans.Comment;
import net.eyde.personalblog.beans.Post;
import net.eyde.personalblog.service.Email;
import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.struts.form.CommentForm;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 *  Description of the Class
 *
 * @author     NEyde
 * @author     <a href="mailto:jorge@bcs.org.uk">Jorge Basto</a>
 * @created    September 9, 2002
 * @modified   June 29, 2003
 * @todo: implement Open Session in View,
 *        persistence services should be within try/catch block, unless using
 *        declarative handling,
 *        handle error msgs,
 */
public final class CommentAction extends BlogGeneralAction {
    private static Log log = LogFactory.getLog(CommentAction.class);

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
    public ActionForward executeFinish(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        ActionErrors errors = new ActionErrors();

        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        //Get the post for comment
        Post y = pblog.getPost(request.getParameter("postId"));

        if (y != null) {
            // Obtain all the request parameters from the Comment
            CommentForm newCommentForm = (CommentForm) form;
            Comment newComment = new Comment();
            BeanUtils.copyProperties(newComment, newCommentForm);

            //could this be a property of the FormBean?
            newComment.setPost(y);
            newComment.setCreated(new Date());

            y.addComment(newComment);
            pblog.updatePost(y);

			log.debug("Send email notification:"+y.getEmailComments());
            if (y.getEmailComments().booleanValue() == true) {
                String toAddress = pblog.getPropertyManager().getProperty(PersonalBlogService.WEBLOG_EMAIL);
                String mailHost = pblog.getPropertyManager().getProperty(PersonalBlogService.EMAIL_HOST);
                Email.sendMessage("Reply to post " + y.getTitle(), newComment.getContent(), toAddress, mailHost);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to extract parent post");
            }

            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("post.missing"));
        }

        if (!errors.isEmpty()) {
            saveErrors(request, errors);

            return forwardInput(mapping);
        }

        // If there were no errors, should we give success messages?
        // Remove obsolete form bean
        removeFormBean(mapping, request);

        return forwardSuccess(mapping);
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
    public ActionForward executeStart(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ActionErrors errors = new ActionErrors();

        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        String postId = request.getParameter("postId");

        // I would keep these in the form
        request.setAttribute("post", pblog.getPost(postId));
        request.setAttribute("postId", postId);
        checkEmoticonsLoaded();

        if (!errors.isEmpty()) {
            saveErrors(request, errors);

            return forwardFailure(mapping);
        }

        return forwardSuccess(mapping);
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
    public ActionForward executeRecent(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        ActionErrors errors = new ActionErrors();

        checkEmoticonsLoaded();

        if (!errors.isEmpty()) {
            saveErrors(request, errors);

            return forwardFailure(mapping);
        }

        return forwardSuccess(mapping);
    }
}
