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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.forum.dao.ForumDAOFactory;
import net.sf.jboard.model.forum.dao.ForumDAOInterface;
import net.sf.jboard.model.forum.dto.ForumMessage;
import net.sf.jboard.model.forum.dto.ForumThread;
import net.sf.jboard.struts.fwk.actions.ConstantsMessages;

/**
 * the ForumMessageClient client which interact with the DAO layer to act on the
 * DATA layer.
 * the visibility of this class is reduces to package.
 * the only public class is ForumFacadeClient.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.17 $
 */
 public class ForumMessageClient extends ForumBaseClient implements ConstantsMessages {


	private static Log logger = LogFactory.getLog(ForumMessageClient.class);
    private static ForumDAOInterface dao;

    /**
	 * constructor.
	 */
     protected ForumMessageClient() {
        super();
        dao = ForumDAOFactory.getDAO();
    }

    /**
     * create a message.
     *
     * @param UserID unique Id of the ForumUser.
     * @param String the content of the message
     *
     * @return boolean
     */
    public ForumThread createMessage(ForumMessage msg) throws ClientException {

       ForumThread ft = null;
       
		try {
            ForumDAOFactory.getDAO().createMessage(msg);
            ft = ForumDAOFactory.getDAO().readForumThread(msg.getParentId());
		} catch (DAOException e) {
		   logger.debug("DAOEXception create message method in ForumMessageClient");
		   throw new ClientException("errors_dao");
		}
        return ft;

    }

	/**
		 * return a ForumCategory.
		 *
		 * @param catId
		 *
		 * @return ForumCategory
		 *
		 * @throws DAOException
		 */
		public ForumMessage readMessage(Long msgId) throws ClientException {

			ForumMessage msg = null;
			try {
				msg = ForumDAOFactory.getDAO().readMessage(msgId);
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return msg;

		}

    /**
     * update a message.
     *
     * @return boolean true if the update success
     */
    public ForumThread updateMessage(ForumMessage msg) throws ClientException {

		
        try {
			dao.updateMessage(msg);
            return dao.readForumThread(msg.getParentId());
		} catch (DAOException e) {
			logger.error("error"+e.getMessage());
            throw new ClientException("error"+e.getMessage());
		}
		

    }

    /**
     * delete the message
     *
     * @return boolean true if the message has been deleted
     */
    public ForumThread deleteMessage(Long messageId,Long forumThreadId) throws ClientException {
        
        try {
			dao.deleteMessage(messageId);
            return dao.readForumThread(forumThreadId);
            
		} catch (DAOException e) {
			logger.error("error"+e.getMessage());
            throw new ClientException("error"+e.getMessage());
		}

        

    }

}
