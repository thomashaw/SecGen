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
package pebble.webservice;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import pebble.blog.*;
import pebble.mail.Notifier;
import pebble.mail.MailNotifier;

/**
 * A handler for the Blogger API (accessed via XML-RPC).
 *
 * @author    Simon Brown
 */
public class BloggerAPIHandler extends AbstractAPIHandler {

  static final String URL = "url";
  static final String BLOG_ID = "blogid";
  static final String BLOG_NAME = "blogName";
  static final String DATE_CREATED = "dateCreated";
  static final String USER_ID = "userId";
  static final String POST_ID = "postid";
  static final String CONTENT = "content";

  static final String TITLE_START_DELIMITER = "<title>";
  static final String TITLE_END_DELIMITER = "</title>";
  static final String CATEGORY_START_DELIMITER = "<category>";
  static final String CATEGORY_END_DELIMITER = "</category>";
  static final char BLOG_ID_SEPARATOR = '/';

  /** the log used by this class */
  private static Log log = LogFactory.getLog(BloggerAPIHandler.class);

  /**
   * Gets information about the user logging in.
   *
   * @param appkey    the client application key (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  a Hashtable containing user information
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public Hashtable getUserInfo(String appkey, String username, String password) throws XmlRpcException {
    log.debug("BloggerAPI.getUserInfo(" +
        appkey + ", " +
        username + ", " +
        "********)");

    boolean authenticated = false;

    Collection blogs = new HashSet();
    if (BlogManager.getInstance().isMultiUser()) {
      CompositeBlog cb = (CompositeBlog)BlogManager.getInstance().getBlog();
      blogs.addAll(cb.getBlogs());
    } else {
      blogs.add(BlogManager.getInstance().getBlog());
    }

    SimpleBlog blog;
    Iterator it = blogs.iterator();
    while (it.hasNext()) {
      blog = (SimpleBlog)it.next();

      if (blog.isXmlRpcEnabled() && authenticate(blog, username, password)) {
        authenticated = true;
      }
    }

    if (authenticated) {
      Hashtable ht = new Hashtable();
      ht.put("userid", username);

      return ht;
    } else {
      throw new XmlRpcAuthenticationException("Username and password did not pass authentication.");
    }
  }

  /**
   * Gets a list of the blogs that the specified user can edit. Pabble
   * only has the concept of a single blog.
   *
   * @param appkey    the client application key (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  a Vector of Hashtables (an array of structs) representing blogs
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public Vector getUsersBlogs(String appkey, String username, String password) throws XmlRpcException {
    log.debug("BloggerAPI.getUsersBlogs(" +
        appkey + ", " +
        username + ", " +
        "********)");

    Collection blogs = new HashSet();
    if (BlogManager.getInstance().isMultiUser()) {
      CompositeBlog cb = (CompositeBlog)BlogManager.getInstance().getBlog();
      blogs.addAll(cb.getBlogs());
    } else {
      blogs.add(BlogManager.getInstance().getBlog());
    }

    Vector usersBlogs = new Vector();

    SimpleBlog blog;
    Iterator it = blogs.iterator();
    while (it.hasNext()) {
      blog = (SimpleBlog)it.next();

      if (blog.isXmlRpcEnabled() && authenticate(blog, username, password)) {
        Hashtable blogInfo = new Hashtable();
        blogInfo.put(URL, blog.getUrl());
        blogInfo.put(BLOG_ID, blog.getId());
        blogInfo.put(BLOG_NAME, blog.getName());

        usersBlogs.add(blogInfo);
      }
    }

    return usersBlogs;
  }

  /**
   * Gets a list of the recent blog entries.
   *
   * @param appkey    the client application key (ignored)
   * @param blogid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param numberOfPosts   the number of posts to get
   * @return  a Vector of Hashtables (an array of structs) representing blog entries
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public Vector getRecentPosts(String appkey, String blogid, String username, String password, int numberOfPosts) throws XmlRpcException {
    log.debug("BloggerAPI.getRecentPosts(" +
        appkey + ", " +
        blogid + ", " +
        username + ", " +
        "********)");

    SimpleBlog blog = authenticate(blogid, username, password);

    Vector posts = new Vector();
    Collection coll = blog.getRecentBlogEntries(numberOfPosts);

    Iterator it = coll.iterator();
    BlogEntry entry;
    while (it.hasNext()) {
      entry = (BlogEntry)it.next();
      posts.add(adaptBlogEntry(entry));
    }

    return posts;
  }

  /**
   * Gets an individual blog entry.
   *
   * @param appkey    the client application key (ignored)
   * @param postid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  a Hashtable representing a blog entry
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public Hashtable getPost(String appkey, String postid, String username, String password) throws XmlRpcException {
    log.debug("BloggerAPI.getPost(" +
        appkey + ", " +
        postid + ", " +
        username + ", " +
        "********)");

    String blogid = getBlogId(postid);
    postid = getPostId(postid);
    SimpleBlog blog = authenticate(blogid, username, password);
    BlogEntry entry = blog.getBlogEntry(postid);

    if (entry != null) {
      return adaptBlogEntry(entry);
    } else {
      throw new XmlRpcException(0, "Blog entry with ID of " + postid + " was not found.");
    }
  }

  /**
   * Creates a new blog entry.
   *
   * @param appkey    the client application key (ignored)
   * @param blogid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param content   the content of the new blog entry
   * @param publish   a flag to indicate whether the entry should be published
   *                  (this is ignored as all new entries are published)
   * @return  a String representing the ID of the new blog entry
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public String newPost(String appkey, String blogid, String username, String password, String content, boolean publish) throws XmlRpcException {
    log.debug("BloggerAPI.newPost(" +
        appkey + ", " +
        blogid + ", " +
        username + ", " +
        "********, " +
        content + ", " +
        publish + ")");

    try {
      SimpleBlog blog = authenticate(blogid, username, password);
      DailyBlog today = blog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();

      populateEntry(entry, content, username);

      today.addEntry(entry);
      entry.store();

      // and should we send update notification pings?
      if (blog.isUpdateNotificationPingsEnabled()) {
        UpdateNotificationPingsClient client = new UpdateNotificationPingsClient();
        client.sendUpdateNotificationPing(blog);
      }

      // notify via email of new entry?
      if (blog.isBlogEntryNotificationEnabled()) {
        Notifier notifier = new MailNotifier(blog);
        notifier.sendNotification(entry);
      }

      return formatPostId(blogid, entry.getId());
    } catch (BlogException be) {
      throw new XmlRpcException(0, be.getMessage());
    }
  }

  /**
   * Edits an existing blog entry.
   *
   * @param appkey    the client application key (ignored)
   * @param postid    the ID of the blog entry to be edited
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param content   the new content of the new blog entry
   * @param publish   a flag to indicate whether the entry should be published
   *                  (this is ignored as all new entries are published)
   * @return  a boolean true value to signal success
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public boolean editPost(String appkey, String postid, String username, String password, String content, boolean publish) throws XmlRpcException {
    log.debug("BloggerAPI.editPost(" +
        appkey + ", " +
        postid + ", " +
        username + ", " +
        "********, " +
        content + ", " +
        publish + ")");

    try {
      String blogid = getBlogId(postid);
      postid = getPostId(postid);
      SimpleBlog blog = authenticate(blogid, username, password);
      BlogEntry entry = blog.getBlogEntry(postid);

      if (entry != null) {
        populateEntry(entry, content, username);
        entry.store();

        // and should we send update notification pings?
        if (blog.isUpdateNotificationPingsEnabled()) {
          UpdateNotificationPingsClient client = new UpdateNotificationPingsClient();
          client.sendUpdateNotificationPing(blog);
        }

        // notify via email of new entry?
        if (blog.isBlogEntryNotificationEnabled()) {
          Notifier notifier = new MailNotifier(blog);
          notifier.sendNotification(entry);
        }
      } else {
        throw new XmlRpcException(0, "Blog entry with ID of " + postid + " was not found.");
      }

      return true;
    } catch (BlogException be) {
      throw new XmlRpcException(0, be.getMessage());
    }
  }

  /**
   * Deletes an existing blog entry.
   *
   * @param appkey    the client application key (ignored)
   * @param postid    the ID of the blog entry to be edited
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param publish   a flag to indicate whether the entry should be published
   *                  (this is ignored)
   * @return  a boolean true value to signal success
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public boolean deletePost(String appkey, String postid, String username, String password, boolean publish) throws XmlRpcException {
    log.debug("BloggerAPI.deletePost(" +
        appkey + ", " +
        postid + ", " +
        username + ", " +
        "********, " +
        publish + ")");

    try {
      String blogid = getBlogId(postid);
      postid = getPostId(postid);
      SimpleBlog blog = authenticate(blogid, username, password);
      BlogEntry blogEntry = blog.getBlogEntry(postid);

      if (blogEntry != null) {
        DailyBlog dailyBlog = blogEntry.getDailyBlog();
        blogEntry.remove();
        dailyBlog.removeEntry(blogEntry);

        return true;
      } else {
        throw new XmlRpcException(0, "Blog entry with ID of " + postid + " was not found.");
      }
    } catch (BlogException be) {
      throw new XmlRpcException(0, be.getMessage());
    }
  }

  /**
   * A helper method to authenticate a username/password pair against the
   * details defined in the blog.properties file.
   *
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @throws XmlRpcException  if XML-RPC is not enabled for the blog or if the
   *                          credentials are incorrect
   */
  private SimpleBlog authenticate(String blogid, String username, String password) throws XmlRpcException {
    SimpleBlog blog = BlogManager.getInstance().getBlog(blogid);

    if (blog == null) {
      throw new XmlRpcException(0, "Blog with ID of " + blogid + " not found.");
    }

    if (blog.isXmlRpcEnabled()) {
      if (authenticate(blog, username, password)) {
          return blog;
      } else {
        throw new XmlRpcAuthenticationException("Username and password did not pass authentication.");
      }
    } else {
      throw new XmlRpcException(0, "XML-RPC not enabled for this blog.");
    }
  }

