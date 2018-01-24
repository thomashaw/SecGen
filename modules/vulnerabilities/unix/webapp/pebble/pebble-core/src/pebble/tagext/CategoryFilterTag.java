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
package pebble.tagext;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import pebble.Constants;
import pebble.blog.Category;
import pebble.blog.SimpleBlog;

/**
 * Given a collection, this tag produces a HTML select (dropdown) list
 * based upon the collection.
 *
 * @author    Simon Brown
 */
public class CategoryFilterTag extends TagSupport {

  /**
   * Called when the starting tag is encountered.
   */
  public int doStartTag() throws JspException {
    // setup the iterator to be used
    SimpleBlog rootBlog = (SimpleBlog)pageContext.getRequest().getAttribute(Constants.BLOG_KEY);
    Collection categories = rootBlog.getBlogCategoryManager().getCategories();
    List sortedCategories = new ArrayList(categories);
    Collections.sort(sortedCategories);
    Iterator iterator = sortedCategories.iterator();

    Category currentCategory = (Category)pageContext.getAttribute(
        Constants.CURRENT_CATEGORY_KEY, PageContext.SESSION_SCOPE);

    try {
      JspWriter out = pageContext.getOut();

      // write the starting tag of the select control
      out.print("<select name=\"category\">");
      out.print("<option value=\"all\">All Categories</option>");

      while (iterator.hasNext()) {
        // get the next JavaBean from the collection
        Category category = (Category)iterator.next();

        // and now generate the HTML
        out.print("<option value=\"");
        out.print(category.getId());
        out.print("\"");

        if (currentCategory != null && currentCategory.equals(category)) {
          out.print(" selected=\"true\"");
        }

        out.print(">");

        // and do the same for the label property
        out.print(category.getName());
        out.print("</option>");
      }

      // write the ending tag of the select control
      out.print("</select>");
    } catch (Exception e) {
      throw new JspTagException(e.getMessage());
    }

    // and skip the body
    return SKIP_BODY;

  }

}