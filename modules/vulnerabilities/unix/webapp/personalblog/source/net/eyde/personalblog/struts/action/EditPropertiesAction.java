package net.eyde.personalblog.struts.action;

import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.struts.form.PropertyForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public final class EditPropertiesAction extends BlogGeneralAction {
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
    public ActionForward executeStart(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        log.debug("property editor init");

        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        if (form == null) {
            log.debug("creating new property form for " +
                mapping.getAttribute());
            form = new PropertyForm();
            request.setAttribute(mapping.getAttribute(), form);
        }

        PropertyForm propForm = (PropertyForm) form;

        if (propForm != null) {
            log.debug("property form already exists " + mapping.getAttribute());
            log.debug("initializing property form");
            propForm.setBlogProperties(pblog.getPropertyManager().getProperties());
        }

        return (mapping.findForward("success"));
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
        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        PropertyForm propForm = (PropertyForm) form;

        if (propForm != null) {
            pblog.getPropertyManager().updateProperties(propForm.getBlogProperties());
        }

        return (mapping.findForward("success"));
    }

    /**
     * @return
     */
}
