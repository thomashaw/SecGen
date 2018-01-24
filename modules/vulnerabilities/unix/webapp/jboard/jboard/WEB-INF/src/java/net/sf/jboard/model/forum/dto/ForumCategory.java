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
 *  ForumCategory class which implements the design pattern Data Transfert Object.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.7 $
 *
 */
public class ForumCategory implements IForumComponent {

    private Long componentPosition = null;
    private Long id = null;
    private IForumComponent parent = null;
    private Long parentId;

    //title
    private String title = null;

    //description
    private String description = null;

    private Collection forumItems = null;
    
    private Date creationDate = null;
    
    /**
     * constructor.
     */
    public ForumCategory() {
        super();
        forumItems = new ArrayList();
        creationDate = new Date();
    }



    /**
     * return the components of the category.
     *
     * @return forumItems;
     */
    public Collection getForumItems() {

        return forumItems;

    }

    /**
     * return filteredTitle.
     *
     * @return strFilteredTitle
     */
    public String getTitle() {

        return title;

    }

    /**
     * set Title.
     *
     * @param string
     */
    public void setTitle(String string) {

        title = string;

    }

    /**
     * set description.
     *
     * @param p_strDescription 
     */
    public void setDescription(String p_strDescription) {

        this.description = p_strDescription;

    }

    /**
     * set description.
     *
     * @return String
     */
    public String getDescription() {

        return description;

    }

    

    /**
     * getCatId.
     *
     * @return long
     */
    public Long getId() {

        return id;

    }

    /**
     * set catId.
     *
     * @param l
     */
    public void setId(Long l) {

        id = l;

    }

    /**
     * @param collection
     */
    public void setForumItems(Collection collection) {
        forumItems = collection;
    }

	/**
     * the only IForumComponent parent is ForumRoot.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#setParent(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void setParentId(Long parentId) {
		
	}
    
	/**
     * add a forumItem.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#addChild(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void addChild(IForumComponent ifc) {
        forumItems.add(ifc);
		
	}


	/**
     * return ForumItems Collection.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getChildren()
	 */
	public Collection getChildren() {
		return forumItems;
	}


	/**
     * the ForumCategories Parent is <strong>unique</strong>.it is ForumRoot.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getParentId()
	 */
	public Long getParentId() {
		
		return null;
	}



	/**
     * define the position.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#setComponentPosition(java.lang.Long)
	 */
	public void setComponentPosition(Long position) {
		componentPosition = position;
		
	}



	/**
     * return the position.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getComponentPosition()
	 */
	public Long getComponentPosition() {
		return componentPosition;
	}

	/**
     * return the  iForumComponent parent.
	 * @return
	 */
	public IForumComponent getParent() {
		return parent;
	}

	/**
     * set the iForumComponent parent.
	 * @param component
	 */
	public void setParent(IForumComponent component) {
		parent = component;
	}

	/**
     * return forum Category's creation date.
	 * @return Date
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
     * set forum category's creation date.
	 * @param date
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}

}
