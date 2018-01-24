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
package pebble.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.AuthenticatedUser;
import pebble.Constants;
import pebble.PebbleProperties;
import pebble.blog.*;

/**
 * A filter that checks for Radio Userland style URLs and forwards on the
 * request to the action needed to display the required blog entries. Examples
 * of Radio style URLs are /2003/01/01.html and /2003/01.html to show blog
 * entries for 1st January 2003 and all of January 2003 respectively. All other
 * requests are simply let through as normal.
 *
 * @author    Simon Brown
 */
public class ViewBlogFilter implements Filter {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(ViewBlogFilter.class);

  /** the config of this filter */
  private FilterConfig filterConfig;

  /** the regex used to check for a blog entry request (long) */
  private static final String LONG_BLOG_ENTRY_REGEX = "/\\d\\d\\d\\d/\\d\\d/\\d\\d/\\d*.html";

  /** the regex used to check for a blog entry request (short) */
  private static final String SHORT_BLOG_ENTRY_REGEX = "/\\d*.html";

  /** the regex used to check for a daily blog request */
  private static final String DAILY_BLOG_REGEX = "/\\d\\d\\d\\d/\\d\\d/\\d\\d.html";

  /** the regex used to check for a monthly blog request */
  private static final String MONTHLY_BLOG_REGEX = "/\\d\\d\\d\\d/\\d\\d.html";

  /**
   * Initialises this instance.
   *
   * @param config    a FilterConfig instance
   */
  public void init(FilterConfig config) {
    this.filterConfig = config;
  }

  /**
   * Called when this filter is taken out of service.
   */
  public void destroy() {
  }

