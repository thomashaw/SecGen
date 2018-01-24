package org.snipsnap.container;

import org.snipsnap.user.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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

public interface SessionService {
  /**
   * Get/Set user from session or cookie.
   */
  public void setUser(HttpServletRequest request, HttpServletResponse response, User user);
  public User getUser(HttpServletRequest request, HttpServletResponse response);

  /**
   * Set cookie with has of encoded user/pass and last login time.
   */
  public void setCookie(HttpServletRequest request, HttpServletResponse response, User user);
  public void removeCookie(HttpServletRequest request, HttpServletResponse response);
  public Cookie getCookie(HttpServletRequest request, String name);
}
