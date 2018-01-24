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

import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.Constants;
import pebble.blog.*;
import pebble.controller.Action;

/**
 * Adds a comment to an existing blog entry.
 *
 * @author    Simon Brown
 */
public class RemoveEmailAddressAction extends Action {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(RemoveEmailAddressAction.class);

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
    BlogEntry blogEntry = null;
    Comment comment = null;

    String entry = request.getParameter("entry");
    String email = request.getParameter("email");

    blogEntry = blog.getBlogEntry(entry);

    Iterator it = blogEntry.getComments().iterator();
    while (it.hasNext()) {
      comment = (Comment)it.next();

      if (comment.getEmail() != null && comment.getEmail().equals(email)) {
        comment.setEmail(null);
      }
    }

    request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);

    try {
      blogEntry.store();

      request.setAttribute(Constants.TITLE_KEY, "Comments");

      return "/jsp/removeEmailAddressConfirmation.jsp";
    } catch (BlogException be) {
      log.error(be.getMessage(), be);
      be.printStackTrace();
      throw new ServletException(be);
    }
  }
}