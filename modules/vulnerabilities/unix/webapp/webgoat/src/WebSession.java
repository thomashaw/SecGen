import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

import org.enhydra.instantdb.jdbc.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of
 *  the Open Web Application Security Project (http://www.owasp.org) This
 *  software package is published by OWASP under the GPL. You should read and
 *  accept the LICENSE before you use, modify and/or redistribute this software.
 *
 *@author     jwilliams@aspectsecurity.com
 *@created    November 6, 2002
 */
public class WebSession
{

	/**
	 *  Description of the Field
	 */
	public final static String ADMIN = "Admin";
	/**
	 *  Description of the Field
	 */
	public final static String BGCOLOR = HtmlColor.convertColor( "#EFECE0" );
	/**
	 *  Description of the Field
	 */
	public final static String CHALL = "Challenge";

	/**
	 *  Description of the Field
	 */
	public final static String COLOR = "color";
	/**
	 *  Description of the Field
	 */
	public final static String DB_DRIVER = "DatabaseDriver";
	/**
	 *  Description of the Field
	 */
	public final static String DB_URL_SUBNAME = "DatabaseURLsubname";
	/**
	 *  Description of the Field
	 */
	public final static String DB_URL_SUBPROTOCOL = "DatabaseURLsubprotocol";
	/**
	 *  Description of the Field
	 */
	public final static int ERROR = 0;
	/**
	 *  Description of the Field
	 */
	public final static String JSESSION_ID = "jsessionid";
	/**
	 *  Description of the Field
	 */
	public final static String NEXT_HINT = "Next";
	/**
	 *  Description of the Field
	 */
	public final static String PREV_HINT = "Previous";
	/**
	 *  Description of the Field
	 */
	public final static String RESTART = "Restart";
	// Screen tags
	/**
	 *  Description of the Field
	 */
	public final static String SCREEN = "Screen";
	/**
	 *  Description of the Field
	 */
	public final static String SESSION = "Session";
	/**
	 *  Description of the Field
	 */
	public final static String SHOWCOOKIES = "ShowCookies";
	/**
	 *  Description of the Field
	 */
	public final static String SHOWHTML = "ShowHtml";
	/**
	 *  Description of the Field
	 */
	public final static String SHOWPARAMS = "ShowParams";
	/**
	 *  Description of the Field
	 */
	public final static String SHOWREQUEST = "ShowRequest";
	/**
	 *  Description of the Field
	 */
	public final static String SHOWSOURCE = "ShowSource";

	/**
	 *  Description of the Field
	 */
	public final static int WELCOME = -1;

	private ServletContext context = null;

	/**
	 *  Description of the Field
	 */
	protected Course course;

	private int currentScreen = WELCOME;

	private static boolean databaseBuilt = false;
	/**
	 *  Description of the Field
	 */
	protected String dbDriver;
	/**
	 *  Description of the Field
	 */
	protected String dbURL;
	/**
	 *  Description of the Field
	 */
	protected String dbURLsubname;
	/**
	 *  Description of the Field
	 */
	protected String dbURLsubprotocol;

	/**
	 *  Description of the Field
	 */
	protected int hintNum = -1;
	private boolean isAdmin = false;

	private boolean isAuthenticated = false;

	private boolean isChallenge = false;

	private boolean isColor = false;

	private StringBuffer message = new StringBuffer( "" );
	private ParameterParser myParser;
	private int previousScreen = ERROR;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;
	private String servletName;

	/**
	 *  Description of the Field
	 */
	protected boolean showCookies = false;

	/**
	 *  Description of the Field
	 */
	protected boolean showHtml = false;

	/**
	 *  Description of the Field
	 */
	protected boolean showParams = false;

	/**
	 *  Description of the Field
	 */
	protected boolean showRequest = false;

	/**
	 *  Description of the Field
	 */
	protected boolean showSource = false;

	private int stage = 1;


	/**
	 *  Constructor for the WebSession object
	 *
	 *@param  servlet  Description of the Parameter
	 *@param  context  Description of the Parameter
	 */
	public WebSession( HttpServlet servlet, ServletContext context )
	{
		// initialize from web.xml
		showParams = "true".equals( servlet.getInitParameter( SHOWPARAMS ) );
		showCookies = "true".equals( servlet.getInitParameter( SHOWCOOKIES ) );
		showSource = "true".equals( servlet.getInitParameter( SHOWSOURCE ) );
		showHtml = "true".equals( servlet.getInitParameter( SHOWHTML ) );
		showRequest = "true".equals( servlet.getInitParameter( SHOWREQUEST ) );

		dbDriver = servlet.getInitParameter( DB_DRIVER );
		dbURLsubprotocol = servlet.getInitParameter( DB_URL_SUBPROTOCOL );
		dbURLsubname = servlet.getInitParameter( DB_URL_SUBNAME );

		servletName = servlet.getServletName();
		this.context = context;

		course = new Course();
		course.loadCourses( context, "/" );

		// initialize the internal database once.  There is a concurrency problem
		// here but for this application we don't care.
		if ( !databaseBuilt )
		{
			new RefreshDBScreen().refreshDB( this );
			databaseBuilt = true;
		}

	}



	/**
	 *  Description of the Method
	 *
	 *@param  param          Description of the Parameter
	 *@return                Description of the Return Value
	 *@exception  Exception  Description of the Exception
	 */
	private boolean checkboxValue( String param )
		 throws Exception
	{
		// if the checkbox parameter is present it's value is true ( checked ).  The actual
		// value sent does not matter.  Only checked checkboxes are sent by the browser.
		return ( myParser.getBooleanParameter( param + ".x", false ) );
	}


	/**
	 *  Description of the Method
	 */
	public void clearMessage()
	{
		message.setLength( 0 );
	}


	/**
	 *  Description of the Method
	 */
	public void eatCookies()
	{
		Cookie[] cookies = request.getCookies();
		for ( int loop = 0; loop < cookies.length; loop++ )
		{
			if ( !cookies[loop].getName().startsWith( "JS" ) )
			{
				// skip jsessionid cookie

				cookies[loop].setMaxAge( 0 );
				// mark for deletion by browser
				response.addCookie( cookies[loop] );
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  enabled  Description of the Parameter
	 */
	public void enableClues( boolean enabled )
	{
		this.showCookies = false;
		this.showParams = false;
		this.showRequest = false;
		this.showSource = false;
		this.showHtml = false;
	}


	/**
	 *  Gets the challengeStage attribute of the WebSession object
	 *
	 *@return    The challengeStage value
	 */
	public int getChallengeStage()
	{
		return ( stage );
	}


	/**
	 *  Gets the context attribute of the WebSession object
	 *
	 *@return    The context value
	 */
	public ServletContext getContext()
	{
		return context;
	}


	/**
	 *  Gets the course attribute of the WebSession object
	 *
	 *@return    The course value
	 */
	public Course getCourse()
	{
		return course;
	}


	/**
	 *  Gets the currentScreen attribute of the WebSession object
	 *
	 *@return    The currentScreen value
	 */
	public int getCurrentScreen()
	{
		return ( currentScreen );
	}


	/**
	 *  Gets the dBDriver attribute of the WebSession object
	 *
	 *@return    The dBDriver value
	 */
	public String getDBDriver()
	{
		return ( dbDriver );
	}


	/**
	 *  Gets the dBUrl attribute of the WebSession object
	 *
	 *@return    The dBUrl value
	 */
	public String getDBUrl()
	{

		// a database url of the form jdbc:subprotocol:subname
		dbURL = "jdbc:" + dbURLsubprotocol + ":"
			 + context.getRealPath( dbURLsubname );
		System.out.println( "DB_URL:" + dbURL );
		return ( dbURL );
	}


	/**
	 *  Gets the hint1 attribute of the WebSession object
	 *
	 *@return    The hint1 value
	 */
	public int getHintNum()
	{

		return ( hintNum );
	}


	/**
	 *  Gets the message attribute of the WebSession object
	 *
	 *@return    The message value
	 */
	public String getMessage()
	{
		return ( message.toString() );
	}


	/**
	 *  Gets the parser attribute of the WebSession object
	 *
	 *@return    The parser value
	 */
	public ParameterParser getParser()
	{
		return ( myParser );
	}


	/**
	 *  Gets the previousScreen attribute of the WebSession object
	 *
	 *@return    The previousScreen value
	 */
	public int getPreviousScreen()
	{
		return ( previousScreen );
	}



	/**
	 *  Gets the request attribute of the WebSession object
	 *
	 *@return    The request value
	 */
	public HttpServletRequest getRequest()
	{
		return request;
	}


	/**
	 *  Gets the response attribute of the WebSession object
	 *
	 *@return    The response value
	 */
	public HttpServletResponse getResponse()
	{
		return response;
	}


	/**
	 *  Gets the servletName attribute of the WebSession object
	 *
	 *@return    The servletName value
	 */
	public String getServletName()
	{
		return ( servletName );
	}


	/**
	 *  Gets the sourceFile attribute of the WebSession object
	 *
	 *@param  screen  Description of the Parameter
	 *@return         The sourceFile value
	 */
	public String getSourceFile( String screen )
	{
		// FIXME: doesn't work for admin path!
		return ( context.getRealPath( "src/lessons/" + screen + ".java" ) );
	}


	/**
	 *  Gets the admin attribute of the WebSession object
	 *
	 *@return    The admin value
	 */
	public boolean isAdmin()
	{
		return ( isAdmin );
	}


	/**
	 *  Gets the authenticated attribute of the WebSession object
	 *
	 *@return    The authenticated value
	 */
	public boolean isAuthenticated()
	{
		return ( isAuthenticated );
	}


	/**
	 *  Gets the challenge attribute of the WebSession object
	 *
	 *@return    The challenge value
	 */
	public boolean isChallenge()
	{
		return ( isChallenge );
	}


	/**
	 *  Gets the color attribute of the WebSession object
	 *
	 *@return    The color value
	 */
	public boolean isColor()
	{
		return ( isColor );
	}


	/**
	 *  Gets the screen attribute of the WebSession object
	 *
	 *@param  value  Description of the Parameter
	 *@return        The screen value
	 */
	public boolean isScreen( int value )
	{
		return ( getCurrentScreen() == value );
	}


	/**
	 *  Gets the user attribute of the WebSession object
	 *
	 *@return    The user value
	 */
	public boolean isUser()
	{
		return ( !isAdmin && !isChallenge );
	}


	/**
	 *  Sets the challengeStage attribute of the WebSession object
	 *
	 *@param  stage  The new challengeStage value
	 */
	public void setChallengeStage( int stage )
	{
		this.stage = stage;
	}


	/**
	 *  Sets the message attribute of the WebSession object
	 *
	 *@param  text  The new message value
	 */
	public void setMessage( String text )
	{
		if ( message.length() == 0 )
		{
			message.append( " * " + text );
		}
		else
		{
			message.append( "<BR>" + " * " + text );
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public boolean showCookies()
	{
		return ( showCookies );
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public boolean showHtml()
	{
		return ( showHtml );
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public boolean showParams()
	{
		return ( showParams );
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public boolean showRequest()
	{
		return ( showRequest );
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public boolean showSource()
	{
		return ( showSource );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  request   Description of the Parameter
	 *@param  response  Description of the Parameter
	 *@param  name      Description of the Parameter
	 */
	public void update( HttpServletRequest request, HttpServletResponse response, String name )
	{

		clearMessage();
		this.request = request;
		this.response = response;
		this.servletName = name;

		if ( myParser == null )
		{
			myParser = new ParameterParser( request );
		}
		else
		{
			myParser.update( request );
		}

		if ( myParser.getRawParameter( "reload", null ) != null )
		{
			course = new Course();
			course.loadCourses( context, "/" );
			currentScreen = WELCOME;
			previousScreen = ERROR;
		}

		if ( this.getPreviousScreen() == WebSession.WELCOME )
		{
			currentScreen = course.getFirstLesson();
			hintNum = -1;
			enableClues( false );
		}

		// update the screen variables
		previousScreen = currentScreen;
		try
		{
			currentScreen = myParser.getIntParameter( SCREEN, currentScreen );
		}
		catch ( Exception e )
		{
		}

		// clear variables when switching screens
		if ( this.getCurrentScreen() != this.getPreviousScreen() )
		{
			setMessage( "Changed to a new screen, clearing cookies and disabling hints" );
			eatCookies();
			hintNum = -1;
		}

		// else update global variables for the current screen
		else
		{
			showParams = myParser.getRawParameter( SHOWPARAMS + ".x", null ) == null ? showParams : !showParams;
			//showSource = myParser.getRawParameter( SHOWSOURCE + ".x", null ) == null ? showSource : !showSource;
			showCookies = myParser.getRawParameter( SHOWCOOKIES + ".x", null ) == null ? showCookies : !showCookies;
			showHtml = myParser.getRawParameter( SHOWHTML + ".x", null ) == null ? showHtml : !showHtml;
			//showRequest = myParser.getRawParameter( SHOWREQUEST + ".x", null ) == null ? showRequest : !showRequest;

			if ( myParser.getIntParameter( PREV_HINT + ".x", -1 ) != -1 )
			{
				hintNum--;
			}
			else if ( myParser.getIntParameter( NEXT_HINT + ".x", -1 ) != -1 )
			{
				hintNum++;
			}
			if ( myParser.getBooleanParameter( RESTART, false ) )
			{
				stage = 1;
			}
		}

		isAdmin = myParser.getBooleanParameter( ADMIN, isAdmin );
		isColor = myParser.getBooleanParameter( COLOR, isColor );
		isChallenge = myParser.getBooleanParameter( CHALL, isChallenge );

		System.out.println( "showParams:" + showParams );
		System.out.println( "showSource:" + showSource );
		System.out.println( "showCookies:" + showCookies );
		System.out.println( "showHtml:" + showHtml );
		System.out.println( "showRequest:" + showRequest );
	}

}

