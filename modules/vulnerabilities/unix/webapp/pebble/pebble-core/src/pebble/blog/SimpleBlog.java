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

import java.io.File;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import pebble.Constants;
import pebble.blog.persistence.BlogEntryDAO;
import pebble.blog.persistence.DAOFactory;
import pebble.blog.persistence.PersistenceException;
import pebble.comparator.BlogEntryByTitleComparator;
import pebble.comparator.BlogEntryResponseByDateComparator;
import pebble.logging.AbstractLogger;
import pebble.logging.CombinedLogFormatLogger;
import pebble.decorator.BlogEntryDecoratorManager;

public class SimpleBlog extends Blog {

  public static final String EMAIL_KEY = "email";
  public static final String SMTP_HOST_KEY = "smtpHost";
  public static final String BLOG_ENTRY_NOTIFICATION_KEY = "blogEntryNotification";
  public static final String COMMENT_NOTIFICATION_KEY = "commentNotification";
  public static final String THEME_KEY = "theme";
  public static final String UPDATE_NOTIFICATION_PINGS_ENABLED_KEY = "updateNotificationPingsEnabled";
  public static final String UPDATE_NOTIFICATION_PINGS_KEY = "updateNotificationPings";
  public static final String XML_RPC_ENABLED_KEY = "xmlRpcEnabled";
  public static final String XML_RPC_USERNAME_KEY = "xmlRpcUsername";
  public static final String XML_RPC_PASSWORD_KEY = "xmlRpcPassword";
  public static final String BLOG_OWNERS_KEY = "blogOwners";
  public static final String BLOG_CONTRIBUTORS_KEY = "blogContributors";
  public static final String PRIVATE_KEY = "private";
  public static final String LUCENE_ANALYZER_KEY = "luceneAnalyzer";
  public static final String BLOG_ENTRY_DECORATORS_KEY = "blogEntryDecorators";

  /** the ID of this blog */
  private String id = "blog";

  /** the collection of YearlyBlog instance that this root blog is managing */
  private List yearlyBlogs;

  /** the set of recent comments */
  private SortedSet recentComments;

  /** the set of recent TrackBacks */
  private SortedSet recentTrackBacks;

  /** the set of recent responses (comments and TrackBacks) */
  private SortedSet recentResponses;

  /** the category manager associated with this blog */
  private CategoryManager blogCategoryManager;

  /** the referer filter associated with this blog */
  private RefererFilterManager refererFilterManager;

  /** the story index associated with this blog */
  private StaticPageIndex staticPageIndex;

  /** the editable theme belonging to this blog */
  private Theme editableTheme;

  /** the log used to log referers, requests, etc */
  private CombinedLogFormatLogger logger;

  /** the decorator manager associated with this blog */
  private BlogEntryDecoratorManager blogEntryDecoratorManager;

  /**
   * Creates a new Blog instance, based at the specified location.
   *
   * @param root    an absolute path pointing to the root directory of the blog
   */
  public SimpleBlog(String root) {
    super(root);
  }

  protected void init() {
    super.init();

    blogCategoryManager = new CategoryManager(this);
    refererFilterManager = new RefererFilterManager(this);
    recentComments = new TreeSet(new BlogEntryResponseByDateComparator());
    recentTrackBacks = new TreeSet(new BlogEntryResponseByDateComparator());
    recentResponses = new TreeSet(new BlogEntryResponseByDateComparator());
    logger = new CombinedLogFormatLogger(this);
    blogEntryDecoratorManager = new BlogEntryDecoratorManager(this, getBlogEntryDecorators());

    try {
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      BlogEntryDAO dao = factory.getBlogEntryDAO();
      yearlyBlogs = dao.getYearlyBlogs(this);
    } catch (PersistenceException pe) {
      pe.printStackTrace();
    }

    staticPageIndex = new StaticPageIndex(this);

    File imagesDirectory = new File(getImagesDirectory());
    if (!imagesDirectory.exists()) {
      imagesDirectory.mkdir();
    }

    File filesDirectory = new File(getFilesDirectory());
    if (!filesDirectory.exists()) {
      filesDirectory.mkdir();
    }

    File logDirectory = new File(getLogsDirectory());
    if (!logDirectory.exists()) {
      logDirectory.mkdir();
    }
  }

