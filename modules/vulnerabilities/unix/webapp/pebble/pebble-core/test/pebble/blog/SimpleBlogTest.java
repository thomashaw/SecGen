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
import java.io.File;

import pebble.Constants;

/**
 * Tests for the Blog class.
 *
 * @author    Simon Brown
 */
public class SimpleBlogTest extends SimpleBlogTestCase {

  public void testConstructionOfDefaultInstance() {
    assertEquals(TEST_BLOG_LOCATION.getAbsolutePath(), rootBlog.getRoot());
    assertNull(rootBlog.getRootBlog());
    assertEquals("My blog", rootBlog.getName());
    assertEquals("My blog description", rootBlog.getDescription());
    assertEquals("Blog Owner", rootBlog.getAuthor());
    assertEquals("blog@yourdomain.com", rootBlog.getEmail());
    assertEquals(TimeZone.getTimeZone("Europe/London"), rootBlog.getTimeZone());
    assertEquals("en", rootBlog.getLanguage());
    assertEquals("GB", rootBlog.getCountry());
    assertEquals("UTF-8", rootBlog.getCharacterEncoding());
    assertEquals(3, rootBlog.getRecentBlogEntriesOnHomePage());
    assertEquals(0, rootBlog.getRecentCommentsOnHomePage());
    assertFalse(rootBlog.isCommentNotificationEnabled());
    assertFalse(rootBlog.isBlogEntryNotificationEnabled());
    assertFalse(rootBlog.isUpdateNotificationPingsEnabled());
    assertTrue(rootBlog.getUpdateNotificationPingsAsCollection().isEmpty());
    assertTrue(rootBlog.isPublic());
    assertFalse(rootBlog.isPrivate());
    assertEquals("pebble.decorator.HtmlDecorator\r\npebble.decorator.EscapeMarkupDecorator\r\npebble.decorator.RelativeUriDecorator", rootBlog.getBlogEntryDecorators());
  }

  /**
   * Tests that we can get a specific property.
   */
  public void testGetProperty() {
    assertEquals(rootBlog.getName(), rootBlog.getProperty(Blog.NAME_KEY));
  }

  /**
   * Tests that we can get a specific property.
   */
  public void testGetProperties() {
    Properties props = rootBlog.getProperties();
    assertNotNull(props);
    assertEquals(rootBlog.getName(), props.getProperty(Blog.NAME_KEY));
  }

  /**
   * Tests that we can set a specific property.
   */
  public void testSetProperty() {
    rootBlog.setProperty(Blog.NAME_KEY, "New name");
    assertEquals("New name", rootBlog.getProperty(Blog.NAME_KEY));
    assertEquals("New name", rootBlog.getName());

    // and a new property
    rootBlog.setProperty("aNewPropertyKey", "A new property value");
    assertEquals("A new property value", rootBlog.getProperty("aNewPropertyKey"));
  }

  /**
   * Tests that we can store properties.
   public void testStoreProperties() {
   rootBlog.setProperty("aNewPropertyKey", "A new property value");
   try {
   rootBlog.storeProperties();

   rootBlog = new Blog(TEST_BLOG_LOCATION.getAbsolutePath());
   assertEquals("A new property value", rootBlog.getProperty("aNewPropertyKey"));

   // and clean up
   rootBlog.removeProperty("aNewPropertyKey");
   rootBlog.storeProperties();
   } catch (BlogException e) {
   fail();
   }
   }
   */

  /**
   * Tests that we can remove a specific property.
   */
  public void testRemoveProperty() {
    rootBlog.setProperty("aNewPropertyKey", "A new property value");
    assertEquals("A new property value", rootBlog.getProperty("aNewPropertyKey"));
    rootBlog.removeProperty("aNewPropertyKey");
    assertNull(rootBlog.getProperty("aNewPropertyKey"));
  }

  /**
   * Tests that the correct calendar (with timezone) is created.
   */
  public void testCalendar() {
    Calendar cal = rootBlog.getCalendar();
    assertEquals(rootBlog.getTimeZone(), cal.getTimeZone());
  }

