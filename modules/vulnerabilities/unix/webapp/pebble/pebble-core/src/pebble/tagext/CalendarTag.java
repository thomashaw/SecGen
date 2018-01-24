/*
 * Copyright (c) 2003-2004, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pebble.tagext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import pebble.Constants;
import pebble.blog.*;

/**
 * A custom tag that outputs a HTML base element.
 *
 * @author    Simon Brown
 */
public class CalendarTag extends TagSupport {

  /**
   * Implementation from the Tag interface - this is called when the opening tag
   * is encountered.
   *
   * @return  an integer specifying what to do afterwards
   * @throws  JspException    if something goes wrong
   */
  public int doStartTag() throws JspException {

    HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
    SimpleBlog rootBlog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    MonthlyBlog month = (MonthlyBlog)request.getAttribute("monthlyBlog");
    DailyBlog todaysBlog = rootBlog.getBlogForToday();
    Calendar today = rootBlog.getCalendar();
    Category currentCategory = (Category)pageContext.getAttribute(
        Constants.CURRENT_CATEGORY_KEY, PageContext.SESSION_SCOPE);

    if (month == null) {
      month = todaysBlog.getMonthlyBlog();
    }

    // how many days in this month?
    Calendar cal = rootBlog.getCalendar();
    cal.setTime(month.getDate());
    int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

    SimpleDateFormat monthAndYearFormatter = new SimpleDateFormat("MMMM yyyy", rootBlog.getLocale());
    monthAndYearFormatter.setTimeZone(rootBlog.getTimeZone());
    SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM", rootBlog.getLocale());
    monthFormatter.setTimeZone(rootBlog.getTimeZone());

    MonthlyBlog firstMonth = rootBlog.getBlogForFirstMonth();

    try {
      JspWriter out = pageContext.getOut();

      out.write("<table width=\"100%\">");

      out.write("<tr>");
      out.write("<td colspan=\"7\" align=\"center\">");
      if (month.before(firstMonth)) {
        out.write("<b>");
        out.write(monthAndYearFormatter.format(month.getDate()));
        out.write("</b>");
      } else {
        out.write("<b><a href=\"");
        out.write(month.getPermalink());
        out.write("\" title=\"See entries for this month\">");
        out.write(monthAndYearFormatter.format(month.getDate()));
        out.write("</a></b>");
      }
      out.write("</td>");
      out.write("</tr>");

      DateFormatSymbols symbols = new DateFormatSymbols(rootBlog.getLocale());
      String[] days = symbols.getShortWeekdays();

      out.write("<tr>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.MONDAY] + "</td>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.TUESDAY] + "</td>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.WEDNESDAY] + "</td>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.THURSDAY] + "</td>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.FRIDAY] + "</td>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.SATURDAY] + "</td>");
      out.write("<td class=\"calendarDayHeader\">" + days[Calendar.SUNDAY] + "</td>");
      out.write("</tr>");

      DailyBlog daily;

      daily = month.getBlogForDay(1);
      Calendar c = rootBlog.getCalendar();
      c.setTime(daily.getDate());

      // first week in month - pad first few days if first day not a monday
      if (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        int padding = c.get(Calendar.DAY_OF_WEEK) - 2;
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          padding = 6;
        }
        out.write("<tr>");
        for (int i = 0; i < padding; i++) {
          out.write("<td class=\"calendarDay\">&nbsp;</td>");
        }
      }

      for (int day = 1; day <= maxDays; day++) {

        daily = month.getBlogForDay(day);
        c = rootBlog.getCalendar();
        c.setTime(daily.getDate());

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
          out.write("<tr>");
        }

        if (today.get(Calendar.YEAR) == c.get(Calendar.YEAR) &&
            today.get(Calendar.MONTH) == c.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)) {
          out.write("<td class=\"calendarToday\">");
          if (daily.hasEntries(currentCategory)) {
            out.write("&nbsp;<a href=\"" + daily.getPermalink() + "\" title=\"See entries for this day\">" + day + "</a>&nbsp;");
          } else {
            out.write("&nbsp;" + day + "&nbsp;");
          }
        } else {
          if (daily.hasEntries(currentCategory)) {
            out.write("<td class=\"calendarDayWithEntries\">");
            out.write("&nbsp;<a href=\"" + daily.getPermalink() + "\" title=\"See entries for this day\">" + day + "</a>&nbsp;");
          } else {
            out.write("<td class=\"calendarDay\">");
            out.write("&nbsp;" + day + "&nbsp;");
          }
        }
        out.write("</td>");

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          out.write("</tr>");
        }
      }

      // last week in month - pad if month doesn't end on a sunday
      if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        for (int i = 0; i < 7 - c.get(Calendar.DAY_OF_WEEK); i++) {
          out.write("<td class=\"calendarDay\">&nbsp;</td>");
        }
        out.write("<td class=\"calendarDay\">&nbsp;</td>");
        out.write("</tr>");
      }

      MonthlyBlog previous = month.getPreviousMonth();
      MonthlyBlog next = month.getNextMonth();

      out.write("<tr>");
      out.write("<td colspan=\"7\" align=\"center\">");

      // only display the previous month link if there are blog entries
      if (previous.before(firstMonth)) {
        out.write(monthFormatter.format(previous.getDate()));
      } else {
        out.write("<a href=\"" + previous.getPermalink() + "\" title=\"See entries for the previous month\">" + monthFormatter.format(previous.getDate()) + "</a>");
      }

      out.write("&nbsp; | &nbsp;");
      out.write("<a href=\"" + todaysBlog.getPermalink() + "\" title=\"See entries for today\">Today</a>");
      out.write("&nbsp; | &nbsp;");

      // only display the next month date if it's not in the future
      if (next.getDate().after(today.getTime()) || next.before(firstMonth)) {
        out.write(monthFormatter.format(next.getDate()));
      } else {
        out.write("<a href=\"" + next.getPermalink() + "\" title=\"See entries for the next month\">" + monthFormatter.format(next.getDate()) + "</a>");
      }
      out.write("</td>");
      out.write("</tr>");

      out.write("</table>");
    } catch (IOException ioe) {
      throw new JspTagException(ioe.getMessage());
    }

    return SKIP_BODY;
  }

}