package net.eyde.personalblog.struts.action;

import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eyde.personalblog.service.InitializationManager;
import net.eyde.personalblog.service.PersonalBlogService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.TilesRequestProcessor;

/**
 * @author Emerson Cargnin
 *
 * This class is used to make the startup checking of personalblog installation
 */
public class BlogTilesProcessor extends TilesRequestProcessor {
    private static Log log = LogFactory.getLog(BlogTilesProcessor.class);

    private Properties hibernate_properties;

    /* (non-Javadoc)
     * @see org.apache.struts.action.RequestProcessor#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void process(HttpServletRequest arg0, HttpServletResponse arg1)
        throws IOException, ServletException {

        super.process(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.struts.action.RequestProcessor#init(org.apache.struts.action.ActionServlet, org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet servlet, ModuleConfig module) throws ServletException {
        super.init(servlet, module);

        log.debug("starting initialization");

        String state = PersonalBlogService.STATE_UNDEFINED;
		InitializationManager init=new InitializationManager();
		Vector v=new Vector(); 
        if (!init.hibernateFileExists()) {
            state = PersonalBlogService.STATE_NO_HIBERNATE_FILE;
        } else if (!init.dataBaseOn()) {
            state = PersonalBlogService.STATE_DATABASE_OFF;
        } else if (!init.tablesCreated(v)) {
			servlet.getServletContext().setAttribute("tables_not_created", v);
            state = PersonalBlogService.STATE_TABLES_NOT_CREATED;
        } else if (init.propertiesMissing()) {
            state = PersonalBlogService.STATE_MISSING_PROPERTIES;
        } else
            state = PersonalBlogService.STATE_OK;

        servlet.getServletContext().setAttribute(PersonalBlogService.INSTALLATION_STATE, state);

        log.info("PersonalBlog state : " + state);
    }

  


    
}
