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

import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.decorator.BlogEntryDecoratorManager;
import pebble.decorator.BlogEntryDecoratorContext;
import pebble.blog.*;
import pebble.controller.Action;

/**
 * Finds all blog entries for a particular month, ready for them
 * to be displayed.
 *
 * @author    Simon Brown
 */
public class ViewMonthlyBlogAction extends Action {

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
    Category category = (Category)request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY);
    MonthlyBlog monthly = null;

    String year = request.getParameter("year");
    String month = request.getParameter("month");

    if (year != null && year.length() > 0 &&
        month != null && month.length() > 0) {
      monthly = blog.getBlogForMonth(Integer.parseInt(year), Integer.parseInt(month));
    } else {
      monthly = blog.getBlogForThisMonth();
    }

    BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
    decoratorContext.setView(BlogEntryDecoratorContext.SUMMARY_VIEW);
    decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);

    request.setAttribute("monthlyBlog", monthly);
    request.setAttribute("blogEntries", BlogEntryDecoratorManager.executeDecorators(monthly.getBlogEntries(category), decoratorContext));

    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
    sdf.setTimeZone(blog.getTimeZone());
    request.setAttribute(Constants.TITLE_KEY, sdf.format(monthly.getDate()));

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=blogEntries.jsp";
  }

}