  /**
   * Tests that we can get a specific YearlyBlog instance.
   public void testGetBlogForYear() {
   Calendar cal = rootBlog.getCalendar();
   YearlyBlog yearlyBlog = rootBlog.getBlogForYear(cal.get(Calendar.YEAR));
   assertNotNull(yearlyBlog);
   assertEquals(cal.get(Calendar.YEAR), yearlyBlog.getYear());
   }

   /**
   * Tests that we can get a previous YearlyBlog instance.
   public void testGetBlogForPreviousYear() {
   Calendar cal = rootBlog.getCalendar();
   YearlyBlog yearlyBlog = rootBlog.getBlogForYear(cal.get(Calendar.YEAR));
   yearlyBlog = rootBlog.getBlogForPreviousYear(yearlyBlog);
   assertNotNull(yearlyBlog);
   assertEquals(cal.get(Calendar.YEAR)-1, yearlyBlog.getYear());
   }

   /**
   * Tests that we can get a next YearlyBlog instance.
   public void testGetBlogForNextYear() {
   Calendar cal = rootBlog.getCalendar();
   YearlyBlog yearlyBlog = rootBlog.getBlogForYear(cal.get(Calendar.YEAR));
   yearlyBlog = rootBlog.getBlogForNextYear(yearlyBlog);
   assertNotNull(yearlyBlog);
   assertEquals(cal.get(Calendar.YEAR)+1, yearlyBlog.getYear());
   }
   */

  /**
   * Tests that we can get the first MonthlyBlog instance.
   */
  public void testGetBlogForFirstMonth() {
    MonthlyBlog monthlyBlog = rootBlog.getBlogForFirstMonth();
    assertNotNull(monthlyBlog);
//    assertEquals(rootBlog.getBlogForFirstYear(), monthlyBlog.getYearlyBlog());
    assertEquals(1, monthlyBlog.getMonth());
  }

  /**
   * Tests that we can get a MonthlyBlog instance.
   */
  public void testGetBlogForMonth() {
    MonthlyBlog monthlyBlog = rootBlog.getBlogForMonth(2003, 4);
    assertNotNull(monthlyBlog);
    assertEquals(2003, monthlyBlog.getYearlyBlog().getYear());
    assertEquals(4, monthlyBlog.getMonth());
  }

  /**
   * Tests that we can get the MonthlyBlog instance for this month.
   */
  public void testGetBlogForThisMonth() {
    Calendar cal = rootBlog.getCalendar();
    MonthlyBlog monthlyBlog = rootBlog.getBlogForThisMonth();
    assertNotNull(monthlyBlog);
    assertEquals(cal.get(Calendar.YEAR), monthlyBlog.getYearlyBlog().getYear());
    assertEquals(cal.get(Calendar.MONTH) + 1, monthlyBlog.getMonth());
  }

  /**
   * Tests that we can get a DailyBlog instance.
   */
  public void testGetBlogForDay() {
    DailyBlog dailyBlog = rootBlog.getBlogForDay(2003, 7, 14);
    assertNotNull(dailyBlog);
    assertEquals(2003, dailyBlog.getMonthlyBlog().getYearlyBlog().getYear());
    assertEquals(7, dailyBlog.getMonthlyBlog().getMonth());
    assertEquals(14, dailyBlog.getDay());
  }

  /**
   * Tests that we can get a DailyBlog instance.
   */
  public void testGetBlogForDate() {
    Calendar cal = rootBlog.getCalendar();
    cal.set(Calendar.YEAR, 2003);
    cal.set(Calendar.MONTH, 6);
    cal.set(Calendar.DAY_OF_MONTH, 14);
    DailyBlog dailyBlog = rootBlog.getBlogForDay(cal.getTime());
    assertNotNull(dailyBlog);
    assertEquals(2003, dailyBlog.getMonthlyBlog().getYearlyBlog().getYear());
    assertEquals(7, dailyBlog.getMonthlyBlog().getMonth());
    assertEquals(14, dailyBlog.getDay());
  }

  /**
   * Tests that we can get the DailyBlog instance for today.
   */
  public void testGetBlogForToday() {
    Calendar cal = rootBlog.getCalendar();
    DailyBlog dailyBlog = rootBlog.getBlogForToday();
    assertNotNull(dailyBlog);
    assertEquals(cal.get(Calendar.YEAR), dailyBlog.getMonthlyBlog().getYearlyBlog().getYear());
    assertEquals(cal.get(Calendar.MONTH) + 1, dailyBlog.getMonthlyBlog().getMonth());
    assertEquals(cal.get(Calendar.DAY_OF_MONTH), dailyBlog.getDay());
  }

