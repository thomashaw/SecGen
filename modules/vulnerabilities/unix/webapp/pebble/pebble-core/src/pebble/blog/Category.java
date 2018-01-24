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

import java.io.Serializable;

/**
 * Represents a blog category.
 *
 * @author    Simon Brown
 */
public class Category implements Comparable, Serializable {

  /** the owning blog */
  private Blog blog;

  /** the id of the category */
  private String id;

  /** the name of the category */
  private String name;

  /**
   * Creates a new category with the specified properties.
   *
   * @param id          the id
   * @param name          the name
   */
  public Category(String id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Gets the id of this category.
   *
   * @return    the id as a String
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the name of this category.
   *
   * @return    the name as a String
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this category.
   *
   * @param name    the new category name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the owning blog.
   *
   * @param blog    a Blog instance
   */
  public void setBlog(Blog blog) {
    this.blog = blog;
  }

  /**
   * Gets a permalink to this category.
   *
   * @return  a URL that points to this category
   */
  public String getPermalink() {
    return blog.getUrl() + "categories/" + id + ".html";
  }

  /**
   * Gets the hashcode of this object.
   *
   * @return  the hashcode as an int
   */
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Determines whether the specified object is equal to this one.
   *
   * @param o   the object to compare against
   * @return    true if Object o represents the same category, false otherwise
   */
  public boolean equals(Object o) {
    if (!(o instanceof Category)) {
      return false;
    }

    Category cat = (Category)o;
    return (cat.getId().equals(id));
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.<p>
   *
   * @param   o the Object to be compared.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   *
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this Object.
   */
  public int compareTo(Object o) {
    Category category = (Category)o;
    return getName().compareTo(category.getName());
  }

  /**
   * Returns a String representation of this object.
   *
   * @return  a String
   */
  public String toString() {
    return this.name;
  }

}