  /**
   * Initialises the list of recent comments so that they can be displayed
   * on the home page.
   */
  private void initRecentComments() {
    if (getRecentCommentsOnHomePage() > 0) {
      loadAllBlogEntries();
    }
  }

  /**
   * Utility method to load all blog entries. This is called so that recent
   * comments and TrackBacks can be registered and displayed on the home page.
   */
  private void loadAllBlogEntries() {
    Thread t = new Thread("pebble-loadblog") {
      public void run() {
        for (int year = yearlyBlogs.size()-1; year >= 0; year--) {
          YearlyBlog yearlyBlog = (YearlyBlog)yearlyBlogs.get(year);
          MonthlyBlog[] months = yearlyBlog.getMonthlyBlogs();
          for (int month = 11; month >= 0; month--) {
            months[month].getAllDailyBlogs();
          }
        }
      }
    };

    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
  }

  /**
   * Gets the default properties for a SimpleBlog.
   *
   * @return    a Properties instance
   */
  protected Properties getDefaultProperties() {
    Properties defaultProperties = new Properties();
    defaultProperties.setProperty(NAME_KEY, "My blog");
    defaultProperties.setProperty(DESCRIPTION_KEY, "My blog description");
    defaultProperties.setProperty(IMAGE_KEY, "http://pebble.sourceforge.net/common/images/powered-by-pebble.gif");
    defaultProperties.setProperty(AUTHOR_KEY, "Blog Owner");
    defaultProperties.setProperty(EMAIL_KEY, "blog@yourdomain.com");
    defaultProperties.setProperty(TIMEZONE_KEY, "Europe/London");
    defaultProperties.setProperty(LANGUAGE_KEY, "en");
    defaultProperties.setProperty(COUNTRY_KEY, "GB");
    defaultProperties.setProperty(CHARACTER_ENCODING_KEY, "UTF-8");
    defaultProperties.setProperty(RECENT_BLOG_ENTRIES_ON_HOME_PAGE_KEY, "3");
    defaultProperties.setProperty(RECENT_COMMENTS_ON_HOME_PAGE_KEY, "0");
    defaultProperties.setProperty(COMMENT_NOTIFICATION_KEY, FALSE);
    defaultProperties.setProperty(BLOG_ENTRY_NOTIFICATION_KEY, FALSE);
    defaultProperties.setProperty(UPDATE_NOTIFICATION_PINGS_ENABLED_KEY, FALSE);
    defaultProperties.setProperty(UPDATE_NOTIFICATION_PINGS_KEY, "");
    defaultProperties.setProperty(XML_RPC_ENABLED_KEY, FALSE);
    defaultProperties.setProperty(XML_RPC_USERNAME_KEY, "");
    defaultProperties.setProperty(XML_RPC_PASSWORD_KEY, "");
    defaultProperties.setProperty(THEME_KEY, "default");
    defaultProperties.setProperty(PRIVATE_KEY, FALSE);
    defaultProperties.setProperty(LUCENE_ANALYZER_KEY, "org.apache.lucene.analysis.standard.StandardAnalyzer");
    defaultProperties.setProperty(BLOG_ENTRY_DECORATORS_KEY, "pebble.decorator.HtmlDecorator\r\npebble.decorator.EscapeMarkupDecorator\r\npebble.decorator.RelativeUriDecorator");

    return defaultProperties;
  }

  /**
   * Gets the ID of this blog.
   *
   * @return  the ID as a String
   */
  public String getId() {
    return this.id;
  }

  /**
   * Sets the ID of this blog.
   *
   * @param id    the ID as a String
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the theme being used.
   *
   * @return  a theme name as a String
   */
  public String getTheme() {
    return properties.getProperty(THEME_KEY);
  }

  /**
   * Gets the e-mail address of the blog owner.
   *
   * @return    the e-mail address
   */
  public String getEmail() {
    return properties.getProperty(EMAIL_KEY);
  }

  /**
   * Gets a Collection of e-mail addresses.
   *
   * @return  a Collection of String instances
   */
  public Collection getEmailAddresses() {
    return Arrays.asList(getEmail().split(","));
  }

  /**
   * Gets the first of multiple e-mail addresses.
   *
   * @return  the firt e-mail address as a String
   */
  public String getFirstEmailAddress() {
    Collection emailAddresses = getEmailAddresses();
    if (emailAddresses != null && !emailAddresses.isEmpty()) {
      return (String)emailAddresses.iterator().next();
    } else {
      return "";
    }
  }

