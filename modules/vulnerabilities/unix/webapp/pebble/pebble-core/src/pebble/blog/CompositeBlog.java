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

import javax.servlet.http.HttpServletRequest;

import pebble.comparator.SimpleBlogComparator;

/**
 * A composite blog is one that is made up of one or more simple blogs. This
 * is effectively a container when Pebble is running in multi-user mode - there
 * being a single CompositeBlog and many SimpleBlog instances.
 * <br /><br />
 * In addition to managing one or more simple blogs, a composite blog provides
 * some aggegration functionality over the blogs it manages in order to
 * generate an aggregated XML (RSS/RDF/Atom) feed.
 *
 * @author    Simon Brown
 */
public class CompositeBlog extends Blog {

  /** the blogs that are currently being managed */
  private HashMap blogs = new HashMap();

  /**
   * Creates a new Blog instance, based at the specified location.
   *
   * @param root    an absolute path pointing to the root directory of the blog
   */
  public CompositeBlog(String root) {
    super(root);
  }

  /**
   * Gets the default properties for a CompositeBlog.
   *
   * @return    a Properties instance
   */
  protected Properties getDefaultProperties() {
    Properties defaultProperties = new Properties();
    defaultProperties.setProperty(NAME_KEY, "Pebble Blogs");
    defaultProperties.setProperty(DESCRIPTION_KEY, "Blogs powered by Pebble");
    defaultProperties.setProperty(IMAGE_KEY, "http://pebble.sourceforge.net/common/images/powered-by-pebble.gif");
    defaultProperties.setProperty(AUTHOR_KEY, "Various");
    defaultProperties.setProperty(TIMEZONE_KEY, "Europe/London");
    defaultProperties.setProperty(RECENT_BLOG_ENTRIES_ON_HOME_PAGE_KEY, "3");
    defaultProperties.setProperty(RECENT_COMMENTS_ON_HOME_PAGE_KEY, "0");
    defaultProperties.setProperty(LANGUAGE_KEY, "en");
    defaultProperties.setProperty(COUNTRY_KEY, "GB");
    defaultProperties.setProperty(CHARACTER_ENCODING_KEY, "UTF-8");

    return defaultProperties;
  }

  public void addBlog(SimpleBlog blog) {
    blogs.put(blog.getId(), blog);
    blog.setUrl(this.url + blog.getId() + "/");
  }

  /**
   * Gets all blogs that are currently being managed.
   *
   * @return  a Collection of SimpleBlog instances
   */
  public Collection getBlogs() {
    List sortedBlogs = new ArrayList(blogs.values());
    Collections.sort(sortedBlogs, new SimpleBlogComparator());
    return sortedBlogs;
  }

  /**
   * Gets all blogs that are currently being managed and are
   * to be included in aggregated pages and feeds.
   *
   * @return  a Collection of SimpleBlog instances
   */
  public Collection getPublicBlogs() {
    Collection coll = getBlogs();
    List list = new ArrayList();
    Iterator it = coll.iterator();
    while (it.hasNext()) {
      SimpleBlog simpleBlog = (SimpleBlog)it.next();
      if (simpleBlog.isPublic()) {
        list.add(simpleBlog);
      }
    }

    return list;
  }

  /**
   * Gets the blog with the specified ID.
   *
   * @param id    the blog ID
   * @return  the corresponding Blog instance
   */
  public SimpleBlog getBlog(String id) {
    return (SimpleBlog)blogs.get(id);
  }

  /**
   * Determines whether there is a blog with the specified ID.
   *
   * @param id    the blog ID
   * @return  true if a blog with the specified ID exists, false otherwise
   */
  public boolean hasBlog(String id) {
    return blogs.containsKey(id);
  }

  /**
   * Gets the date that this blog was last updated.
   *
   * @return  a Date instance representing the time of the most recent entry
   */
  public Date getLastModified() {
    Date date = new Date(0);

    Iterator it = blogs.values().iterator();
    SimpleBlog blog;
    while (it.hasNext()) {
      blog = (SimpleBlog)it.next();
      if (blog.getLastModified().after(date)) {
        date = blog.getLastModified();
      }
    }

    return date;
  }

  /**
   * Gets the most recent blog entries for a given category, the number
   * of which is specified.
   *
   * @param   category          a Category instance, or null
   * @param   numberOfEntries   the number of entries to get
   * @return  a List containing the most recent blog entries
   */
  public List getRecentBlogEntries(Category category, int numberOfEntries) {
    List blogEntries = new ArrayList();

    Iterator it = getPublicBlogs().iterator();
    SimpleBlog blog;
    while (it.hasNext()) {
      blog = (SimpleBlog)it.next();
      blogEntries.addAll(blog.getRecentBlogEntries(category, numberOfEntries));
    }

    return blogEntries;
  }

  /**
   * Logs this request for blog.
   *
   * @param request   the HttpServletRequest instance for this request
   */
  public synchronized void log(HttpServletRequest request) {
    // only log requests for those blogs that have not opted
    // out of aggregation
    Iterator it = getPublicBlogs().iterator();
    SimpleBlog blog;
    while (it.hasNext()) {
      blog = (SimpleBlog)it.next();
      blog.log(request);
    }
  }

}