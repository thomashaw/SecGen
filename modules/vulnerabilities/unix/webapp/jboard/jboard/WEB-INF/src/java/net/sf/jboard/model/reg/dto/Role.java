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
package net.sf.jboard.model.reg.dto;

import java.util.Set;

/**
 * @author Charles GAY
 *
 */
public class Role {
    
    private long id;
    private String roleName;
    private Set users;

	/**
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @param l
	 */
	public void setId(long l) {
		id = l;
	}

	/**
	 * @param string
	 */
	public void setRoleName(String string) {
		roleName = string;
	}

	/**
	 * @return
	 */
	public Set getUsers() {
		return users;
	}

	/**
	 * @param set
	 */
	public void setUsers(Set set) {
		users = set;
	}
    /**
     * redefine the Object equals method.
     * @param Role role to compare
     * @return boolean equality boolean result
	 */
	public boolean equals(Role role) {
		if(role.getId()== this.id){
            return true;
		}
        return false;
		
	}


}
