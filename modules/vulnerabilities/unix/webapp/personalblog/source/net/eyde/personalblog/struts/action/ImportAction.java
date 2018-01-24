package net.eyde.personalblog.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.eyde.personalblog.service.*;

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
 */
public final class ImportAction extends BlogGeneralAction {

    private static Log log = LogFactory.getLog(ImportAction.class);
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
        HttpSession session = request.getSession();
        ActionErrors errors = new ActionErrors();


        ImportExportService ies = new ImportExportService();
        
        // Check that user is logged in
        if (!isSignedIn(request)) {
            if (log.isDebugEnabled()){
               log.debug("Authentication missing");
            }
            errors.add( ActionErrors.GLOBAL_ERROR,
                    new ActionError("authentication.missing") );
            saveErrors(request, errors);
            return forwardFailure( mapping );
        }

        ies.loadDbConnectParms();
        ies.importPostsFromXml();

        return forwardSuccess( mapping );
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

        ActionErrors errors = new ActionErrors();

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return forwardFailure( mapping );
        }

        return forwardSuccess( mapping );
    }
}
