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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * Data Transfert Object (DTO pattern), which handle a ForumThread content.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.12 $
 *
 */
public class ForumThread implements IForumComponent {

    private IForumComponent parent = null;
    private Long componentPosition = null;
    private Collection forumMessages = null;
    private String title = null;
    private Long parentId = null;
    private Long id = null;
    private Date creationDate = null;
    
    
    /**
     * Creates a new ForumThread object.
     */
    public ForumThread() {
        super();
        forumMessages = new ArrayList();
        creationDate = new Date();
    }

    /**
     * return IForumComponent parent id.
     *
     * @return Long
     */
    public Long getParentId() {

        return parentId;

    }

    /**
     * return forumThread title.
     *
     * @return String
     */
    public String getTitle() {

        return title;

    }

    /**
     * set the IForumComponent parent identifier.
     *
     * @param l
     */
    public void setParentId(Long l) {

        parentId = l;

    }

    /**
     * set the forumThread title.
     *
     * @param string
     */
    public void setTitle(String string) {

        title = string;

    }

    /**
     * return ForumThread's messages.
     *
     * @return ArrayList
     */
    public Collection getForumMessages() {

        return forumMessages;

    }


    /**
     * set the forumThread identifier.
     *
     * @param l
     */
    public void setId(Long l) {

        id = l;

    }

    /**
     * set forumThread 's messages.
     * @param list
     */
    public void setForumMessages(Collection list) {
        forumMessages = list;
    }

	

	/**
     * add message to the forumthread.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#addChild(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void addChild(IForumComponent ifc) {
        forumMessages.add(ifc);
		
	}

	/**
     * return the ForumMessages Collection.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getChildren()
	 */
	public Collection getChildren() {
		return forumMessages;
	}

	
	/**
     * return IForumComponent Id.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getId()
	 */
	public Long getId() {
		return id;
	}

	

	/**
     * return the forumThread 's position in the list.
	 * @return Long
	 */
	public Long getComponentPosition() {
		return componentPosition;
	}

	/**
     * define the forumThread 's position in the list.
	 * @param long1
	 */
	public void setComponentPosition(Long long1) {
		componentPosition = long1;
	}

	/**
     * return the IForumComponent parent.
	 * @return IForumComponent
	 */
	public IForumComponent getParent() {
		return parent;
	}

	/**
     * define the IForumComponent parent.
	 * @param component
	 */
	public void setParent(IForumComponent component) {
		parent = component;
	}


	/**
     * return the forumThread's creation Date.
	 * @return
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
     * define the forumthread's creation Date. 
	 * @param date
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}

}
