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
import javax.servlet.http.Cookie;

import pebble.Constants;
import pebble.util.CookieUtils;
import pebble.blog.BlogEntry;
import pebble.blog.SimpleBlog;
import pebble.blog.Comment;
import pebble.controller.Action;

/**
 * Finds comments for a blog entry, ready to display them.
 *
 * @author    Simon Brown
 */
public class ViewCommentsAction extends Action {

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
    BlogEntry blogEntry = blog.getBlogEntry(request.getParameter("entry"));

    request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
    request.setAttribute(Constants.TITLE_KEY, "Comments");

    // is "remember me" set?
    Cookie rememberMe = CookieUtils.getCookie(request.getCookies(), "rememberMe");
    if (rememberMe != null) {
      request.setAttribute("rememberMe", "true");
    }

    Comment comment = getComment(blog, blogEntry, request);
    request.setAttribute("previewComment", comment);

    return "/jsp/viewComments.jsp";
  }

  protected Comment getComment(SimpleBlog blog, BlogEntry blogEntry, HttpServletRequest request) {
    Comment comment = (Comment)request.getAttribute("previewComment");
    if (comment == null) {
      comment = blogEntry.createComment("", "", "", "", "", request.getRemoteAddr());
    }

    // is "remember me" set?
    Cookie rememberMe = CookieUtils.getCookie(request.getCookies(), "rememberMe");
    if (rememberMe != null) {
      // remember me has been checked and we're not already previewing a comment
      // so create a new comment as this will populate the author/email/website
      Cookie author = CookieUtils.getCookie(request.getCookies(), "rememberMe.author");
      Cookie email = CookieUtils.getCookie(request.getCookies(), "rememberMe.email");
      Cookie website = CookieUtils.getCookie(request.getCookies(), "rememberMe.website");

      comment.setAuthor(author.getValue());
      comment.setEmail(email.getValue());
      comment.setWebsite(website.getValue());
    }

    return comment;
  }

}