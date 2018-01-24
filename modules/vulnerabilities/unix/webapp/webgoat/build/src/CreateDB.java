import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.enhydra.instantdb.jdbc.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of
 *  the Open Web Application Security Project (http://www.owasp.org) This
 *  software package is published by OWASP under the GPL. You should read and
 *  accept the LICENSE before you use, modify and/or redistribute this software.
 *
 *@author     jwilliams@aspectsecurity.com
 *@created    November 6, 2002
 */
public class CreateDB
{

	/**
	 *  Description of the Method
	 */
	public void CreateDB() { }


	/**
	 *  Description of the Method
	 *
	 *@param  connection                  Description of the Parameter
	 *@exception  ClassNotFoundException  Description of the Exception
	 *@exception  SQLException            Description of the Exception
	 */
	private void createMessageTable( Connection connection ) throws ClassNotFoundException, SQLException
	{
		Statement statement = connection.createStatement();

		// Drop admin user table
		String dropTable = "DROP TABLE messages";
		statement.executeQuery( dropTable );

		// Create the new table
		String createTableStatement = "CREATE TABLE messages (" +
			"num int not null primary key," +
			"title varchar(50)," +
			"message varchar(1000)," +
			")";
		statement.executeQuery( createTableStatement );

		// Populate
		/*
		 *  String insertData1 = "INSERT INTO product_system_data VALUES ('32226','Dog Bone','$1.99')";
		 *  String insertData2 = "INSERT INTO product_system_data VALUES ('35632','DVD Player','$214.99')";
		 *  String insertData3 = "INSERT INTO product_system_data VALUES ('24569','60 GB Hard Drive','$149.99')";
		 *  String insertData4 = "INSERT INTO product_system_data VALUES ('56970','80 GB Hard Drive','$179.99')";
		 *  String insertData5 = "INSERT INTO product_system_data VALUES ('14365','56 inch HDTV','$6999.99')";
		 *  statement.executeQuery(insertData1);
		 *  statement.executeQuery(insertData2);
		 *  statement.executeQuery(insertData3);
		 *  statement.executeQuery(insertData4);
		 *  statement.executeQuery(insertData5);
		 */
	}


	/**
	 *  Description of the Method
	 *
	 *@param  connection                  Description of the Parameter
	 *@exception  ClassNotFoundException  Description of the Exception
	 *@exception  SQLException            Description of the Exception
	 */
	private void createProductTable( Connection connection ) throws ClassNotFoundException, SQLException
	{
		Statement statement = connection.createStatement();

		// Drop admin user table
		String dropTable = "DROP TABLE product_system_data";
		statement.executeQuery( dropTable );

		// Create the new table
		String createTableStatement = "CREATE TABLE product_system_data (" +
			"productid varchar(6) not null primary key," +
			"product_name varchar(20)," +
			"price varchar(10)," +
			")";
		statement.executeQuery( createTableStatement );

		// Populate
		String insertData1 = "INSERT INTO product_system_data VALUES ('32226','Dog Bone','$1.99')";
		String insertData2 = "INSERT INTO product_system_data VALUES ('35632','DVD Player','$214.99')";
		String insertData3 = "INSERT INTO product_system_data VALUES ('24569','60 GB Hard Drive','$149.99')";
		String insertData4 = "INSERT INTO product_system_data VALUES ('56970','80 GB Hard Drive','$179.99')";
		String insertData5 = "INSERT INTO product_system_data VALUES ('14365','56 inch HDTV','$6999.99')";

		statement.executeQuery( insertData1 );
		statement.executeQuery( insertData2 );
		statement.executeQuery( insertData3 );
		statement.executeQuery( insertData4 );
		statement.executeQuery( insertData5 );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  connection                  Description of the Parameter
	 *@exception  ClassNotFoundException  Description of the Exception
	 *@exception  SQLException            Description of the Exception
	 */
	private void createUserAdminTable( Connection connection ) throws ClassNotFoundException, SQLException
	{
		Statement statement = connection.createStatement();

		// Drop admin user table
		String dropTable = "DROP TABLE user_system_data";
		statement.executeQuery( dropTable );

		// Create the new table
		String createTableStatement = "CREATE TABLE user_system_data (" +
			"userid varchar(5) not null primary key," +
			"user_name varchar(12)," +
			"password varchar(10)," +
			"cookie varchar(30)," +
			")";
		statement.executeQuery( createTableStatement );

		// Populate
		String insertData1 = "INSERT INTO user_system_data VALUES ('101','jblow','passwd1', '')";
		String insertData2 = "INSERT INTO user_system_data VALUES ('102','jdoe','passwd2', '')";
		String insertData3 = "INSERT INTO user_system_data VALUES ('103','jplane','passwd3', '')";
		String insertData4 = "INSERT INTO user_system_data VALUES ('104','jeff','jeff', '')";
		String insertData5 = "INSERT INTO user_system_data VALUES ('105','dave','dave', '')";
		statement.executeQuery( insertData1 );
		statement.executeQuery( insertData2 );
		statement.executeQuery( insertData3 );
		statement.executeQuery( insertData4 );
		statement.executeQuery( insertData5 );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  connection                  Description of the Parameter
	 *@exception  ClassNotFoundException  Description of the Exception
	 *@exception  SQLException            Description of the Exception
	 */
	private void createUserDataTable( Connection connection ) throws ClassNotFoundException, SQLException
	{
		Statement statement = connection.createStatement();

		// Delete table if there is one
		String dropTable = "DROP TABLE user_data";
		statement.executeQuery( dropTable );

		// Create the new table
		String createTableStatement = "CREATE TABLE user_data (" +
			"userid varchar(5) not null primary key," +
			"first_name varchar(20)," +
			"last_name varchar(20)," +
			"cc_number varchar(30)," +
			"cc_type varchar(10)," +
			")";
		statement.executeQuery( createTableStatement );

		// Populate it
		String insertData1 = "INSERT INTO user_data VALUES ('101','Joe','Blow','987654321','VISA')";
		String insertData2 = "INSERT INTO user_data VALUES ('101','Joe','Blow','222200001111','MC')";
		String insertData3 = "INSERT INTO user_data VALUES ('102','John','Doe','222200002222','MC')";
		String insertData4 = "INSERT INTO user_data VALUES ('102','John','Doe','222200002222','AMEX')";
		String insertData5 = "INSERT INTO user_data VALUES ('103','Jane','Plane','123456789','MC')";
		String insertData6 = "INSERT INTO user_data VALUES ('103','Jane','Plane','333300003333','AMEX')";
		statement.executeQuery( insertData1 );
		statement.executeQuery( insertData2 );
		statement.executeQuery( insertData3 );
		statement.executeQuery( insertData4 );
		statement.executeQuery( insertData5 );
		statement.executeQuery( insertData6 );
	}


	private static String dbURL;

// dbURL "jdbc:idb:c:\jakarta-tomcat-3.2.1\webapps\WebGoat\WEB-INF\database\database.prp";
// Driver "org.enhydra.instantdb.jdbc.idbDriver"


	private static String jdbcDriver = "org.enhydra.instantdb.jdbc.idbDriver";


	/**
	 *  Description of the Method
	 *
	 *@param  args  Description of the Parameter
	 */
	public static void main( String args[] )
	{

		try
		{
			if ( args.length == 1 )
			{
				// jdbcDriver = args[0];
				dbURL = args[0];

				Class.forName( jdbcDriver );
				Connection connection = DriverManager.getConnection( dbURL );

				CreateDB db = new CreateDB();
				db.makeDB( connection );

				connection.close();
			}
			else
			{
				System.out.println( "\nUsage: java CreateDB [DB URL]\n" );
			}

		}
		catch ( ClassNotFoundException cnfe )
		{
			System.out.println( "Failed to load driver" );
			cnfe.printStackTrace();
		}
		catch ( SQLException sqle )
		{
			System.out.println( "Error connecting to Database" );
			sqle.printStackTrace();
		}

	}



	/**
	 *  Description of the Method
	 *
	 *@param  connection                  Description of the Parameter
	 *@exception  ClassNotFoundException  Description of the Exception
	 *@exception  SQLException            Description of the Exception
	 */
	public void makeDB( Connection connection ) throws ClassNotFoundException, SQLException
	{

		System.out.println( "Successful connection to " + dbURL );

		createUserDataTable( connection );
		createUserAdminTable( connection );
		createProductTable( connection );
		createMessageTable( connection );

		System.out.println( "Success: creating tables." );
	}

}