  /**
   * Determines whether comment notification is enabled.
   *
   * @return  true if notification is enabled, false otherwise
   */
  public boolean isCommentNotificationEnabled() {
    return properties.getProperty(COMMENT_NOTIFICATION_KEY).equalsIgnoreCase(TRUE);
  }

  /**
   * Determines whether blog entry notification is enabled.
   *
   * @return  true if notification is enabled, false otherwise
   */
  public boolean isBlogEntryNotificationEnabled() {
    return properties.getProperty(BLOG_ENTRY_NOTIFICATION_KEY).equalsIgnoreCase(TRUE);
  }

  /**
   * Gets the name of the SMTP (mail) host.
   *
   * @return    the SMTP host
   */
  public String getSmtpHost() {
    return properties.getProperty(SMTP_HOST_KEY);
  }

  /**
   * Determines whether websites should be sent a notification when this
   * blog has been updated.
   *
   * @return  true if update pings should be sent, false otherwise
   */
  public boolean isUpdateNotificationPingsEnabled() {
    return properties.getProperty(UPDATE_NOTIFICATION_PINGS_ENABLED_KEY).equalsIgnoreCase(TRUE);
  }

  /**
   * Gets the list of websites that should be pinged when this
   * blog is updated.
   *
   * @return  a comma separated list of website addresses
   */
  public String getUpdateNotificationPings() {
    return properties.getProperty(UPDATE_NOTIFICATION_PINGS_KEY);
  }

  /**
   * Gets the collection of websites that should be pinged when this
   * blog is updated.
   *
   * @return  a Collection of Strings representing website addresses
   */
  public Collection getUpdateNotificationPingsAsCollection() {
    Set websites = new HashSet();
    String notificationPings = getUpdateNotificationPings();
    if (notificationPings != null && notificationPings.length() > 0) {
      String s[] = notificationPings.split("\\s+");
      for (int i = 0; i < s.length; i++) {
        websites.add(s[i]);
      }
    }

    return websites;
  }

  /**
   * Gets the XML-RPC username.
   *
   * @return    the username as a String
   */
  public String getXmlRpcUsername() {
    return properties.getProperty(XML_RPC_USERNAME_KEY);
  }

  /**
   * Gets the XML-RPC password.
   *
   * @return    the password as a String
   */
  public String getXmlRpcPassword() {
    return properties.getProperty(XML_RPC_PASSWORD_KEY);
  }

  /**
   * Determines whether XML-RPC requests are enabled for this blog.
   *
   * @return  true if XML-RPC requests are enabled, false otherwise
   */
  public boolean isXmlRpcEnabled() {
    return properties.getProperty(XML_RPC_ENABLED_KEY).equalsIgnoreCase(TRUE);
  }

  /**
   * Gets a comma separated list of the users that are blog owners
   * for this blog.
   *
   * @return  a String containng a comma separated list of user names
   */
  public String getBlogOwners() {
    return properties.getProperty(BLOG_OWNERS_KEY);
  }

  /**
   * Gets a comma separated list of the users that are blog contributors
   * for this blog.
   *
   * @return  a String containng a comma separated list of user names
   */
  public String getBlogContributors() {
    return properties.getProperty(BLOG_CONTRIBUTORS_KEY);
  }

  /**
   * Gets the name of the Lucene analyzer to use.
   *
   * @return  a fully qualified class name
   */
  public String getLuceneAnalyzer() {
    return properties.getProperty(LUCENE_ANALYZER_KEY);
  }

  /**
   * Gets a Collection containing the names of users that are blog owners
   * for this blog.
   *
   * @return  a Collection containng user names as Strings
   */
  public Collection getUsersInRole(String roleName) {
    Set users = new HashSet();
    String commaSeparatedUsers = null;
    StringTokenizer tok = null;

    if (roleName.equals(Constants.BLOG_OWNER_ROLE)) {
      commaSeparatedUsers = properties.getProperty(BLOG_OWNERS_KEY);
    } else if (roleName.equals(Constants.BLOG_CONTRIBUTOR_ROLE)) {
      commaSeparatedUsers = properties.getProperty(BLOG_CONTRIBUTORS_KEY);
    }

    if (commaSeparatedUsers != null) {
      tok = new StringTokenizer(commaSeparatedUsers, ",");
      while (tok.hasMoreTokens()) {
        users.add(tok.nextToken());
      }
    }

    return users;
  }

