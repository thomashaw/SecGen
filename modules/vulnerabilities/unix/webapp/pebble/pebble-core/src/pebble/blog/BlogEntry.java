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

import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.blog.persistence.BlogEntryDAO;
import pebble.blog.persistence.DAOFactory;
import pebble.blog.persistence.PersistenceException;
import pebble.controller.ValidationContext;
import pebble.search.BlogIndexer;
import pebble.util.StringUtils;

/**
 * Represents a blog entry.
 *
 * @author Simon Brown
 */
public class BlogEntry implements Permalinkable, Cloneable {

  /**
   * the log used by this class
   */
  private static Log log = LogFactory.getLog(BlogEntry.class);

  /**
   * the id of the blog entry
   */
  private String id;

  /**
   * the title of the blog entry
   */
  private String title;

  /**
   * the category that the blog entry falls into
   */
  private Set categories = new HashSet();

  /**
   * the excerpt of the blog entry
   */
  private String excerpt = "";

  /**
   * the body/content of the blog entry
   */
  private String body = "";

  /**
   * the date that the entry was created
   */
  private Date date;

  /** the timezone where the entry was posted */
  private TimeZone timeZone;

  /**
   * the author of the blog entry
   */
  private String author = "";

  /**
   * a flag to indicate whether comments are enabled for this entry
   */
  private boolean commentsEnabled = true;

  /**
   * a flag to indicate whether TrackBacks are enabled for this entry
   */
  private boolean trackBacksEnabled = true;

  /**
   * the collection of comments for the blog entry
   */
  private List comments = new ArrayList();

  /**
   * the collection of trackbacks for the blog entry
   */
  private List trackBacks = new ArrayList();

  /**
   * the alternative permalink for this blog entry
   */
  private String originalPermalink;

  /**
   * the owning DailyBlog
   */
  private DailyBlog dailyBlog;

  public static final int PUBLISHED = 0;
  public static final int NEW = 1;
  public static final int DRAFT = 2;
  public static final int TEMPLATE = 4;
  public static final int STATIC_PAGE = 8;
  private int type = PUBLISHED;

  /**
   * Creates a new blog entry with the specified attributes.
   *
   * @param dailyBlog the owning DailyBlog
   */
  public BlogEntry(DailyBlog dailyBlog) {
    this.dailyBlog = dailyBlog;
  }

  /**
   * Gets the unique id of this blog entry.
   *
   * @return the id as a String
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the title of this blog entry.
   *
   * @return the title as a String
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title of this blog entry.
   *
   * @param title the title as a String
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the category of this blog entry.
   *
   * @return the category as a String
   */
  public Set getCategories() {
    return categories;
  }

  /**
   * Sets the category of this blog entry.
   *
   * @param category the category as a String
   */
  public void addCategory(Category category) {
    if (category != null) {
      categories.add(category);
    }
  }

  /**
   * Removes all categories from this blog entry.
   */
  public void removeAllCategories() {
    categories.clear();
  }

  /**
   * Determines whether this blog entry is in the specified category.
   *
   * @param category a Category instance
   * @return true if this entry is in the specified category,
   *         false otherwise
   */
  public boolean inCategory(Category category) {
    if (category != null) {
      return categories.contains(category);
    } else {
      return true;
    }
  }

  /**
   * Gets the body of this blog entry.
   *
   * @return the body as a String
   */
  public String getBody() {
    return body;
  }

  /**
   * Gets the excerpt of this blog entry.
   *
   * @return the excerpt as a String
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * Gets an excerpt of this blog entry, by truncating the body to a maximum
   * of 255 characters.
   *
   * @return the first 255 characters of the body, chopped to 252 + ...
   *         if the length of the body is greater then 255
   */
  public String getExcerptFromBody() {
    if (body != null && body.length() > 255) {
      return body.substring(0, 252) + "...";
    } else {
      return body;
    }
  }

  /**
   * Sets the excerpt of this blog entry.
   *
   * @param excerpt   the excerpt as a String
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  /**
   * Sets the body of this blog entry.
   *
   * @param body the body as a String
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Gets the date that this blog entry was created.
   *
   * @return a java.util.Date instance
   */
  public Date getDate() {
    return date;
  }

