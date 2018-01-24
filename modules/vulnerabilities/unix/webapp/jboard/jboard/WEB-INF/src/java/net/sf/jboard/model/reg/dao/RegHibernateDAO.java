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


import java.util.HashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.reg.dto.Role;
import net.sf.jboard.model.reg.dto.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class RegHibernateDAO implements RegDAOInterface {

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
     
    private static Log logger = LogFactory.getLog(RegHibernateDAO.class);
    private static Map map;
    private Session session;
    private String hibernateConfigFile = "/reg.cfg.xml";    

    /* (non-Javadoc)
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#setAuthDatasourceName(java.lang.String)
     */
    public void setAuthDatasourceName(String authDatasourceName) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getAuthDatasourceName()
     */
    public String getAuthDatasourceName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#setForumDatasourceName(java.lang.String)
     */
    public void setForumDatasourceName(String authDatasourceName) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getForumDatasourceName()
     */
    public String getForumDatasourceName() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * add users not present in the database.
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#addNecessaryUsersAndRoles(java.util.List)
     */
    public void addUser(User user) throws DAOException {
            
            
            try {
                session = RegHibernateUtil.currentSession();
				session.save(user);
                session.flush();
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
                
                try {
					RegHibernateUtil.closeSession();
				} catch (HibernateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
        
    }
    
    
    public void addRole(Role role){
            
            try {
                session = RegHibernateUtil.currentSession();
				session.save(role);
                session.flush();
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
                try {
                    RegHibernateUtil.closeSession();
                } catch (HibernateException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
			}
        
        
    }

    /* (non-Javadoc)
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#setAuthenticationDatasourceSettings(java.util.Map)
     */
    public void setAuthenticationDatasourceSettings(Map map) {
        // TODO Auto-generated method stub

    }

    /**
     * @param String Username
     * @return Map of ids and associated passwords
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#validateUser(java.lang.String)
     */
    public Map validateUser(String userName) throws DAOException {
        
        Map userMap;
        
        try {
                session = RegHibernateUtil.currentSession();
                List users = session.find(" from net.sf.jboard.model.reg.dto.User as user where user.userName=? ",userName,Hibernate.STRING);;
                userMap = new HashMap();
                User user =(User)users.get(0);
                userMap.put("id",user.getId());
                userMap.put("password",user.getPassword());
        } catch (HibernateException e) {
            throw new DAOException(e.getMessage()); 
        }finally{
            
            try {
                RegHibernateUtil.closeSession();
            } catch (HibernateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return userMap;
    }

    /**
     * retrieve user's roles.
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#retrieveUserInformation(int)
     */
    public Set retrieveUserRoles(long id) throws DAOException {
        Set rolesSet = new HashSet();
        try {
        session = RegHibernateUtil.currentSession();
        User user = (User)session.load(User.class,new Long(id));
        
        return user.getRoles();
        } catch (HibernateException e) {
          throw new DAOException(e.getMessage()); 
        }finally{
    
            try {
                RegHibernateUtil.closeSession();
            } catch (HibernateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

     /**
     * checks if a user with the same username already exists.
     * @param User 
     * @return result. true if a user with the same name exists, false otherwise.
     */
    public boolean userAlreadyExists(User user) throws DAOException {
       int rowNumber = 0;
        
    try {
		session = RegHibernateUtil.currentSession();
        Query q = session.createQuery("select count(*) from net.sf.jboard.model.reg.dto.User as user " +            "where user.userName=:username and user.password=:password ");
        q.setParameter("username",user.getUserName());
        q.setParameter("password",user.getPassword());
        
        //we don't initalize the collection to find the collection size
        rowNumber = ((Integer)q.iterate().next()).intValue();
        if(rowNumber>0){
            return true;
        }
	} catch (HibernateException e) {
	  throw new DAOException(e.getMessage()); 
    }finally{
        
        try {
			RegHibernateUtil.closeSession();
		} catch (HibernateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
       
        return false;
    }

   /**
    *  checks if a role with the same name already exists.
    * @param role
    * @return
    * @throws DAOException
    */
    public boolean roleAlreadyExists(Role role) throws DAOException{
        int rowNumber = 0;
  
        try {
            session = RegHibernateUtil.currentSession();
            Query q = session.createQuery("select count(*) from net.sf.jboard.model.reg.dto.Role as role " +
                "where role.roleName=:roleName ");
            q.setParameter("roleName",role.getRoleName());
           
        
            //we don't initalize the collection to find the collection size
            rowNumber = ((Integer)q.iterate().next()).intValue();
            if(rowNumber>0){
                return true;
            }
        } catch (HibernateException e) {
          throw new DAOException(e.getMessage()); 
        }finally{
        
            try {
                RegHibernateUtil.closeSession();
            } catch (HibernateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
       
            return false;
       
    }


    /* (non-Javadoc)
     * @see net.sf.jboard.model.reg.dao.RegDAOInterface#register(net.sf.jboard.model.reg.dto.User)
     */
    public boolean register(User user) throws DAOException {
        
        
        try{
            session = RegHibernateUtil.currentSession();
            session.save(user);
            session.flush();
        } catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
            try {
				RegHibernateUtil.closeSession();
			} catch (HibernateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
        
        
        return false;
    }

	/**
	 * @return
	 */
	public static String getAuthenticationDatasourceName() {
		return authenticationDatasourceName;
	}

	/**
	 * @return
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param string
	 */
	public static void setAuthenticationDatasourceName(String string) {
		authenticationDatasourceName = string;
	}

	/**
	 * @param session
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
     * initialize the sessionFactory.
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#initDAO()
	 */
	public void initDAO() throws DAOException {
        
        RegHibernateUtil.init(hibernateConfigFile);
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getRole(java.lang.String)
	 */
	public Role getRole(String name) throws DAOException {
        List results = null;
        
        try{
            session = RegHibernateUtil.currentSession();
             results  = (List)session.find(" from net.sf.jboard.model.reg.dto.Role role where role.roleName = ? ",name,Hibernate.STRING);
            
            session.flush();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                RegHibernateUtil.closeSession();
            } catch (HibernateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        return (Role)results.get(0);
		
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#addRoleToUser(net.sf.jboard.model.reg.dto.User, java.lang.String)
	 */
	public void addRoleToUser(User user, String roleName) throws DAOException {
		
        try{
            session = RegHibernateUtil.currentSession();
            Role role = getRole(roleName);
            session = RegHibernateUtil.currentSession();
            user.getRoles().add(role);
            session.saveOrUpdate(user);
            session.flush();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                RegHibernateUtil.closeSession();
            } catch (HibernateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.jboard.model.reg.dao.RegDAOInterface#getUser(java.lang.String)
	 */
	public User getUser(String userName) throws DAOException {
        User user = null;
        List results = null;
        try{
            session = RegHibernateUtil.currentSession();
            results  = session.find(" from net.sf.jboard.model.reg.dto.User user where user.userName = ? ",userName,Hibernate.STRING);
            session.flush();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                RegHibernateUtil.closeSession();
            } catch (HibernateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        return (User)results.get(0);
	}


}
