import java.io.*;
import java.util.*;
import javax.servlet.*;

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
public class ErrorScreen extends Screen
{

	/**
	 *  Description of the Field
	 */
	protected Throwable error;

	/**
	 *  Description of the Field
	 */
	protected String message;


	/**
	 *  Constructor for the ErrorScreen object
	 *
	 *@param  s  Description of the Parameter
	 */
	public ErrorScreen( WebSession s )
	{
		setup( s );
	}


	/**
	 *  Constructor for the ErrorScreen object
	 *
	 *@param  msg  Description of the Parameter
	 */
	public ErrorScreen( String msg )
	{
		this.message = msg;
		setup( null );
	}


	/**
	 *  Constructor for the ErrorScreen object
	 *
	 *@param  t  Description of the Parameter
	 */
	public ErrorScreen( Throwable t )
	{
		this.error = t;
		setup( null );
	}


	/**
	 *  Constructor for the ErrorScreen object
	 *
	 *@param  s  Description of the Parameter
	 *@param  t  Description of the Parameter
	 */
	public ErrorScreen( WebSession s, Throwable t )
	{
		this.error = t;
		setup( s );
	}


	/**
	 *  Constructor for the ErrorScreen object
	 *
	 *@param  s    Description of the Parameter
	 *@param  msg  Description of the Parameter
	 */
	public ErrorScreen( WebSession s, String msg )
	{
		this.message = msg;
		setup( s );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element createContent( WebSession s )
	{
		System.out.println( "errorscreen createContent Error:" + this.error + " message:" + this.message );
		Element content;
		if ( this.error != null )
		{
			content = createContent( this.error );
		}
		else if ( this.message != null )
		{
			content = createContent( this.message );
		}
		else
		{
			content = new StringElement( "An unknown error occurred." );
		}

		return content;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element createContent( String s )
	{
		StringElement list = new StringElement( s );
		return ( list );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  t  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element createContent( Throwable t )
	{
		StringElement list = new StringElement();
		list.addElement( new H2().addElement( new StringElement( "Error Message: " + t.getMessage() ) ) );
		list.addElement( formatStackTrace( t ) );

		if ( t instanceof ServletException )
		{
			Throwable root = ( (ServletException) t ).getRootCause();
			if ( root != null )
			{
				list.addElement( new H2().addElement( new StringElement( "Root Message: " + root.getMessage() ) ) );
				list.addElement( formatStackTrace( root ) );
			}
		}
		return ( new Small().addElement( list ) );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  t  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static Element formatStackTrace( Throwable t )
	{
		String trace = getStackTrace( t );
		StringElement list = new StringElement();
		StringTokenizer st = new StringTokenizer( trace, "\r\n\t" );
		while ( st.hasMoreTokens() )
		{
			String line = st.nextToken();
			list.addElement( new Div( line ) );
		}
		return ( list );
	}


	/**
	 *  Gets the stackTrace attribute of the ErrorScreen class
	 *
	 *@param  t  Description of the Parameter
	 *@return    The stackTrace value
	 */
	public static String getStackTrace( Throwable t )
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter( bytes, true );
		t.printStackTrace( writer );
		return ( bytes.toString() );
	}


	/**
	 *  Gets the title attribute of the ErrorScreen object
	 *
	 *@return    The title value
	 */
	public String getTitle()
	{
		return ( "Error" );
	}

}


