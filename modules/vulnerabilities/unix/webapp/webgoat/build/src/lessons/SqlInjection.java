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
public class SqlInjection extends LessonAdapter
{

	/**
	 *  Constructor for the DatabaseFieldScreen object
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


	private final static String ACCT_NUM = "account_number";
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

			ec.addElement(new P().addElement("Enter an Account Number: "));
			String accountNumber = s.getParser().getRawParameter(ACCT_NUM, "101");

			Input input = new Input(Input.TEXT, ACCT_NUM, accountNumber.toString());
			ec.addElement(input);

			Element b = ECSFactory.makeButton("Go!");
			ec.addElement(b);

			String query = "SELECT * FROM user_data WHERE userid = '" + accountNumber + "'";

			ec.addElement(new PRE(query));

			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery(query);
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
	 *  Gets the hints attribute of the DatabaseFieldScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("The application is taking your input and inserting it at the end of a pre-formed SQL command.");
		hints.add("Compound SQL statements can be made by joining multiple tests with keywords like AND and OR.");
		hints.add("Try entering [ 101\' OR 1 = 1 ].");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the DatabaseFieldScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("SQL Injection", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the DatabaseFieldScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(70);
	}


	/**
	 *  Gets the title attribute of the DatabaseFieldScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Perform SQL Injection");
	}


	/**
	 *  Gets the instructions attribute of the SqlInjection object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Enter your account number to review your credit card numbers.";
		return (instructions);
	}

}

