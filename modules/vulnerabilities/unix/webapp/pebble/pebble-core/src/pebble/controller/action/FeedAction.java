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
import java.util.*;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.decorator.BlogEntryDecoratorManager;
import pebble.decorator.BlogEntryDecoratorContext;
import pebble.blog.*;
import pebble.comparator.BlogEntryComparator;
import pebble.controller.Action;

/**
 * Gets the RSS for a blog.
 *
 * @author    Simon Brown
 */
public class FeedAction extends Action {

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

    Blog rootBlog = (Blog)request.getAttribute(Constants.BLOG_KEY);
    String flavor = request.getParameter("flavor");

    SimpleDateFormat httpFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    httpFormat.setTimeZone(rootBlog.getTimeZone());

    long ifModifiedSince = request.getDateHeader("If-Modified-Since");
    String ifNoneMatch = request.getHeader("If-None-Match");

    response.setContentType("application/xml; charset=" + rootBlog.getCharacterEncoding());
    Date lastModified = rootBlog.getLastModified();
    response.setDateHeader("Last-Modified", lastModified.getTime());
    response.setHeader("ETag", "\"" + httpFormat.format(lastModified) + "\"");

    if (ifModifiedSince > -1 && ifModifiedSince == rootBlog.getLastModified().getTime()) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return null;
    } else if (ifNoneMatch != null && ifNoneMatch.equals("\"" + rootBlog.getLastModified() + "\"")) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return null;
    } else {
      Category category = getCategory(rootBlog, request);
      String s = request.getParameter("includeAggregatedContent");
      boolean includeAggregatedContent = (s == null || s.equalsIgnoreCase("true"));

      List blogEntries = rootBlog.getRecentBlogEntries(category);
      List blogEntriesForFeed = new ArrayList();
      Iterator it = blogEntries.iterator();
      while (it.hasNext()) {
        BlogEntry entry = (BlogEntry)it.next();

        if (!includeAggregatedContent && entry.isAggregated()) {
          continue;
        } else {
          blogEntriesForFeed.add(entry);
        }
      }

      Collections.sort(blogEntriesForFeed, new BlogEntryComparator());

      request.setAttribute("category", category);
      BlogEntryDecoratorContext context = new BlogEntryDecoratorContext();
      context.setView(BlogEntryDecoratorContext.SUMMARY_VIEW);
      context.setMedia(BlogEntryDecoratorContext.NEWS_FEED);
      request.setAttribute("blogEntries", BlogEntryDecoratorManager.executeDecorators(blogEntriesForFeed, context));

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
    }

    return null;
  }

  /**
   * Helper method to find a named category from a request parameter.
   *
   * @param blog      the blog for which the feed is for
   * @param request   the HTTP request containing the category parameter
   * @return  a Category instance, or null if the category isn't
   *          specified or can't be found
   */
  private Category getCategory(Blog blog, HttpServletRequest request) {
    if (blog instanceof CompositeBlog) {
      // getting Category based, aggregated feed isn't supported
      return null;
    } else {
      SimpleBlog simpleBlog = (SimpleBlog)blog;

      String categoryName = request.getParameter("category");
      if (categoryName != null) {
        return simpleBlog.getBlogCategoryManager().getCategory(categoryName);
      } else {
        return null;
      }
    }
  }

}