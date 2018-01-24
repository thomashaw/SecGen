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
package net.sf.jboard.model.forum.clients;


import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.forum.dao.ForumDAOFactory;
import net.sf.jboard.model.forum.dao.ForumDAOInterface;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumThread;
import net.sf.jboard.model.forum.dto.ForumMessage;
import net.sf.jboard.struts.forum.actions.ForumThreadDispatchAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * the business client which call the DAO layer to do some forumThread actions.
 * the visibility of this class is reduces to package.
 * the only public class is ForumFacadeClient.
 * @author $author$
 * @version $Revision: 1.19 $
 */
 public class ForumThreadClient extends ForumBaseClient {

    private static Log logger = LogFactory.getLog(ForumThreadDispatchAction.class);
    private static ForumDAOInterface dao;
    
    /**
     * constructor.
     */
     protected ForumThreadClient() {
        super();
        dao = ForumDAOFactory.getDAO();
    }

    /**
     * business client method wich call the DAO layer to create a new ForumThread.
     * @param ForumThread ft the new ForumThread to fill, created by the ForumThread Action
     * @return boolean
     */
    public ForumItem createForumThread(ForumThread ft,ForumMessage msg) throws ClientException {
		
		
		ForumItem fit = null;
		
		try {
            
            
            //create the forumThread with the DAO layer
            // and his first message in the same transaction
            ForumDAOFactory.getDAO().createForumThread(ft,msg);
			
			
   
           //retrieve the forumItem corresponding object
           //to see the new ForumThread displayed
           fit = ForumDAOFactory.getDAO().readForumItem(ft.getParentId());
		   logger.debug("fit.getId()="+fit.getId());
		   logger.debug(" fit.getTitle()"+fit.getTitle()); ;
		   logger.debug(" fit.getDescription()="+fit.getDescription());
           
		} catch(DAOException daoex) {

			logger.error(" DAO problem");
			throw new ClientException("errors_clientException");

		} catch(Exception ex) {

			System.out.println("DAO problem" + ex.getMessage());

		}

		logger.debug("end createForumThread  ForumThreadClient");
        return fit;

    }

    /**
     * business client method wich call the DAO layer to retrieve a  ForumThread object filled.
     *
     * @param String forumThreadID 
     *
     * @return ForumThread
     */
    public ForumThread readForumThread(Long forumThreadID) {

        logger.debug("START method getForumItem ForumItemClient");

        ForumThread ft = null;

        try {

          ft = ForumDAOFactory.getDAO().readForumThread(forumThreadID);

        } catch(DAOException ex) {

            logger.warn("pb grave.pas de recup DAO" + ex.getMessage());

        }

        logger.debug("END method getForumItem  ForumCategoryClient");

        return ft;

    }

    /**
     * business client method wich call the DAO layer to update a  ForumThread .
     *
     * @param ForumThread ft the forumThread to update
     * @return boolean
     */
    public ForumItem updateForumThread(ForumThread ft) throws ClientException {
        boolean success = false;
        
         try {
			dao.updateForumThread(ft);
            return dao.readForumItem(ft.getParentId());
		} catch (DAOException e) {
			logger.error("error"+e.getMessage());
            throw new ClientException(e.getMessage());
		}
        

    }

    /**
     * delete a forumThread .
     *
     * @return boolean
     */
    public ForumItem deleteForumThread(Long forumThreadId,Long forumItemId) throws ClientException {
        
        try {
			dao.deleteForumThread(forumThreadId);
            return dao.readForumItem(forumItemId);
		} catch (DAOException e) {
            logger.error(""+e.getMessage());
			throw new ClientException(e.getMessage());
		}
       

    }

}