  /**
   * Gets the date that this blog entry was last updated.
   *
   * @return  a Date instance representing the time of the last comment/TrackBack
   */
  public Date getLastModified() {
    Date date = getDate();

    Iterator it = comments.iterator();
    while (it.hasNext()) {
      Comment comment = (Comment)it.next();
      if (comment.getDate().after(date)) {
        date = comment.getDate();
      }
    }

    it = trackBacks.iterator();
    while (it.hasNext()) {
      TrackBack trackBack = (TrackBack)it.next();
      if (trackBack.getDate().after(date)) {
        date = trackBack.getDate();
      }
    }

    return date;
  }

  /**
   * Sets the date that this blog entry was created.
   *
   * @param date a java.util.Date instance
   */
  public void setDate(Date date) {

    if (this.date != null) {
      throw new RuntimeException("The date on this blog entry has already been set and cannot be reset.");
    }

    Calendar cal = dailyBlog.getRootBlog().getCalendar();
    cal.setTime(date);
    this.date = cal.getTime();
    this.id = "" + this.date.getTime();
  }

  /**
   * Gets the timezone of this blog entry.
   *
   * @return  a TimeZone instance of this entry, or the timezone instance
   *          of the owning blog as a default
   */
  public TimeZone getTimeZone() {
    if (this.timeZone != null) {
      return this.timeZone;
    } else {
      return getRootBlog().getTimeZone();
    }
  }

  /**
   * Sets the timezone of this blog entry.
   *
   * @param timeZone    a TimeZone instance
   */
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * Gets the author of this blog entry.
   *
   * @return the author as a String
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Sets the author of this blog entry.
   *
   * @param author the author as a String
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Determines whether this blog entry has been aggregated from another
   * source. An aggregated blog entry will have a specified permalink.
   *
   * @return true if this blog entry has been aggegrated, false otherwise
   */
  public boolean isAggregated() {
    return (originalPermalink != null);
  }

  /**
   * Gets the alternative permalink for this blog entry.
   *
   * @return an absolute URL as a String
   */
  public String getOriginalPermalink() {
    return this.originalPermalink;
  }

  /**
   * Sets the alternative permalink for this blog entry.
   *
   * @param permalink an absolute URL as a String
   */
  public void setOriginalPermalink(String permalink) {
    if (permalink == null || permalink.length() == 0) {
      this.originalPermalink = null;
    } else {
      this.originalPermalink = permalink;
    }
  }

  /**
   * Gets a permalink for this blog entry.
   *
   * @return an absolute URL as a String
   */
  public String getPermalink() {
    if (isAggregated()) {
      return getOriginalPermalink();
    } else {
      return getLocalPermalink();
    }
  }

  /**
   * Gets a short permalink for this blog entry.
   *
   * @return an absolute URL as a String
   */
  public String getShortPermalink() {
    return getRootBlog().getUrl() + id + ".html";
  }

  /**
   * Gets a permalink for this blog entry that is local to the blog. In other
   * words, it doesn't take into account the original permalink for
   * aggregated content.
   *
   * @return an absolute URL as a String
   */
  public String getLocalPermalink() {
    if (type == STATIC_PAGE) {
      return getRootBlog().getUrl() + "pages/" + staticName + ".html";
    } else {
      return getDefaultPermalink();
    }
  }

  /**
   * Gets a permalink for this blog entry.
   *
   * @return an absolute URL as a String
   */
  private String getDefaultPermalink() {
    DecimalFormat format = new DecimalFormat("00");
    int year = getDailyBlog().getMonthlyBlog().getYearlyBlog().getYear();
    int month = getDailyBlog().getMonthlyBlog().getMonth();
    int day = getDailyBlog().getDay();

    return getRootBlog().getUrl() + year + "/" + format.format(month) + "/" +
        format.format(day) + "/" + id + ".html";
  }

  private String staticName;

  public void setStaticName(String staticName) {
    this.staticName = staticName;
  }

  public String getStaticName() {
    return this.staticName;
  }

  /**
   * Determines whether comments are enabled for this blog entry.
   *
   * @return true if comments are enabled, false otherwise
   */
  public boolean isCommentsEnabled() {
    return this.commentsEnabled;
  }

