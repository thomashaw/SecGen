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
package pebble.controller.action;

import java.util.*;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.logging.Log;
import pebble.logging.CountedUrl;
import pebble.blog.*;
import pebble.comparator.CountedUrlComparator;
import pebble.controller.Action;

/**
 * Gets the todays referers for the blog.
 *
 * @author    Simon Brown
 */
public class ViewReferersForTodayAction extends Action {

  /**
   * Peforms the processing associated with this action.
   *
   * @param request  The HttpServletRequest instance.
   * @param response   The HttpServletResponse instance.
   * @return       The name of the next view
   */
  public String process(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException {

    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);

    Calendar cal = blog.getCalendar();
    Log log = blog.getLogger().getLog();
    String logPeriod = "";
    List referers = new ArrayList(log.getReferers());
    referers = blog.getRefererFilterManager().filter(referers);
    Collections.sort(referers, new CountedUrlComparator());

    // now calculate the total number of referers, after filtering spam
    int totalReferers = 0;
    Iterator it = referers.iterator();
    CountedUrl url;
    while (it.hasNext()) {
      url = (CountedUrl)it.next();
      totalReferers += url.getCount();
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    dateFormat.setTimeZone(blog.getTimeZone());
    logPeriod = dateFormat.format(cal.getTime());

    request.setAttribute("logPeriod", logPeriod);
    request.setAttribute("referers", referers);
    request.setAttribute("totalReferers", new Integer(totalReferers));
    request.setAttribute(Constants.TITLE_KEY, "Referers");

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/viewReferers.jsp";
  }

}