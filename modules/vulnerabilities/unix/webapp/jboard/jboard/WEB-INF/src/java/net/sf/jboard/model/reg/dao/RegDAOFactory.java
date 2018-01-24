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
package net.sf.jboard.model.reg.dao;


import java.util.Map;

import net.sf.jboard.fwk.exception.DAOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Charles GAY
 *
 */
public class RegDAOFactory {


    private static Log logger = LogFactory.getLog(RegDAOFactory.class);
    private static RegDAOInterface rdi;
    private static String daoImpl;
    private static String authDatasourceName;    

	/**
	 * Convenience method that returns the current {@link RegDAOInterface}
	 * implementation.
	 *
	 * @return ForumDAOInterface
	 */
	public  static RegDAOInterface getDAO() {
	
        //RegDAOInvocationHandler fdih;
        RegDAOInterface rdi = new RegHibernateDAO();        
                
        //fdih =  new RegDAOInvocationHandler(rdi);
        //rdi =  (RegDAOInterface) Proxy.newProxyInstance(RegHibernateDAO.class.getClassLoader(),new Class[] { RegDAOInterface.class },fdih);
       
	    return rdi;
	
	}
    
    /**
     * initialize the DAO implementation.
     * @param daoImplementation
     * @param dataSrcName
     * @param map
     */
    public static  void initRegFactory(String daoImplementation,String dataSrcName,Map map){
            daoImpl = "net.sf.jboard.model.reg.dao.Reg"+daoImplementation;
            
            authDatasourceName = dataSrcName;
            logger.debug("daoImpl="+daoImpl);
            try {
                
                logger.debug("daoImpl="+daoImpl);
                rdi = (RegDAOInterface)Class.forName(daoImpl).newInstance();
                rdi.setAuthDatasourceName(authDatasourceName);
                rdi.setAuthenticationDatasourceSettings(map);
                rdi.initDAO();
            
            } catch (InstantiationException e) {
                logger.error("InstantiationException DAOImpl problem");
            } catch (IllegalAccessException e) {
                logger.error("IllegalAccessException DAOImpl problem");
            } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException DAOImpl problem");
            } catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
}
