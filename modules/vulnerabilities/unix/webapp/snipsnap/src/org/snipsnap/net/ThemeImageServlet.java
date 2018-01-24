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
import org.snipsnap.container.Components;
import org.snipsnap.snip.Snip;
import org.snipsnap.snip.SnipSpace;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for handling theme images attached to the main theme snip.
 * @author Matthias L. Jugel
 * @version $Id: ThemeImageServlet.java,v 1.2 2004/05/17 10:56:17 leo Exp $
 */
public class ThemeImageServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String fileName = request.getPathInfo();

    if (fileName != null && fileName.length() > 1) {
      Configuration config = Application.get().getConfiguration();
      SnipSpace space = (SnipSpace)Components.getComponent(SnipSpace.class);
      Snip themeSnip = space.load("SnipSnap/themes/"+config.getTheme());

      if(themeSnip != null) {
        request.setAttribute(FileDownloadServlet.FILENAME, fileName.substring(1));
        request.setAttribute(FileDownloadServlet.SNIP, themeSnip);
        RequestDispatcher dispatcher =
          getServletContext().getNamedDispatcher("org.snipsnap.net.FileDownloadServlet");
        dispatcher.forward(request, response);
        return;
      }
    }
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
}
