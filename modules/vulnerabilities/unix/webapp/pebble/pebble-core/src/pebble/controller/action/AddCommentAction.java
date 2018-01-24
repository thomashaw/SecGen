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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.Constants;
import pebble.blog.Comment;
import pebble.blog.BlogEntry;
import pebble.blog.BlogException;
import pebble.blog.SimpleBlog;
import pebble.controller.Action;
import pebble.controller.ValidationContext;
import pebble.mail.MailNotifier;
import pebble.mail.Notifier;
import pebble.util.CookieUtils;
import pebble.util.MailUtils;

/**
 * Adds a comment to an existing blog entry.
 *
 * @author    Simon Brown
 */
public class AddCommentAction extends Action {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(AddCommentAction.class);

  /** the value used if the comment is being previewed rather than added */
  private static final String PREVIEW = "preview";

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
    Comment comment = null;

    String entry = request.getParameter("entry");
    String author = request.getParameter("author");
    String email = request.getParameter("email");
    String website = request.getParameter("website");
    String ipAddress = request.getRemoteAddr();
    String title = request.getParameter("title");
    String body = request.getParameter("body");
    long parent = -1;
    try {
      parent = Long.parseLong(request.getParameter("parent"));
    } catch (NumberFormatException nfe) {
    }
    String rememberMe = request.getParameter("rememberMe");
    String submitType = request.getParameter("submit");

    blogEntry = rootBlog.getBlogEntry(entry);
    comment = blogEntry.createComment(title, body, author, email, website, ipAddress);
    comment.setParent(blogEntry.getComment(parent));

    ValidationContext context = new ValidationContext();
    MailUtils.validate(comment.getEmail(), context);
    request.setAttribute("validationContext", context);
    request.setAttribute("rememberMe", rememberMe);

    // are we previewing or adding the comment?
    if (submitType.equalsIgnoreCase(PREVIEW) || context.hasErrors()) {
      // create a dummy comment to allow a preview
      request.setAttribute("previewComment", comment);
      request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);

      return "/viewComments.action?entry=" + entry;
    } else {
      // we are storing the comment
      blogEntry.addComment(comment);

      try {
        blogEntry.store();
        notify(rootBlog, comment);

        request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
        request.setAttribute(Constants.TITLE_KEY, "Comments");

        // remember me functionality
        if (rememberMe != null && rememberMe.equals("true")) {
          CookieUtils.addCookie(response, "rememberMe", "true", CookieUtils.ONE_MONTH);
          CookieUtils.addCookie(response, "rememberMe.author", author, CookieUtils.ONE_MONTH);
          CookieUtils.addCookie(response, "rememberMe.email", email, CookieUtils.ONE_MONTH);
          CookieUtils.addCookie(response, "rememberMe.website", website, CookieUtils.ONE_MONTH);
        } else {
          CookieUtils.removeCookie(response, "rememberMe");
          CookieUtils.removeCookie(response, "rememberMe.author");
          CookieUtils.removeCookie(response, "rememberMe.email");
          CookieUtils.removeCookie(response, "rememberMe.website");
        }

        return "/jsp/addCommentConfirmation.jsp";
      } catch (BlogException be) {
        log.error(be.getMessage(), be);
        be.printStackTrace();
        throw new ServletException(be);
      }
    }
  }

  /**
   * Helper method to notify the blog owner that a new comment has been added.
   *
   * @param comment   the Comment instance representing the new comment
   */
  private void notify(SimpleBlog blog, Comment comment) {
    if (blog.isCommentNotificationEnabled()) {
      // just uses e-mail for now
      Notifier notifier = new MailNotifier(blog);
      notifier.sendNotification(comment);
    }
  }

}