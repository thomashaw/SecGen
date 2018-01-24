package net.eyde.personalblog.struts.action;

import net.eyde.personalblog.service.InitializationManager;
import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.service.PropertyManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 *  Description of the Class
 *
 * @created
 * @modified   June 29, 2003
 */

//public final class LogonAction extends DispatchAction 
public final class InitializeAction extends DispatchAction {
    private static Log log = LogFactory.getLog(InitializeAction.class);

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

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        return (mapping.findForward("success"));
    }

    public ActionForward executeTestDatabase(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        InitializationManager init = new InitializationManager();
        Vector v = new Vector();

        if (init.dataBaseOn()) {
            if (!init.tablesCreated(v)) {
                request.setAttribute("tables_not_created", v);

                return (mapping.findForward("tables_not_created"));
            } else if (init.propertiesMissing()) {
                return (mapping.findForward("missing_properties"));
            }

            return (mapping.findForward("sucess"));
        } else {
            return (mapping.findForward("database_off"));
        }
    }

    public ActionForward executeCreateTables(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        Vector v = new Vector();
        InitializationManager init = new InitializationManager();

        //this should not be hardcoded
        init.createTables("/mysql.xml");

        if (init.tablesCreated(v)) {
            if (init.propertiesMissing()) {
                return (mapping.findForward("missing_properties"));
            } else {
                return (mapping.findForward("sucess"));
            }
        } else {
            request.setAttribute("tables_not_created", v);

            return (mapping.findForward("tables_not_created"));
        }
    }

    //TODO:CREATE THE method below and relate it to respective jsp file

    /*
     ExecuteCreateHibernateFile
      Properties connectionProperties = new Properties();
            connectionProperties.put("hibernate.connection.url",
                request.getParameter("dburl"));
            connectionProperties.put("hibernate.connection.driver_class",
                request.getParameter("dbdriver"));
            connectionProperties.put("hibernate.connection.username",
                request.getParameter("dbuser"));
            connectionProperties.put("hibernate.connection.password",
                request.getParameter("dbpassword"));

            InitializationManager x = new InitializationManager(connectionProperties);

            //boolean bdOk="true".equals(request.getSession().getAttribute("bd_ok"));
            //if (!bdOk) {
            /                        x.createHibernateConfigFile(request.getParameter("dbdriver"),request.getParameter("dburl"),
                                                                                                            request.getParameter("dbuser"),request.getParameter("dbpassword"),
                                                                                                            session.getServletContext().getRealPath("\\WEB-INF\\classes\\hibernate.properties"));
            //x.createHibernateConfigFile(session.getServletContext().getRealPath("\\WEB-INF\\classes\\hibernate.properties"));


    */

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

        Properties applicationProperties = new Properties();
        applicationProperties.put(PersonalBlogService.WEBLOG_DESCRIPTION,
            request.getParameter("description"));
        applicationProperties.put(PersonalBlogService.WEBLOG_TITLE,
            request.getParameter("title"));

        applicationProperties.put(PersonalBlogService.WEBLOG_PICTURE,
            request.getParameter("picture"));
        applicationProperties.put(PersonalBlogService.WEBLOG_OWNER_NICK_NAME,
            request.getParameter("nickname"));
        applicationProperties.put(PersonalBlogService.WEBLOG_OWNER,
            request.getParameter("owner"));
        applicationProperties.put(PersonalBlogService.WEBLOG_EMAIL,
            request.getParameter("email"));

        applicationProperties.put(PersonalBlogService.WEBLOG_URL,
            request.getParameter("blogurl"));
        applicationProperties.put(PersonalBlogService.LOGON_ID,
            request.getParameter("adminid"));
        applicationProperties.put(PersonalBlogService.LOGON_PASSWORD,
            request.getParameter("adminpassword"));
        applicationProperties.put(PersonalBlogService.CATEGORY_TITLES,
            request.getParameter("categorytitles"));
        applicationProperties.put(PersonalBlogService.CATEGORY_VALUES,
            request.getParameter("categoryvalues"));
        applicationProperties.put(PersonalBlogService.CATEGORY_IMAGES,
            request.getParameter("categoryimages"));
        applicationProperties.put(PersonalBlogService.EMOTICON_VALUES,
            request.getParameter("emoticonvalues"));
        applicationProperties.put(PersonalBlogService.EMOTICON_IMAGES,
            request.getParameter("emoticonimages"));
        applicationProperties.put(PersonalBlogService.EDITOR,
            request.getParameter("posteditor"));
        applicationProperties.put(PersonalBlogService.LINK_POST, "0");
        applicationProperties.put(PersonalBlogService.EMAIL_HOST,
            request.getParameter("emailhost"));
        applicationProperties.put(PersonalBlogService.EMAIL_TRANSPORT,
            request.getParameter("emailtransport"));
        applicationProperties.put(PersonalBlogService.EMAIL_USERNAME,
            request.getParameter("emailusername"));
        applicationProperties.put(PersonalBlogService.EMAIL_PASSWORD,
            request.getParameter("emailpassword"));

        //TODO:Read the above data from personalblog.properties legacy file :)
        //properties population
        PropertyManager props = new PropertyManager();
        props.load(applicationProperties);

        log.debug("Pre-Initializing PersonalBlog Service");

        PersonalBlogService pblog = PersonalBlogService.getInstance();

        log.info("Initialization is complete");
        servlet.getServletContext().setAttribute(PersonalBlogService.INSTALLATION_STATE,
            PersonalBlogService.STATE_OK);

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        return (mapping.findForward("success"));
    }
}


// end of the class LogonAction
