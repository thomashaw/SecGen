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
import java.util.List;


/**
 * Data Transfert Object (DTO pattern), which handle all the ForumBoard.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.9 $
 */
public class ForumRoot  {


    private ArrayList forumCategories = null;

    /**
     * Creates a new ForumRoot object.
     */
    public ForumRoot() {
        super();
        forumCategories = new ArrayList();

    }


    /**
     * get all the categories.
     *
     * @return ArrayList
     */
    public ArrayList getForumCategories() {

        return forumCategories;

    }

    /**
     * @param list
     */
    public void setForumCategories(List list) {
        forumCategories = (ArrayList)list;
    }


	/**
     * add a ForumCategory.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#addChild(net.sf.jboard.model.forum.dto.IForumComponent)
	 */
	public void addChild(IForumComponent ifc) {
		
        forumCategories.add(ifc);
	}

	/**
     * return the ForumCategories Collection.
	 * @see net.sf.jboard.model.forum.dto.IForumComponent#getChildren()
	 */
	public Collection getChildren() {
		return forumCategories;
	}

	
	/**
	 * @param list
	 */
	public void setForumCategories(ArrayList list) {
		forumCategories = list;
	}

	

}
