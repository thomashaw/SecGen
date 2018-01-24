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
package net.sf.jboard.struts.plugins;

import java.util.HashMap;
import java.util.Map;

import net.sf.jboard.model.reg.clients.RegBaseClient;
import net.sf.jboard.security.DBViaJNDILogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import javax.servlet.ServletException;


/**
 * this class is lauched when the application parse the struts-config.xml file.
 * it initialize the security settings, like the jaas settings.
 * 
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.5 $
 */
public class SecurityPlugin implements PlugIn {

    private static Log logger = LogFactory.getLog(SecurityPlugin.class);

    //datasource which provides users informations
    private String regAuthenticationDatasourceName;
    private String regDaoImpl;
    private String userTable;
    private String roleMapTable;
    private String roleTable;
    private String initialAdminLogin;
    private String initialAdminPassword;
    private Map map;
    private RegBaseClient rbc;
    /**
     * @see org.apache.struts.action.PlugIn#destroy()
     */
    public void destroy() {

        
    }

    /** 
     * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet, org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet arg0, ModuleConfig arg1) throws ServletException {


        try {
            
            map =  new HashMap();
            map.put("userTable",userTable);
            map.put("roleMapTable",roleMapTable);
            map.put("roleTable",roleTable);
            
            RegBaseClient.init(regDaoImpl,regAuthenticationDatasourceName,map);
            
            logger.info("regAuthenticationDatasourceName="+regAuthenticationDatasourceName);
            logger.info("regDaoImpl="+regDaoImpl);
            //configure dynamically the loginModule

            
            DBViaJNDILogin.initializeUsersAndRoles(initialAdminLogin,initialAdminPassword);

        } catch(Exception ex) {

            System.err.println("Can't initialize Security Plugin " + ex.getMessage());
            System.out.println("Can't initialize Security Plugin " + ex.getMessage());
            
        }

    }


	/**
	 * @return
	 */
	public String getRegAuthenticationDatasourceName() {
		return regAuthenticationDatasourceName;
	}

	/**
	 * @param string
	 */
	public void setRegAuthenticationDatasourceName(String string) {
        regAuthenticationDatasourceName = string;
	}

	/**
	 * @return
	 */
	public String getRoleMapTable() {
		return roleMapTable;
	}

	/**
	 * @return
	 */
	public String getRoleTable() {
		return roleTable;
	}

	/**
	 * @return
	 */
	public String getUserTable() {
		return userTable;
	}

	/**
	 * @param string
	 */
	public void setRoleMapTable(String string) {
		roleMapTable = string;
	}

	/**
	 * @param string
	 */
	public void setRoleTable(String string) {
		roleTable = string;
	}

	/**
	 * @param string
	 */
	public void setUserTable(String string) {
		userTable = string;
	}

	/**
	 * @return
	 */
	public String getInitialAdminLogin() {
		return initialAdminLogin;
	}

	/**
	 * @return
	 */
	public String getInitialAdminPassword() {
		return initialAdminPassword;
	}

	/**
	 * @param string
	 */
	public void setInitialAdminLogin(String string) {
		initialAdminLogin = string;
	}

	/**
	 * @param string
	 */
	public void setInitialAdminPassword(String string) {
		initialAdminPassword = string;
	}

    /**
     * @return
     */
    public String getRegDaoImpl() {
        return regDaoImpl;
    }

    /**
     * @param string
     */
    public void setRegDaoImpl(String string) {
        regDaoImpl = string;
    }

}
