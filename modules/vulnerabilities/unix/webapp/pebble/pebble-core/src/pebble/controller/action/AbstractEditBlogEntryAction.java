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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.BlogEntry;
import pebble.blog.SimpleBlog;
import pebble.controller.SecureAction;
import pebble.decorator.BlogEntryDecoratorContext;
import pebble.decorator.BlogEntryDecoratorManager;

/**
 * Superclass for actions that allow an existing blog entry to be edited.
 *
 * @author    Simon Brown
 */
public abstract class AbstractEditBlogEntryAction extends SecureAction {

  /**
   * Peforms the processing associated with this action.
   *
   * @param request  The HttpServletRequest instance.
   * @param response   The HttpServletResponse instance.
   * @return       The name of the next view
   */
  public final String process(HttpServletRequest request,
                        HttpServletResponse response) {

    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    String entryId = request.getParameter("entry");
    BlogEntry entry = getBlogEntry(blog, entryId);
    request.setAttribute(Constants.BLOG_ENTRY_KEY, entry);
    request.setAttribute(Constants.TITLE_KEY, getTitle());
    BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
    decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
    decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
    request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(entry, decoratorContext));

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
  }

  /**
   * Finds the blog entry that is to be edited.
   *
   * @param blog    the SimpleBlog that owns the entry
   * @return    a BlogEntry instance
   */
  protected abstract BlogEntry getBlogEntry(SimpleBlog blog, String entryId);

  /**
   * Gets the title of the page (this differs between blog entry types).
   *
   * @return the title as a String
   */
  protected String getTitle() {
    return "Edit blog entry";
  }

  /**
   * Gets a list of all roles that are allowed to access this action.
   *
   * @return  an array of Strings representing role names
   * @param request
   */
  public String[] getRoles(HttpServletRequest request) {
    return new String[]{Constants.BLOG_CONTRIBUTOR_ROLE};
  }

}