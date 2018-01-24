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
public class DatabaseXss extends LessonAdapter
{

	/**
	 *  Description of the Field
	 */
	public final static String MESSAGE = "message";
	/**
	 *  Description of the Field
	 */
	public final static int MESSAGE_COL = 3;

	/**
	 *  Description of the Field
	 */
	public final static String NUMBER = "Num";

	/**
	 *  Description of the Field
	 */
	public final static int NUM_COL = 1;

	private final static String STANDARD_QUERY = "SELECT * FROM messages";
	/**
	 *  Description of the Field
	 */
	public final static String TITLE = "title";
	/**
	 *  Description of the Field
	 */
	public final static int TITLE_COL = 2;

	private static Connection connection = null;

	private static Random random = new Random();


	/**
	 *  Adds a feature to the Message attribute of the MessageBoardScreen object
	 *
	 * @param  s  The feature to be added to the Message attribute
	 */
	protected void addMessage(WebSession s)
	{
		try
		{
			String title = s.getParser().getRawParameter(TITLE, "");
			String message = s.getParser().getRawParameter(MESSAGE, "");
			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}
			//  add the new message to the DB
			Statement statement = connection.createStatement();
			String query = "INSERT INTO messages VALUES (" + random.nextInt() + ",'" + title + "','" + message + "')";
			statement.executeQuery(query);
		}
		catch (Exception e)
		{
			s.setMessage("Could not add message to database");
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		addMessage(s);

		ElementContainer ec = new ElementContainer();
		ec.addElement(makeInput(s));
		ec.addElement(new HR());
		ec.addElement(makeCurrent(s));
		ec.addElement(new HR());
		ec.addElement(makeList(s));

		return (ec);
	}


	/**
	 *  Gets the hints attribute of the MessageBoardScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("You can put HTML tags in your message.");
		hints.add("Bury a SCRIPT tag in the message to attack anyone who reads it.");
		hints.add("Enter this: &lt;script language=\"javascript\" type=\"text/javascript\"&gt;alert(\"Ha Ha Ha\");&lt;/script&gt; in the message field.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the MessageBoardScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Database XSS", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the MessageBoardScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(100);
	}



	/**
	 *  Gets the title attribute of the MessageBoardScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Perform Database Cross Site Scripting (XSS)");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeCurrent(WebSession s)
	{
		ElementContainer ec = new ElementContainer();
		try
		{
			int messageNum = s.getParser().getIntParameter(NUMBER, 0);

			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}

			Statement statement = connection.createStatement();
			String query = "SELECT * FROM messages WHERE num = " + messageNum;

			ResultSet results = statement.executeQuery(query);

			if (results != null && results.first())
			{
				Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);
				TR row1 = new TR(new TD(new StringElement("Title:")));
				row1.addElement(new TD(new StringElement(results.getString(TITLE_COL))));
				t.addElement(row1);

				TR row2 = new TR(new TD(new StringElement("Message:")));
				row2.addElement(new TD(new StringElement(results.getString(MESSAGE_COL))));
				t.addElement(row2);

				ec.addElement(t);
			}
			else
			{
				ec.addElement(new P().addElement("Could not find message " + messageNum));
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
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeInput(WebSession s)
	{
		Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);

		TR row1 = new TR();
		TR row2 = new TR();

		row1.addElement(new TD(new StringElement("Title: ")));
		Input inputTitle = new Input(Input.TEXT, TITLE, "");
		row1.addElement(new TD(inputTitle));

		TD item1 = new TD();
		item1.setVAlign("TOP");
		item1.addElement(new StringElement("Message: "));
		row2.addElement(item1);

		TD item2 = new TD();
		TextArea ta = new TextArea(MESSAGE, 5, 60);
		item2.addElement(ta);
		row2.addElement(item2);

		t.addElement(row1);
		t.addElement(row2);

		Element b = ECSFactory.makeButton("Submit");

		ElementContainer ec = new ElementContainer();
		ec.addElement(t);
		ec.addElement(new P().addElement(b));
		return (ec);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public static Element makeList(WebSession s)
	{
		Table t = new Table(0).setCellSpacing(0).setCellPadding(0).setBorder(0);
		try
		{
			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}
			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery(STANDARD_QUERY);
			if (results != null && results.first() == true)
			{
				results.beforeFirst();
				for (int i = 0; results.next(); i++)
				{
					A a = ECSFactory.makeLink(results.getString(TITLE_COL), NUMBER, results.getInt(NUM_COL));
					TD td = new TD().addElement(a);
					TR tr = new TR().addElement(td);
					t.addElement(tr);
				}

			}
		}
		catch (Exception e)
		{
			s.setMessage("Error while getting message list.");
		}
		ElementContainer ec = new ElementContainer();
		ec.addElement(new H1("Message List"));
		ec.addElement(t);
		return (ec);
	}


	/**
	 *  Gets the instructions attribute of the DatabaseXss object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Please post a message to to the WebGoat forum. Your messages will be available for everyone to read.";
		return (instructions);
	}

}

