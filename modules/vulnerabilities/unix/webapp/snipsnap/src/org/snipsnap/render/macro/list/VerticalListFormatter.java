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
package org.snipsnap.render.macro.list;

import org.snipsnap.container.Components;
import org.snipsnap.snip.Snip;
import org.snipsnap.snip.SnipLink;
import org.snipsnap.snip.SnipSpace;
import org.snipsnap.user.UserManagerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

/**
 * Vertical list formatter. If the collections contains snips then
 * the formatter renders comments snips different.
 *
 * @author Matthias L. Jugel
 * @version $Id: VerticalListFormatter.java,v 1.16 2004/06/24 12:24:16 leo Exp $
 */
public class VerticalListFormatter implements ListFormatter {
  public String getName() {
    return "vertical";
  }

  /**
   * Display a simple vertical list.
   *
   * @param writer      Writer to write the list output to
   * @param listComment String to display before the list
   * @param c           Collection of Linkables, Snips or Nameables to display
   * @param emptyText   Text to display if collection is empty
   * @param showSize    If showSize is true then the size of the collection is displayed
   */
  public void format(Writer writer, Linkable current, String listComment, Collection c, String emptyText, boolean showSize)
          throws IOException {
    writer.write("<div class=\"list\"><div class=\"list-title\">");
    writer.write(listComment);
    if (showSize) {
      writer.write(" (");
      writer.write("" + c.size());
      writer.write(")");
    }
    writer.write("</div>");
    if (c.size() > 0) {
      writer.write("<ul>");
      Iterator nameIterator = c.iterator();
      while (nameIterator.hasNext()) {
        Object object = nameIterator.next();
        writer.write("<li>");
        if (object instanceof Snip) {
          formatSnipName(object, writer);
        } else if (object instanceof Linkable) {
          writer.write(((Linkable) object).getLink());
        } else if (object instanceof Nameable) {
          SnipLink.appendLink(writer, ((Nameable) object).getName());
        } else {
          writer.write(object.toString());
        }
        writer.write("</li>");
      }
      writer.write("</ul>");
    } else {
      writer.write(emptyText);
    }
    writer.write("</div>");
    return;
  }

  private void formatSnipName(Object object, Writer writer) throws IOException {
    Snip snip = (Snip) object;
    String name = snip.getName();
    String realName = snip.getTitle();
    if (name.startsWith("comment-")) {
      int lastIndex = name.lastIndexOf("-");
      SnipSpace space = (SnipSpace) Components.getComponent(SnipSpace.class);
      Snip commentedSnip = space.load(name.substring(name.indexOf("-") + 1, lastIndex));
      realName = commentedSnip.getTitle();
      SnipLink.appendImage(writer, "Icon-Comment", "");
      writer.write(" ");
      SnipLink.appendLinkWithRoot(writer, SnipLink.getCommentsRoot(),
                                  SnipLink.encode(commentedSnip.getName()) + "#" + name, realName);
      //SnipLink.appendLink(writer, name, realName);
      writer.write(" (");
      SnipLink.appendLink(writer, snip.getCUser());
      writer.write(")");
      // @TODO replace with Type Snip check
    } else if (UserManagerFactory.getInstance().exists(name)) {
      SnipLink.appendImage(writer, "Icon-Person", "");
      writer.write(" ");
      SnipLink.appendLink(writer, ((Nameable) object).getName());
      //SnipLink.appendLinkWithRoot(writer, SnipLink.getCommentsRoot(), SnipLink.encode(realName) + "#" + name, realName);
    } else {
      SnipLink.appendImage(writer, "Icon-Snip", "");
      writer.write(" ");
      SnipLink.appendLink(writer, name, realName);
    }
  }
}
