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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.SimpleBlog;
import pebble.comparator.BlogEntryComparator;
import pebble.controller.SecureAction;

/**
 * Exports an entire blog as RSS/RDF/Atom.
 *
 * @author    Simon Brown
 */
public class ExportBlogAction extends SecureAction {

  private static final String RSS_WITH_COMMENTS_AND_TRACKBACKS = "rss20WithCommentsAndTrackBacks";

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

    String flavor = request.getParameter("flavor");

    if (flavor != null && flavor.equalsIgnoreCase("zip")) {
      return "/zipDirectory.secureaction?type=blogData";
    }

    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);

    response.setContentType("application/xml; charset=" + blog.getCharacterEncoding());

    List blogEntries = blog.getBlogEntries();
    Collections.sort(blogEntries, new BlogEntryComparator());
    request.setAttribute("blogEntries", blogEntries);

    try {
      if (flavor != null && flavor.equalsIgnoreCase("atom")) {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/feeds/atom.jsp");
        dispatcher.include(request, response);
      } else if (flavor != null && flavor.equalsIgnoreCase("rdf")) {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/feeds/rdf.jsp");
        dispatcher.include(request, response);
      } else if (flavor != null && flavor.equalsIgnoreCase(RSS_WITH_COMMENTS_AND_TRACKBACKS)) {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/feeds/rss20WithCommentsAndTrackBacks.jsp");
        dispatcher.include(request, response);
      } else {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/feeds/rss20.jsp");
        dispatcher.include(request, response);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new ServletException(ioe);
    }

    return null;
  }

  /**
   * Gets a list of all roles that are allowed to access this action.
   *
   * @return  an array of Strings representing role names
   * @param request
   */
  public String[] getRoles(HttpServletRequest request) {
    return new String[]{Constants.BLOG_OWNER_ROLE};
  }

}