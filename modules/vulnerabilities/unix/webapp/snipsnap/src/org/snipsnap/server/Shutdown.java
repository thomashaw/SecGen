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
package org.snipsnap.server;

import org.mortbay.util.Code;
import org.mortbay.util.Log;

/**
 * Helper class for shutting down the Server
 * @author Matthias L. Jugel
 * @version $Id: Shutdown.java,v 1.6 2003/09/10 16:41:20 leo Exp $
 */
public class Shutdown {
  /**
   * Shut down complete server ...
   */
  public static void shutdown() {
    if (AppServer.jettyServer.isStarted()) {
      Log.event("Application: stopping server");
      System.out.println("INFO: Stopping server ...");
      try {
        AppServer.jettyServer.stop();
      } catch (Exception e) {
        Code.ignore(e);
      }
      System.out.println("INFO: SnipSnap shutdown procedure finished.");
      Log.event("Application: stopped server");
    }
  }
}
