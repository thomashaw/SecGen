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

import java.util.*;

import pebble.blog.persistence.*;

/**
 * A class to manage blog categories.
 *
 * @author    Simon Brown
 */
public class CategoryManager {

  /** the owning root blog */
  private Blog rootBlog;

  /** the collection of all categories */
  private Collection categories;

  /**
   * Creates a new instance.
   */
  CategoryManager(Blog rootBlog) {
    this.rootBlog = rootBlog;
    init();
  }

  /**
   * Initializes the categories.
   */
  private void init() {
    try {
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      CategoryDAO dao = factory.getCategoryDAO();
      categories = dao.getCategories(rootBlog);
      Iterator it = categories.iterator();
      while(it.hasNext()) {
        Category category = (Category)it.next();
        category.setBlog(rootBlog);
      }
    } catch (PersistenceException pe) {
      pe.printStackTrace();
    }
  }

  /**
   * Gets a Category with the specified ID.
   *
   * @param id    the key of the blog category to find
   * @return  a Category instance corresponding to the key,
   *          or null if one doesn't exist
   */
  public Category getCategory(String id) {
    Iterator it = categories.iterator();
    Category category;
    while (it.hasNext()) {
      category = (Category)it.next();
      if (category.getId().equals(id)) {
        return category;
      }

    }

    return null;
  }

  /**
   * Adds a new blog category. If the category exists, the name is updated,
   * otherwise a new category instance is created.
   *
   * @param newCategory   the new Category to be added
   */
  public synchronized Category addCategory(Category newCategory) {
    Category category = getCategory(newCategory.getId());

    try {
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      CategoryDAO dao = factory.getCategoryDAO();

      if (category != null) {
        category.setName(newCategory.getName());
        category.setBlog(rootBlog);
        dao.updateCategory(category, rootBlog);
      } else {
        newCategory.setBlog(rootBlog);
        dao.addCategory(newCategory, rootBlog);
        categories.add(newCategory);
      }
    } catch (PersistenceException pe) {
      pe.printStackTrace();
    }

    return newCategory;
  }

  /**
   * Adds a new blog category. If the category exists, the name is updated,
   * otherwise a new category instance is created.
   *
   * @param id    the key of the category
   * @param name  the name of the category
   */
  public synchronized Category addCategory(String id, String name) {
    return addCategory(new Category(id, name));
  }

  /**
   * Removes a Category with the specified ID.
   *
   * @param category    the Category to be removed
   */
  public synchronized void removeCategory(Category category) {
    if (categories.contains(category)) {
      try {
        // remove it from the persistent store
        DAOFactory factory = DAOFactory.getConfiguredFactory();
        CategoryDAO dao = factory.getCategoryDAO();
        dao.deleteCategory(category, rootBlog);

        // and now remove the in-memory representation
        categories.remove(category);
        category.setBlog(null);
      } catch (PersistenceException pe) {
        pe.printStackTrace();
      }
    }
  }

  /**
   * Gets a collection containing all blog categories,
   * ordered by category name.
   *
   * @return  a sorted List of Category instances
   */
  public List getCategories() {
    List sortedCategories = new ArrayList(categories);
    Collections.sort(sortedCategories);

    return sortedCategories;
  }

}