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
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class User {
    
    private Long id;
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private Integer icq;
    private String aim;
    private String msn;
    private String yahoo;
    private String webSite;
    private String localisation;
    private String job;
    private String hobbies;
    private String signature;
    private Set roles;

    /**
     * return the User's aim ID.
     * @return aim Id
     */
    public String getAim() {
        return aim;
    }

    /**
     * return the User's first name.
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * return the user 'hobbies description.
     * @return hobbies description
     */
    public String getHobbies() {
        return hobbies;
    }

    /**
     * return the user's icq id.
     * @return icq number
     */
    public Integer getIcq() {
        return icq;
    }

    /**
     * return the user's kob.
     * @return job
     */
    public String getJob() {
        return job;
    }

    /**
     * return the user's last name.
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * return user's localisation.
     * @return localisation
     */
    public String getLocalisation() {
        return localisation;
    }

    /**
     * return user's msn id.
     * @return msn id
     */
    public String getMsn() {
        return msn;
    }

    /**
     * return user's password.
     * @return password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * return user's pseudonym Id.
     * @return pseudonym
     */
    public String getUserName() {
        return userName;
    }

    /**
     * return user's signature.
     * @return signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * return user's personal website adress.
     * @return website adress
     */
    public String getWebSite() {
        return webSite;
    }

    /**
     * return user's yahoo id.
     * @return yahoo id
     */
    public String getYahoo() {
        return yahoo;
    }

    /**
     * set aim id.
     * @param string
     */
    public void setAim(String string) {
        aim = string;
    }

    /**
     * set user's first name.
     * @param string
     */
    public void setFirstName(String string) {
        firstName = string;
    }

    /**
     * set user' hobbies.
     * @param string
     */
    public void setHobbies(String string) {
        hobbies = string;
    }

    /**
     * set user's icq id.
     * @param integer
     */
    public void setIcq(Integer integer) {
        icq = integer;
    }

    /**
     * set user's job.
     * @param string
     */
    public void setJob(String string) {
        job = string;
    }

    /**
     * set user's last name.
     * @param string
     */
    public void setLastName(String string) {
        lastName = string;
    }

    /**
     * set user's localisation.
     * @param string
     */
    public void setLocalisation(String string) {
        localisation = string;
    }

    /**
     * set user's msn id.
     * @param string
     */
    public void setMsn(String string) {
        msn = string;
    }

    /**
     * set user's password.
     * @param string
     */
    public void setPassword(String string) {
        password = string;
    }

    /**
     * set user's pseudonym.
     * @param string
     */
    public void setUserName(String string) {
        userName = string;
    }

    /**
     * set user's signature.
     * @param string
     */
    public void setSignature(String string) {
        signature = string;
    }

    /**
     * set user's website adress.
     * @param string
     */
    public void setWebSite(String string) {
        webSite = string;
    }

    /**
     * set user's yahoo id.
     * @param string
     */
    public void setYahoo(String string) {
        yahoo = string;
    }

	/**
     * return the user id.
	 * @return Long
	 */
	public Long getId() {
		return id;
	}

	/**
     * set the user id.
	 * @param long1
	 */
	public void setId(Long long1) {
		id = long1;
	}

	/**
	 * @return
	 */
	public Set getRoles() {
		return roles;
	}

	/**
	 * @param set
	 */
	public void setRoles(Set set) {
		roles = set;
	}

}
