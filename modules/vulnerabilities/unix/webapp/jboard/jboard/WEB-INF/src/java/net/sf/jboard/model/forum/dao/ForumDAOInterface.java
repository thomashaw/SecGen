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

import net.sf.hibernate.Session;
import net.sf.jboard.fwk.exception.*;
import net.sf.jboard.model.forum.dto.ForumCategory;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.model.forum.dto.ForumThread;
import net.sf.jboard.model.forum.dto.ForumMessage;


/**
 * Forum module DAO Interface.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.9 $
 */
public interface ForumDAOInterface {

    /**
     * 
     * @return Session
     */
	public Session getSession();
	
	/**
	 * 
	 * @param session
	 */
	public void setSession(Session session);
    

    /**
     * create a category.
     *
     * @param cat 
     *
     * @throws DAOException Data Access exception
     */
    public void createCategory(ForumCategory cat) throws DAOException;

    /**
     * retrieve the category content.
     *
     * @param catId
     *
     * @return ForumCategory
     *
     * @throws DAOException Data Access exception
     */
    public ForumCategory readCategory(Long catId) throws DAOException;


	/**
	 * retrieve the ForumMessage content.
	 *
	 * @param msgId
	 *
	 * @return ForumCategory
	 *
	 * @throws DAOException Data Access exception
	 */
	public ForumMessage readMessage(Long msgId) throws DAOException;
	
    /**
     * delete a category.
     *
     * @param categoryId 
     *
     * @throws DAOException Data Access exception
     */
    public void deleteCategory(Long categoryId) throws DAOException;

    /**
     * update a category.
     *
     * @param cat 
     *
     * @throws DAOException Data Access exception
     */
    public void updateCategory( ForumCategory cat) throws DAOException;

    /**
     * create a forum item.
     *
     * @param forumItem 
     *
     * @throws DAOException Data Access exception
     */
    public void createForumItem(ForumItem forumItem) throws DAOException;

    /**
     * delete a forum item.
     *
     * @param forumItemId 
     *
     * @throws DAOException Data Access exception
     */
    public void deleteForumItem(Long forumItemId) throws DAOException;

    /**
     * update a forumItem.
     *
     * @param forumItemId 
     * @param forumItem 
     *
     * @throws DAOException Data Access exception
     */
    public void updateForumItem(ForumItem forumItem) throws DAOException;

    /**
     * create a forumThread.
     *
     * @param forumThread 
     *
     * @throws DAOException Data Access exception
     */
    public void createForumThread(ForumThread forumThread,ForumMessage msg) throws DAOException;

    /**
     * delete a forumThread.
     *
     * @param forumThreadId
     *
     * @throws DAOException Data Access exception
     */
    public void deleteForumThread(Long forumThreadId) throws DAOException;

    /**
     * update a forumThread.
     *
     * @param forumThreadId 
     * @param p_forumThread 
     *
     * @throws DAOException Data Access exception
     */
    public void updateForumThread( ForumThread forumThread) throws DAOException;

    /**
     * create a message.
     *
     * @param message 
     *
     * @throws DAOException Data Access exception
     */
    public void createMessage(ForumMessage message) throws DAOException;

    /**
     * Delete a message.
     *
     * @param messageId 
     *
     * @throws DAOException Data Access exception
     */
    public void deleteMessage(Long messageId) throws DAOException;

    /**
     * update a message.
     *
     * @param messageId 
     * @param message 
     *
     * @throws DAOException Data Access exception
     */
    public void updateMessage(ForumMessage message) throws DAOException;

    /**
     * retrieve the forum Content.
     *
     * @return ForumRoot
     *
     * @throws DAOException Data Access exception
     */
    public ForumRoot readPanorama() throws DAOException;

    /**
     * retrieve ForumItem content .
     *
     * @param forumitemID
     *
     * @return ForumItem
     *
     * @throws DAOException Data Access exception
     */
    public ForumItem readForumItem(Long forumitemID) throws DAOException;

    /**
     * retrieve the forumthread content.
     *
     * @param forumThreadID 
     *
     * @return ForumThread
     *
     * @throws DAOException Data Access exception
     */
    public ForumThread readForumThread(Long forumThreadID) throws DAOException;

    
}
