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

import java.util.Collection;
import java.util.Calendar;

import pebble.Constants;
import pebble.blog.BlogEntry;
import pebble.blog.Category;
import pebble.blog.CompositeBlogTestCase;
import pebble.blog.DailyBlog;
import pebble.mock.MockHttpServletRequest;
import pebble.mock.MockHttpServletResponse;

/**
 * Tests for the FeedAction class.
 *
 * @author    Simon Brown
 */
public class CompositeFeedActionTest extends CompositeBlogTestCase {

  private FeedAction action;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  public void setUp() {
    super.setUp();

    action = new FeedAction();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    request.setAttribute(Constants.BLOG_KEY, blog1);
  }

  public void testCategoriesExcludedFromFeedInMultiUserMode() throws Exception {
    Category cat1 = new Category("cat1", "Category 1");
    Category cat2 = new Category("cat2", "Category 2");
    blog1.getBlogCategoryManager().addCategory(cat1);
    blog1.getBlogCategoryManager().addCategory(cat2);
    DailyBlog today = blog1.getBlogForToday();
    Calendar cal1 = blog1.getCalendar();
    cal1.set(Calendar.HOUR_OF_DAY, 2);
    Calendar cal2 = blog1.getCalendar();
    cal2.set(Calendar.HOUR_OF_DAY, 3);
    BlogEntry entry1 = today.createBlogEntry("title", "body", cal1.getTime());
    entry1.addCategory(cat1);
    today.addEntry(entry1);
    BlogEntry entry2 = today.createBlogEntry("title", "body", cal2.getTime());
    entry2.addCategory(cat2);
    today.addEntry(entry2);

    request.setParameter("category", "cat2");
    action.process(request, response);
    Category category = (Category)request.getAttribute("category");
    Collection entries = (Collection)request.getAttribute("blogEntries");

    assertEquals(cat2, category);
    assertFalse(entries.contains(entry1));
    assertTrue(entries.contains(entry2));
  }

}
