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

package org.snipsnap.snip.storage.query;

import org.snipsnap.snip.Snip;

import java.util.Comparator;

/**
 * Compares to snips for sorting
 *
 * @author Stephan J. Schmidt
 * @version $Id: SnipComparator.java,v 1.3 2003/03/21 12:08:56 stephan Exp $
 */

public abstract class SnipComparator implements Comparator {
  /**
   * Implementation of the Comparator interface compare method that
   * takes to objects and casts them to snips
   *
   * @param o1 Snip to compare
   * @param o2 Snip to compare
   */
  public int compare(Object o1, Object o2) {
    if (!((o1 instanceof Snip) && (o2 instanceof Snip))) {
      throw new ClassCastException();
    }
    return compare((Snip) o1, (Snip) o2);

  }

  /**
   * Compares two snips according to the rules of the Comparator
   * interface. This method is used for determing the sorting order
   * of snips.
   *
   * @param s1 Snip to compare
   * @param s2 Snip to compare
   */
  public abstract int compare(Snip s1, Snip s2);
}
