/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */
package org.snipsnap.user;

import org.snipsnap.app.Application;
import org.snipsnap.config.Configuration;
import org.snipsnap.snip.SnipLink;
import org.snipsnap.render.macro.list.Linkable;

import java.sql.Timestamp;

/**
 * User class.
 *
 * @author Stephan J. Schmidt
 * @version $Id: User.java,v 1.43 2004/05/17 10:56:18 leo Exp $
 */
public class User implements Linkable {
  private String applicationOid;
  private String login;
  private String passwd;
  private String email;
  private String status;
  private Roles roles;
  // @TODO: composite object
  private Timestamp lastLogin, lastAccess, lastLogout;
  // @TODO: -> Modified composite object
  private Timestamp cTime, mTime;

  private boolean guest = false;
  private boolean nonUser = false;

  public User() {
    this("", "", "");
  }
  
  public User(String login, String passwd, String email) {
    this.login = login;
    setPasswd(passwd);
    setEmail(email);
  }

  public void setApplication(String applicationOid) {
    this.applicationOid = applicationOid;
  }

  public String getApplication() {
    return applicationOid;
  }


  public Timestamp getCTime() {
    return cTime;
  }

  public void setCTime(Timestamp cTime) {
    this.cTime = cTime;
  }

  public Timestamp getMTime() {
    return mTime;
  }

  public void setMTime(Timestamp mTime) {
    this.mTime = mTime;
  }

  /**
   * LastAccess sets the time when the user
   * last accessed SnipSnap. This is used
   * to find snips since his last access.
   */
  public void lastAccess() {
    this.lastAccess = new Timestamp(new java.util.Date().getTime());
    //Logger.debug(this.login+" hashcode: "+((Object) this).hashCode());
    //Logger.debug("Set lastAccess() "+this.login+" "+lastAccess);
  }

  public Timestamp getLastLogout() {
    return lastLogout;
  }

  public void setLastLogout(Timestamp lastLogout) {
    //Logger.debug(this.login+" hashcode: "+((Object) this).hashCode());
    //Logger.debug("Set LastLogout() "+this.login+" "+lastLogout+" old: "+this.lastLogout);
    this.lastLogout = lastLogout;
  }

  public Timestamp getLastAccess() {
    return this.lastAccess;
  }

  public void setLastAccess(Timestamp lastAccess) {
    this.lastAccess = lastAccess;
  }

  public Timestamp getLastLogin() {
    return lastLogin;
  }

  public void lastLogin() {
    setLastLogin(new Timestamp(new java.util.Date().getTime()));
  }

  public void setLastLogin(Timestamp lastLogin) {
    this.lastLogin = lastLogin;
  }

  public void setStatus(String status) {
    this.status = status;
    return;
  }

  public String getStatus() {
    if (null == status) {
      status = "not set";
    }
    return status;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  // Set passwd of user. Takes unecrypted
  // passwd and then sets an encrypted version
  public void setPasswd(String passwd) {
    if (passwd != null && passwd.length() > 30) {
      this.passwd = passwd;
    } else {
      this.passwd = Digest.getDigest(passwd);
    }
  }

  public String getPasswd() {
    return passwd;
  }

  /**
   * WARNING: DO NOT USE THIS METHOD UNLESS YOU KNOW WHAT YOU DO.
   * @param login
   */
  public void setLogin(String login) {
    this.login = login;
  }

  public String getLogin() {
    return login;
  }

  public void setRoles(Roles roles) {
    this.roles = roles;
    return;
  }

  public Roles getRoles() {
    if (null == roles) {
      roles = new Roles();
    }
    return roles;
  }

  public boolean isAdmin() {
    Application app = Application.get();
    Configuration config = app.getConfiguration();
    return (config.getAdminLogin() != null && config.getAdminLogin().equals(login))
      || getRoles().contains(Roles.ADMIN);
  }

  public void setGuest(boolean guest) {
    this.guest = guest;
  }

  public boolean isGuest() {
    return guest;
  }

  public void setNonUser(boolean nonUser) {
    this.nonUser = nonUser;
  }

  public boolean isNonUser() {
    return nonUser;
  }

  public int hashCode() {
    return getLogin().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof User && obj != null && this.getLogin() != null) {
      return this.getLogin().equals(((User) obj).getLogin());
    }
    return false;
  }

  public String toString() {
    return "User[" + login + "," + (passwd != null ? "pass set" : "no pass") + "," + email + "," + status + "," + roles + "]";
  }

  public String getLink() {
    if (isNonUser()) {
      StringBuffer tmp = new StringBuffer();
      tmp.append("<a href=\"");
      tmp.append(getEmail());
      tmp.append("\">");
      tmp.append(getLogin());
      tmp.append("</a>");
      return tmp.toString();
    } else if (isGuest()) {
      return "Guest";
    } else {
      return SnipLink.createLink(getLogin());
    }
  }

}