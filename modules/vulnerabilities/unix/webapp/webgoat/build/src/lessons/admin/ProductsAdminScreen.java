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
public class ProductsAdminScreen extends LessonAdapter
{

	private final static String QUERY = "SELECT * FROM product_system_data";
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

			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery(QUERY);
			if (results != null)
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
	 *  Gets the category attribute of the ProductsAdminScreen object
	 *
	 * @return    The category value
	 */
	public String getCategory()
	{
		return "Admin Functions";
	}


	/**
	 *  Gets the menuItem attribute of the ProductsAdminScreen object
	 *
	 * @return    The menuItem value
	 */

	protected Element getMenuItem()
	{
		return makeMenuItem("Products", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the role attribute of the ProductsAdminScreen object
	 *
	 * @return    The role value
	 */
	public String getRole()
	{
		return ADMIN_ROLE;
	}


	/**
	 *  Gets the title attribute of the ProductsAdminScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("Product Information");
	}

}

