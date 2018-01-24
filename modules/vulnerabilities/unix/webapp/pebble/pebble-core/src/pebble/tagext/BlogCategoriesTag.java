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

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import pebble.Constants;
import pebble.blog.SimpleBlog;

/**
 * A tag that provides iteration over the collection of all categories.
 *
 * @author      Simon Brown
 */
public class BlogCategoriesTag extends TagSupport {

  private Iterator iterator;

  /**
   * Called when the starting tag is encountered.
   *
   * @return  EVAL_BODY_INCLUDE or SKIP_BODY, depending whether there
   *          are categories or not
   * @throws  JspException    if something goes wrong outputting the property
   */
  public int doStartTag() throws JspException {
    SimpleBlog rootBlog = (SimpleBlog)pageContext.getRequest().getAttribute(Constants.BLOG_KEY);

    iterator = rootBlog.getBlogCategoryManager().getCategories().iterator();
    if (iterator.hasNext()) {
      pageContext.setAttribute("category", iterator.next(), PageContext.REQUEST_SCOPE);
      return EVAL_BODY_INCLUDE;
    } else {
      return SKIP_BODY;
    }
  }

  /**
   * Called after the body of the tag has been evaluated.
   *
   * @return  EVAL_BODY_AGAIN or SKIP_BODY, depending whether there
   *          are categories or not
   * @throws  JspException    if something goes wrong outputting the property
   */
  public int doAfterBody() throws JspException {
    if (iterator.hasNext()) {
      pageContext.setAttribute("category", iterator.next(), PageContext.REQUEST_SCOPE);
      return EVAL_BODY_AGAIN;
    } else {
      return SKIP_BODY;
    }
  }

}