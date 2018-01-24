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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.Session;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.common.ConnectionFactory;
import net.sf.jboard.model.reg.dto.Role;
import net.sf.jboard.model.reg.dto.User;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class RegPostgresqlDAO implements RegDAOInterface{

    /**
     * authentication datasource name.
     * this datasource and the forumDatasource can be different.
     * one to authenticate the user
     * (a centralized enterprise datasource which is common to 
     * all the enterprise applications),
     * and the forumDatasource which concerns only our forum application.
     */
    private static String authenticationDatasourceName;
    private static String forumDatasourceName;
     
    private static Log logger = LogFactory.getLog(RegPostgresqlDAO.class);
    private static Map map;


    /**
     * constructor.
     *
     */
    public RegPostgresqlDAO(){
        
    }
    
    /**
     * checks if a user with the same username already exists.
     * @param User 
     * @return result. true if a user with the same name exists, false otherwise.
     */
    public boolean userAlreadyExists(User user) throws DAOException{
        StringBuffer request = new StringBuffer(" SELECT UserID,Password FROM ");
        request.append((String)map.get("userTable"));
        request.append(" where username=\'");
        request.append(user.getUserName());
        request.append("\'");
        PreparedStatement psu;
        ResultSet rsu;
        Connection con = ConnectionFactory.getConnection(authenticationDatasourceName);
        try {
            psu = con.prepareStatement(request.toString());
        
            rsu = psu.executeQuery();
            if(!rsu.next()){
               return false;
            }
            // a User with the same username already exists
            return true;
        
        } catch (SQLException e) {
              logger.error(" authentication datasource access failed ");
              return false;
        }
        
    }

	/**
     * create users and roles defined in the list.
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#addNecessaryUserAndRole(java.lang.String, java.lang.String, java.lang.String)
     * @param databaseSettings 
     * @param list which contains user, password, and role to create
	 */
	public  void addNecessaryUsersAndRoles(List list) throws DAOException {
     
            PreparedStatement pst;
            ResultSet rsu;
            int result;
            String userTable;
            String roleTable;
            String roleMapTable;
            String login;
            String password;
            String role;
            long sequenceNumber;
            long sequenceNumber2;
            Iterator it;
            Connection con = ConnectionFactory.getConnection(authenticationDatasourceName);
            
            
            
            userTable = (String)map.get("userTable");
            roleTable = (String)map.get("roleTable");
            roleMapTable = (String)map.get("roleMapTable");
            
            it = list.iterator();
            
            while(it.hasNext()){
                   login = (String)it.next();
                   password = (String)it.next();
                   role = (String)it.next();
                
                try{
                
                    pst = con.prepareStatement(" SELECT userid FROM "+userTable+" where username = ? and  password =? ");
                    pst.setString(1,login);
                    pst.setString(2,password);
                    rsu = pst.executeQuery();
                    rsu.beforeFirst();
                    //if this user doesn't exist, create it!
                    if(!rsu.next()){
                        pst = con.prepareStatement(" INSERT INTO "+userTable+" (userid,username,password) VALUES(nextval('User_seq'), ? , ? )" );
                        
                        pst.setString(1,login);
                        pst.setString(2,password);
                        result = pst.executeUpdate();
                        
                        pst = con.prepareStatement(" SELECT userid FROM "+userTable+" where username = ? and  password =? ");
                        pst.setString(1,login);
                        pst.setString(2,password);
                        rsu = pst.executeQuery();
                        rsu.beforeFirst();
                        rsu.next();
                    }
                    
                    sequenceNumber = rsu.getLong(1);
                    
                    
                    pst = con.prepareStatement(" SELECT roleid FROM "+roleTable+" where rolename = ? ");
                    pst.setString(1,role);
                    rsu = pst.executeQuery();
                    rsu.beforeFirst();
                    //if this role doesn't exist, create it!
                    if(!rsu.next()){
                    
                        pst = con.prepareStatement(" INSERT INTO "+roleTable+" (roleid,rolename) VALUES( nextval('Role_seq') , ? )" );
                        pst.setString(1,role);
                        result = pst.executeUpdate();
                        
                        pst = con.prepareStatement(" SELECT roleid FROM "+roleTable+" where rolename = ? ");
                        pst.setString(1,role);
                        rsu = pst.executeQuery();
                        rsu.beforeFirst();
                        rsu.next();
                    }
                    
                    sequenceNumber2 = rsu.getLong(1);
                    
                    pst = con.prepareStatement(" SELECT roleid FROM "+roleMapTable+" where userid = ? and roleid = ? ");
                    pst.setLong(1,sequenceNumber);
                    pst.setLong(2,sequenceNumber2);
                    rsu = pst.executeQuery();
                    rsu.beforeFirst();
                    
                    if(!rsu.next()){ 
                    
                        //make the link between role and user
                        //only if the link doesn't exist
                        pst = con.prepareStatement(" INSERT INTO "+roleMapTable+" (userid,roleid) VALUES( "+sequenceNumber+" ,"+sequenceNumber2+" )" );
                        result = pst.executeUpdate();
                    }
                    
                }catch(SQLException sqle){
                    logger.error("necessary roles and users insert failed "+sqle.getMessage());
                    throw new DAOException("necessaryRoleAndUsers");
                }
            }
            
        }
		
	/**
	 * @return
	 */
	public static String getAuthenticationDatasourceName() {
		return authenticationDatasourceName;
	}

	/**
     * set the authentication Datasource Name.
	 * @param string
	 */
	public  void setAuthDatasourceName(String dataSrcName) {
		authenticationDatasourceName = dataSrcName;
	}


	
    
    
    /**
     * 
     * @param userName
     * @return
     * @throws DAOException
     */
    public Map validateUser(String userName) throws DAOException{
        
               Connection con = ConnectionFactory.getConnection(authenticationDatasourceName);
               PreparedStatement psu;
               
               ResultSet rsu;
               
               String userTable = (String)map.get("userTable");
               HashMap results = new HashMap();
               try {
    				psu = con.prepareStatement("SELECT UserID,Password FROM "+userTable+"  WHERE UserName= ? " );
                    
                    
                    
                    psu.setString(1, userName);
                    rsu = psu.executeQuery();
                        
                    if (!rsu.next())  {
                       return null;
                    }
                    results.put("id", new Integer(rsu.getInt(1)));  
                   results.put("password", rsu.getString(2));    
                    
                                                        
    			} catch (SQLException e) {
                    logger.error(" authentication datasource access failed ");
                    throw new DAOException();
                    
    			}
                                                   
            
            return results;
               
    }
    
    /**
     * retrieve user's roles.
     * @param username
     * @return
     */
	public Set retrieveUserRoles(long userId) throws DAOException{
        
        PreparedStatement ps;
        ResultSet rs;
        String roleMapTable = (String)map.get("roleMapTable");
        String roleTable = (String)map.get("roleTable");
        Set roleSet = new HashSet();
        try{
            Connection con = ConnectionFactory.getConnection(authenticationDatasourceName);
            
            ps = con.prepareStatement("SELECT " + roleTable + ".RoleName FROM " +
                                     roleMapTable + "," + roleTable +
                                     " WHERE " + roleMapTable + ".UserID=? AND " +
                                     roleMapTable + ".RoleID=" + roleTable + ".RoleID");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            while(rs.next()){
                roleSet.add(rs.getString(1));
            }
            return roleSet;
        } catch (SQLException e) {
            
            logger.error(" authentication datasource access failed ");
            throw new DAOException();
                      
        }
            
    }

	/**
     * set the map which contains the datasourceSettings like roletable name and so on...
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#setAuthenticationDatasourceSettings(java.util.Map)
	 */
	public void setAuthenticationDatasourceSettings(Map map) {
        RegPostgresqlDAO.map = map;
		
	}


    /** 
     * register the user in the database.
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#register(net.sf.jboard.model.reg.dto.User)
     */
    public boolean register(User user) throws DAOException {
        
        Connection forumCon = ConnectionFactory.getConnection(forumDatasourceName);
        Connection authCon = ConnectionFactory.getConnection(authenticationDatasourceName);
        PreparedStatement ps;
        
        
        //insert into authenticated datasource
        StringBuffer userTableRequest = new StringBuffer("INSERT INTO ") ;
        userTableRequest.append((String)map.get("userTable"));
        userTableRequest.append(" (username,password) values(\'");
        userTableRequest.append(user.getUserName());
        userTableRequest.append("\',\'");
        userTableRequest.append(user.getPassword());
        userTableRequest.append("\')");
        try {
                ps = authCon.prepareStatement(userTableRequest.toString());
                ps.executeUpdate();
        } catch (SQLException e) {
           logger.error(" registration in authentication datasource failed ");
           throw new DAOException(e.getMessage());
        }

        
        StringBuffer forumUsersRequest = new StringBuffer("INSERT INTO ");
        forumUsersRequest.append(" ForumUsers(pseudonym,password,firstName,lastName,icq,aim,msn,yahoo,webSite,localisation,job,hobbies,signature) ");
        forumUsersRequest.append(" values (\'");
        forumUsersRequest.append(user.getUserName());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getPassword());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getFirstName());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getLastName());
        forumUsersRequest.append("\',");
        forumUsersRequest.append(user.getIcq());
        forumUsersRequest.append(",\'");
        forumUsersRequest.append(user.getAim());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getMsn());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getYahoo());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getWebSite());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getLocalisation());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getJob());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getHobbies());
        forumUsersRequest.append("\',\'");
        forumUsersRequest.append(user.getSignature());
        forumUsersRequest.append("\' )");
        
        try {
            ps = forumCon.prepareStatement(forumUsersRequest.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
           logger.error(" registration in authentication datasource failed ");
           throw new DAOException(e.getMessage());
           
        }

        return true;
    }


    /**
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getAuthDatasourceName()
     */
    public String getAuthDatasourceName() {
        
        return authenticationDatasourceName;
        
    }


    /**
     * set the forum Datasource Name.
     * @param String forum Datasource Name
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#setForumDatasourceName(java.lang.String)
     */
    public void setForumDatasourceName(String forumDatasrcName) {
        forumDatasourceName = forumDatasrcName;
        
    }


    /**
     * get the forumDatasource name.
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getForumDatasourceName()
     */
    public String getForumDatasourceName() {
        
         return forumDatasourceName;
    }

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#initDAO()
	 */
	public void initDAO() throws DAOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#setSession(net.sf.hibernate.Session)
	 */
	public void setSession(Session session) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#addUser(net.sf.jboard.model.reg.dto.User)
	 */
	public void addUser(User user) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#addRole(net.sf.jboard.model.reg.dto.Role)
	 */
	public void addRole(Role role) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#roleAlreadyExists(net.sf.jboard.model.reg.dto.Role)
	 */
	public boolean roleAlreadyExists(Role role) throws DAOException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getRole(java.lang.String)
	 */
	public Role getRole(String name) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#addRoleToUser(net.sf.jboard.model.reg.dto.User, java.lang.String)
	 */
	public void addRoleToUser(User user, String roleName) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getUser(java.lang.String)
	 */
	public User getUser(String userName) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
