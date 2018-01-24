/*
 * Created on 26/06/2003
 */
package net.eyde.personalblog.struts.action;

import net.eyde.personalblog.global.GlobalData;
import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.service.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * @author Emerson Cargnin
 * @author <a href="mailto:jorge@bcs.org.uk">Jorge Basto</a>
 * @modified   June 29, 2003
 *
 *  
 */
public abstract class BlogGeneralAction extends DispatchAction {
    private static Log log = LogFactory.getLog(BlogGeneralAction.class);

    /**
     *
     */
    public BlogGeneralAction() {
        super();
    }

    /*
     * Method common to all actions
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        log.debug("execute method at GeneralAction:");

        String state = (String) servlet.getServletContext().getAttribute(PersonalBlogService.INSTALLATION_STATE);
        log.debug("PersonalBlog State:" + state);

        if ((state != null) && !state.equals(PersonalBlogService.STATE_OK)) {
            log.info("PersonalBlog needs to be initialized.");

            // maybe changing all those ifs and use the string from the constant to make the forward
            if (state.equals(PersonalBlogService.STATE_DATABASE_OFF)) {
                log.info("Found database off");

                return (mapping.findForward("database_off"));
            } else if (state.equals(PersonalBlogService.STATE_TABLES_NOT_CREATED)) {
                log.info("tables not created yet");

                return (mapping.findForward("tables_not_created"));
			} else if (state.equals(PersonalBlogService.STATE_MISSING_PROPERTIES)) {
							log.info("missing_properties");

							return (mapping.findForward("missing_properties"));
			}


            return (mapping.findForward("initialize"));
        }

        PersonalBlogService pblog = null;

        try {
            pblog = PersonalBlogService.getInstance();
        } catch (Exception e) {
            log.info("PersonalBlog Service couldn't be started.");
        }

        // check and build referer list
        pblog.checkReferer(request.getHeader("Referer"));

        List referrers = pblog.getReferrers();
        log.debug("qty of referrers:" + referrers.size());
        this.getServlet().getServletContext().setAttribute("refer", referrers);

        List comments = pblog.getRecentComments();

        if (comments != null) {
            request.setAttribute("recentcomments", comments);
        }

        prepareCalendar(request, pblog);
        setLastUsers(request);
        loadCategories(pblog);

        return executeSub(mapping, form, request, response);
    }

    private void loadCategories(PersonalBlogService pblog) {
        // Check if categories have been loaded
        if (this.getServlet().getServletContext().getAttribute("cats") == null) {
            //try {
            this.getServlet().getServletContext().setAttribute("cats",
                pblog.getCategories());

            //} catch (ServiceException se) {
            //    se.printStackTrace();
            //}
        }
    }

    private void prepareCalendar(HttpServletRequest request,
        PersonalBlogService pblog) throws ServiceException {
        // Get Date Urls that will be used for setting calendar links
        request.setAttribute("cal", pblog.getCalendarActions(""));
        request.setAttribute("currMonth", pblog.getCurrMonth(""));
        request.setAttribute("prevMonth", pblog.getPrevMonth(""));
        request.setAttribute("nextMonth", pblog.getNextMonth(""));
        request.setAttribute("links", pblog.getLinks());
    }

    protected boolean isSignedIn(HttpServletRequest request) {
        String signedIn = (String) request.getSession().getAttribute("signedIn");

        if ((signedIn == null) || !signedIn.equals("true")) {
            return false;
        } else {
            return true;
        }
    }

    protected void setLastUsers(HttpServletRequest request) {
        //		using host as key
        String host = request.getRemoteHost();
        int count;

        if (GlobalData.getUsersMap().containsKey(host)) {
            count = ((Integer) GlobalData.getUsersMap().get(host)).intValue();
            count++;
        } else {
            count = 1;
        }

        GlobalData.getUsersMap().put(host, new Integer(count));
        log.info("host:" + request.getRemoteHost() + " / counter:" + count);

        //show the users
        // I think this does not escalate well -it should be a table as well.
        request.setAttribute("iplog", GlobalData.getUsersMap());
    }

    protected String cleanNull(String val) {
        if ((val == null) || val.trim().equals("")) {
            return "";
        } else {
            return val;
        }
    }

    /**
     *
     * Subclasses that want to be dispatcher, can't implement this method,
     * as subclasse that otherwise would subclass Action,
     * should subclass this one. Maybe cut this in two classes,
     * one DispatcherAction subclass, and other Action sibclass
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward executeSub(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        String actionParameter=mapping.getParameter();
        String dispatch=request.getParameter(actionParameter);
        if (dispatch!=null)
        	return dispatchMethod(mapping, form, request, response,
            dispatch);
            else
            return null;
    }

    protected void checkEmoticonsLoaded() throws Exception {
        PersonalBlogService pblog = PersonalBlogService.getInstance();

        // Check if emoticons have been loaded
        if (this.getServlet().getServletContext().getAttribute("emoticons") == null) {
            this.getServlet().getServletContext().setAttribute("emoticons",
                pblog.getEmoticons());
        }
    }

    /**
     * Convenience method for removing the obsolete {@link ActionFrom} form bean.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param request The HTTP request we are processing
     */
    protected void removeFormBean(ActionMapping mapping,
        HttpServletRequest request) {
        // Remove the obsolete form bean
        if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope())) {
                request.removeAttribute(mapping.getAttribute());
            } else {
                HttpSession session = request.getSession();
                session.removeAttribute(mapping.getAttribute());
            }
        }
    }

    /**
     * Convenience method to update an {@link ActionForm} form bean in it's scope
     *
     * @param mapping The ActionMapping used to select this instance
     * @param request The HTTP request we are processing
     * @param form The ActionForm
     */
    protected void updateFormBean(ActionMapping mapping,
        HttpServletRequest request, ActionForm form) {
        // Update the form bean
        if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope())) {
                request.setAttribute(mapping.getAttribute(), form);
            } else {
                HttpSession session = request.getSession();
                session.setAttribute(mapping.getAttribute(), form);
            }
        }
    }

    /**
     * Convenience method for getting an {@link ActionForm} based on it's
     * mapped scope.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param request The HTTP request we are processing
     *
     * @return ActionForm the form from the specified scope, or <code>null</code>
     *  if nothing was found
     */
    protected ActionForm getActionForm(ActionMapping mapping,
        HttpServletRequest request) {
        ActionForm actionForm = null;

        // Retrieve the form bean
        if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope())) {
                actionForm = (ActionForm) request.getAttribute(mapping.getAttribute());
            } else {
                HttpSession session = request.getSession();
                actionForm = (ActionForm) session.getAttribute(mapping.getAttribute());
            }
        }

        return actionForm;
    }

    /**
     * Forwards control to the calling component.
     * @param mapping the ActionMapping used to select this instance
     * @return ActionForward specified as the input URI
     */
    protected ActionForward forwardInput(ActionMapping mapping) {
        return new ActionForward(mapping.getInput());
    }

    /**
     * Forwards control to the specified success URI.
     * @param mapping the ActionMapping used to select this instance
     * @return ActionForward specified as the success URI
     */
    protected ActionForward forwardSuccess(ActionMapping mapping) {
        return mapping.findForward(GlobalData.SUCCESS_KEY);
    }

    /**
     * Forward control to the failure URI.
     * @param mapping the ActionMapping used to select this instance
     * @return ActionForward specified as the failure URI
     */
    protected ActionForward forwardFailure(ActionMapping mapping) {
        return mapping.findForward(GlobalData.FAILURE_KEY);
    }

    /**
     * Forward control to the specified cancel URI.
     * @param mapping the ActionMapping used to select this instance
     * @return ActionForward specified as the cancel URI
     */
    protected ActionForward forwardCancel(ActionMapping mapping) {
        return mapping.findForward(GlobalData.CANCEL_KEY);
    }

    /**
     * Convenience method to set a {@link Cookie} on the remote host.
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are processing
     * @param name The {@link Cookie} name
     * @param value The {@link Cookie} value
     * @return HttpServletResponse with the {@link Cookie} added
     */
    protected HttpServletResponse setCookies(HttpServletRequest request,
        HttpServletResponse response, String name, String value, int expiry)
        throws IOException {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(expiry);
        response.addCookie(cookie);

        return response;
    }

    /**
     * Convenience method to delete all {@link Cookie}s set.
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are processing
     * @param name The {@link Cookie} name
     */
    protected HttpServletResponse deleteCookies(HttpServletRequest request,
        HttpServletResponse response, String name) {
        Cookie cookie = getCookies(request, response, name);

        if (cookie == null) {
            return response;
        }

        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return response;
    }

    /**
     * Convenience method to get {@link Cookie}s by name.
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are processing
     * @param name The {@link Cookie} name
     */
    protected Cookie getCookies(HttpServletRequest request,
        HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        Cookie cookie = null;

        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie thisCookie = cookies[i];

                if (thisCookie.getName().equals(name)) {
                    if (!thisCookie.getValue().equals("")) {
                        cookie = thisCookie;

                        break;
                    }
                }
            }
        }

        return cookie;
    }
}
