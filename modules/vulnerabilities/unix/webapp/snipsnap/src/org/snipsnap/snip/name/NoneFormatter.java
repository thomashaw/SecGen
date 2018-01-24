package org.snipsnap.snip.name;

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


/**
 * Formatter that does nothing
 *
 * @author stephan
 * @version $Id: NoneFormatter.java,v 1.1 2003/08/27 09:22:47 stephan Exp $
 */

public class NoneFormatter implements NameFormatter {
  private NameFormatter parent;

  public void setParent(NameFormatter parent) {
    this.parent = parent;
  }

  public String format(String name) {
    if (parent != null) {
      return parent.format(name);
    } else {
      return name;
    }
  }
}
