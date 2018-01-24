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
package net.sf.jboard.model.forum.dto;


import java.util.Date;
import java.util.Collection;


/**
 * ForumMessage text posted by user.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.9 $
 */
public class ForumMessage implements IForumComponent {

    private Long componentPosition = null;
    private IForumComponent parent = null;
    //ForumMessage text posted by user.
    private String message = null;
    //UserID
    private Long id = null;

    //message Id
    private long messageId = 0;

    //CreationDate
    private Date creationDate = null;

    //Number of times when people view this message
    private long viewsNumber = 0;

    //id of the forumThread
    private Long parentId = null;

    private long forumMessagePosition = 0;
    
    private Long userId = new Long(1);
    /**
     * default constructor.
     */
    public ForumMessage() {
        super();
        
        creationDate = new Date();
    }

    /**
     * constructor with the input message string.
     *
     * @param p_strMessage 
     */
    public ForumMessage(Long uId,Long fThreadId, String msg) {

        message = msg;
        userId = uId;
		parentId = fThreadId;
    }

    /**
     * return the filtered ForumMessage.
     *
     * @return message
     */
    public String getMessage() {


        return message;

    }

    /**
     * setter of the message String.
     *
     * @param p_strMessage
     */
    public void setMessage(String p_strMessage) {

        message = p_strMessage;

    }

    /**
     * getter of the message id.
     *
     * @return
     */
    public long getMessageId() {

        return messageId;

    }

    /**
     * getter of the user ID.
     *
     * @return
     */
    public Long getId() {

        return id;

    }

   

    /**
     * set the message id.
     *
     * @param l
     */
    public void setId(Long l) {

        id = l;

    }

    /**
     * set the creation date.
     *
     * @param p_timestamp
     */
    public void setCreationDate(Date date) {

        creationDate = date;

    }

    /**
     * getter of the forumTHread ID.
     *
     * @return
     */
    public Long getParentId() {

        return parentId;

    }

    /**
     * setter of the ForumThread ID
     *
     * @param long l
     */
    public void setParentId(Long l) {

        parentId = l;

    }

    /**
     * getter of the views number.
     *
     * @return
     */
    public long getViewsNumber() {

        return viewsNumber;

    }

    /**
     * setter of the views number.
     *
     * @param l
     */
    public void setViewsNumber(long l) {

        viewsNumber = l;

    }
    
    /**
     * @return
     */
    public long getForumMessagePosition() {
        return forumMessagePosition;
    }

    /**
     * @param l
     */
    public void setForumMessagePosition(long l) {
        forumMessagePosition = l;
    }

	/**
     * set the Parent forumThread Id.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#setParent(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void setParent(long parentId) {
		setParentId(new Long(parentId));
		
	}

	/**
     * a ForumMessage has got no child.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#addChild(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void addChild(IForumComponent ifc) {
			
	}

	/**
     * a ForumMessage hasn't got any children.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getChildren()
	 */
	public Collection getChildren() {
		
		return null;
	}

	
	/**
     * set the ForumIComponent Parent Id.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#setParent(java.lang.Long)
	 */
	public void setParent(Long pId) {
		parentId = pId;
		
	}

	/**
	 * @return
	 */
	public Long getComponentPosition() {
		return componentPosition;
	}

	/**
	 * @param long1
	 */
	public void setComponentPosition(Long long1) {
		componentPosition = long1;
	}

	/**
	 * @return
	 */
	public IForumComponent getParent() {
		return parent;
	}

	/**
	 * @param component
	 */
	public void setParent(IForumComponent component) {
		parent = component;
	}

	/**
	 * @return
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param long1
	 */
	public void setUserId(Long long1) {
		userId = long1;
	}

	/**
	 * @return
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param l
	 */
	public void setMessageId(long l) {
		messageId = l;
	}

}
