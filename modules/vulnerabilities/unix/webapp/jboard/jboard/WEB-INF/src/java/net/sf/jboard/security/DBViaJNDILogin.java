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
package net.sf.jboard.security;


import java.util.ArrayList;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.reg.dao.RegDAOFactory;
import net.sf.jboard.model.reg.dao.RegDAOInterface;
import net.sf.jboard.model.reg.dto.Role;
import net.sf.jboard.model.reg.dto.User;

import com.tagish.auth.SimpleLogin;
import com.tagish.auth.TypedPrincipal;
import com.tagish.auth.Utils;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class DBViaJNDILogin extends SimpleLogin {

    
    private static String userTable;
    private static String roleMapTable;
    private static String roleTable;
    private static Log logger = LogFactory.getLog(DBViaJNDILogin.class);
    private static RegDAOInterface regDAOImpl;

    private static Role adminRole;
    private static Role guestRole;
    private static User adminUser;
    private static User guestUser;

    /**
     * @param username
     * @param char[] password
     * @return Vector of Principals
     */
    protected synchronized Vector validateUser(
        String username,
        char password[])
        throws LoginException {
        
        Map map;
        RegDAOInterface regDaoImpl;
        //id from datasource
        long id;
        //password from datasource
        String pwd;
        String passwordFromForm;
        Set rolesSet;
        Iterator it;
        Vector p ;
        
            regDaoImpl = RegDAOFactory.getDAO();
            p = new Vector();
            
            try{

                map = regDaoImpl.validateUser(username);
                if(map==null){
                    throw new LoginException(" validation failed");
                }
                id = ((Long) map.get("id")).intValue();
                pwd = (String) map.get("password");
    
                try {
                    passwordFromForm = new String(Utils.cryptPassword(password));
                } catch (Exception e) {
                    throw new LoginException("Error encoding password (" + e.getMessage() + ")");
                }
                logger.debug("pwd="+pwd);
                logger.debug("passwordFromForm="+passwordFromForm);
                if (!passwordFromForm.equals(pwd))
                    throw new FailedLoginException("Bad password");
    
                rolesSet = regDaoImpl.retrieveUserRoles(id);
                
                it = rolesSet.iterator();
                
               // p.add(new TypedPrincipal(username, TypedPrincipal.USER));
    
                while (it.hasNext()){
                    p.add( new TypedPrincipal(((Role)it.next()).getRoleName(), TypedPrincipal.USER));
                }
                
            }catch(DAOException daoex){
                logger.error(" authentication datasource access failed ");    
                throw new LoginException("authentication datasource access failed");        
            }
            return p;

        

    }

    public void initialize(
        Subject subject,
        CallbackHandler callbackHandler,
        Map sharedState,
        Map options) {
        super.initialize(subject, callbackHandler, sharedState, options);
    }

    /**
     * intialize admin login & password if the tables are empty (first initialisation).
     * @param login
     * @param password
     * @return true if the administrator login & password has been set
     */
    public static boolean initializeUsersAndRoles(String login, String password) throws DAOException {
      
            regDAOImpl = RegDAOFactory.getDAO();
            initializeRoles(); 
            initializeUsers(login, password);
            relyRolesAndUsers();
            return true;
    }

	/**
	 * 
	 */
	private static void relyRolesAndUsers() throws DAOException {
		boolean oneUserisAdmin = false;
        boolean oneUserisGuest = false;
        
        Role adminRole = regDAOImpl.getRole("admin");
        if(adminRole.getUsers().size()>=1){
            oneUserisAdmin = true;
        }else{
            adminUser = regDAOImpl.getUser("admin");
            regDAOImpl.addRoleToUser(adminUser,"admin");
        }
        Role guestRole = regDAOImpl.getRole("guest");
        if(guestRole.getUsers().size()>=1){
            oneUserisGuest = true;
        }else{
            guestUser = regDAOImpl.getUser("guest");
            regDAOImpl.addRoleToUser(guestUser,"guest");
        }
	}

	private static void initializeUsers(String login, String password) {
            

    		List list;
			adminUser = new User();
		    adminUser.setUserName(login);
            Iterator it;
            User user;
		    
		    try {
		        adminUser.setPassword(String.valueOf(Utils.cryptPassword(password.toCharArray())));
		    } catch (Exception e1) {
		        logger.error("encoding admin password failed" + e1.getMessage());
		    }
		
		   guestUser = new User();
		   guestUser.setUserName("guest");
		    
		    try {
		        //guest password 
		        guestUser.setPassword(String.valueOf(Utils.cryptPassword("guest".toCharArray())));
		    } catch (Exception e) {
		        logger.error("encoding admin password failed" + e.getMessage());
		    }
		   list = new ArrayList();
           list.add(adminUser);   
           list.add(guestUser);
           it = list.iterator();
           
           try{
                 while(it.hasNext()){
                     user = (User)it.next(); 
                    if(!regDAOImpl.userAlreadyExists(user)) {
                        regDAOImpl.addUser(user);
                    }
                 }
    		    
           }catch(DAOException daoex){
               
               //TODO enhance exception handling 
           }
	}
    
    private static void initializeRoles(){
        
        adminRole = new Role();
        guestRole = new Role();
        adminRole.setRoleName("admin");
        guestRole.setRoleName("guest");
        
        
        try{
        if(!regDAOImpl.roleAlreadyExists(adminRole)){
           regDAOImpl.addRole(adminRole);
        }
        if(!regDAOImpl.roleAlreadyExists(guestRole)){
        regDAOImpl.addRole(guestRole);
        }
        }catch(DAOException daoex){
            //enhance exception handling
        }
    }
   

    /**
     * @param string
     */
    public static void setRoleMapTable(String string) {
        roleMapTable = string;
    }

    /**
     * @param string
     */
    public static void setRoleTable(String string) {
        roleTable = string;
    }

    /**
     * @param string
     */
    public static void setUserTable(String string) {
        userTable = string;
    }

}
