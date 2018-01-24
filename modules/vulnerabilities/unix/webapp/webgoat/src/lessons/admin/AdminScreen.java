import java.io.*;
import java.sql.*;
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
public abstract class AdminScreen extends Screen
{

	/**
	 *  Constructor for the AdminScreen object
	 *
	 *@param  s  Description of the Parameter
	 *@param  q  Description of the Parameter
	 */
	public AdminScreen( WebSession s, String q )
	{
		setQuery( q );
		// setupAdmin(s);  FIXME: what was this supposed to do?
	}


	/**
	 *  Constructor for the AdminScreen object
	 *
	 *@param  s  Description of the Parameter
	 */
	public AdminScreen( WebSession s ) { }


	/**
	 *  Constructor for the AdminScreen object
	 */
	public AdminScreen() { }


	private static Connection connection = null;


	/**
	 *  Gets the title attribute of the AdminScreen object
	 *
	 *@return    The title value
	 */
	public String getTitle()
	{
		return ( "Admin Information" );
	}


	/**
	 *  Description of the Field
	 */
	protected String query = null;


	/**
	 *  Sets the query attribute of the AdminScreen object
	 *
	 *@param  q  The new query value
	 */
	public void setQuery( String q )
	{
		query = q;
	}

}

