import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of the Open Web
 *  Application Security Project (http://www.owasp.org) This software package is published by OWASP
 *  under the GPL. You should read and accept the LICENSE before you use, modify and/or redistribute
 *  this software.
 *
 * @author     jwilliams@aspectsecurity.com
 * @created    November 6, 2002
 */
public class FailOpenAuthentication extends WeakAuthenticationCookie
{

	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		boolean logout = s.getParser().getBooleanParameter(LOGOUT, false);
		if (logout)
		{
			s.setMessage("Goodbye!");
			s.eatCookies();
			return (makeLogin(s));
		}

		try
		{
			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}

			String username = "";
			String password = "";
			try
			{
				username = s.getParser().getRawParameter(USERNAME);
				password = s.getParser().getRawParameter(PASSWORD);

				String query = "SELECT * FROM user_system_data WHERE user_name = '" + username + "' AND " + "password = '" + password + "'";
				Statement statement = connection.createStatement();
				ResultSet results = statement.executeQuery(query);

				// HERE IS THE ASSUMPTION
				if (results == null || !results.first())
				{
					s.setMessage("Invalid username and password entered.");
					return (makeLogin(s));
				}
			}
			catch (Exception e)
			{
			}

			// continue and display the page
			if (username != null && username.length() > 0)
			{
				return (makeUser(s, username, "PARAMETERS"));
			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
		}
		return (makeLogin(s));
	}


	/**
	 *  Gets the hints attribute of the AuthenticateScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("You can force errors during the authentication process.");
		hints.add("You can change length, existance, or values of authentication parameters.");
		hints.add("Try removing password parameter with Achilles.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the AuthenticateScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Fail Open Authentication", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the AuthenticateScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(20);
	}



	/**
	 *  Gets the title attribute of the AuthenticateScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Bypass a Fail Open Authentication Scheme");
	}


	/**
	 *  Gets the instructions attribute of the FailOpenAuthentication object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Please enter a username and password to login.";
		return (instructions);
	}

}

