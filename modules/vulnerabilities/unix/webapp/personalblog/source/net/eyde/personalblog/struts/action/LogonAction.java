package net.eyde.personalblog.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.struts.form.LogonForm;

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
 * @author
 * @author     <a href="mailto:jorge@bcs.org.uk">Jorge Basto</a>
 * @created
 * @modified   June 29, 2003
 */
public final class LogonAction extends BlogGeneralAction {
    private static Log log = LogFactory.getLog(LogonAction.class);
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

        ActionErrors errors = new ActionErrors();

// is this code really needed?
// we could have a method at superclass just to do the default task, I think we could use the execute, like method=execute

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

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
    public ActionForward executeFinish(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ActionErrors errors = new ActionErrors();

        // Get request parameters
        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        LogonForm logonForm = (LogonForm) form;

        String username = logonForm.getUserName();
        String password = logonForm.getPassword();

        if (pblog.checkLogIn(username,password)) {
            session.setAttribute("signedIn", "true");
        } else {
            errors.add( ActionErrors.GLOBAL_ERROR,
                    new ActionError("authentication.failure") );
            session.removeAttribute("signedIn");
        }

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return forwardFailure( mapping );
        }

        return forwardSuccess( mapping );
    }
}
 // end of the class LogonAction
