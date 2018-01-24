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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.BlogEntry;
import pebble.blog.SimpleBlog;
import pebble.controller.Action;

/**
 * Finds a particular blog entry via a story name, ready to be displayed.
 *
 * @author    Simon Brown
 */
public class ViewStaticPageAction extends Action {

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
    String name = request.getParameter("name");

    BlogEntry blogEntry = blog.getStaticPageIndex().getStaticPage(name);
    if (blogEntry == null) {
      // the entry cannot be found - it may have been removed or the
      // requesting URL was wrong
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    } else {
      request.setAttribute("monthlyBlog", blogEntry.getDailyBlog().getMonthlyBlog());
      request.setAttribute("blogEntry", blogEntry);
      request.setAttribute("viewingIndividualBlogEntry", Boolean.TRUE);
      request.setAttribute(Constants.TITLE_KEY, blogEntry.getTitle());

      String printable = request.getParameter("printable");
      if (printable != null && printable.equalsIgnoreCase("true")) {
        return "/themes/" + blog.getTheme() + "/jsp/printableTemplate.jsp?content=staticPage.jsp";
      } else {
        return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=staticPage.jsp";
      }
    }
  }

}