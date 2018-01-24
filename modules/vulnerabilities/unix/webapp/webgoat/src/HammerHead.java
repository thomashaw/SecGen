import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*; 

import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;

import java.io.IOException;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of the Open Web
 *  Application Security Project (http://www.owasp.org) This software package is published by OWASP
 *  under the GPL. You should read and accept the LICENSE before you use, modify and/or redistribute
 *  this software.
 *
 * @author     jwilliams@aspectsecurity.com
 * @created    November 6, 2002
 */
public class HammerHead extends HttpServlet
{

	/**
	 *  Description of the Field
	 */
	protected static SimpleDateFormat httpDateFormat;

	/**
	 *  Description of the Field
	 */
	protected WebSession mySession;
	private final static int sessionTimeoutSeconds = 60 * 30;


	/**
	 *  Description of the Method
	 *
	 * @param  request               Description of the Parameter
	 * @param  response              Description of the Parameter
	 * @exception  IOException       Description of the Exception
	 * @exception  ServletException  Description of the Exception
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException
	{
		doPost(request, response);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  request               Description of the Parameter
	 * @param  response              Description of the Parameter
	 * @exception  IOException       Description of the Exception
	 * @exception  ServletException  Description of the Exception
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException
	{
		Screen screen = null;
		try
		{
			//setCacheHeaders(response, 0);
			ServletContext context = getServletContext();
			mySession = updateSession(request, response, context);
			screen = makeScreen(mySession);
			log(request, screen.getClass().getName() + " | " + mySession.getParser().toString());
		}
		catch (Throwable t)
		{
			log("ERROR: " + t);
			screen = new ErrorScreen(t);
		}
		finally
		{
			try
			{
				this.writeScreen(screen, response);
			}
			catch (Throwable thr)
			{
				thr.printStackTrace();
				log(request, "Could not write error screen: " + thr.getMessage());
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  date  Description of the Parameter
	 * @return       RFC 1123 http date format
	 */
	protected static String formatHttpDate(Date date)
	{
		synchronized (httpDateFormat)
		{
			return httpDateFormat.format(date);
		}
	}


	/**
	 *  Return information about this servlet
	 *
	 * @return    The servletInfo value
	 */
	public String getServletInfo()
	{
		return "WebGoat is sponsored by Aspect Security.";
	}


	/**
	 *  Description of the Method
	 *
	 * @exception  ServletException  Description of the Exception
	 */
	public void init()
		throws ServletException
	{
		httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US);
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

	}


	/**
	 *  Description of the Method
	 *
	 * @param  request  Description of the Parameter
	 * @param  message  Description of the Parameter
	 */
	public void log(HttpServletRequest request, String message)
	{
		String output = new Date() + " | " + request.getRemoteHost() + ":" + request.getRemoteAddr() + " | " + message;
		log(output);
		System.out.println(output);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Screen makeScreen(WebSession s)
	{
		Screen screen = null;
		int scr = s.getCurrentScreen();

		Course course = s.getCourse();

		if (s.isUser() || s.isChallenge())
		{
			if (scr == WebSession.WELCOME)
			{
				screen = new WelcomeScreen(s);
			}
			else
			{
				AbstractLesson lesson = course.getLesson(scr, AbstractLesson.USER_ROLE);
				if (lesson != null)
				{
					screen = lesson;
					lesson.start(s);
				}
				else
				{
					screen = new ErrorScreen(s, "Invalid screen requested.");
				}
			}
		}

		else if (s.isAdmin())
		{
			if (scr == WebSession.WELCOME)
			{
				screen = new WelcomeAdminScreen(s);
			}
			else
			{
				AbstractLesson lesson = course.getLesson(scr, AbstractLesson.ADMIN_ROLE);
				if (lesson != null)
				{
					screen = lesson;
					lesson.start(s);
				}
				else
				{
					screen = new ErrorScreen(s, "Invalid screen requested.  Try Setting Admin to false");
				}
			}
		}

		return (screen);
	}


	/**
	 *  This method sets the required expiration headers in the response for a given RunData object.
	 *  This method attempts to set all relevant headers, both for HTTP 1.0 and HTTP 1.1.
	 *
	 * @param  response  The new cacheHeaders value
	 * @param  expiry    The new cacheHeaders value
	 */
	protected static void setCacheHeaders(HttpServletResponse response, int expiry)
	{
		if (expiry == 0)
		{
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Expires", formatHttpDate(new Date()));
		}
		else
		{
			Date expiryDate = new Date(System.currentTimeMillis() + expiry);
			response.setHeader("Expires", formatHttpDate(expiryDate));
		}
	}



	/**
	 *  Description of the Method
	 *
	 * @param  request   Description of the Parameter
	 * @param  response  Description of the Parameter
	 * @param  context   Description of the Parameter
	 * @return           Description of the Return Value
	 */
	protected WebSession updateSession(HttpServletRequest request, HttpServletResponse response, ServletContext context)
	{
		HttpSession hs;
		hs = request.getSession(true);

		// Get our session object out of the HTTP session
		WebSession session = null;
		Object o = hs.getAttribute(WebSession.SESSION);
		if (o != null && o instanceof WebSession)
		{
			session = (WebSession) o;
		}
		else
		{
			// Create new custom session and save it in the HTTP session
			session = new WebSession(this, context);
			hs.setAttribute(WebSession.SESSION, session);
			hs.setMaxInactiveInterval(sessionTimeoutSeconds);
			// reset timeout
		}

		session.update(request, response, this.getServletName());
		// to authenticate

		return (session);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s                Description of the Parameter
	 * @param  response         Description of the Parameter
	 * @exception  IOException  Description of the Exception
	 */
	protected void writeScreen(Screen s, HttpServletResponse response)
		throws IOException
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (s == null)
		{
			s = new ErrorScreen(mySession, "Page to display was null");
		}
		s.output(out);
		out.close();
	}

}

////////////////////////////////////////////////////
class InvokeServlets {
    public static void main(String[] args) throws IOException {
	processServlets();
    }

    public static void processServlets() { 
        try {
            HttpServletRequest request   = new MockHttpServletRequest();
            HttpServletResponse response = new MockHttpServletResponse();

	    HammerHead servlet = new HammerHead();

            servlet.doGet(request, response);
	    servlet.doPost(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

    } 
}
