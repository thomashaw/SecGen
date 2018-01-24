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
public class WeakAuthenticationCookie extends LessonAdapter
{
	/**
	 *  Description of the Field
	 */
	protected final static String AUTHCOOKIE = "AuthCookie";
	/**
	 *  Description of the Field
	 */
	protected final static String COOKIE_COL_NAME = "cookie";
	/**
	 *  Description of the Field
	 */
	protected final static String LOGOUT = "Logout";
	/**
	 *  Description of the Field
	 */
	protected final static String PASSWORD = "Password";
	/**
	 *  Description of the Field
	 */
	protected final static String USERNAME = "Username";


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected String checkCookie(WebSession s)
		throws Exception
	{
		String cookie = getCookie(s);
		if (cookie != null)
		{
			String query = "SELECT * from user_system_data WHERE cookie = '" + cookie + "'";
			Statement statement3 = connection.createStatement();
			ResultSet results = statement3.executeQuery(query);

			if (results != null && results.first() == true)
			{
				return (results.getString("user_name"));
			}
			else
			{
				s.setMessage("Invalid username and password entered.");
				s.eatCookies();
			}
		}
		return (null);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected String checkParams(WebSession s)
		throws Exception
	{
		String username = s.getParser().getStringParameter(USERNAME, "");
		String password = s.getParser().getStringParameter(PASSWORD, "");

		if (username.length() > 0 && password.length() > 0)
		{
			String query = "SELECT * FROM user_system_data WHERE " +
					"user_name = '" + username + "' AND " +
					"password = '" + password + "'";

			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(query);

			if (results != null && results.first())
			{
				// make a new cookie
				Cookie newCookie = new Cookie(AUTHCOOKIE, mangleLogin(username, password));
				s.setMessage("Your password has been remembered");
				s.getResponse().addCookie(newCookie);

				// update the DB with the new Cookie
				results.updateString(COOKIE_COL_NAME, newCookie.getValue());
				results.updateRow();
				return (username);
			}
			else
			{
				s.setMessage("Invalid username and password entered.");
			}
		}
		return (null);
	}


	/**
	 *  Description of the Field
	 */
	protected static Connection connection = null;


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
			s.setMessage("Your password has been forgotten");
			s.eatCookies();
			return (makeLogin(s));
		}

		try
		{
			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}

			String user = null;
			user = checkCookie(s);
			if (user != null && user.length() > 0)
			{
				return (makeUser(s, user, "COOKIE"));
			}

			user = checkParams(s);
			if (user != null && user.length() > 0)
			{
				return (makeUser(s, user, "PARAMETERS"));
			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
		}
		return (makeLogin(s));
	}


	/**
	 *  Description of the Field
	 */
	protected static String currentUser = null;


	/**
	 *  Gets the cookie attribute of the CookieScreen object
	 *
	 * @param  s  Description of the Parameter
	 * @return    The cookie value
	 */
	protected String getCookie(WebSession s)
	{
		Cookie[] cookies = s.getRequest().getCookies();
		for (int i = 0; i < cookies.length; i++)
		{
			if (cookies[i].getName().equalsIgnoreCase(AUTHCOOKIE))
			{
				return (cookies[i].getValue());
			}
		}
		return (null);
	}


	/**
	 *  Gets the hints attribute of the CookieScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("The server skips authentication if you send the right cookie.");
		hints.add("Is the AuthCookie value guessable knowing the username and password?");
		hints.add("Add 'AuthCookie=ggfkggfk;' to the Cookie: header using Achilles.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the CookieScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Weak Authentication Cookie", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the CookieScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(90);
	}


	/**
	 *  Gets the title attribute of the CookieScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Spoof an Authentication Cookie");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeLogin(WebSession s)
	{
		ElementContainer ec = new ElementContainer();

		// add the login fields
		Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);
		if (s.isColor())
		{
			t.setBorder(1);
		}
		TR row1 = new TR();
		TR row2 = new TR();

		row1.addElement(new TD(new StringElement("User Name: ")));
		row2.addElement(new TD(new StringElement("Password: ")));

		Input input1 = new Input(Input.TEXT, USERNAME, "");
		Input input2 = new Input(Input.PASSWORD, PASSWORD, "");
		row1.addElement(new TD(input1));
		row2.addElement(new TD(input2));

		t.addElement(row1);
		t.addElement(row2);

		Element b = ECSFactory.makeButton("Login");
		t.addElement(new TR(new TD(b)));

		ec.addElement(t);
		return (ec);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @param  user           Description of the Parameter
	 * @param  method         Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element makeUser(WebSession s, String user, String method)
		throws Exception
	{
		ElementContainer ec = new ElementContainer();

		String query = "SELECT * FROM user_system_data WHERE user_name = '" + user + "'";
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery(query);
		if (results != null && results.first())
		{
			ResultSetMetaData resultsMetaData = results.getMetaData();
			ec.addElement(DatabaseUtilities.writeTable(results, resultsMetaData));
			ec.addElement(new P().addElement("You have been authenticated with " + method));
		}
		else
		{
			throw new Exception("Bad cookie");
		}
		ec.addElement(new P().addElement(ECSFactory.makeLink("Logout", LOGOUT, true)));
		ec.addElement(new P().addElement(ECSFactory.makeLink("Refresh", "", "")));
		return (ec);
	}



	/**
	 *  Description of the Method
	 *
	 * @param  username  Description of the Parameter
	 * @param  password  Description of the Parameter
	 * @return           Description of the Return Value
	 */
	protected String mangleLogin(String username, String password)
	{
		StringBuffer name = new StringBuffer();
		for (int i = 0; i < username.length(); i++)
		{
			name.append(String.valueOf((char) (username.charAt(i) + 1)));
		}

		StringBuffer pass = new StringBuffer();
		for (int j = 0; j < password.length(); j++)
		{
			pass.append(String.valueOf((char) (password.charAt(j) + 1)));
		}

		String returnVal = name.reverse().toString() + pass.reverse().toString();
		System.out.println("cookie val is " + returnVal);
		return returnVal;
	}


	/**
	 *  Gets the instructions attribute of the WeakAuthenticationCookie object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Login using the jeff/jeff or dave/dave accounts.";
		return (instructions);
	}

}