  /**
   * Determines whether the specified user is in the specified role.
   *
   * @param roleName    the name of the role
   * @param user        the name of the user
   * @return  true if the user is a member of the role or the list of users
   *          is empty, false otherwise
   */
  public boolean isUserInRole(String roleName, String user) {
    Collection users = getUsersInRole(roleName);
    if (users.isEmpty() || users.contains(user)) {
      return true;
    }

    return false;
  }

  /**
   * Gets the YearlyBlog instance for the specified year.
   *
   * @param year    the year as an int (e.g. 2003)
   * @return    a YearlyBlog instance
   */
  public YearlyBlog getBlogForYear(int year) {
    Iterator it = yearlyBlogs.iterator();
    YearlyBlog yearlyBlog;
    while (it.hasNext()) {
      yearlyBlog = (YearlyBlog)it.next();
      if (yearlyBlog.getYear() == year) {
        return yearlyBlog;
      }
    }

    yearlyBlog = new YearlyBlog(this, year);
    yearlyBlogs.add(yearlyBlog);
    return yearlyBlog;
  }

  /**
   * Given a YearlyBlog, this method returns the YearlyBlog instance
   * representing the previous year.
   *
   * @param yearlyBlog    a YearlyBlog instance
   * @return    a YearlyBlog representing the previous year
   */
  public YearlyBlog getBlogForPreviousYear(YearlyBlog yearlyBlog) {
    return getBlogForYear(yearlyBlog.getYear() - 1);
  }

  /**
   * Given a YearlyBlog, this method returns the YearlyBlog instance
   * representing the next year.
   *
   * @param yearlyBlog    a YearlyBlog instance
   * @return    a YearlyBlog representing the next year
   */
  public YearlyBlog getBlogForNextYear(YearlyBlog yearlyBlog) {
    return getBlogForYear(yearlyBlog.getYear() + 1);
  }

  /**
   * Gets the YearlyBlog instance representing this year.
   *
   * @return  a YearlyBlog instance for this year
   */
  public YearlyBlog getBlogForThisYear() {
    Calendar cal = getCalendar();
    return getBlogForYear(cal.get(Calendar.YEAR));
  }

  /**
   * Gets all YearlyBlogs managed by this root blog.
   *
   * @return  a Collection of YearlyBlog instances
   */
  public List getYearlyBlogs() {
    return yearlyBlogs;
  }

  /**
   * Gets the MonthlyBlog instance representing the first month that
   * contains blog entries.
   *
   * @return  a MonthlyBlog instance
   */
  public MonthlyBlog getBlogForFirstMonth() {
    YearlyBlog year;

    if (!yearlyBlogs.isEmpty()) {
      year = (YearlyBlog)yearlyBlogs.get(0);
    } else {
      year = getBlogForYear(getCalendar().get(Calendar.YEAR));
    }

    return year.getBlogForFirstMonth();
  }

  /**
   * Gets a MonthlyBlog intance for the specified year and month.
   *
   * @param year    the year as an int
   * @param month   the month as an int
   * @return    a MonthlyBlog instance representing the specified year and month
   */
  public MonthlyBlog getBlogForMonth(int year, int month) {
    return getBlogForYear(year).getBlogForMonth(month);
  }

