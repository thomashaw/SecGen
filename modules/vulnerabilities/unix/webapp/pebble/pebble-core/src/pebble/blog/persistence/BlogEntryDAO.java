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
package pebble.blog.persistence;

import java.util.List;
import java.util.Collection;

import pebble.blog.*;

public interface BlogEntryDAO {

  /**
   * Stores the specified blog entry.
   *
   * @param blogEntry   the blog entry to store
   * @throws PersistenceException   if something goes wrong storing the entry
   */
  public void store(BlogEntry blogEntry) throws PersistenceException;

  /**
   * Removes the specified blog entry.
   *
   * @param blogEntry   the blog entry to remove
   * @throws PersistenceException   if something goes wrong removing the entry
   */
  public void remove(BlogEntry blogEntry) throws PersistenceException;

  /**
   * Gets the YearlyBlogs that the specified root blog is managing.
   *
   * @param rootBlog    the owning Blog instance
   * @throws  PersistenceException    if the yearly blogs cannot be loaded
   */
  public List getYearlyBlogs(SimpleBlog rootBlog) throws PersistenceException;

  /**
   * Loads the blog entries for a given daily blog.
   *
   * @param dailyBlog   the DailyBlog instance
   * @return  a List of BlogEntry instances
   * @throws  pebble.blog.persistence.PersistenceException    if blog entries cannot be loaded
   */
  public List getBlogEntries(DailyBlog dailyBlog) throws PersistenceException;

  /**
   * Loads the draft blog entries for a given blog.
   *
   * @param blog    the owning SimpleBlog instance
   * @return  a List of BlogEntry instances
   * @throws  pebble.blog.persistence.PersistenceException    if blog entries cannot be loaded
   */
  public Collection getDraftBlogEntries(SimpleBlog blog) throws PersistenceException;

  /**
   * Loads the blog entry templates for a given blog.
   *
   * @param blog    the owning SimpleBlog instance
   * @return  a List of BlogEntry instances
   * @throws  pebble.blog.persistence.PersistenceException    if blog entries cannot be loaded
   */
  public Collection getBlogEntryTemplates(SimpleBlog blog) throws PersistenceException;

  /**
   * Loads the static pages for a given blog.
   *
   * @param blog    the owning SimpleBlog instance
   * @return  a List of BlogEntry instances
   * @throws  pebble.blog.persistence.PersistenceException    if blog entries cannot be loaded
   */
  public Collection getStaticPages(SimpleBlog blog) throws PersistenceException;

}
