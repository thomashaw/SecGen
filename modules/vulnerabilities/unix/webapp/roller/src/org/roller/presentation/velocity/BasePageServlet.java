package org.roller.presentation.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.PageData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
/**
 * Base Servlet for Servlets that render user page templates. Loads the
 * Velocity context using the ContextLoader and runs the page template
 * selected by the request.
 *
 * @author llavandowska
 * @author David M Johnson
 */
public abstract class BasePageServlet extends VelocityServlet
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(BasePageServlet.class);
	/**
	 *  <p>Sets servletContext for WebappResourceLoader.</p>
	 *
	 * @param config servlet configuation
	 */
	public void init( ServletConfig config )
		throws ServletException
	{
		super.init( config );
		WebappResourceLoader.setServletContext( getServletContext() );
	}
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context ctx ) throws Exception
    {
        String pid = null;
        Template outty = null;
        Exception pageException = null;
        try
        {
            PageContext pageContext =
                JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
            // Needed to init request attributes, etc.
            RollerRequest rreq = RollerRequest.getRollerRequest(pageContext);
            PageData pd = null;
            // If request specified the page, then go with that
            if ( rreq.getPage() != null )
            {                pd = rreq.getPage();
                pid = pd.getId();
            }
            // If page not available from request, then use website's default
            else if ( rreq.getPage() == null && rreq.getWebsite() != null )
            {
                UserManager userMgr = rreq.getRoller().getUserManager();
                WebsiteData wd = rreq.getWebsite();
                pd = userMgr.retrievePage(wd.getDefaultPageId());
                pid = pd.getId();
            }
            // Still no page ID, then we have a problem
            if ( pid == null )
            {
                throw new ResourceNotFoundException("Page not found");
            }                        // if page has an extension - use that to set the contentType            int period = pd.getLink().indexOf('.');            if (period > -1) {                String extension = pd.getLink().substring(period+1);                if ("js".equals(extension)) {                	extension = "javascript";                }                response.setContentType("text/" + extension);            }
            // Made it this far, populate the Context
            ContextLoader.setupContext( ctx, rreq, response );            
            // run the page
            outty =  getTemplate( pid, "UTF-8" );
        }
        catch( ParseErrorException pee )
        {
			pageException = pee;
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        catch( ResourceNotFoundException rnfe )
        {
			pageException = rnfe;
            response.setStatus( HttpServletResponse.SC_NOT_FOUND );
        }
        catch( RollerException re )
        {
			pageException = re;
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        catch( Exception e )
        {
			pageException = e;
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if (pageException != null)
        {
        	mLogger.error("EXCEPTION: in RollerServlet", pageException);
			request.setAttribute("DisplayException", pageException);
        }
        return outty;
    }
    //------------------------------------------------------------------------
    /**
     * Handle error in Velocity processing.
     */
    protected void error( HttpServletRequest req, HttpServletResponse res,
        Exception e) throws ServletException, IOException
    {
        mLogger.warn("ERROR in VelocityServlet",e);
    }
}
