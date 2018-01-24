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
package org.snipsnap.jsp;

import org.radeox.util.logging.Logger;
import org.snipsnap.app.Application;
import org.snipsnap.snip.Snip;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class CoordinatesTag extends TagSupport {
  Snip snip = null;

  public int doStartTag() throws JspException {
    try {
      JspWriter out = pageContext.getOut();
      Application app = Application.get();
      String coordinates = app.getConfiguration().getGeoCoordinates();
      //Logger.debug("Coordinates="+coordinates);
      if (null != coordinates) {
        out.print("<meta name=\"ICBM\" content=\"");
        out.print(coordinates);
        out.print("\"/>");
      }
    } catch (IOException e) {
      Logger.warn("CoordinatesTag: unable print to JSP writer", e);
    }
    return super.doStartTag();
  }
}
