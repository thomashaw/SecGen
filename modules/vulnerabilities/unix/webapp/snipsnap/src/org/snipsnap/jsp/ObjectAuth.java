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

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.radeox.util.logging.Logger;
import org.snipsnap.snip.Snip;
import org.snipsnap.user.Roles;
import org.snipsnap.user.Security;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;
import java.util.StringTokenizer;


public class ObjectAuth extends ConditionalTagSupport {
  protected Roles roles;
  protected Snip snip;
  protected String permission;

  protected boolean invertCheck = false;

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public void setSnip(String snip) {
    try {
      this.snip = (Snip) ExpressionEvaluatorManager.evaluate("snip", snip, Snip.class, this, pageContext);
    } catch (JspException e) {
      Logger.warn("unable to evaluate expression" , e);
    }
  }

  public void setRoles(String roles) {
    StringTokenizer tok = new StringTokenizer(roles, ":,");
    this.roles = new Roles();
    while (tok.hasMoreTokens()) {
      String token = tok.nextToken();
      this.roles.add(token);
    }
  }

  public void setInvert(Boolean value) {
    invertCheck = value.booleanValue();
  }

  protected boolean condition() throws JspTagException {
    if (snip != null) {
      boolean isTrue = Security.existsPermission(permission, snip, roles);
      return (invertCheck ? !isTrue : isTrue);
    } else {
      return false;
    }
  }
}