  /**
   * Tests that we can get a BlogEntry instance.
   */
  public void testGetBlogEntry() {
    DailyBlog dailyBlog = rootBlog.getBlogForToday();
    BlogEntry blogEntry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(blogEntry);
    String id = blogEntry.getId();
    assertNotNull(blogEntry);
    blogEntry = rootBlog.getBlogEntry(id);
    assertNotNull(blogEntry);
    assertEquals(id, blogEntry.getId());
  }

  /**
   * Tests that blog owners can be assigned.
   */
  public void testAssignBlogOwners() {
    rootBlog.setProperty(SimpleBlog.BLOG_OWNERS_KEY, "user1");
    assertEquals("user1", rootBlog.getProperty(SimpleBlog.BLOG_OWNERS_KEY));
    assertEquals("user1", rootBlog.getBlogOwners());

    Collection users = rootBlog.getUsersInRole(Constants.BLOG_OWNER_ROLE);
    assertEquals(1, users.size());
    assertTrue(users.contains("user1"));

    rootBlog.setProperty(SimpleBlog.BLOG_OWNERS_KEY, "user1,user2");
    assertEquals("user1,user2", rootBlog.getProperty(SimpleBlog.BLOG_OWNERS_KEY));
    assertEquals("user1,user2", rootBlog.getBlogOwners());

    users = rootBlog.getUsersInRole(Constants.BLOG_OWNER_ROLE);
    assertEquals(2, users.size());
    assertTrue(users.contains("user1"));
    assertTrue(users.contains("user2"));
  }

  /**
   * Tests that blog owners can be assigned.
   */
  public void testNullBlogOwners() {
    rootBlog.removeProperty(SimpleBlog.BLOG_OWNERS_KEY);
    assertEquals(null, rootBlog.getBlogOwners());

    Collection users = rootBlog.getUsersInRole(Constants.BLOG_OWNER_ROLE);
    assertEquals(0, users.size());
  }

  /**
   * Tests that it can be determined that a user is a blog owner.
   */
  public void testUserIsBlogOwner() {
    rootBlog.setProperty(SimpleBlog.BLOG_OWNERS_KEY, "user1");
    assertTrue(rootBlog.isUserInRole(Constants.BLOG_OWNER_ROLE, "user1"));
    assertFalse(rootBlog.isUserInRole(Constants.BLOG_OWNER_ROLE, "user2"));
  }

  /**
   * Tests that when no blog contributors are specified, then everybody takes
   * on that role.
   */
  public void testUserIsBlogOwnerByDefault() {
    rootBlog.removeProperty(SimpleBlog.BLOG_OWNERS_KEY);
    assertTrue(rootBlog.isUserInRole(Constants.BLOG_OWNER_ROLE, "user1"));
    assertTrue(rootBlog.isUserInRole(Constants.BLOG_OWNER_ROLE, "usern"));
  }

