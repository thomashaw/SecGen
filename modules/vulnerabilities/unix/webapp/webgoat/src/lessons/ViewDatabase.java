import java.io.*;
import java.sql.*;
import java.util.*;

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
public class ViewDatabase extends LessonAdapter
{

	private final static String SQL = "sql";
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
			ec.addElement(new StringElement("Enter a SQL statement: "));
			StringBuffer sqlStatement = new StringBuffer(s.getParser().getRawParameter(SQL, ""));

			Input input = new Input(Input.TEXT, SQL, sqlStatement.toString());
			ec.addElement(input);

			Element b = ECSFactory.makeButton("Go!");
			ec.addElement(b);

			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}
			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery(sqlStatement.toString());
			if (results != null && results.first() == true)
			{
				ResultSetMetaData resultsMetaData = results.getMetaData();
				ec.addElement(DatabaseUtilities.writeTable(results, resultsMetaData));
			}

		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}

		return (ec);
	}


	/**
	 *  Gets the category attribute of the DatabaseScreen object
	 *
	 * @return    The category value
	 */
	public String getCategory()
	{
		return "Admin Functions";
	}


	/**
	 *  Gets the hints attribute of the DatabaseScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("There are no hints defined");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the DatabaseScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("View Database", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the title attribute of the DatabaseScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("Database Dump");
	}


	/**
	 *  Gets the instructions attribute of the ViewDatabase object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Please post a message to to the WebGoat forum. Your messages will be available for everyone to read.";
		return (instructions);
	}

}

