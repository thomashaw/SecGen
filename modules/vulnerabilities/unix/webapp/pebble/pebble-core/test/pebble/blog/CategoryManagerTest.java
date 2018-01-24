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
package pebble.blog;

import java.util.List;

/**
 * Tests for the CategoryManager class.
 *
 * @author    Simon Brown
 */
public class CategoryManagerTest extends SimpleBlogTestCase {

  private CategoryManager categoryManager;

  public void setUp() {
    super.setUp();
    categoryManager = new CategoryManager(rootBlog);
  }

  /**
   * Test that there are no categories to start with.
   */
  public void testHasNoCategoriesByDefault() {
    assertEquals(0, categoryManager.getCategories().size());
  }

  /**
   * Test that categories can be added and accessed.
   */
  public void testCategoryCanBeAddedAndAccessed() {
    Category category;
    category = categoryManager.addCategory("newCategory", "New Category");
    assertNotNull(category);
    assertEquals("newCategory", category.getId());
    assertEquals("New Category", category.getName());
    assertEquals(category, categoryManager.getCategory("newCategory"));

    categoryManager.addCategory("newCategory", "Another Category");
    assertEquals("newCategory", category.getId());
    assertEquals("Another Category", category.getName());
  }

  /**
   * Test that asking for a non-existent category returns null.
   */
  public void testRequestForNonExistentCategory() {
    assertNull(categoryManager.getCategory("unknownCategory"));
  }

  /**
   * Test that a category can be removed.
   */
  public void testRemovalOfCategory() {
    Category category = categoryManager.addCategory("newCategory", "New Category");
    assertEquals(category, categoryManager.getCategory("newCategory"));
    categoryManager.removeCategory(category);
    assertNull(categoryManager.getCategory("newCategory"));
  }

  /**
   * Test that a category can be removed.
   */
  public void testRemovalOfNonExistentCategory() {
    Category category = new Category("unknownId", "Unknown category");

    // this shouldn't throw an exception - it fails silently
    categoryManager.removeCategory(category);
    assertNull(categoryManager.getCategory("unknownId"));
  }

  /**
   * Tests that we can get a sorted list of categories.
   */
  public void testGetSortedCategoryList() {
    Category categoryC = categoryManager.addCategory("C", "C");
    Category categoryA = categoryManager.addCategory("A", "A");
    Category categoryB = categoryManager.addCategory("B", "B");

    List categories = categoryManager.getCategories();
    assertEquals(categoryA, categories.get(0));
    assertEquals(categoryB, categories.get(1));
    assertEquals(categoryC, categories.get(2));
  }

}
