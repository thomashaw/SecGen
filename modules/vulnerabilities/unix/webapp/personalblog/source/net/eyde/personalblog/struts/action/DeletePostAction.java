package net.eyde.personalblog.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.eyde.personalblog.service.PersonalBlogService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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
public final class DeletePostAction extends BlogGeneralAction {

    private static Log log = LogFactory.getLog(DeletePostAction.class);
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
    public ActionForward executeStart(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
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
        } else {
            return forwardSuccess( mapping );
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
    public ActionForward executeFinish(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ActionErrors errors = new ActionErrors();

        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();
        if (!isSignedIn(request)) {
            if (log.isDebugEnabled()){
               log.debug("Authentication missing");
            }
            errors.add( ActionErrors.GLOBAL_ERROR,
                    new ActionError("authentication.missing") );
            saveErrors(request, errors);
            return forwardFailure( mapping );
        }

        pblog.deletePost(request.getParameter("postId"));

        return forwardSuccess( mapping );
    }
}