  /**
   * Contains the processing associated with this filter.
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    HttpServletRequest req = (HttpServletRequest)request;
    String uri = req.getRequestURI();

    // have filters been applied before?
    Boolean filtersApplied = (Boolean)request.getAttribute(Constants.FILTERS_APPLIED);
    if (filtersApplied != null && filtersApplied.booleanValue()) {
      if (chain != null) {
        chain.doFilter(request, response);
      }
      return;
    } else {
      request.setAttribute(Constants.FILTERS_APPLIED, Boolean.TRUE);
      req.setAttribute(Constants.PEBBLE_PROPERTIES, PebbleProperties.getInstance());
      req.setAttribute(Constants.BLOG_MANAGER, BlogManager.getInstance());

      if (BlogManager.getInstance().getBaseUrl() == null ||
          BlogManager.getInstance().getBaseUrl().length() == 0) {
        String url = "http://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
        BlogManager.getInstance().setBaseUrl(url);
      }

      if (req.getUserPrincipal() != null) {
        // some J2EE web containers don't allow programmatic access to the
        // principal information from resources that don't fall under a
        // security constraint - for this reason this information is placed into
        // the user's session
        AuthenticatedUser user = new AuthenticatedUser();
        user.setName(req.getUserPrincipal().getName());
        user.setPebbleAdmin(req.isUserInRole(Constants.PEBBLE_ADMIN_ROLE));
        user.setBlogOwner(req.isUserInRole(Constants.BLOG_OWNER_ROLE));
        user.setBlogContributor(req.isUserInRole(Constants.BLOG_CONTRIBUTOR_ROLE));
        req.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
      }
    }

    Blog rootBlog = null;
    if (BlogManager.getInstance().isMultiUser()) {
      // strip off the context (e.g. /blog)
      uri = uri.substring(req.getContextPath().length(), uri.length());
      if (uri.length() == 0) {
        uri = "/";
      }
      int index = uri.indexOf("/", 1);
      if (index == -1) {
        index = uri.length();
      }
      String blogName = uri.substring(1, index);
      CompositeBlog compositeBlog = (CompositeBlog)BlogManager.getInstance().getBlog();
      if (compositeBlog.hasBlog(blogName)) {
        rootBlog = compositeBlog.getBlog(blogName);
        req.setAttribute(Constants.BLOG_KEY, rootBlog);
        uri = uri.substring(index, uri.length());
        uri = transformUri(uri, req);
        uri = "/" + blogName + uri;
      } else {
        rootBlog = BlogManager.getInstance().getBlog();
        req.setAttribute(Constants.BLOG_KEY, rootBlog);
        try {
          uri = transformUri(uri, req);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    } else {
      rootBlog = BlogManager.getInstance().getBlog();
      req.setAttribute(Constants.BLOG_KEY, rootBlog);

      // strip off the context (e.g. /blog)
      uri = uri.substring(req.getContextPath().length(), uri.length());
      uri = transformUri(uri, req);
    }

    // change the character encoding so that we can successfully get
    // international characters from the request when HTML forms are submitted
    // ... but only if the browser doesn't send the character encoding back
    if (request.getCharacterEncoding() == null) {
      request.setCharacterEncoding(rootBlog.getCharacterEncoding());
    }

    try {
      RequestDispatcher dispatcher = request.getRequestDispatcher(uri);
      dispatcher.forward(request, response);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      e.printStackTrace();
      throw new ServletException(e);
    }
  }

  /**
   * Checks for Radio Userland style URIs and converts them as appropriate.
   *
   * @param uri   the initial URI
   * @param request   the HttpServletRequest
   *
   * @return    the transformed URI if a Radio style URI was requested,
   *            or the original URI otherwise
   */
  private String transformUri(String uri, HttpServletRequest request) {
    Blog rootBlog = (Blog)request.getAttribute(Constants.BLOG_KEY);
    String result = uri;

    if (uri.length() == 0) {
      uri = "/";
    }

    if (uri.matches(LONG_BLOG_ENTRY_REGEX)) {
      // URL matches /XXXX/XX/XX/XXXXXXXXXXXXX.html - a request for a blog entry
      String blogId = uri.substring(12, 25);

      // since the user is asking for a specific entry, we should unset
      // the current category otherwise, if the current category is A and
      // the requested entry is in category B only, the entry won't show up!
      request.getSession().removeAttribute(Constants.CURRENT_CATEGORY_KEY);

      result = "/viewBlogEntry.action";
      result += "?entryId=" + blogId;

      rootBlog.log(request);
    } else if (uri.matches(SHORT_BLOG_ENTRY_REGEX)) {
      // URL matches /XXXXXXXXXXXXX.html - a request for a blog entry
      String blogId = uri.substring(1, 14);

      // since the user is asking for a specific entry, we should unset
      // the current category otherwise, if the current category is A and
      // the requested entry is in category B only, the entry won't show up!
      request.getSession().removeAttribute(Constants.CURRENT_CATEGORY_KEY);

      result = "/viewBlogEntry.action";
      result += "?entryId=" + blogId;

      rootBlog.log(request);
    } else if (uri.matches(DAILY_BLOG_REGEX)) {
      // URL matches /XXXX/XX/XX.html - a request for a daily blog
      String year = uri.substring(1, 5);
      String month = uri.substring(6, 8);
      String day = uri.substring(9, 11);

      result = "/viewDailyBlog.action";
      result += "?year=" + year;
      result += "&month=" + month;
      result += "&day=" + day;

      rootBlog.log(request);
    } else if (uri.matches(MONTHLY_BLOG_REGEX)) {
      // URL matches /XXXX/XX.html - a request for a daily blog
      String year = uri.substring(1, 5);
      String month = uri.substring(6, 8);

      result = "/viewMonthlyBlog.action";
      result += "?year=" + year;
      result += "&month=" + month;

      rootBlog.log(request);
    } else if (uri.startsWith("/categories/")) {
      SimpleBlog simpleBlog = (SimpleBlog)rootBlog;
      // URI of the form /categories/category.html
      result = "/changeCategory.action?category=" + uri.substring(12, uri.length()-5);
      rootBlog.log(request);
    } else if (uri.startsWith("/pages/")) {
      // url matches /pages/xyz.html
      String name = uri.substring(7, uri.length()-5);

      result = "/viewStaticPage.action?name=";
      result += name;

      rootBlog.log(request);
    } else if (uri.startsWith("/images/")) {
      // url matches /images/xyz.xyz
      String name = uri.substring(7, uri.length());

      result = "/file.action?type=" + FileMetaData.BLOG_IMAGE + "&name=";
      result += name;
    } else if (uri.startsWith("/files/")) {
      // url matches /files/xyz.xyz
      String name = uri.substring(6, uri.length());

      result = "/file.action?type=" + FileMetaData.BLOG_FILE + "&name=";
      result += name;

      rootBlog.log(request);
    } else if (uri.startsWith("/feed.xml")) {
      // url matches feed.xml
      result = "/feed.action";
      rootBlog.log(request);
    } else if (uri.startsWith("/rss.xml")) {
      // url matches rss.xml
      result = "/feed.action?flavor=rss20";
      rootBlog.log(request);
    } else if (uri.startsWith("/rssWithCommentsAndTrackBacks.xml")) {
      // url matches rss.xml
      result = "/feed.action?flavor=rss20WithCommentsAndTrackBacks";
      rootBlog.log(request);
    } else if (uri.startsWith("/rdf.xml")) {
      // url matches rdf.xml
      result = "/feed.action?flavor=rdf";
      rootBlog.log(request);
    } else if (uri.startsWith("/atom.xml")) {
      // url matches atom.xml
      result = "/feed.action?flavor=atom";
      rootBlog.log(request);
    } else if (uri.startsWith("/changeCategory.action")) {
      rootBlog.log(request);
    } else if (rootBlog instanceof SimpleBlog) {
      if (uri.equals("/") || uri.equals("/index.jsp") || uri.equals("/index.html")) {
        result = "/viewHomePage.action";
        rootBlog.log(request);
      }
    }

    return result;
  }

}
