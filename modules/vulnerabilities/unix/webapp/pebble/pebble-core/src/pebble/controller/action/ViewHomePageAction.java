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

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.Category;
import pebble.blog.SimpleBlog;
import pebble.controller.Action;
import pebble.decorator.BlogEntryDecoratorContext;
import pebble.decorator.BlogEntryDecoratorManager;

/**
 * Finds all blog entries that are to be displayed on the home page. By default
 * this is just the previous 3 days worth of entries.
 *
 * @author    Simon Brown
 */
public class ViewHomePageAction extends Action {

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

    BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
    decoratorContext.setView(BlogEntryDecoratorContext.SUMMARY_VIEW);
    decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);

    request.setAttribute("monthlyBlog", blog.getBlogForThisMonth());
    List blogEntries = blog.getRecentBlogEntries(category);
    request.setAttribute("blogEntries", BlogEntryDecoratorManager.executeDecorators(blogEntries, decoratorContext));

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=blogEntries.jsp";
  }

}