  /**
   * Gets the MonthlyBlog instance representing this month.
   *
   * @return  a MonthlyBlog instance for this month
   */
  public MonthlyBlog getBlogForThisMonth() {
    Calendar cal = getCalendar();
    return getBlogForMonth(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1));
  }

  /**
   * Gets a DailyBlog intance for the specified year, month and day.
   *
   * @param year    the year as an int
   * @param month   the month as an int
   * @param day     the day as an int
   * @return    a DailyBlog instance representing the specified year, month and day
   */
  public DailyBlog getBlogForDay(int year, int month, int day) {
    return getBlogForMonth(year, month).getBlogForDay(day);
  }

  /**
   * Gets a DailyBlog intance for the specified Date.
   *
   * @param date    a java.util.Date instance
   * @return    a DailyBlog instance representing the specified Date
   */
  public DailyBlog getBlogForDay(Date date) {
    Calendar cal = getCalendar();
    cal.setTime(date);

    int year = cal.get(Calendar.YEAR);
    int month = (cal.get(Calendar.MONTH) + 1);
    int day = cal.get(Calendar.DAY_OF_MONTH);

    return getBlogForDay(year, month, day);
  }

  /**
   * Gets the DailyBlog instance for today.
   *
   * @return    a DailyBlog instance
   */
  public DailyBlog getBlogForToday() {
    return this.getBlogForDay(getCalendar().getTime());
  }

  /**
   * Gets all blog entries for this blog.
   *
   * @return  a List of BlogEntry objects
   */
  public List getBlogEntries() {
    List blogEntries = new ArrayList();
    for (int year = yearlyBlogs.size()-1; year >= 0; year--) {
      YearlyBlog yearlyBlog = (YearlyBlog)yearlyBlogs.get(year);
      MonthlyBlog[] months = yearlyBlog.getMonthlyBlogs();
      for (int month = 11; month >= 0; month--) {
        blogEntries.addAll(months[month].getBlogEntries());
      }
    }

    return blogEntries;
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
    Calendar cal = getCalendar();

    List entries = new ArrayList();
    int count = 0;
    // the count < (28*6) (6 months-ish) is here so that we don't go infinitely back in time
    // if enough entries cannot be found
    while ((entries.size() < numberOfEntries) && (count < (28 * 6))) {
      DailyBlog blog = getBlogForDay(cal.getTime());
      if (blog.hasEntries()) {
        Iterator it = blog.getEntries(category).iterator();
        while ((entries.size() < numberOfEntries) && it.hasNext()) {
          entries.add(it.next());
        }
      }

      count++;
      cal.add(Calendar.DAY_OF_YEAR, -1);
    }

    return entries;
  }

  /**
   * Gets recent comments.
   *
   * @return  a collection containing comments that have been left most recently
   */
  public Collection getRecentComments() {
    return this.recentComments;
  }

  /**
   * Gets recent TrackBacks.
   *
   * @return  a collection containing TrackBacks that have been left most recently
   */
  public Collection getRecentTrackBacks() {
    return this.recentTrackBacks;
  }

  /**
   * Gets recent responses (combined comments and TrackBacks).
   *
   * @return  a collection containing comments and TrackBacks that have been left
   *          most recently
   */
  public Collection getRecentResponses() {
    return this.recentResponses;
  }

  /**
   * Gets the date that this blog was last updated.
   *
   * @return  a Date instance representing the time of the most recent entry
   */
  public Date getLastModified() {
    Date date = new Date(0);
    List blogEntries = getRecentBlogEntries();
    Iterator it = blogEntries.iterator();
    while (it.hasNext()) {
      BlogEntry blogEntry = (BlogEntry)it.next();
      if (blogEntry.getDate().after(date)) {
        date = blogEntry.getDate();
      }
    }

    return date;
  }

  /**
   * Gets the blog entry with the specified id.
   *
   * @param entryId   the id of the blog entry
   * @return  a BlogEntry instance, or null if the entry couldn't be found
   */
  public BlogEntry getBlogEntry(String entryId) {
    if (entryId == null || entryId.length() == 0) {
      return null;
    }

    Calendar cal = getCalendar();
    cal.setTime(new Date(Long.parseLong(entryId)));

    int year = cal.get(Calendar.YEAR);
    int month = (cal.get(Calendar.MONTH) + 1);
    int day = cal.get(Calendar.DAY_OF_MONTH);

    DailyBlog blog = getBlogForDay(year, month, day);
    return blog.getEntry(entryId);
  }

  public BlogEntry getPreviousBlogEntry(BlogEntry blogEntry) {
    BlogEntry previous = blogEntry.getDailyBlog().getPreviousBlogEntry(blogEntry);
    if (previous != null) {
      return previous;
    }

    DailyBlog dailyBlog = blogEntry.getDailyBlog();
    DailyBlog firstDailyBlog = getBlogForFirstMonth().getBlogForFirstDay();
    while (dailyBlog != firstDailyBlog && previous == null) {
      dailyBlog = dailyBlog.getPreviousDay();
      previous = dailyBlog.getLastBlogEntry();
    }

    return previous;
  }

  public BlogEntry getNextBlogEntry(BlogEntry blogEntry) {
    BlogEntry next = blogEntry.getDailyBlog().getNextBlogEntry(blogEntry);
    if (next != null) {
      return next;
    }

    DailyBlog dailyBlog = blogEntry.getDailyBlog();
    DailyBlog lastDailyBlog = getBlogForToday();
    while (dailyBlog != lastDailyBlog && next == null) {
      dailyBlog = dailyBlog.getNextDay();
      next = dailyBlog.getFirstBlogEntry();
    }

    return next;
  }

  /**
   * Gets the object managing blog categories.
   *
   * @return  a CategoryManager instance
   */
  public CategoryManager getBlogCategoryManager() {
    return this.blogCategoryManager;
  }

  /**
   * Gets the object managing referer filters.
   *
   * @return  a RefererFilterManager instance
   */
  public RefererFilterManager getRefererFilterManager() {
    return this.refererFilterManager;
  }

  /**
   * Gets the story index.
   *
   * @return  a StaticPageIndex instance
   */
  public StaticPageIndex getStaticPageIndex() {
    return this.staticPageIndex;
  }

  /**
   * Logs this request for blog.
   *
   * @param request   the HttpServletRequest instance for this request
   */
  public synchronized void log(HttpServletRequest request) {
    logger.log(request);
  }

  /**
   * Gets an object representing the editable theme.
   *
   * @return    an EditableTheme instance
   */
  public Theme getEditableTheme() {
    return editableTheme;
  }

  /**
   * Sets an object representing the editable theme.
   *
   * @param editableTheme    an EditableTheme instance
   */
  public void setEditableTheme(Theme editableTheme) {
    this.editableTheme = editableTheme;
  }

  /**
   * Gets the location where the blog files are stored.
   *
   * @return    an absolute, local path on the filing system
   */
  public String getFilesDirectory() {
    return getRoot() + File.separator + "files";
  }

  /**
   * Gets the location where the blog theme is stored.
   *
   * @return    an absolute, local path on the filing system
   */
  public String getThemeDirectory() {
    return getRoot() + File.separator + "theme";
  }

  /**
   * Determines whether this blog is public.
   *
   * @return  true if public, false otherwise
   */
  public boolean isPublic() {
    return properties.getProperty(PRIVATE_KEY).equalsIgnoreCase(FALSE);
  }

  /**
   * Determines whether this blog is private.
   *
   * @return  true if public, false otherwise
   */
  public boolean isPrivate() {
    return properties.getProperty(PRIVATE_KEY).equalsIgnoreCase(TRUE);
  }

  public List getDraftBlogEntries() {
    List blogEntries = new ArrayList();

    try {
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      BlogEntryDAO dao = factory.getBlogEntryDAO();
      blogEntries.addAll(dao.getDraftBlogEntries(this));
    } catch (PersistenceException e) {
      e.printStackTrace();
    }

    Collections.sort(blogEntries, new BlogEntryByTitleComparator());

    return blogEntries;
  }

  /**
   * Gets the list of blog entry templates for this blog.
   *
   * @return  a list of BlogEntry instances
   */
  public List getBlogEntryTemplates() {
    List blogEntries = new ArrayList();

    try {
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      BlogEntryDAO dao = factory.getBlogEntryDAO();
      blogEntries.addAll(dao.getBlogEntryTemplates(this));
    } catch (PersistenceException e) {
      e.printStackTrace();
    }

    Collections.sort(blogEntries, new BlogEntryByTitleComparator());

    return blogEntries;
  }

  /**
   * Gets the list of static pages for this blog.
   *
   * @return  a list of BlogEntry instances
   */
  public List getStaticPages() {
    List blogEntries = new ArrayList();

    try {
      DAOFactory factory = DAOFactory.getConfiguredFactory();
      BlogEntryDAO dao = factory.getBlogEntryDAO();
      blogEntries.addAll(dao.getStaticPages(this));
    } catch (PersistenceException e) {
      e.printStackTrace();
    }

    Collections.sort(blogEntries, new BlogEntryByTitleComparator());

    return blogEntries;
  }

  /**
   * Gets the draft blog entry with the specified id.
   *
   * @param entryId   the id of the blog entry
   * @return  a BlogEntry instance, or null if the entry couldn't be found
   */
  public BlogEntry getDraftBlogEntry(String entryId) {
    if (entryId == null) {
      return null;
    }

    List blogEntries = getDraftBlogEntries();
    Iterator it = blogEntries.iterator();
    while (it.hasNext()) {
      BlogEntry blogEntry = (BlogEntry)it.next();
      if (blogEntry.getId().equals(entryId)) {
        return blogEntry;
      }
    }

    return null;
  }

  /**
   * Gets the blog entry template with the specified id.
   *
   * @param entryId   the id of the blog entry
   * @return  a BlogEntry instance, or null if the entry couldn't be found
   */
  public BlogEntry getBlogEntryTemplate(String entryId) {
    if (entryId == null) {
      return null;
    }

    List blogEntries = getBlogEntryTemplates();
    Iterator it = blogEntries.iterator();
    while (it.hasNext()) {
      BlogEntry blogEntry = (BlogEntry)it.next();
      if (blogEntry.getId().equals(entryId)) {
        return blogEntry;
      }
    }

    return null;
  }

  /**
   * Gets the static page with the specified id.
   *
   * @param entryId   the id of the blog entry
   * @return  a BlogEntry instance, or null if the entry couldn't be found
   */
  public BlogEntry getStaticPage(String entryId) {
    if (entryId == null) {
      return null;
    }

    List blogEntries = getStaticPages();
    Iterator it = blogEntries.iterator();
    while (it.hasNext()) {
      BlogEntry blogEntry = (BlogEntry)it.next();
      if (blogEntry.getId().equals(entryId)) {
        return blogEntry;
      }
    }

    return null;
  }

  /**
   * Called when a comment has been added.
   *
   * @param comment   a Comment instance
   */
  public synchronized void commentAdded(Comment comment) {
    if (getRecentCommentsOnHomePage() > 0) {
      recentComments.add(comment);
      if (recentComments.size() > getRecentCommentsOnHomePage()) {
        recentComments.remove(recentComments.last());
      }
      recentResponses.add(comment);
      if (recentResponses.size() > getRecentCommentsOnHomePage()) {
        recentResponses.remove(recentResponses.last());
      }
    }
  }

  /**
   * Called when a comment has been removed.
   *
   * @param comment   a Comment instance
   */
  public synchronized void commentRemoved(Comment comment) {
    if (getRecentCommentsOnHomePage() > 0) {
      recentComments.remove(comment);
      recentResponses.remove(comment);
    }
  }

  /**
   * Called when a TrackBack has been added.
   *
   * @param trackBack   a TrackBack instance
   */
  public synchronized void trackBackAdded(TrackBack trackBack) {
    if (getRecentCommentsOnHomePage() > 0) {
      recentTrackBacks.add(trackBack);
      if (recentTrackBacks.size() > getRecentCommentsOnHomePage()) {
        recentTrackBacks.remove(recentTrackBacks.last());
      }

      recentResponses.add(trackBack);
      if (recentResponses.size() > getRecentCommentsOnHomePage()) {
        recentResponses.remove(recentResponses.last());
      }
    }
  }

  /**
   * Called when a TrackBack has been removed.
   *
   * @param trackBack   a TrackBack instance
   */
  public synchronized void trackBackRemoved(TrackBack trackBack) {
    if (getRecentCommentsOnHomePage() > 0) {
      recentTrackBacks.remove(trackBack);
      recentResponses.remove(trackBack);
    }
  }

  /**
   * Called to start (i.e. activate/initialise, restore the theme, etc) this
   * blog.
   */
  void start() {
    logger.start();
    editableTheme.restore();
    initRecentComments();
  }

  /**
   * Called to shutdown this blog.
   */
  void stop() {
    logger.stop();
    editableTheme.backup();
  }

  /**
   * Gets the logger associated with this blog.
   *
   * @return    an AbstractLogger implementation
   */
  public AbstractLogger getLogger() {
    return this.logger;
  }

  /**
   * Gets the list of plugins.
   *
   * @return  a comma separated list of class names
   */
  public String getBlogEntryDecorators() {
    return getProperty(BLOG_ENTRY_DECORATORS_KEY);
  }

  /**
   * Gets the decorator manager associated with this blog.
   *
   * @return  a BlogEntryDecoratorManager instance
   */
  public BlogEntryDecoratorManager getBlogEntryDecoratorManager() {
    return this.blogEntryDecoratorManager;
  }

}