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

import pebble.Constants;
import pebble.blog.Category;

/**
 * Tests for the ChangeCategoryAction class.
 *
 * @author    Simon Brown
 */
public class ChangeCategoryActionTest extends ActionTestCase {

  public void setUp() {
    super.setUp();

    action = new ChangeCategoryAction();
  }

  public void testCategoryNotSpecified() {
    try {
      String result = action.process(request, response);

      // check that the category has been reset
      assertNull(request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));
      assertEquals("/viewHomePage.action", result);
    } catch (ServletException e) {
      fail();
    }
  }

  public void testAllCategoriesSpecified() {
    try {
      request.setParameter("category", "all");
      String result = action.process(request, response);

      // check that the category has been reset
      assertNull(request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));
      assertEquals("/viewHomePage.action", result);
    } catch (ServletException e) {
      fail();
    }
  }

  public void testSettingCategoryForTheFirstTime() {
    try {
      // check that the current category is null
      assertNull(request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));

      Category category = new Category("aCategory", "A category");
      rootBlog.getBlogCategoryManager().addCategory(category);

      request.setParameter("category", "aCategory");
      String result = action.process(request, response);

      // check that the category has been set
      assertEquals(category, request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));
      assertEquals("/viewHomePage.action", result);
    } catch (ServletException e) {
      fail();
    }
  }

  public void testInvalidCategoryResetsCurrentCategory() {
    try {
      // check that the current category is null
      assertNull(request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));

      Category category = new Category("aCategory", "A category");
      rootBlog.getBlogCategoryManager().addCategory(category);

      // set the current category to A category
      request.getSession().setAttribute(Constants.CURRENT_CATEGORY_KEY, category);

      request.setParameter("category", "someUnknownCategory");
      String result = action.process(request, response);

      // check that the category has been reset
      assertNull(request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));
      assertEquals("/viewHomePage.action", result);
    } catch (ServletException e) {
      fail();
    }
  }

  public void testChangingTheCategory() {
    try {
      Category category1 = new Category("category1", "Category 1");
      Category category2 = new Category("category2", "Category 2");
      rootBlog.getBlogCategoryManager().addCategory(category1);
      rootBlog.getBlogCategoryManager().addCategory(category2);

      // set the current category to Category 1
      request.getSession().setAttribute(Constants.CURRENT_CATEGORY_KEY, category1);

      request.setParameter("category", "category2");
      String result = action.process(request, response);

      // check that the category has been changed to Category 2
      assertEquals(category2, request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));
      assertEquals("/viewHomePage.action", result);
    } catch (ServletException e) {
      fail();
    }
  }

  public void testInvalidCategoriesSpecified() {
    try {
      request.setParameter("category", "nonexistent");
      String result = action.process(request, response);

      // check that the category has been reset
      assertNull(request.getSession().getAttribute(Constants.CURRENT_CATEGORY_KEY));
      assertEquals("/viewHomePage.action", result);
    } catch (ServletException e) {
      fail();
    }
  }

}
