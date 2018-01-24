/*
jboard is a java bulletin board.
version $Name:  $
http://sourceforge.net/projects/jboard/
Copyright (C) 2003 Charles GAY
This file is part of jboard.
jboard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

jboard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jboard; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.sf.jboard.model.common;


import net.sf.jboard.fwk.exception.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * Factory for Database Connection.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.3 $
 */
public final class ConnectionFactory  {

    private static Log log = LogFactory.getLog(ConnectionFactory.class);

    /** An exception message to throw if datasource is null (if applicable). */
    public static final String DATASOURCE_ERROR = " Connection pool is not available. Check your configuration, and be sure  you are using valid parameters ";

	
    private  static Map datasources;
    /** The database connection. */
    private static Connection conn = null;
	
    /**
     * Returns a database connection, encapsulating the DAO details.
     *
     * @return java.sql.Connection obtained either from a pool or explicitly
     * 
     * @throws SQLException if an SQL error occurs
     */
    public static Connection getConnection(String datasourceName) throws DAOException  {
        
        //generalize this factory to return any connection from datasource
        //=> datasourceRef will be stored under a map
        try{
			
            //if the datasourceFactory hasn't be initialized
            if(datasources==null){
                initDatasourceFactory();
            }
            //if the datasource hasn't be stored in the map
            if(!datasources.containsKey(datasourceName)){
                init(datasourceName);
            }
            conn = ((DataSource)datasources.get(datasourceName)).getConnection();
			
        }catch(SQLException sqlex){
			log.debug("cause="+sqlex.getCause());
			log.debug("message="+sqlex.getMessage());
			log.debug("error code="+String.valueOf(sqlex.getErrorCode()));
			log.debug("sqlstate="+sqlex.getSQLState());
         log.error("DBCP error Connexion not established!!!!");	
         throw new DAOException(sqlex);

        
        }catch(Exception ex){
			log.debug("cause="+ex.getCause());
			log.debug("message="+ex.getMessage());
			log.debug("stack="+ex.fillInStackTrace());
			throw new DAOException(ex);
        }
   
        // return the connection
        return conn;

    }
    /**
     * initialize the jndi datasource reference storage system.
     * @param dsRef datasource reference
     */

    public static void init(String dsRef) throws DAOException{
   		try{

            
               //we store the jndi datasource 
            
                DataSource ds;
    			InitialContext initContext = new InitialContext();
    			Context envContext  = (Context)initContext.lookup("java:/comp/env");
    			log.debug("context retrieved");
    			ds = (DataSource)envContext.lookup(dsRef);
                //store the jndi reference, and his datasource object
                datasources.put(dsRef,ds);
            
            
		}catch(NamingException nex){
			
			log.debug("namingException");
			throw new DAOException(nex);
			  
		}catch (NullPointerException npe){
			
			log.debug("null pointer exception"+npe.getMessage());
    	
			throw new DAOException(npe);	
    	
    	}
    
    }
    
        
    /**
     * initialiase the datasource factory,
     * and create a <strong>synchronized</strong> datasource references hashmap.
     * 
     * @return boolean true for success, false for failure.
     */
    public static boolean initDatasourceFactory(){
            
            datasources = Collections.synchronizedMap(new HashMap());
            return true;   
    }

}