  /**
   * Tests that blog contributors can be assigned.
   */
  public void testAssignBlogContributors() {
    rootBlog.setProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY, "user1");
    assertEquals("user1", rootBlog.getProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY));
    assertEquals("user1", rootBlog.getBlogContributors());

    Collection users = rootBlog.getUsersInRole(Constants.BLOG_CONTRIBUTOR_ROLE);
    assertEquals(1, users.size());
    assertTrue(users.contains("user1"));

    rootBlog.setProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY, "user1,user2");
    assertEquals("user1,user2", rootBlog.getProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY));
    assertEquals("user1,user2", rootBlog.getBlogContributors());

    users = rootBlog.getUsersInRole(Constants.BLOG_CONTRIBUTOR_ROLE);
    assertEquals(2, users.size());
    assertTrue(users.contains("user1"));
    assertTrue(users.contains("user2"));
  }

  /**
   * Tests that blog contributors can be assigned.
   */
  public void testNullBlogContributors() {
    rootBlog.removeProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY);
    assertEquals(null, rootBlog.getBlogContributors());

    Collection users = rootBlog.getUsersInRole(Constants.BLOG_CONTRIBUTOR_ROLE);
    assertEquals(0, users.size());
  }

  /**
   * Tests that it can be determined that a user is a blog contributor.
   */
  public void testUserIsBlogContributor() {
    rootBlog.setProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY, "user1");
    assertTrue(rootBlog.isUserInRole(Constants.BLOG_CONTRIBUTOR_ROLE, "user1"));
    assertFalse(rootBlog.isUserInRole(Constants.BLOG_CONTRIBUTOR_ROLE, "user2"));
  }

  /**
   * Tests that when no blog contributors are specified, then everybody takes
   * on that role.
   */
  public void testUserIsBlogContributorByDefault() {
    rootBlog.removeProperty(SimpleBlog.BLOG_CONTRIBUTORS_KEY);
    assertTrue(rootBlog.isUserInRole(Constants.BLOG_CONTRIBUTOR_ROLE, "user1"));
    assertTrue(rootBlog.isUserInRole(Constants.BLOG_CONTRIBUTOR_ROLE, "usern"));
  }

  public void testUpdateNotificationPingsEnabled() {
    assertFalse(rootBlog.isUpdateNotificationPingsEnabled());

    rootBlog.setProperty(SimpleBlog.UPDATE_NOTIFICATION_PINGS_ENABLED_KEY, "false");
    assertFalse(rootBlog.isUpdateNotificationPingsEnabled());

    rootBlog.setProperty(SimpleBlog.UPDATE_NOTIFICATION_PINGS_ENABLED_KEY, "true");
    assertTrue(rootBlog.isUpdateNotificationPingsEnabled());
  }

  public void testUpdateNotificationPingWebSites() {
    rootBlog.setProperty(SimpleBlog.UPDATE_NOTIFICATION_PINGS_KEY, null);
    assertTrue(rootBlog.getUpdateNotificationPingsAsCollection().isEmpty());

    rootBlog.setProperty(SimpleBlog.UPDATE_NOTIFICATION_PINGS_KEY, "");
    assertTrue(rootBlog.getUpdateNotificationPingsAsCollection().isEmpty());

    rootBlog.setProperty(SimpleBlog.UPDATE_NOTIFICATION_PINGS_KEY, "http://www.abc.com");
    assertEquals(1, rootBlog.getUpdateNotificationPingsAsCollection().size());
    assertEquals("http://www.abc.com", rootBlog.getUpdateNotificationPingsAsCollection().iterator().next());

    rootBlog.setProperty(SimpleBlog.UPDATE_NOTIFICATION_PINGS_KEY, "http://www.abc.com\r\nhttp://www.def.com");
    assertEquals(2, rootBlog.getUpdateNotificationPingsAsCollection().size());
    Iterator it = rootBlog.getUpdateNotificationPingsAsCollection().iterator();
    assertEquals("http://www.abc.com", it.next());
    assertEquals("http://www.def.com", it.next());
  }

  public void testInvalidDayOfMonthAfterTimeZoneChanges() {
    rootBlog.getRecentBlogEntries();
    rootBlog.setProperty(Blog.TIMEZONE_KEY, "America/New_York");

    // this should not cause an exception to be thrown
    rootBlog.getRecentBlogEntries();
  }

  public void testGetRecentBlogEntriesFromEmptyBlog() {
    assertTrue(rootBlog.getRecentBlogEntries(3).isEmpty());
  }

  public void testGetRecentBlogEntries() {
    Calendar cal1 = rootBlog.getCalendar();
    cal1.set(Calendar.HOUR_OF_DAY, 1);
    Calendar cal2 = rootBlog.getCalendar();
    cal2.set(Calendar.HOUR_OF_DAY, 2);
    Calendar cal3 = rootBlog.getCalendar();
    cal3.set(Calendar.HOUR_OF_DAY, 3);
    Calendar cal4 = rootBlog.getCalendar();
    cal4.set(Calendar.HOUR_OF_DAY, 4);

    DailyBlog today = rootBlog.getBlogForToday();
    BlogEntry entry1 = today.createBlogEntry("title1", "body1", cal1.getTime());
    today.addEntry(entry1);
    BlogEntry entry2 = today.createBlogEntry("title2", "body2", cal2.getTime());
    today.addEntry(entry2);
    BlogEntry entry3 = today.createBlogEntry("title3", "body3", cal3.getTime());
    today.addEntry(entry3);
    BlogEntry entry4 = today.createBlogEntry("title4", "body4", cal4.getTime());
    today.addEntry(entry4);
    List entries = rootBlog.getRecentBlogEntries(3);

    assertFalse(entries.isEmpty());
    assertEquals(3, entries.size());
    assertEquals(entry4, entries.get(0));
  }

  /**
   * Tests the images directory is correct and that it exists.
   */
  public void testImagesDirectoryAccessible() {
    File file = new File(rootBlog.getRoot(), "images");
    assertEquals(file, new File(rootBlog.getImagesDirectory()));
    assertTrue(file.exists());
  }

  /**
   * Tests the files directory is correct and that it exists.
   */
  public void testFilesDirectoryAccessible() {
    File file = new File(rootBlog.getRoot(), "files");
    assertEquals(file, new File(rootBlog.getFilesDirectory()));
    assertTrue(file.exists());
  }

  /**
   * Tests the theme directory is correct and that it doesn't exist by default
   *  - starting up Pebble creates a theme based on the template if the theme
   *  - directory doesn't exist.
   */
  public void testThemeDirectoryAccessible() {
    File file = new File(rootBlog.getRoot(), "theme");
    assertEquals(file, new File(rootBlog.getThemeDirectory()));
    assertFalse(file.exists());
  }

  /**
   * Tests setting a single e-mail address.
   */
  public void testSingleEmailAddress() {
    rootBlog.setProperty(SimpleBlog.EMAIL_KEY, "me@mydomain.com");
    assertEquals("me@mydomain.com", rootBlog.getEmail());
    assertEquals(1, rootBlog.getEmailAddresses().size());
    assertEquals("me@mydomain.com", rootBlog.getEmailAddresses().iterator().next());
  }

  /**
   * Tests setting multiple e-mail address.
   */
  public void testMultipleEmailAddresses() {
    rootBlog.setProperty(SimpleBlog.EMAIL_KEY, "me@mydomain.com,you@yourdomain.com");
    assertEquals("me@mydomain.com,you@yourdomain.com", rootBlog.getEmail());
    assertEquals(2, rootBlog.getEmailAddresses().size());
    Iterator it = rootBlog.getEmailAddresses().iterator();
    assertEquals("me@mydomain.com", it.next());
    assertEquals("you@yourdomain.com", it.next());
  }

  /**
   * Tests getting the first of multiple e-mail addresses.
   */
  public void testFirstEmailAddress() {
    rootBlog.setProperty(SimpleBlog.EMAIL_KEY, "");
    assertEquals("", rootBlog.getFirstEmailAddress());
    rootBlog.setProperty(SimpleBlog.EMAIL_KEY, "me@mydomain.com");
    assertEquals("me@mydomain.com", rootBlog.getFirstEmailAddress());
    rootBlog.setProperty(SimpleBlog.EMAIL_KEY, "me@mydomain.com,you@yourdomain.com");
    assertEquals("me@mydomain.com", rootBlog.getFirstEmailAddress());
  }

  /**
   * Tests that a draft blog entry can be accessed.
   */
  public void testDraftBlogEntryAccessible() throws Exception {
    assertEquals(0, rootBlog.getDraftBlogEntries().size());
    BlogEntry draft = rootBlog.getBlogForToday().createBlogEntry();
    draft.setType(BlogEntry.DRAFT);
    draft.store();
    assertEquals(1, rootBlog.getDraftBlogEntries().size());
    assertEquals(draft, rootBlog.getDraftBlogEntry(draft.getId()));
  }

  /**
   * Tests that a blog entry template can be accessed.
   */
  public void testBlogEntryTemplateAccessible() throws Exception {
    assertEquals(0, rootBlog.getBlogEntryTemplates().size());
    BlogEntry draft = rootBlog.getBlogForToday().createBlogEntry();
    draft.setType(BlogEntry.TEMPLATE);
    draft.store();
    assertEquals(1, rootBlog.getBlogEntryTemplates().size());
    assertEquals(draft, rootBlog.getBlogEntryTemplate(draft.getId()));
  }

  /**
   * Tests that a static page can be accessed.
   */
  public void testStaticPageAccessible() throws Exception {
    assertEquals(0, rootBlog.getStaticPages().size());
    BlogEntry draft = rootBlog.getBlogForToday().createBlogEntry();
    draft.setType(BlogEntry.STATIC_PAGE);
    draft.store();
    assertEquals(1, rootBlog.getStaticPages().size());
    assertEquals(draft, rootBlog.getStaticPage(draft.getId()));
  }

  /**
   * Tests the domain.
   */
  public void testDomain() {
    assertEquals("www.yourdomain.com", rootBlog.getDomainName());

    BlogManager.getInstance().setBaseUrl("http://www.yourdomain.com:8080/blog");
    assertEquals("www.yourdomain.com", rootBlog.getDomainName());
  }

  /**
   * Tests the logger can be accessed.
   */
  public void testLogger() {
    assertNotNull(rootBlog.getLogger());
  }

}