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
package org.snipsnap.net;

import org.snipsnap.app.Application;
import org.snipsnap.config.Configuration;
import org.snipsnap.snip.HomePage;
import org.snipsnap.snip.SnipLink;
import org.snipsnap.user.User;
import org.snipsnap.user.UserManager;
import org.snipsnap.user.UserManagerFactory;
import org.snipsnap.container.SessionService;
import org.snipsnap.container.Components;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Servlet to register a new user.
 * @author Matthias L. Jugel
 * @version $Id: NewUserServlet.java,v 1.32 2003/12/11 13:24:56 leo Exp $
 */
public class NewUserServlet extends HttpServlet {
  private final static String ERR_EXISTS = "login.register.error.user.exists";
  private final static String ERR_TOO_SHORT = "login.register.error.user.short";
  private final static String ERR_ILLEGAL = "login.register.error.user.illegal";
  private final static String ERR_PASSWORD = "login.register.error.passwords";
  private final static String ERR_PASSWORD_TOO_SHORT = "login.register.error.password.short";
  private final static String ERR_NOT_ALLOWED = "login.register.error.not.allowed";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    HttpSession session = request.getSession();
    session.removeAttribute("errors");
    Map errors = new HashMap();

    Application app = Application.get();
    Configuration config = app.get().getConfiguration();

    if (!config.deny(Configuration.APP_PERM_REGISTER)) {
      String login = request.getParameter("login");
      String email = request.getParameter("email");
      String password = request.getParameter("password");
      String password2 = request.getParameter("password2");

      login = login != null ? login : "";
      email = email != null ? email : "";


      if (request.getParameter("cancel") == null) {
        UserManager um = UserManagerFactory.getInstance();
        User user = um.load(login);
        // check whether user exists or not
        if (user != null) {
          errors.put("login", ERR_EXISTS);
          sendError(session, errors, request, response);
          return;
        }

        if (login.length() < 3) {
          errors.put("login", ERR_TOO_SHORT);
          sendError(session, errors, request, response);
          return;
        }

        // TODO 1.4 if(!login.matches("[A-Za-z0-9._ ]+")) {
        StringTokenizer tok = new StringTokenizer(login, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._ ");
        if (login.startsWith(" ") || tok.hasMoreTokens()) {
          errors.put("login", ERR_ILLEGAL + ": " + (tok.hasMoreTokens() ? tok.nextToken() : ""));
          sendError(session, errors, request, response);
          return;
        }

        // check whether the password is correctly typed
        if (!password.equals(password2)) {
          errors.put("password", ERR_PASSWORD);
          sendError(session, errors, request, response);
          return;
        }

        if (password.length() < 3) {
          errors.put("password", ERR_PASSWORD_TOO_SHORT);
          sendError(session, errors, request, response);
          return;
        }

        // create user ...
        user = um.create(login, password, email);
        app.setUser(user, session);
        HomePage.create(login);

        // store user name and app in cookie and session
        SessionService sessionService = (SessionService)Components.getComponent(SessionService.class);
        sessionService.setCookie(request, response, user);

        response.sendRedirect(config.getUrl("/space/" + SnipLink.encode(login)));
        return;
      }

      String referer = request.getParameter("referer");
      response.sendRedirect(referer != null ? referer : config.getUrl("/space/"+config.getStartSnip()));
    } else {
      errors.put("Fatal", ERR_NOT_ALLOWED);
      sendError(session, errors, request, response);
    }
  }

  private void sendError(HttpSession session, Map errors, HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    session.setAttribute("errors", errors);
    RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/register.jsp");
    dispatcher.forward(request, response);
  }
}
