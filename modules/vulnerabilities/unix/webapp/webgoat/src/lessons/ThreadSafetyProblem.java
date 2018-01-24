import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;

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
public class ThreadSafetyProblem extends LessonAdapter
{

	/**
	 *  Constructor for the ConcurrencyScreen object
	 *
	 * @param  s  Description of the Parameter
	 */
	public void start(WebSession s)
	{
		try
		{
			setup(s);

			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception caught: " + e);
			e.printStackTrace(System.out);
		}
	}


	private final static String USER_NAME = "username";
	private static Connection connection = null;


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		ElementContainer ec = new ElementContainer();
		try
		{
			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}

			ec.addElement(new StringElement("Enter user name: "));
			ec.addElement(new Input(Input.TEXT, USER_NAME, ""));
			currentUser = s.getParser().getRawParameter(USER_NAME, "");

			Element b = ECSFactory.makeButton("Submit");
			ec.addElement(b);

			if (!"".equals(currentUser))
			{
				Thread.sleep(1000);

				// Get the users info from the DB
				String query = "SELECT * FROM user_system_data WHERE user_name = '" + currentUser + "'";

				Statement statement = connection.createStatement();

				ResultSet results = statement.executeQuery(query);
				if (results != null && results.first() == true)
				{
					ResultSetMetaData resultsMetaData = results.getMetaData();
					ec.addElement(DatabaseUtilities.writeTable(results, resultsMetaData));
				}

			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}

		return (ec);
	}


	private static String currentUser;


	/**
	 *  Gets the hints attribute of the ConcurrencyScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("Web applications handle many HTTP requests at the same time.");
		hints.add("Developers use variables that are not thread safe.");
		hints.add("Open two browsers and send 'jeff' in one and 'dave' in the other.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the ConcurrencyScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Thread Safety", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the ConcurrencyScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(80);
	}


	/**
	 *  Gets the title attribute of the ConcurrencyScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Exploit Thread Safety Problems");
	}


	/**
	 *  Gets the instructions attribute of the ThreadSafetyProblem object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Please enter your username to access your account.";
		return (instructions);
	}

}

