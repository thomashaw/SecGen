import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of
 *  the Open Web Application Security Project (http://www.owasp.org) This
 *  software package is published by OWASP under the GPL. You should read and
 *  accept the LICENSE before you use, modify and/or redistribute this software.
 *
 *@author     jwilliams@aspectsecurity.com
 *@created    November 6, 2002
 */
public abstract class AbstractLesson extends Screen implements Comparable
{

	/**
	 *  Description of the Field
	 */
	public final static String ADMIN_ROLE = "admin";
	/**
	 *  Description of the Field
	 */
	public final static String USER_ROLE = "user";

	private static int count = 1;

	private Integer id = null;

	final static IMG nextGrey = new IMG( "images/right16.gif" ).setAlt( "Next" ).setBorder( 0 ).setHspace( 0 ).setVspace( 0 );

	final static IMG previousGrey = new IMG( "images/left14.gif" ).setAlt( "Previous" ).setBorder( 0 ).setHspace( 0 ).setVspace( 0 );

	private String sourceURL;


	/**
	 *  Constructor for the Lesson object
	 */
	public AbstractLesson()
	{
		id = new Integer( ++count );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  table     Description of the Parameter
	 *@param  category  Description of the Parameter
	 *@param  s         Description of the Parameter
	 */
	private void buildMenuCategory( WebSession s, Table table, String category )
	{
		Course course = s.getCourse();

		if ( course == null )
		{
			return;
		}

		List lessons = course.getLessons( category );

		TR tr = new TR();
		tr.addElement( makeMenuCategory( category ).setBgColor( "#000066" ) );
		table.addElement( tr );

		Iterator iter = lessons.iterator();
		while ( iter.hasNext() )
		{
			tr = new TR();
			tr.addElement( ( (TD) ( (AbstractLesson) iter.next() ).getMenuItem() ).setBgColor( "#000066" ) );
			table.addElement( tr );
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  obj  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int compareTo( Object obj )
	{
		return this.getRanking().compareTo( ( (AbstractLesson) obj ).getRanking() );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  obj  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public boolean equals( Object obj )
	{

		return this.getScreenId() == ( (AbstractLesson) obj ).getScreenId();
	}



	/**
	 *  Gets the category attribute of the Lesson object
	 *
	 *@return    The category value
	 */
	public abstract String getCategory();


	/**
	 *  Gets the fileMethod attribute of the Lesson class
	 *
	 *@param  reader      Description of the Parameter
	 *@param  methodName  Description of the Parameter
	 *@param  numbers     Description of the Parameter
	 *@return             The fileMethod value
	 */
	public static String getFileMethod( BufferedReader reader, String methodName, boolean numbers )
	{
		int count = 0;
		StringBuffer sb = new StringBuffer();
		boolean echo = false;
		boolean startCount = false;
		int parenCount = 0;
		try
		{
			String line;
			while ( ( line = reader.readLine() ) != null )
			{
				if ( line.indexOf( methodName ) != -1 && ( line.indexOf( "public" ) != -1 || line.indexOf( "protected" ) != -1 || line.indexOf( "private" ) != -1 ) )
				{
					echo = true;
					startCount = true;
				}

				if ( echo && startCount )
				{
					if ( numbers )
					{
						sb.append( pad( ++count ) + "    " );
					}
					sb.append( line + "\n" );
				}

				if ( echo && line.indexOf( "{" ) != -1 )
				{
					parenCount++;
				}

				if ( echo && line.indexOf( "}" ) != -1 )
				{
					parenCount--;
					if ( parenCount == 0 )
					{
						startCount = false;
						echo = false;
					}
				}

			}
			reader.close();
		}
		catch ( Exception e )
		{
			System.out.println( e );
			e.printStackTrace();
		}
		return ( sb.toString() );
	}


	/**
	 *  Gets the fileText attribute of the Screen class
	 *
	 *@param  reader   Description of the Parameter
	 *@param  numbers  Description of the Parameter
	 *@return          The fileText value
	 */
	public static String getFileText( BufferedReader reader, boolean numbers )
	{
		int count = 0;
		StringBuffer sb = new StringBuffer();
		try
		{
			String line;
			while ( ( line = reader.readLine() ) != null )
			{
				if ( numbers )
				{
					sb.append( pad( ++count ) + "  " );
				}

				sb.append( line + "\n" );
			}
			reader.close();
		}
		catch ( Exception e )
		{
			System.out.println( e );
			e.printStackTrace();
		}
		return ( sb.toString() );
	}


	/**
	 *  Gets the hintCount attribute of the Lesson object
	 *
	 *@return    The hintCount value
	 */
	protected int getHintCount()
	{
		return getHints().size();
	}


	/**
	 *  Fill in a minor hint that will help people who basically get it, but are
	 *  stuck on somthing silly.
	 *
	 *@return    The hint1 value
	 */
	protected abstract List getHints();


	/**
	 *  Gets the instructions attribute of the AbstractLesson object
	 *
	 *@return    The instructions value
	 */
	protected abstract String getInstructions();


	/**
	 *  Gets the lessonPlan attribute of the Lesson object
	 *
	 *@return    The lessonPlan value
	 */
	protected String getLessonName()
	{
		return this.getClass().getName();
	}


	/**
	 *  Gets the lessonPlanURL attribute of the Lesson object
	 *
	 *@return    The lessonPlanURL value
	 */
	protected String getLessonPlanURL()
	{
		return "lesson_plans/" + getLessonName() + ".html";
	}


	/**
	 *  Gets the menuItem attribute of the Lesson object
	 *
	 *@return    The menuItem value
	 */
	protected abstract Element getMenuItem();


	/**
	 *  Gets the ranking attribute of the Lesson object
	 *
	 *@return    The ranking value
	 */
	protected abstract Integer getRanking();


	/**
	 *  Gets the role attribute of the AbstractLesson object
	 *
	 *@return    The role value
	 */
	public String getRole()
	{
		// FIXME: Each lesson should have a role assigned to it.  Each user/student
		//        should also have a role(s) assigned.  The user would only be allowed
		// 		  to see lessons that correspond to their role.  Eventually these roles
		//		  will be stored in the internal database.  The user will be able to hack
		// 		  into the database and change their role.  This will allow the user to
		//		  see the admin screens, once they figure out how to turn the admin switch on.
		return USER_ROLE;
	}


	/**
	 *  Gets the uniqueID attribute of the AbstractLesson object
	 *
	 *@return    The uniqueID value
	 */
	protected int getScreenId()
	{
		return id.intValue();
	}


	/**
	 *  Gets the sourceURL attribute of the Lesson object
	 *
	 *@return    The sourceURL value
	 */
	protected String getSourceURL()
	{
		if ( sourceURL != null )
		{
			return sourceURL.substring( 1 );
		}
		return sourceURL;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeClues( WebSession s )
	{
// FIXME: doesn't work -- hangs TOMCAT
//            t.addElement( makeMenuItem( s, ( s.showRequest() ? "Hide Request" : "Show Request" ), WebSession.SHOWREQUEST, !s.showRequest() ) );
		ElementContainer ec = new ElementContainer();

		Table table = new Table().setCellSpacing( 0 ).setCellPadding( 1 ).setBorder( 0 );
		if ( s.isColor() )
		{
			table.setBorder( 1 );
		}
		TR tr = new TR();

		if ( s.showParams() )
		{
			Input checked = new Input( "IMAGE", WebSession.SHOWPARAMS, "false" );
			checked.setSrc( "images/checkbox_checked.gif" );
			tr.addElement( new TD().setAlign( "right" ).addElement( checked ) );
		}
		else
		{
			Input unchecked = new Input( "IMAGE", WebSession.SHOWPARAMS, "false" );
			unchecked.setSrc( "images/checkbox_unchecked.gif" );
			tr.addElement( new TD().setAlign( "right" ).addElement( unchecked ) );
		}
		tr.addElement( new TD().setAlign( "left" ).setVAlign( "center" ).addElement( "Show Params" ) );

		if ( s.showCookies() )
		{
			Input checked = new Input( "IMAGE", WebSession.SHOWCOOKIES, "false" );
			checked.setSrc( "images/checkbox_checked.gif" );
			tr.addElement( new TD().setAlign( "right" ).setVAlign( "CENTER" ).addElement( checked ) );
		}
		else
		{
			Input unchecked = new Input( "IMAGE", WebSession.SHOWCOOKIES, "true" );
			unchecked.setSrc( "images/checkbox_unchecked.gif" );
			tr.addElement( new TD().setAlign( "right" ).addElement( unchecked ) );
		}
		tr.addElement( new TD().setAlign( "left" ).setVAlign( "center" ).addElement( "Show Cookies" ) );
		table.addElement( tr );

		tr = new TR();

		if ( s.showHtml() )
		{
			Input checked = new Input( "IMAGE", WebSession.SHOWHTML, "false" );
			checked.setSrc( "images/checkbox_checked.gif" );
			tr.addElement( new TD().setAlign( "right" ).addElement( checked ) );
		}
		else
		{
			Input unchecked = new Input( "IMAGE", WebSession.SHOWHTML, "true" );
			unchecked.setSrc( "images/checkbox_unchecked.gif" );
			tr.addElement( new TD().setAlign( "right" ).addElement( unchecked ) );
		}
		tr.addElement( new TD().setAlign( "left" ).setVAlign( "center" ).addElement( "Show HTML" ) );

		Input showSource = new Input( "IMAGE", WebSession.SHOWSOURCE, "true" );
		showSource.setOnClick( "makeWindow('" + getSourceURL() + "')" );
		showSource.setSrc( "images/checkbox_nochecked.gif" );
		tr.addElement( new TD().setAlign( "right" ).addElement( showSource ) );
		tr.addElement( new TD().setAlign( "left" ).setVAlign( "center" ).addElement( "Show Java" ) );
		table.addElement( tr );

		ec.addElement( table );
		return ec;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeContentHeader( WebSession s )
	{

		Table table = new Table().setWidth( "100%" );
		if ( s.isColor() )
		{
			table.setBgColor( HtmlColor.GREEN );
			table.setBorder( 1 );
		}

		TR tr = new TR();
		tr.addElement( new TD().setColSpan( 3 ).addElement( new HR() ) );
		table.addElement( tr );

		tr = new TR();

		tr.addElement( new TD().setAlign( "CENTER" ).addElement( makeHintsNav( s ) ) );
		tr.addElement( new TD().setAlign( "CENTER" ).addElement( makeClues( s ) ) );

		Element b = ECSFactory.makeOnClickInput( "Show Lesson Plan", "makeWindow('" + getLessonPlanURL() + "')", Input.SUBMIT );
		tr.addElement( new TD().setAlign( "CENTER" ).addElement( b ) );
		table.addElement( tr );
		// add the javascript for the lesson plan popup
		getHead().addElement( new StringElement( makeWindowScript( "LessonPlan" ) ) );

		tr = new TR();
		tr.addElement( new TD().setColSpan( 3 ).addElement( new HR() ) );
		table.addElement( tr );

		if ( getInstructions() != null )
		{
			tr = new TR();
			tr.addElement( new TD().setColSpan( 3 ).addElement( new StringElement( getInstructions() ) ) );
			table.addElement( tr );
		}

		return table;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected TD makeCookieDump( WebSession s )
	{
		UL list = new UL();
		HttpServletRequest request = s.getRequest();
		Cookie[] cookies = request.getCookies();

		if ( cookies.length == 0 )
		{
			list.addElement( new LI( "No Cookies" ) );
		}

		for ( int i = 0; i < cookies.length; i++ )
		{
			Cookie cookie = cookies[i];
			list.addElement( new LI( cookie.getName() + " -> " + cookie.getValue() ) );
		}

		ElementContainer ec = new ElementContainer();
		ec.addElement( new B( "Cookies from HTTP Request" ) );
		ec.addElement( list );

		return ( new TD().setVAlign( "TOP" ).addElement( ec ) );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeHints( WebSession s )
	{
		if ( s.getHintNum() < 0 )
		{
			return new StringElement();
		}

		ElementContainer ec = new ElementContainer();
		ec.addElement( new B( "Hints:" ).addElement( new BR() ) );
		ec.addElement( new StringElement( (String) getHints().get( s.getHintNum() ) ) );

		Table t = new Table().setCellSpacing( 0 ).setCellPadding( 0 ).setBorder( 0 );
		if ( s.isColor() )
		{
			t.setBorder( 1 );
		}
		t.addElement( new TR().addElement( new TD().setVAlign( "TOP" ).addElement( ec ) ) );
		return ( t );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeHintsNav( WebSession s )
	{

		Table table = new Table().setCellSpacing( 0 ).setCellPadding( 0 ).setBorder( 0 );
		if ( s.isColor() )
		{
			table.setBorder( 1 );
		}
		TR tr1 = new TR();
		TR tr2 = new TR();
		String hint = "Hint";

		if ( s.getHintNum() >= 0 )
		{
			Input prev = new Input( "IMAGE", WebSession.PREV_HINT, 0 );
			prev.setSrc( "images/left.gif" );
			tr2.addElement( new TD().addElement( prev ) );
			hint = hint.concat( " #" + ( s.getHintNum() + 1 ) );
		}
		else
		{
			tr2.addElement( new TD().addElement( previousGrey ) );
		}

		tr1.addElement( new TD().setColSpan( 2 ).setAlign( "CENTER" ).setVAlign( "MIDDLE" ).addElement( new B( hint ) ) );

		if ( s.getHintNum() < getHintCount() - 1 )
		{
			Input next = new Input( "IMAGE", WebSession.NEXT_HINT, 2 );
			next.setSrc( "images/right.gif" );
			tr2.addElement( new TD().addElement( next ) );
		}
		else
		{
			tr2.addElement( new TD().addElement( nextGrey ) );
		}
		table.addElement( tr1 );
		table.addElement( tr2 );
		return table;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeLeft( WebSession s )
	{
		Table t = new Table().setWidth( "100%" ).setCellSpacing( 0 ).setBgColor( "#000066" ).setCellPadding( 2 ).setBorder( 0 );
		if ( s.isColor() )
		{
			t.setBorder( 1 );
		}

		// FIXME:  Eventually we would like to have categories assigned to roles. Users
		//         would have roles assigned at the start of the course.  By default all
		// 		   categories would have the user role, the admin categories would then
		//		   be displayed when the user hacked the database and gave themselves the
		//		   admin role.
		//
		//		   Once this scheme is in place this whole section of code becomes generic

		if ( s.isAdmin() )
		{
			buildMenuCategory( s, t, "Admin Functions" );
		}
		if ( s.isChallenge() )
		{
			// FIXME: this lesson interacts with the WebSession.  This lesson will need to be
			//		  refactored to remove this dependency.  All lessons must stand alone.

			TR tr = new TR();
			tr.addElement( makeMenuCategory( "Challenge" ).setBgColor( "#000066" ) );
			t.addElement( tr );
			tr = new TR();
			String makeLinkHack = WebSession.CHALL + "=false&" + WebSession.SCREEN;
			tr.addElement( ( (TD) ( makeMenuItem( "Quit Challenge", makeLinkHack, s.getPreviousScreen() ) ).setBgColor( "#000066" ) ) );
			t.addElement( tr );
			tr = new TR();
			makeLinkHack = WebSession.RESTART + "=true&" + WebSession.SCREEN;
			tr.addElement( ( (TD) ( makeMenuItem( "Restart Challenge", makeLinkHack, getScreenId() ) ).setBgColor( "#000066" ) ) );
			t.addElement( tr );

		}
		else
		{
			buildMenuCategory( s, t, "General" );
		}
		return ( t );
	}



	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected TD makeParamDump( WebSession s )
	{
		Vector v = new Vector();
		if ( s.getParser() != null )
		{
			Enumeration e = s.getParser().getParameterNames();
			while ( e != null && e.hasMoreElements() )
			{
				String name = (String) e.nextElement();
				String[] values = s.getParser().getParameterValues( name );
				for ( int loop = 0; values != null && loop < values.length; loop++ )
				{
					v.add( name + " -> " + values[loop] );
				}
			}
			Collections.sort( v );
		}

		UL list = new UL();
		if ( v.size() == 0 )
		{
			list.addElement( new LI( "No parameters" ) );
		}
		Iterator i = v.iterator();
		while ( i.hasNext() )
		{
			String str = (String) i.next();
			list.addElement( new LI( str ) );
		}
		ElementContainer ec = new ElementContainer();
		ec.addElement( new B( "Parameters from HTTP Request" ) );
		ec.addElement( list );

		return ( new TD().setVAlign( "TOP" ).addElement( ec ) );
	}


	// this doesn't work -- I think it's because getting parameters
	// also causes the servlet container to read the request
	// but I'm not sure.
	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeRequestDump( WebSession s )
	{
		Element el = null;
		try
		{
			el = new StringElement( readFromFile( s.getRequest().getReader(), false ) );
		}
		catch ( Exception e )
		{
			s.setMessage( "Couldn't read HTTP request" );
		}

		ElementContainer ec = new ElementContainer();
		ec.addElement( new B( "HTTP Request" ) );
		ec.addElement( el );

		Table t = new Table().setCellSpacing( 0 ).setCellPadding( 0 ).setBorder( 0 );
		if ( s.isColor() )
		{
			t.setBorder( 1 );
		}
		t.addElement( new TR().addElement( new TD().setVAlign( "TOP" ).addElement( ec ) ) );

		return ( t );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element makeSourceDump( WebSession s )
	{
		if ( !s.showSource() )
		{
			return new StringElement();
		}

		String filename = s.getSourceFile( this.getClass().getName() );
		Table t = new Table().setWidth( Screen.MAIN_SIZE );
		if ( s.isColor() )
		{
			t.setBorder( 1 );
			t.setBgColor( HtmlColor.CORAL );
		}
		t.addElement( new TR().addElement( new TD().addElement( new HR() ) ) );
		try
		{
			t.addElement( new TR().addElement( new TD().addElement( convertMetachars( readFromFile( new BufferedReader( new FileReader( filename ) ), true ) ) ) ) );
		}
		catch ( IOException e )
		{
			System.out.println( "reading file EXECPTION: " + filename );
			s.setMessage( "Could not find source file" );
		}
		return ( t );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  windowName  Description of the Parameter
	 *@return             Description of the Return Value
	 */
	public static String makeWindowScript( String windowName )
	{
		// FIXME: make this string static
		StringBuffer script = new StringBuffer();

		script.append( "<script language=\"JavaScript\">\n" );
		script.append( "	<!--\n" );
		script.append( "	  function makeWindow(url) {\n" );
		script.append( "\n" );
		script.append( "	      agent = navigator.userAgent;\n" );
		script.append( "\n" );
		script.append( "	      params  = \"\";\n" );
		script.append( "	      params += \"toolbar=0,\";\n" );
		script.append( "	      params += \"location=0,\";\n" );
		script.append( "	      params += \"directories=0,\";\n" );
		script.append( "	      params += \"status=0,\";\n" );
		script.append( "	      params += \"menubar=0,\";\n" );
		script.append( "	      params += \"scrollbars=1,\";\n" );
		script.append( "	      params += \"resizable=1,\";\n" );
		script.append( "	      params += \"width=500,\";\n" );
		script.append( "	      params += \"height=350\";\n" );
		script.append( "\n" );
		script.append( "		  // close the window to vary the window size\n" );
		script.append( "	   	  if (typeof(win) == \"object\" && !win.closed){\n" );
		script.append( "            win.close();\n" );
		script.append( "	      }\n" );
		script.append( "\n" );
		script.append( "	      win = window.open(url, '" + windowName + "' , params);\n" );
		script.append( "\n" );
		script.append( " 		  // bring the window to the front\n" );
		script.append( "		  win.focus();\n" );
		script.append( "	  }\n" );
		script.append( "	//-->\n" );
		script.append( "	</script>\n" );

		return script.toString();
	}


	/**
	 *  Reads text from a file into an ElementContainer. Each line in the file is
	 *  represented in the ElementContainer by a StringElement. Each StringElement
	 *  is appended with a new-line character.
	 *
	 *@param  reader   Description of the Parameter
	 *@param  numbers  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public static String readFromFile( BufferedReader reader, boolean numbers )
	{
		return ( getFileText( reader, numbers ) );
	}


	/**
	 *  Simply reads a url into an Element for display. CAUTION: you might want to
	 *  tinker with any non-https links (href)
	 *
	 *@param  url  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public static Element readFromURL( String url )
	{
		ElementContainer ec = new ElementContainer();
		try
		{
			URL u = new URL( url );
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			BufferedReader reader = new BufferedReader( new InputStreamReader( huc.getInputStream() ) );

			String line;
			while ( ( line = reader.readLine() ) != null )
			{
				ec.addElement( new StringElement( line ) );
			}
			reader.close();
		}
		catch ( Exception e )
		{
			System.out.println( e );
			e.printStackTrace();
		}
		return ( ec );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  reader      Description of the Parameter
	 *@param  numbers     Description of the Parameter
	 *@param  methodName  Description of the Parameter
	 *@return             Description of the Return Value
	 */
	public static Element readMethodFromFile( BufferedReader reader, String methodName, boolean numbers )
	{
		PRE pre = new PRE().addElement( getFileMethod( reader, methodName, numbers ) );
		return ( pre );
	}


	/**
	 *  Sets the sourceURL attribute of the Lesson object
	 *
	 *@param  sourceURL  The new sourceURL value
	 */
	protected void setSourceURL( String sourceURL )
	{
		this.sourceURL = sourceURL;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	public void start( WebSession s )
	{
		setup( s );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element wrapForm( WebSession s )
	{
		if ( s == null )
		{
			return new StringElement( "Invalid Session" );
		}

		Table container = new Table().setWidth( "100%" ).setCellSpacing( 10 ).setCellPadding( 0 ).setBorder( 0 );
		if ( s.isColor() )
		{
			container.setBgColor( HtmlColor.YELLOW );
			container.setBorder( 1 );

		}
		container.addElement( new TR().addElement( new TD().setColSpan( 2 ).addElement( makeContentHeader( s ) ) ) );
		TR tr = new TR();
		if ( s.showParams() )
		{
			container.addElement( tr.addElement( makeParamDump( s ) ) );
		}
		if ( s.showCookies() )
		{
			container.addElement( tr.addElement( makeCookieDump( s ) ) );
		}
		if ( s.showRequest() )
		{
			container.addElement( tr.addElement( new TD().setColSpan( 2 ).addElement( makeRequestDump( s ) ) ) );
		}
		container.addElement( new TR().addElement( new TD().setColSpan( 2 ).setVAlign( "TOP" ).addElement( makeHints( s ) ) ) );

		// CreateContent can generate error messages so you MUST call it before makeMessages()
		Element content = createContent( s );
		container.addElement( new TR().addElement( new TD().setColSpan( 2 ).setVAlign( "TOP" ).addElement( makeMessages( s ) ) ) );
		container.addElement( new TR().addElement( new TD().setColSpan( 2 ).addElement( content ) ) );
		container.addElement( new TR() );
		container.addElement( new TR().addElement( new TD().setColSpan( 2 ).addElement( makeSourceDump( s ) ) ) );
		return ( container );
	}

}

