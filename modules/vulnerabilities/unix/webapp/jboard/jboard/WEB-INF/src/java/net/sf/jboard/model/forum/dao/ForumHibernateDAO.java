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
package net.sf.jboard.model.forum.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.forum.dto.ForumCategory;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.model.forum.dto.ForumThread;
import net.sf.jboard.model.forum.dto.ForumMessage;
import net.sf.jboard.model.forum.dto.IForumComponent;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class ForumHibernateDAO implements ForumDAOInterface {

    /** The <code>Log</code> instance for this application. */
    private Log logger = LogFactory.getLog(ForumHibernateDAO.class);
    private Session session;
   

   

    /**
     * create a category in the datasource.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#createCategory(net.sf.jboard.model.forum.dto.ForumCategory)
     */
    public void createCategory(ForumCategory cat) throws DAOException {
        
        
        try {
            session = ForumHibernateUtil.currentSession();
          
            session.save(cat);

            List results;
            results = session.find("select max(forumCategory.componentPosition) from " +ForumCategory.class.getName()+" forumCategory " );
            Long max = (Long)results.get(0);
            if(max!=null){
            max = new Long(max.longValue()+1);
            }else{
            max = new Long(1);            
            }
            cat.setComponentPosition(max);
            session.flush();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
       
    }

    /**
     * retrieve the Category.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#readCategory(long)
     */
    public ForumCategory readCategory(Long catId) throws DAOException {
        ForumCategory cat = null;
        
        try {
                session = ForumHibernateUtil.currentSession();
                cat = (ForumCategory) session.load(ForumCategory.class, catId);
                session.flush();
            } catch (HibernateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        return cat;
    }

    /**
     * retrieve the message.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#readMessage(long)
     */
    public ForumMessage readMessage(Long msgId) throws DAOException {
        ForumMessage msg = null;
        
           try {
               session = ForumHibernateUtil.currentSession();
               msg = (ForumMessage) session.load(ForumMessage.class, msgId);
               session.flush();
           } catch (HibernateException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
        
           return msg;
    }

    /**
     * delete the forumCategory and her relative objects.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#deleteCategory(long)
     */
    public void deleteCategory(Long categoryId) throws DAOException {
        try {
            session = ForumHibernateUtil.currentSession();
            ForumCategory fcat =(ForumCategory)session.load(ForumCategory.class,categoryId);
			session.delete(fcat);
            session.flush();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ;
    }

    /**
     * update Category.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#updateCategory(net.sf.jboard.model.forum.dto.ForumCategory)
     */
    public void updateCategory(ForumCategory cat) throws DAOException {
        
        try {
            session = ForumHibernateUtil.currentSession();
			session.update(cat);
            session.flush();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    /* (non-Javadoc)
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#createForumItem(net.sf.jboard.model.forum.dto.ForumItem)
     */
    public void createForumItem(ForumItem forumItem) throws DAOException {
        
        List results;
       
        try {
            session = ForumHibernateUtil.currentSession();

            //load the parent
            ForumCategory cat =(ForumCategory)session.load(ForumCategory.class,forumItem.getParentId());
            addHibernateChild(cat,forumItem);


            forumItem.setComponentPosition(getComponentPosition(ForumItem.class,forumItem.getParentId()));
            session.save(forumItem);            
            session.flush();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
    }

    /* (non-Javadoc)
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#deleteForumItem(long)
     */
    public void deleteForumItem(Long forumItemId) throws DAOException {
       ForumItem fit = new ForumItem();
       fit.setId(forumItemId);
       try {
             session = ForumHibernateUtil.currentSession();
		     session.delete(fit);
             session.flush();
    	} catch (HibernateException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
        
    }

    /**
     * update  forumItem.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#updateForumItem(net.sf.jboard.model.forum.dto.ForumItem)
     */
    public void updateForumItem(ForumItem forumItem) throws DAOException {
       
        ForumItem fit;
        ForumCategory cat;
        try {
            session = ForumHibernateUtil.currentSession();
            fit = (ForumItem)session.load(ForumItem.class,forumItem.getId());
            
           
            fit.setTitle(forumItem.getTitle());
            fit.setDescription(forumItem.getDescription());
            session.update(fit);
           
            
            session.flush();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }

    /**
     * create a forumThread and his first associated message.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#createForumThread(net.sf.jboard.model.forum.dto.ForumThread, net.sf.jboard.model.forum.dto.ForumMessage)
     */
    public void createForumThread(ForumThread forumThread, ForumMessage msg)
        throws DAOException {

            List results;               
            try {
                session = ForumHibernateUtil.currentSession();
    
                //load the parent
                ForumItem fit =(ForumItem)session.load(ForumItem.class,forumThread.getParentId());
                addHibernateChild(fit,forumThread);
                session.save(forumThread);
                addHibernateChild(forumThread,msg);
                session.save(msg);
                session.flush();
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         
        
    }

    /**
     * delete a forumThread.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#deleteForumThread(long)
     */
    public void deleteForumThread(Long forumThreadId) throws DAOException {
        ForumThread ft = new ForumThread();
        ft.setId(forumThreadId);
        try {
            session = ForumHibernateUtil.currentSession();
			session.delete(ft);
            session.flush();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }

    /**
     * update  forumThread.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#updateForumThread(net.sf.jboard.model.forum.dto.ForumThread)
     */
    public void updateForumThread(ForumThread forumThread)
        throws DAOException {
        ForumItem fit;
        ForumThread ft;
        try {
            session = ForumHibernateUtil.currentSession();
            ft = (ForumThread)session.load(ForumThread.class,forumThread.getId());
           
            ft.setTitle(forumThread.getTitle());
            session.update(ft);
            session.flush();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    /**
     * create a forumMessage.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#createMessage(net.sf.jboard.model.forum.dto.ForumMessage)
     */
    public void createMessage(ForumMessage forumMessage) throws DAOException {
        
        try {
            session = ForumHibernateUtil.currentSession();
            //load the parent
            ForumThread ft =(ForumThread)session.load(ForumThread.class,forumMessage.getParentId());
            addHibernateChild(ft,forumMessage);
            session.save(forumMessage);
            
            session.flush();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
       
    }

    /**
     * delete a message.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#deleteMessage(long)
     */
    public void deleteMessage(Long messageId) throws DAOException {
        
       ForumMessage msg = new ForumMessage();
       msg.setId(messageId);
       try {
           session = ForumHibernateUtil.currentSession();
		   session.delete(msg);
           session.flush();
	} catch (HibernateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       
        
    }

    /**
     * update  message.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#updateMessage(net.sf.jboard.model.forum.dto.ForumMessage)
     */
    public void updateMessage(ForumMessage message) throws DAOException {
        
          ForumThread ft;
          ForumMessage msg;
          try {
              session = ForumHibernateUtil.currentSession();
              msg = (ForumMessage)session.load(ForumMessage.class,message.getId());
            
              try {
                  BeanUtils.copyProperties(msg,message);
              } catch (IllegalAccessException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
              } catch (InvocationTargetException e1) {
                  // TODO Auto-generated catch block
                  e1.printStackTrace();
              }
              //addHibernateChild(ft,msg);
              session.update(msg);
              session.flush();
          } catch (HibernateException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
    }

    /**
     * retrieve the panorama from the database.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#readPanorama()
     * @return ForumRoot
     */
    public ForumRoot readPanorama() throws DAOException {
        
        ForumRoot froot = new ForumRoot();
        
        try{
        
            session = ForumHibernateUtil.currentSession();
            List forumCategories = session.find("from net.sf.jboard.model.forum.dto.ForumCategory forumcategory order by forumcategory.componentPosition ");
            
            boolean vide  = forumCategories.isEmpty();
            froot.setForumCategories(forumCategories);
            session.flush();
            
        }catch(HibernateException hex){
            
            logger.debug(" hibernate problem "+hex.getMessage());
        }
        
        
        return froot;
    }

    /**
     * load a forumItem.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#readForumItem(long)
     */
    public ForumItem readForumItem(Long forumitemId) throws DAOException {
        
        ForumItem fit = null;
        
		try {
            session = ForumHibernateUtil.currentSession();
			fit = (ForumItem) session.load(ForumItem.class, forumitemId);
            session.flush();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return fit;
    }

    /**
     * retrieve this forumThread.
     * @see net.sf.jboard.model.forum.dao.ForumDAOInterface#readForumThread(long)
     */
    public ForumThread readForumThread(Long forumThreadId)
        throws DAOException {
            
           ForumThread ft = null;
        
           try {
               session = ForumHibernateUtil.currentSession();
               ft = (ForumThread) session.load(ForumThread.class, forumThreadId);
               session.flush();
           } catch (HibernateException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
        
           return ft;
    }

	/**
	 * @return
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session
	 */
	public void setSession(Session session) {
		this.session = session;
	}
    
    /**
     * add a child to the IForumComponent in an <strong>hibernate</strong> Way.
     * @param ifc
     */
    public void addHibernateChild(IForumComponent parent,IForumComponent child){
        
        child.setParent(parent);
        child.setParentId(parent.getId());
        parent.addChild(child);
        
    }
    
    /**
     * return the componentPosition for the new created component.
     * @param componentType
     * @param parentComponentId
     * @return
     * @throws HibernateException
     */
    private Long getComponentPosition(Class componentClass,Long parentComponentId) throws HibernateException{
        List results;
        results = session.find("select max(forumComponent.componentPosition) from "+componentClass.getName()+" forumComponent where fk_parent_id = ?",parentComponentId,Hibernate.LONG );
        Long max = (Long)results.get(0);
        if(max!=null){
        max = new Long(max.longValue()+1);
        }else{
        max = new Long(1);            
        }
        return max;
    }

}
