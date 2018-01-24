import java.io.*;
import java.sql.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

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
public class DatabaseUtilities
{

	/**
	 *  Description of the Method
	 *
	 *@param  s                           Description of the Parameter
	 *@return                             Description of the Return Value
	 *@exception  ClassNotFoundException  Description of the Exception
	 *@exception  SQLException            Description of the Exception
	 */
	public static Connection makeConnection( WebSession s ) throws ClassNotFoundException, SQLException
	{
		Class.forName( s.getDBDriver() );
		new org.enhydra.instantdb.jdbc.idbDriver();
		return ( DriverManager.getConnection( s.getDBUrl() ) );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  results           Description of the Parameter
	 *@param  resultsMetaData   Description of the Parameter
	 *@return                   Description of the Return Value
	 *@exception  IOException   Description of the Exception
	 *@exception  SQLException  Description of the Exception
	 */
	public static MultiPartElement writeTable( ResultSet results,
		ResultSetMetaData resultsMetaData ) throws IOException,
		SQLException
	{
		int numColumns = resultsMetaData.getColumnCount();

		results.beforeFirst();
		if ( results.next() )
		{
			Table t = new Table( 1 );
			// 1 = with border
			t.setCellPadding( 1 );
			TR tr = new TR();
			for ( int i = 1; i < numColumns + 1; i++ )
			{
				tr.addElement( new TD( new B( resultsMetaData.getColumnName( i ) ) ) );
			}
			t.addElement( tr );

			results.beforeFirst();
			while ( results.next() )
			{
				TR row = new TR();
				for ( int i = 1; i < numColumns + 1; i++ )
				{
					row.addElement( new TD( results.getString( i ) ) );
				}
				t.addElement( row );
			}

			return ( t );
		}
		else
		{
			return ( new B( "Query Successful; however no data was returned from this query." ) );
		}
	}



}
