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
 * Forumitem Data Transfert Object.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.11 $
 */
public class ForumItem implements IForumComponent {
    
    private Long componentPosition = null;
    private Collection forumThreads = null;
    private Long id = null;
    private String title = null;
    private String description = null;
    private Long parentId = null;
    private IForumComponent parent = null;
    private Date creationDate = null;

    /**
     * Creates a new ForumItem object.
     */
    public ForumItem() {
        super();
        forumThreads = new ArrayList();
        creationDate = new Date();
    }

    /**
     * returns the forumItem's Id.
     *
     * @return long
     */
    public Long getId() {

        return id;

    }

    /**
     * return the ForumItem's Description
     *
     * @return String
     */
    public String getDescription() {

        return description;

    }

    /**
     * return the Forumitem's Title
     *
     * @return
     */
    public String getTitle() {

        return title;

    }

    /**
     * set the ForumItem Id.
     *
     * @param l
     */
    public void setId(Long l) {

        id = l;

    }

    /**
     * set the description's forumitem.
     *
     * @param string
     */
    public void setDescription(String string) {

        description = string;

    }

    /**
     * set the title of the forumItem.
     *
     * @param string
     */
    public void setTitle(String string) {

        title = string;

    }

    /**
     * get the category fitId.
     *
     * @return
     */
    public Long getParentId() {

        return parentId;

    }

    /**
     * define the category fitId.
     *
     * @param l
     */
    public void setParentId(Long l) {

        parentId = l;

    }

    /**
     * return the forumItemComponents
     *
     * @return Arraylist
     */
    public Collection getForumThreads() {

        return forumThreads;

    }

  

    /**
     * @param collection
     */
    public void setForumThreads(Collection collection) {
        forumThreads = collection;
    }

   

	/**
     * set the  ForumCategory parent Id.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#setParent(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void setParent(Long parentId) {
        setParentId(parentId);
		
	}

	/**
     * add a forumThread.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#addChild(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void addChild(IForumComponent ifc) {
        forumThreads.add(ifc);
		
	}

	/**
     * return ForumThreads Collection.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getChildren()
	 */
	public Collection getChildren() {
		return forumThreads;
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
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param date
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}

}
