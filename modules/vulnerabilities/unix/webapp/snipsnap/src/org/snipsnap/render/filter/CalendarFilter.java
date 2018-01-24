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

package org.snipsnap.render.filter;

import org.radeox.filter.regex.RegexTokenFilter;
import org.radeox.regex.MatchResult;
import org.radeox.filter.context.FilterContext;
import org.radeox.util.i18n.ResourceManager;
import org.snipsnap.app.Application;
import org.snipsnap.config.Configuration;
import org.snipsnap.render.filter.context.SnipFilterContext;
import org.snipsnap.snip.Snip;

import java.text.MessageFormat;
import java.text.FieldPosition;
import java.util.ResourceBundle;

/*
 * Class that VCAL content
 *
 * @author stephan
 * @team sonicteam
 * @version $Id: CalendarFilter.java,v 1.10 2004/05/17 10:56:17 leo Exp $
 */

public class CalendarFilter extends RegexTokenFilter {

  public CalendarFilter() {
    super("^BEGIN:VCALENDAR(.*?)END:VCALENDAR", SINGLELINE);
  }

  private final static int CALENDAR_PREFIX_LENGTH = "calendar-".length();

  public void handleMatch(StringBuffer buffer, MatchResult result, FilterContext context) {
    Snip snip = ((SnipFilterContext) context).getSnip();
    Application app = Application.get();
    Configuration config = app.getConfiguration();
    Snip parent = snip.getParent();

    StringBuffer linkBuffer = new StringBuffer();
    linkBuffer.append("<a href=\"");

    String file = null;
    StringBuffer url = new StringBuffer("exec/ical/");
    if (parent != null) {
      file = snip.getName().substring(CALENDAR_PREFIX_LENGTH + parent.getName().length() + 1);
      url.append(parent.getName()).append("/");
      url.append(file);
    } else {
      file = snip.getName().substring(CALENDAR_PREFIX_LENGTH);
      url.append(file);
    }

    String webcalUrl = config.getUrl(url.toString());
    webcalUrl = webcalUrl.substring(webcalUrl.indexOf("//") + 2);
    linkBuffer.append("webcal://").append(webcalUrl);
    linkBuffer.append("\">").append("calendar ").append(file).append("</a>");

    String formatString = ResourceManager.getString("i18n.messages", "filter.calendar.subscribe.to");
    MessageFormat mf = new MessageFormat(formatString);
    buffer.append(mf.format(new Object[] { linkBuffer.toString() }));
  }
}
