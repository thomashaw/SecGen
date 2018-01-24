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

import java.util.Collection;
import java.util.Date;

/**
 * Interface for ForumCategory and Messages classes. this Interface is the Component
 * interface in the design Pattern Composite (GoF). ForumCategory is Composite, and
 * ForumMessage is Leaf.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.10 $
 */
public interface IForumComponent {

     /**
      * define the parent of the IForumComponent.
      * @param ifc
      */
     public void setParentId(Long parentId);
     
     /**
     * return IForumComponent Parent Id.
     * @return
     */
     public Long getParentId();
         
     /**
      * add a Child to this IForumComponent.
      * @param ifc
      */
     public void addChild(IForumComponent ifc);
     
     /**
      * return IForumComponent children.
      * @return Collection
      */
     public Collection getChildren();
     
     /**
      * return Id.
      * @return long
      */
     public Long getId();
     
     /**
      * define id.
      * @param id
      */
     public void setId(Long id);
     
     /**
      * define componentPosition.
      * @param position
      */
     public void setComponentPosition(Long position);
     
     /**
      * return componentPosition.
      * @return position.
      */
     public Long getComponentPosition();

	/**
     * define the IForumComponent parent.
	 * @param parent
	 */
	public void setParent(IForumComponent parent);
    
    /**
     * return the the IForumComponent parent.
     * @return IForumComponent
     */
    public IForumComponent getParent();
    
    /**
     * set the creation Date.
     * @param date
     */
    public void setCreationDate(Date date);
    
    
    /**
     * return the Creation's date of the IForumComponent.
     * @return
     */
    public Date getCreationDate();
}
