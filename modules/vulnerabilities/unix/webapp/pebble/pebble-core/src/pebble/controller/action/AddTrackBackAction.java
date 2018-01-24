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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.Constants;
import pebble.blog.*;
import pebble.controller.Action;
import pebble.mail.MailNotifier;
import pebble.mail.Notifier;

/**
 * Adds a comment to an existing blog entry.
 *
 * @author    Simon Brown
 */
public class AddTrackBackAction extends Action {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(AddTrackBackAction.class);

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

    SimpleBlog rootBlog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    BlogEntry blogEntry = null;

    String entry = request.getParameter("entry");
    String title = request.getParameter("title");
    String excerpt = request.getParameter("excerpt");
    String url = request.getParameter("url");
    String blogName = request.getParameter("blog_name");
    String ipAddress = request.getRemoteAddr();

    if (url == null || url.length() == 0) {
      try {
        response.setContentType("application/xml; charset=" + rootBlog.getCharacterEncoding());
        PrintWriter writer = response.getWriter();
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<response>");
        writer.println("<error>1</error>");
        writer.println("<message>The URL (permalink) must be specified for TrackBacks</message>");
        writer.println("</response>");
        writer.flush();
      } catch (IOException ioe) {
        log.error(ioe.getMessage(), ioe);
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    } else {
      blogEntry = rootBlog.getBlogEntry(entry);

      TrackBack trackBack = blogEntry.createTrackBack(title, excerpt, url, blogName, ipAddress);
      blogEntry.addTrackBack(trackBack);

      try {
        blogEntry.store();
        notify(rootBlog, trackBack);
      } catch (BlogException be) {
        log.error(be.getMessage(), be);
        be.printStackTrace();
        throw new ServletException(be);
      }

      try {
        response.setContentType("application/xml; charset=" + rootBlog.getCharacterEncoding());
        PrintWriter writer = response.getWriter();
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<response>");
        writer.println("<error>0</error>");
        writer.println("</response>");
        writer.flush();
      } catch (IOException ioe) {
        log.error(ioe.getMessage(), ioe);
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    }

    return null;
  }

  /**
   * Helper method to notify the blog owner that a new comment has been added.
   *
   * @param trackBack   the TrackBack instance representing the new trackback
   */
  private void notify(SimpleBlog blog, TrackBack trackBack) {
    if (blog.isCommentNotificationEnabled()) {
      // just uses e-mail for now
      Notifier notifier = new MailNotifier(blog);
      notifier.sendNotification(trackBack);
    }
  }

}