  /**
   * Helper method to adapt a blog entry into an XML-RPC compatible struct.
   * Since the Blogger API doesn't support titles, the title is wrapped in
   * &lt;title&gt;&lt;/title&gt; tags.
   *
   * @param entry   the BlogEntry to adapt
   * @return  a Hashtable representing the major properties of the entry
   */
  private Hashtable adaptBlogEntry(BlogEntry entry) {
    Hashtable post = new Hashtable();
    String categories = "";
    Iterator it = entry.getCategories().iterator();
    while (it.hasNext()) {
      Category category = (Category)it.next();
      categories += category.getId();
      if (it.hasNext()) {
        categories += ",";
      }
    }
    post.put(DATE_CREATED, entry.getDate());
    post.put(USER_ID, entry.getAuthor());
    post.put(POST_ID, formatPostId(entry.getRootBlog().getId(), entry.getId()));
    post.put(CONTENT, TITLE_START_DELIMITER + entry.getTitle() + TITLE_END_DELIMITER
        + CATEGORY_START_DELIMITER + categories + CATEGORY_END_DELIMITER + entry.getBody());

    return post;
  }

  /**
   * Populates a given BlogEntry.
   *
   * @param entry     the BlogEntry to populate
   * @param content   the content (including title)
   * @param username  the author
   */
  private void populateEntry(BlogEntry entry, String content, String username) {
    String title = "";
    String category = "";

    if (content.indexOf(TITLE_START_DELIMITER) > -1 && content.indexOf(TITLE_END_DELIMITER) > -1) {
      content = content.substring(TITLE_START_DELIMITER.length());
      int index = content.indexOf(TITLE_END_DELIMITER);
      title = content.substring(0, index);
      content = content.substring(index);
      content = content.substring(TITLE_END_DELIMITER.length());
    }

    if (content.indexOf(CATEGORY_START_DELIMITER) > -1 && content.indexOf(CATEGORY_END_DELIMITER) > -1) {
      content = content.substring(CATEGORY_START_DELIMITER.length());
      int index = content.indexOf(CATEGORY_END_DELIMITER);
      category = content.substring(0, index);
      content = content.substring(index);
      content = content.substring(CATEGORY_END_DELIMITER.length());
    }

    entry.setTitle(title);
    entry.setBody(content);
    entry.setAuthor(username);

    String[] categories = category.split(",");
    for (int i = 0; i < categories.length; i++) {
    Category c = entry.getRootBlog().getBlogCategoryManager().getCategory(categories[i].trim());
     if (c != null)
       entry.addCategory(c);
    }
  }

