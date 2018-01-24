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
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import pebble.blog.*;
import pebble.mail.Notifier;
import pebble.mail.MailNotifier;
import pebble.PebbleProperties;

/**
 * A handler for the MetaWeblog API (accessed via XML-RPC).
 *
 * @author    Simon Brown
 */
public class MetaWeblogAPIHandler extends AbstractAPIHandler {

  static final String URL = "url";
  static final String BLOG_ID = "blogid";
  static final String BLOG_NAME = "blogName";
  static final String DATE_CREATED = "dateCreated";
  static final String USER_ID = "userId";
  static final String POST_ID = "postid";
  static final String TITLE = "title";
  static final String DESCRIPTION = "description";
  static final String PERMALINK = "permaLink";
  static final String PUB_DATE = "pubDate";
  static final String CATEGORIES = "categories";
  static final String NAME = "name";
  static final String TYPE = "type";
  static final String BITS = "bits";
  static final String HTML_URL = "htmlUrl";
  static final String RSS_URL = "rssUrl";

  /** the log used by this class */
  private static Log log = LogFactory.getLog(MetaWeblogAPIHandler.class);

  /**
   * Creates a new media object on the server.
   *
   * @param blogid    the ID of the blog
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  a Hashtable (structs) containing information about the object
   * @throws org.apache.xmlrpc.XmlRpcException    if something goes wrong, including an authentication error
   */
  public Hashtable newMediaObject(String blogid, String username, String password, Hashtable struct) throws XmlRpcException {
    log.debug("metaWeblog.newMediaObject(" +
        blogid + ", " +
        username + ", " +
        "********)");
    log.debug(" name = " + struct.get(NAME));
    log.debug(" type = " + struct.get(TYPE));

    SimpleBlog blog = authenticate(blogid, username, password);
    Hashtable ht = new Hashtable();

    String name = (String)struct.get(NAME);
    FileManager manager;
    if (name.startsWith("images")) {
      manager = new FileManager(blog, FileMetaData.BLOG_IMAGE);
      name = name.substring(name.indexOf("/"));
    } else {
      manager = new FileManager(blog, FileMetaData.BLOG_FILE);
      name = name.substring(name.indexOf("/"));
    }

    log.debug("Saving to " + name);
    try {
      byte bytes[] = (byte[])struct.get(BITS);
      long itemSize = bytes.length/1024; // number of bytes / 1024
      if (FileManager.hasEnoughSpace(blog, itemSize)) {
        FileMetaData file = manager.saveFile(name, bytes);
        ht.put(URL, file.getUrl());
      } else {
        throw new XmlRpcException(0, "You do not have enough free space - please free some space by removing unused files or asking your system administrator to increase your quota from " + PebbleProperties.getInstance().getFileUploadQuota() + " KB.");
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new XmlRpcException(0, "IOException");
    } catch (IllegalFileAccessException e) {
      e.printStackTrace();
      throw new XmlRpcException(0, "Access forbidden");
    }

    return ht;
  }

  /**
   * Gets a list of categories.
   *
   * @param blogid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  a Hashtable of Hashtables (a struct of structs) representing categories
   * @throws org.apache.xmlrpc.XmlRpcException    if something goes wrong, including an authentication error
   */
  public Hashtable getCategories(String blogid, String username, String password) throws XmlRpcException {
    log.debug("metaWeblog.getCategories(" +
        blogid + ", " +
        username + ", " +
        "********)");

    SimpleBlog blog = authenticate(blogid, username, password);

    Hashtable categories = new Hashtable();
    Iterator it = blog.getBlogCategoryManager().getCategories().iterator();
    Category category;
    while (it.hasNext()) {
      category = (Category)it.next();
      Hashtable struct = new Hashtable();
      struct.put(DESCRIPTION, category.getId());
      struct.put(HTML_URL, category.getPermalink());
      struct.put(RSS_URL, blog.getUrl() + "rss.xml?category=" + category.getId());
      categories.put(category.getId(), struct);
    }

    return categories;
  }

  /**
   * Gets a list of the recent blog entries.
   *
   * @param blogid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param numberOfPosts   the number of posts to get
   * @return  a Vector of Hashtables (an array of structs) representing blog entries
   * @throws org.apache.xmlrpc.XmlRpcException    if something goes wrong, including an authentication error
   */
  public Vector getRecentPosts(String blogid, String username, String password, int numberOfPosts) throws XmlRpcException {
    log.debug("metaWeblog.getRecentPosts(" +
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
   * @param postid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  a Hashtable representing a blog entry
   * @throws org.apache.xmlrpc.XmlRpcException    if something goes wrong, including an authentication error
   */
  public Hashtable getPost(String postid, String username, String password) throws XmlRpcException {
    log.debug("metaWeblog.getPost(" +
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
   * @param blogid    the ID of the blog (ignored)
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param struct    the struct containing the new blog entry
   * @param publish   a flag to indicate whether the entry should be published
   *                  (this is ignored as all new entries are published)
   * @return  a String representing the ID of the new blog entry
   * @throws org.apache.xmlrpc.XmlRpcException    if something goes wrong, including an authentication error
   */
  public String newPost(String blogid, String username, String password, Hashtable struct, boolean publish) throws XmlRpcException {
    log.debug("metaWeblog.newPost(" +
        blogid + ", " +
        username + ", " +
        "********, " +
        struct + ", " +
        publish + ")");

    try {
      SimpleBlog blog = authenticate(blogid, username, password);
      DailyBlog today = blog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();

      populateEntry(entry, struct, username);

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
   * @param postid    the ID of the blog entry to be edited
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @param struct    the new content of the new blog entry
   * @param publish   a flag to indicate whether the entry should be published
   *                  (this is ignored as all new entries are published)
   * @return  a boolean true value to signal success
   * @throws org.apache.xmlrpc.XmlRpcException    if something goes wrong, including an authentication error
   */
  public boolean editPost(String postid, String username, String password, Hashtable struct, boolean publish) throws XmlRpcException {
    log.debug("BloggerAPI.editPost(" +
        postid + ", " +
        username + ", " +
        "********, " +
        struct + ", " +
        publish + ")");

    try {
      String blogid = getBlogId(postid);
      postid = getPostId(postid);
      SimpleBlog blog = authenticate(blogid, username, password);
      BlogEntry entry = blog.getBlogEntry(postid);

      if (entry != null) {
        populateEntry(entry, struct, username);
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
   * A helper method to authenticate a username/password pair against the
   * details defined in the blog.properties file.
   *
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @throws org.apache.xmlrpc.XmlRpcException  if XML-RPC is not enabled for the blog or if the
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
   *
   * @param entry   the BlogEntry to adapt
   * @return  a Hashtable representing the major properties of the entry
   */
  private Hashtable adaptBlogEntry(BlogEntry entry) {
    Hashtable post = new Hashtable();
    post.put(TITLE, entry.getTitle());
    post.put(PERMALINK, entry.getPermalink());
    post.put(TITLE, entry.getTitle());
    post.put(DESCRIPTION, entry.getBody());
    post.put(DATE_CREATED, entry.getDate());
    post.put(PUB_DATE, entry.getDate());
    post.put(USER_ID, entry.getAuthor());
    post.put(POST_ID, formatPostId(entry.getRootBlog().getId(), entry.getId()));

    Vector categories = new Vector();
    Iterator it = entry.getCategories().iterator();
    while (it.hasNext()) {
      Category cat = (Category)it.next();
      categories.add(cat.getId());
    }
    post.put(CATEGORIES, categories);

    return post;
  }

  /**
   * Populates a given BlogEntry.
   *
   * @param entry     the BlogEntry to populate
   * @param struct    a Hashtable containing the blog entry details
   * @param username  the author
   */
  private void populateEntry(BlogEntry entry, Hashtable struct, String username) {
    entry.setTitle((String)struct.get(TITLE));
    entry.setBody((String)struct.get(DESCRIPTION));
    entry.setAuthor(username);

    Vector categories = (Vector)struct.get(CATEGORIES);
    if (categories != null) {
      for (int i = 0; i < categories.size(); i++) {
      Category c = entry.getRootBlog().getBlogCategoryManager().getCategory((String)categories.get(i));
       if (c != null)
         entry.addCategory(c);
      }
    }
  }

}