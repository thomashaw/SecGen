/*
 * Created on 2003-jul-03
 *
 */
package se.bluefish.blueblog.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows startup blog URL to be defined in the web.xml file,
 * by forwarding the browser from the root context to the specified
 * URL (blog). Use the <b>to</b> init parameter to specify startup
 * url (e.g. <b>bb/robert</b>), and the <b>forward</b> init parameter
 * to specify if this should be a forward (set value to <b>true</b>)
 * or a redirect (<b>false</b>, which is the default).
 * 
 * @author Robert Burén
 */
public class ForwardingServlet extends HttpServlet {
	private boolean forward = false; // default = redirect (not forward)
	private String toPath;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException 
	{
		String url = request.getServletPath() + toPath;
		if( forward ) {
			RequestDispatcher disp = getServletContext().getRequestDispatcher(url);
			disp.forward(request, response);
		} else {
			response.sendRedirect(url);
		}
	}

	public void init() throws ServletException {
		toPath = getServletConfig().getInitParameter("to");
		forward = Boolean.valueOf(getServletConfig().getInitParameter("forward")).booleanValue();
	}

}