  /**
   * Sets whether comments are enabled for this blog entry.
   *
   * @param commentsEnabled true if comments should be enabled,
   *                        false otherwise
   */
  public void setCommentsEnabled(boolean commentsEnabled) {
    this.commentsEnabled = commentsEnabled;
  }

  /**
   * Gets a link to the comments for this blog entry.
   *
   * @return an absolute URL as a String
   */
  public String getCommentsLink() {
    return getRootBlog().getUrl() + "viewComments.action?entry=" + id;
  }

  /**
   * Determines whether TrackBacks are enabled for this blog entry.
   *
   * @return true if TrackBacks are enabled, false otherwise
   */
  public boolean isTrackBacksEnabled() {
    return this.trackBacksEnabled;
  }

  /**
   * Sets whether TrackBacks are enabled for this blog entry.
   *
   * @param trackBacksEnabled true if TrackBacks should be enabled,
   *                          false otherwise
   */
  public void setTrackBacksEnabled(boolean trackBacksEnabled) {
    this.trackBacksEnabled = trackBacksEnabled;
  }

  /**
   * Gets a link to the trackbacks for this blog entry.
   *
   * @return an absolute URL as a String
   */
  public String getTrackBacksLink() {
    return getRootBlog().getUrl() + "viewTrackBacks.action?entry=" + id;
  }

  /**
   * Helper method to get the owning Blog instance.
   *
   * @return the overall owning Blog instance
   */
  public SimpleBlog getRootBlog() {
    return dailyBlog.getRootBlog();
  }

  /**
   * Gets the owning daily blog instance.
   *
   * @return a DailyBlog instance
   */
  public DailyBlog getDailyBlog() {
    return dailyBlog;
  }

  /**
   * Sets the owning daily blog.
   *
   * @param blog a DailyBlog instance
   */
  void setDailyBlog(DailyBlog blog) {
    this.dailyBlog = blog;
  }

  /**
   * Gets a collection of all comments.
   *
   * @return a List of Comment instances
   */
  public List getComments() {
    List allComments = new ArrayList();
    Iterator it = comments.iterator();
    while (it.hasNext()) {
      allComments.addAll(getComments((Comment)it.next()));
    }

    return allComments;
  }

  private List getComments(Comment comment) {
    List allComments = new ArrayList();
    allComments.add(comment);
    Iterator it = comment.getComments().iterator();
    while (it.hasNext()) {
      allComments.addAll(getComments((Comment)it.next()));
    }

    return allComments;
  }

  /**
   * Gets the number of comments that have been left for this blog entry.
   *
   * @return the number of comments as a int
   */
  public int getNumberOfComments() {
    return getComments().size();
  }

  /**
   * Gets a collection of all trackbacks.
   *
   * @return a List of TrackBack instances
   */
  public List getTrackBacks() {
    return Collections.unmodifiableList(trackBacks);
  }

  /**
   * Gets the number of trackbacks that have been left for this blog entry.
   *
   * @return the number of trackbacks as a int
   */
  public int getNumberOfTrackBacks() {
    return trackBacks.size();
  }

  /**
   * Creates a new comment for this blog entry. This method doesn't actually
   * <b>add</b> the comment too.
   *
   * @param title     the title of the comment
   * @param body      the body of the comment
   * @param author    the author of the comment
   * @param email     the author's e-mail address
   * @param website   the author's website
   * @param ipAddress the IP address of the author
   * @param date      the date that the comment was created
   * @return a new Comment instance with the specified properties
   */
  public Comment createComment(String title, String body, String author, String email, String website, String ipAddress, Date date) {
    return new Comment(title, body, author, email, website, ipAddress, date, this);
  }

  /**
   * Creates a new comment for this blog entry, with a creation date of now.
   * This method doesn't actually <b>add</b> the comment too.
   *
   * @param title   the title of the comment
   * @param body    the body of the comment
   * @param author  the author of the comment
   * @param email   the author's e-mail address
   * @param website the author's website
   * @param ipAddress the IP address of the author
   * @return a new Comment instance with the specified properties
   */
  public Comment createComment(String title, String body, String author, String email, String website, String ipAddress) {
    Calendar cal = getRootBlog().getCalendar();
    return createComment(title, body, author, email, website, ipAddress, cal.getTime());
  }

