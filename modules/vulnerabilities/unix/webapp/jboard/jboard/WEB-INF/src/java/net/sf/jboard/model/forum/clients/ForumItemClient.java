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

import net.sf.jboard.fwk.exception.*;
import net.sf.jboard.model.forum.dao.ForumDAOFactory;
import net.sf.jboard.model.forum.dao.ForumDAOInterface;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumRoot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * DAO Client which  handle the DTO Result.
 * the visibility of this class is reduces to package.
 * the only public class is ForumFacadeClient.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.15 $
 */
  public class ForumItemClient extends ForumBaseClient {

    private static Log logger = LogFactory.getLog(ForumCategoryClient.class);
    private static ForumDAOInterface dao;
    /**
     * Creates a new ForumItemClient object.
     */
     protected ForumItemClient() {
        super();
        dao = ForumDAOFactory.getDAO();

    }

    /**
     * the method which retrieve from DAO layer the ForumItem object.
     *
     * @param forumitemID the forum id
     *
     * @return ForumItem
     */
    public ForumItem readForumItem(Long forumitemID) {

        logger.debug("begin method getForumItem ForumItemClient");

        ForumItem fit = null;
        
        try {

            fit = dao.readForumItem(forumitemID);
           
        } catch(DAOException ex) {

            logger.warn("bug DAO" + ex.getMessage());

        }

        logger.debug("END method getForumItem  ForumCategoryClient");

        return fit;

    }

    /**
     * the method which call the DAO to create and retrieve a ForumItem object.
     *
     * @param fit ForumItem object
     * @throws ClientException
     */
    public ForumRoot createForumItem(ForumItem fit) throws ClientException {
		logger.debug("begin createForumItem ");
        ForumRoot root = null;
			try {

                ForumDAOFactory.getDAO().createForumItem(fit);
                root = dao.readPanorama(); 
			} catch(DAOException daoex) {

				logger.error(" DAO problem");
				throw new ClientException("errors_clientException");

			} catch(Exception ex) {

				System.out.println("DAO problem" + ex.getMessage());

			}

			logger.debug("end createForumItem");
            return root;
    }
	/**
	 * the method which call the DAO to delete  a ForumItem object.
	 *
	 * @param forumId 
	 * @throws ClientException
	 */
	public ForumRoot deleteForumItem(Long  forumId) throws ClientException {
		logger.debug("begin deleteForumItem ");

			try {

                dao.deleteForumItem(forumId);
                return dao.readPanorama();
			} catch(DAOException daoex) {

				logger.error(" DAO problem");
				throw new ClientException("errors_clientException");

			} 

	}
    
    /**
     * the method which call the DAO to delete  a ForumItem object.
     *
     * @param forumId 
     * @throws ClientException
     */
    public ForumRoot updateForumItem(ForumItem  fit) throws ClientException {
        logger.debug("begin deleteForumItem ");

            try {

                dao.updateForumItem(fit);

            } catch(DAOException daoex) {

                logger.error(" DAO problem");
                throw new ClientException(daoex.getMessage());
                
            } catch(Exception ex) {

                logger.error("DAO problem" + ex.getMessage());
                throw new ClientException(ex.getMessage());
            }
        
            logger.debug("end deleteForumItem ");
            try {
				return dao.readPanorama();
			} catch (DAOException e) {
				throw new ClientException(e.getMessage());
			}
    }
}
