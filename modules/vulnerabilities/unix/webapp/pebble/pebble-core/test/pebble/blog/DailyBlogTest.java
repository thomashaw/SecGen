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

import java.util.Calendar;
import java.util.Date;

/**
 * Tests for the DailyBlog class.
 *
 * @author    Simon Brown
 */
public class DailyBlogTest extends SimpleBlogTestCase {

  private DailyBlog dailyBlog;

  public void setUp() {
    super.setUp();
    dailyBlog = rootBlog.getBlogForToday();
  }

  /**
   * Tests that the root blog is setup correctly.
   */
  public void testGetRootBlog() {
    assertEquals(rootBlog, dailyBlog.getRootBlog());
  }

  /**
   * Tests the getter for the day property.
   */
  public void testGetDay() {
    Calendar cal = rootBlog.getCalendar();
    assertEquals(cal.get(Calendar.DAY_OF_MONTH), dailyBlog.getDay());
  }

  /**
   * Tests the getter for the monthly blog.
   */
  public void testGetMonthlyBlog() {
    assertEquals(rootBlog.getBlogForThisMonth(), dailyBlog.getMonthlyBlog());
  }

  /**
   * Tests the getter for the date property.
   */
  public void testGetDate() {
    dailyBlog = rootBlog.getBlogForDay(2003, 4, 7);
    Date date = dailyBlog.getDate();
    Calendar cal = rootBlog.getCalendar();
    cal.setTime(date);
    assertEquals(2003, cal.get(Calendar.YEAR));
    assertEquals(4, cal.get(Calendar.MONTH) + 1);
    assertEquals(7, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(0, cal.get(Calendar.HOUR));
    assertEquals(0, cal.get(Calendar.MINUTE));
    assertEquals(0, cal.get(Calendar.SECOND));
    assertEquals(0, cal.get(Calendar.MILLISECOND));
  }

  /**
   * Tests the permalink.
   */
  public void testGetPermalink() {
    String permalink = rootBlog.getUrl() + "2003/04/01.html";
    dailyBlog = rootBlog.getBlogForDay(2003, 4, 1);
    assertEquals(permalink, dailyBlog.getPermalink());

    permalink = rootBlog.getUrl() + "2003/04/10.html";
    dailyBlog = rootBlog.getBlogForDay(2003, 4, 10);
    assertEquals(permalink, dailyBlog.getPermalink());
  }

  /**
   * Tests the ability to check for entries.
   */
  public void testHasEntries() {
    assertFalse(dailyBlog.hasEntries());

    BlogEntry entry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(entry);
    assertTrue(dailyBlog.hasEntries());
  }

  /**
   * Tests the ability to check for entries in a specific category.
   */
  public void testHasEntriesInCategory() {
    Category cat = new Category("testCategory1", "Test Category 1");
    assertFalse(dailyBlog.hasEntries(cat));

    BlogEntry entry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(entry);
    assertFalse(dailyBlog.hasEntries(cat));

    entry.addCategory(cat);
    assertTrue(dailyBlog.hasEntries(cat));
  }

  /**
   * Tests the ability to get all daily blog entries.
   */
  public void testGetEntries() {
    assertNotNull(dailyBlog.getEntries());
    assertTrue(dailyBlog.getEntries().isEmpty());

    BlogEntry entry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(entry);
    assertNotNull(dailyBlog.getEntries());
    assertTrue(dailyBlog.getEntries().size() == 1);
    assertTrue(dailyBlog.getEntries().contains(entry));
  }

  /**
   * Tests the ability to get a specific blog entry.
   */
  public void testGetEntry() {
    BlogEntry entry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(entry);
    assertEquals(entry, dailyBlog.getEntry(entry.getId()));

    assertNull(dailyBlog.getEntry("some invalid id"));
  }

  /**
   * Tests the ability to add a specific blog entry.
   */
  public void testAddEntry() {
    BlogEntry entry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(entry);
    assertEquals(entry, dailyBlog.getEntry(entry.getId()));
    assertEquals(1, dailyBlog.getEntries().size());

    // check that the same entry can't be added twice
    dailyBlog.addEntry(entry);
    assertEquals(entry, dailyBlog.getEntry(entry.getId()));
    assertEquals(1, dailyBlog.getEntries().size());

    // this shouldn't throw an exception
    dailyBlog.addEntry(null);
  }

  /**
   * Tests the ability to remove a specific blog entry.
   */
  public void testRemoveEntry() {
    BlogEntry entry = dailyBlog.createBlogEntry();
    dailyBlog.addEntry(entry);
    assertEquals(entry, dailyBlog.getEntry(entry.getId()));
    dailyBlog.removeEntry(entry);
    assertNull(dailyBlog.getEntry(entry.getId()));

    // this shouldn't throw an exception
    dailyBlog.removeEntry(null);
  }

  /**
   * Tests the ability to create a new blog entry.
   */
  public void testCreateBlogEntry() {
    BlogEntry entry = dailyBlog.createBlogEntry();
    assertNotNull(entry);

    // and check it's not yet been added to the DailyBlog instance
    assertFalse(dailyBlog.hasEntries());
  }

  /**
   * Tests that the previous blog entry can be obtained.
   */
  public void testPreviousBlogEntry() {
    Calendar cal = rootBlog.getCalendar();
    cal.set(Calendar.HOUR_OF_DAY, 2);
    BlogEntry entry1 = dailyBlog.createBlogEntry("Title", "Body", cal.getTime());
    dailyBlog.addEntry(entry1);
    cal.set(Calendar.HOUR_OF_DAY, 3);
    BlogEntry entry2 = dailyBlog.createBlogEntry("Title", "Body", cal.getTime());
    dailyBlog.addEntry(entry2);
    cal.set(Calendar.HOUR_OF_DAY, 4);
    BlogEntry entry3 = dailyBlog.createBlogEntry("Title", "Body", cal.getTime());
    dailyBlog.addEntry(entry3);

    assertNull(dailyBlog.getPreviousBlogEntry(null));
    assertNull(dailyBlog.getPreviousBlogEntry(entry1));
    assertEquals(entry1, dailyBlog.getPreviousBlogEntry(entry2));
    assertEquals(entry2, dailyBlog.getPreviousBlogEntry(entry3));
  }

  /**
   * Tests that the next blog entry can be obtained.
   */
  public void testNextBlogEntry() {
    Calendar cal = rootBlog.getCalendar();
    cal.set(Calendar.HOUR_OF_DAY, 2);
    BlogEntry entry1 = dailyBlog.createBlogEntry("Title", "Body", cal.getTime());
    dailyBlog.addEntry(entry1);
    cal.set(Calendar.HOUR_OF_DAY, 3);
    BlogEntry entry2 = dailyBlog.createBlogEntry("Title", "Body", cal.getTime());
    dailyBlog.addEntry(entry2);
    cal.set(Calendar.HOUR_OF_DAY, 4);
    BlogEntry entry3 = dailyBlog.createBlogEntry("Title", "Body", cal.getTime());
    dailyBlog.addEntry(entry3);

    assertNull(dailyBlog.getNextBlogEntry(null));
    assertNull(dailyBlog.getNextBlogEntry(entry3));
    assertEquals(entry2, dailyBlog.getNextBlogEntry(entry1));
    assertEquals(entry3, dailyBlog.getNextBlogEntry(entry2));
  }

  /**
   * Tests that accessing the first entry works, even when there are
   * no entries!
   */
  public void testFirstBlogEntryIsNull() {
    assertNull(dailyBlog.getFirstBlogEntry());
  }

}