  /**
   * Adds the specified comment.
   *
   * @param comment a Comment instance
   */
  public synchronized void addComment(Comment comment) {
    if (comment == null || comments.contains(comment)) {
      return;
    }

    if (comment.getParent() != null) {
      comment.getParent().addComment(comment);
    } else {
      comments.add(comment);
    }

    getRootBlog().commentAdded(comment);
  }

  /**
   * Creates a new trackback for this blog entry. This method doesn't actually
   * <b>add</b> the trackback too.
   *
   * @param title    the title of the entry
   * @param excerpt  the excerpt of the entry
   * @param url      the url (permalink) of the entry
   * @param blogName the name of the blog
   * @param ipAddress   the IP address of the author
   * @param date     the date the trackback was received
   * @return a new TrackBack instance with the specified properties
   */
  public TrackBack createTrackBack(String title, String excerpt, String url, String blogName, String ipAddress, Date date) {
    return new TrackBack(title, excerpt, url, blogName, ipAddress, date, this);
  }

  /**
   * Creates a new trackback for this blog entry with a date of now.
   * This method doesn't actually <b>add</b> the trackback too.
   *
   * @param title       the title of the entry
   * @param excerpt     the excerpt of the entry
   * @param url         the url (permalink) of the entry
   * @param blogName    the name of the blog
   * @param ipAddress   the IP address of the author
   * @return a new Comment instance with the specified properties
   */
  public TrackBack createTrackBack(String title, String excerpt, String url, String blogName, String ipAddress) {
    Calendar cal = getRootBlog().getCalendar();
    return createTrackBack(title, excerpt, url, blogName, ipAddress, cal.getTime());
  }

  /**
   * Adds the specified trackback.
   *
   * @param trackBack a TrackBack instance
   */
  public synchronized void addTrackBack(TrackBack trackBack) {
    if (trackBack == null || trackBacks.contains(trackBack)) {
      return;
    }

    trackBacks.add(trackBack);

    getRootBlog().trackBackAdded(trackBack);
  }

  /**
   * Removes the specified comment.
   *
   * @param id    the id of the comment to be removed
   */
  public synchronized void removeComment(long id) {
    Comment comment = getComment(id);
    if (comment != null) {

      if (comment.getParent() != null) {
        comment.getParent().removeComment(comment);
      } else {
        comments.remove(comment);
      }

      getRootBlog().commentRemoved(comment);
    } else {
      log.warn("A comment with id=" + id + " could not be found - " +
        "perhaps it has been removed already.");
    }
  }

  /**
   * Gets the specified comment.
   *
   * @param id    the id of the comment
   */
  public Comment getComment(long id) {
    Iterator it = getComments().iterator();
    while (it.hasNext()) {
      Comment comment = (Comment) it.next();
      if (comment.getId() == id) {
        return comment;
      }
    }

    return null;
  }

  /**
   * Gets the specified TrackBack.
   *
   * @param id    the id of the TrackBack
   */
  public TrackBack getTrackBack(long id) {
    Iterator it = getTrackBacks().iterator();
    while (it.hasNext()) {
      TrackBack trackBack = (TrackBack)it.next();
      if (trackBack.getId() == id) {
        return trackBack;
      }
    }

    return null;
  }

  /**
   * Removes the specified TrackBack.
   *
   * @param id    the id of the TrackBack to be removed
   */
  public synchronized void removeTrackBack(long id) {
    TrackBack trackBack = getTrackBack(id);
    if (trackBack != null) {
      trackBacks.remove(trackBack);

      getRootBlog().trackBackRemoved(trackBack);
    } else {
      log.warn("A TrackBack with id=" + id + " could not be found - " +
          "perhaps it has been removed already.");
    }
  }

  public int getType() {
    return this.type;
  }

  public void setType(int type) {
    this.type = type;
  }

