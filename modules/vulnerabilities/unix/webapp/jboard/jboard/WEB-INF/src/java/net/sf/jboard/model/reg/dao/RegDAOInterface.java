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
import java.util.Set;

import net.sf.hibernate.Session;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.reg.dto.Role;
import net.sf.jboard.model.reg.dto.User;



/**
 * this DAO provide the ability to operate on the system which stores user profiles.
 * it can be a database, or an ldap server, or anything else....
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public interface RegDAOInterface {
   
   
     
     /**
      * set the authentication datasource reference for the registration module.
      * @param datasourceReference
      */
     public void setAuthDatasourceName(String authDatasourceName);
    /**
     * get the authentication datasource reference for the registration module.
     * @return datasourceReference
     */
    public String  getAuthDatasourceName();
    /**
      * set the forum datasource reference for the registration module.
      * @param datasourceReference
      */
     public void setForumDatasourceName(String authDatasourceName);
    /**
     * get the forum datasource reference for the registration module.
     * @return datasourceReference
     */
    public String  getForumDatasourceName();
     
     
     /**
      * add users in the list to the persistance storage.
      * @param databaseSettings
      * @param users list
      * @throws DAOException
      */
     public  void addUser(User user)  throws DAOException ;
     
     public void addRoleToUser(User user, String roleName)throws DAOException;

    /**
     * add roles in the list to the persistance storage.
     * @param databaseSettings
     * @param users list
     * @throws DAOException
     */
    public  void addRole(Role role)  throws DAOException ;
    
    public Role getRole(String name)throws DAOException;
    
    public User getUser(String userName)throws DAOException;
    
     /**
      * set the map which contains the datasourceSettings like roletable name and so on... 
      * @param map
      */
     public void setAuthenticationDatasourceSettings(Map map);

    /**
     * @param username
     * @return
     */
    public Map validateUser(String username)throws DAOException;

    /**
     * @param Id
     * @return
     */
    public Set retrieveUserRoles(long Id)throws DAOException;
    
    /**
     * checks if a user with the same username already exists.
     * @param User 
     * @return result. true if a user with the same name exists, false otherwise.
     */
    public boolean userAlreadyExists(User user) throws DAOException;
    
    /**
     * checks if a role with the same name already exists.
     * @param role
     * @return
     * @throws DAOException
     */
    public boolean roleAlreadyExists(Role role) throws DAOException;
    
    /**
     * register in datasource the user.
     * @param user
     * @return
     * @throws DAOException
     */
    public boolean register(User user)throws DAOException;
    
    public void initDAO()throws DAOException;
	/**
	 * @param session
	 */
	public void setSession(Session session);
    
}