  /**
   * Gets the specified template type for a blog - not supported by Pebble.
   *
   * @param appkey    the client application key (ignored)
   * @param blogid    the ID of the blog
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param templateType  the type of template to retrieve
   * @return  the text of the specified template
   * @throws XmlRpcException
   */
  public String getTemplate(String appkey, String blogid, String username, String password, String templateType) throws XmlRpcException {
    log.debug("BloggerAPI.getTemplate(" +
        appkey + ", " +
        blogid + ", " +
        username + ", " +
        "********, " +
        templateType + ")");

    throw new XmlRpcException(0, "getTemplate is not supported by Pebble.");
  }

  /**
   * Sets the specified template type for a blog - not supported by Pebble.
   *
   * @param appkey    the client application key (ignored)
   * @param blogid    the ID of the blog
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param template  the new text of the template
   * @param templateType  the type of template to retrieve
   * @return  true if setting the template was successful, false otherwise
   * @throws XmlRpcException
   */
  public boolean setTemplate(String appkey, String blogid, String username, String password, String template, String templateType) throws XmlRpcException {
    log.debug("BloggerAPI.setTemplate(" +
        appkey + ", " +
        blogid + ", " +
        username + ", " +
        "********, " +
        template + ", " +
        templateType + ")");

    throw new XmlRpcException(0, "setTemplate is not supported by Pebble.");
  }

  /**
   * Adds a category to a blog entry - this isn't a standard Blogger API method.
   *
   * @param appkey    the client application key (ignored)
   * @param postid    the ID of the blog entry to be edited
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param category  the category ID
   * @return  a boolean true value to signal success
   * @throws XmlRpcException    if something goes wrong, including an authentication error
   */
  public boolean addCategory(String appkey, String postid, String username, String password, String category) throws XmlRpcException {
    log.debug("BloggerAPI.addCategory(" +
        appkey + ", " +
        postid + ", " +
        username + ", " +
        "********, " +
        category + ")");

    try {
      String blogid = getBlogId(postid);
      postid = getPostId(postid);
      SimpleBlog blog = authenticate(blogid, username, password);
      BlogEntry entry = blog.getBlogEntry(postid);

      if (entry != null) {
        Category c = entry.getRootBlog().getBlogCategoryManager().getCategory(category);
        if (c != null) {
          entry.addCategory(c);
          entry.store();
          return true;
        }
      } else {
        throw new XmlRpcException(0, "Blog entry with ID of " + postid + " was not found.");
      }

      return false;
    } catch (BlogException be) {
      throw new XmlRpcException(0, be.getMessage());
    }
  }

}