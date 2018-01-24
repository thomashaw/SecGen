import java.sql.*;

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
public class RefreshDBScreen extends LessonAdapter
{

	private final static String REFRESH = "Refresh";
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
			boolean refresh = s.getParser().getBooleanParameter(REFRESH, false);

			if (refresh)
			{
				refreshDB(s);

				ec.addElement(new StringElement("Successfully refreshed the database."));
			}
			else
			{
				Element label = new StringElement("Refresh the database? ");
				A link1 = ECSFactory.makeLink("Yes", REFRESH, true);
				A link2 = ECSFactory.makeLink("No", REFRESH, false);

				TD td1 = new TD().addElement(label);
				TD td2 = new TD().addElement(link1);
				TD td3 = new TD().addElement(link2);

				TR row = new TR().addElement(td1).addElement(td2).addElement(td3);
				Table t = new Table().setCellSpacing(40).setWidth("50%");
				if (s.isColor())
				{
					t.setBorder(1);
				}
				t.addElement(row);

				ec.addElement(t);
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
	 */
	public void refreshDB(WebSession s)
	{
		try
		{
			if (connection == null)
			{
				connection = DatabaseUtilities.makeConnection(s);
			}

			CreateDB db = new CreateDB();
			db.makeDB(connection);
			System.out.println("Successfully refreshed the database.");

		}
		catch (Exception e)
		{
			s.setMessage("Error refreshing database " + this.getClass().getName());
			e.printStackTrace();
		}
	}


	/**
	 *  Gets the category attribute of the RefreshDBScreen object
	 *
	 * @return    The category value
	 */
	public String getCategory()
	{
		return "Admin Functions";
	}


	/**
	 *  Gets the menuItem attribute of the RefreshDBScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Refresh Database", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the role attribute of the RefreshDBScreen object
	 *
	 * @return    The role value
	 */
	public String getRole()
	{
		return ADMIN_ROLE;
	}


	/**
	 *  Gets the title attribute of the RefreshDBScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("Refresh Database");
	}

}