  /**
   * Stores this BlogEntry.
   *
   * @throws BlogException if the blog entry cannot be stored
   */
  public synchronized void store() throws BlogException {
    try {
      log.debug("Storing " + getTitle() + " (" + getId() + ")");
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      BlogEntryDAO dao = factory.getBlogEntryDAO();
      dao.store(this);

      if (type == PUBLISHED || type == STATIC_PAGE) {
        // and finally index the published entry
        BlogIndexer indexer = new BlogIndexer();
        indexer.index(this);

        getRootBlog().getStaticPageIndex().reindex();
      }
    } catch (PersistenceException pe) {
      throw new BlogException(pe.getMessage());
    }
  }

  /**
   * Removes this blog entry from the filing system.
   *
   * @throws BlogException if the file backing this blog entry
   *                       cannot be deleted
   */
  public synchronized void remove() throws BlogException {
    try {
      log.debug("Removing " + getTitle() + " (" + getId() + ")");
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      BlogEntryDAO dao = factory.getBlogEntryDAO();
      dao.remove(this);

      if (type == PUBLISHED || type == STATIC_PAGE) {
        // and finally un-index the published entry
        BlogIndexer indexer = new BlogIndexer();
        indexer.removeIndex(this);

        getRootBlog().getStaticPageIndex().reindex();
      }
    } catch (PersistenceException pe) {
      throw new BlogException(pe.getMessage());
    }
  }

  public void validate(ValidationContext context) {
    if (isStaticPage()) {
      if (staticName == null || staticName.length() == 0) {
        context.addError("Name cannot be empty");
      } else if (!staticName.matches("[\\w_/-]+")) {
        context.addError("Name \"" + staticName + "\" must contain only A-Za-z0-9_-/");
      }

      BlogEntry page = getRootBlog().getStaticPageIndex().getStaticPage(staticName);
      if (page != null && !page.equals(this)) {
        context.addError("A page with the name \"" + staticName + "\" already exists");
      }
    }
  }

  /**
   * Returns the blog entry that was posted before this one.
   *
   * @return  a BlogEntry instance, or null if this is the first entry
   */
  public BlogEntry getPreviousBlogEntry() {
    return getRootBlog().getPreviousBlogEntry(this);
  }

  /**
   * Returns the blog entry that was posted after this one.
   *
   * @return  a BlogEntry instance, or null is this is the last entry
   */
  public BlogEntry getNextBlogEntry() {
    return getRootBlog().getNextBlogEntry(this);
  }

  /**
   * Determines whether this blog entry in fact represents a static page.
   *
   * @return  true if the type is STATIC_PAGE, false otherwise
   */
  public boolean isStaticPage() {
      return type == STATIC_PAGE;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param o   the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj
   *         argument; <code>false</code> otherwise.
   * @see #hashCode()
   * @see java.util.Hashtable
   */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof BlogEntry)) {
      return false;
    }

    BlogEntry blogEntry = (BlogEntry)o;
    return id.equals(blogEntry.getId());
  }

  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Gets a string representation of this object.
   *
   * @return  a String
   */
  public String toString() {
    return this.title;
  }

  /**
   * Creates and returns a copy of this object.
   *
   * @return a clone of this instance.
   * @see Cloneable
   */
  public Object clone() {
    BlogEntry entry = new BlogEntry(dailyBlog);
    entry.setTitle(title);
    entry.setExcerpt(excerpt);
    entry.setBody(body);
    entry.setDate(date);
    entry.setAuthor(author);
    entry.setOriginalPermalink(originalPermalink);
    entry.setStaticName(staticName);
    entry.setType(type);
    entry.setTimeZone(timeZone);
    entry.setCommentsEnabled(commentsEnabled);
    entry.setTrackBacksEnabled(trackBacksEnabled);

    // copy the categories
    Iterator it = categories.iterator();
    while (it.hasNext()) {
      entry.addCategory((Category)it.next());
    }

    // and also copy the comments
    it = comments.iterator();
    while (it.hasNext()) {
      entry.comments.add((Comment)((Comment)it.next()).clone());
    }

    // and TrackBacks
    it = trackBacks.iterator();
    while (it.hasNext()) {
      entry.trackBacks.add((TrackBack)((TrackBack)it.next()).clone());
    }

    return entry;
  }

}
