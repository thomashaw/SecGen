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

import pebble.Constants;
import pebble.blog.BlogEntry;

/**
 * Tests for the EditBlogEntryTemplateAction class.
 *
 * @author    Simon Brown
 */
public class EditBlogEntryTemplateActionTest extends SecureActionTestCase {

  public void setUp() {
    super.setUp();

    action = new EditBlogEntryTemplateAction();
  }

  public void testProcess() throws Exception {
    // first of all add a blog entry to be edited
    BlogEntry newTemplate = rootBlog.getBlogForToday().createBlogEntry();
    newTemplate.setType(BlogEntry.TEMPLATE);
    newTemplate.store();

    // now execute the action
    request.setParameter("entry", newTemplate.getId());
    String result = action.process(request, response);

    BlogEntry blogEntry = (BlogEntry)request.getAttribute(Constants.BLOG_ENTRY_KEY);
    assertEquals(newTemplate, blogEntry);
    assertEquals(BlogEntry.TEMPLATE, blogEntry.getType());

    String title= (String)request.getAttribute(Constants.TITLE_KEY);
    assertEquals("Edit blog entry", title);

    assertEquals("/themes/default/jsp/template.jsp?content=/jsp/editBlogEntry.jsp", result);
  }

  /**
   * Test that only blog contributors have access to add a blog entry.
   */
  public void testOnlyBlogContributorsHaveAccess() {
    String roles[] = action.getRoles(request);
    assertEquals(1, roles.length);
    assertEquals(Constants.BLOG_CONTRIBUTOR_ROLE, roles[0]);
  }

}
