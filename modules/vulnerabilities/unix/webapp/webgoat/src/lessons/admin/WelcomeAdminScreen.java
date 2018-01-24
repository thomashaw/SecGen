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
public class WelcomeAdminScreen extends Screen
{
	/**
	 *  Constructor for the WelcomeAdminScreen object
	 *
	 *@param  s  Description of the Parameter
	 */
	public WelcomeAdminScreen( WebSession s )
	{
		super( s );
	}


	/**
	 *  Constructor for the WelcomeAdminScreen object
	 */
	public WelcomeAdminScreen() { }


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	protected Element createContent( WebSession s )
	{
		ElementContainer ec = new ElementContainer();
		try
		{
			ec.addElement( new Center( new H1( "Welcome to the Penetration Testing Course" ) ) );

		}
		catch ( Exception e )
		{
			s.setMessage( "Error generating " + this.getClass().getName() );
			e.printStackTrace();
		}
		return ( ec );
	}


	/**
	 *  Gets the title attribute of the WelcomeAdminScreen object
	 *
	 *@return    The title value
	 */
	public String getTitle()
	{
		return ( "Welcome" );
	}

}

