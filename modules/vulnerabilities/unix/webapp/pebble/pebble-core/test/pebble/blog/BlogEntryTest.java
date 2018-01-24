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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import pebble.controller.ValidationContext;

/**
 * Tests for the BlogEntry class.
 *
 * @author    Simon Brown
 */
public class BlogEntryTest extends SimpleBlogTestCase {

  private BlogEntry blogEntry;

  public void setUp() {
    super.setUp();
    blogEntry = new BlogEntry(rootBlog.getBlogForToday());
    blogEntry.setTitle("A title");
    blogEntry.setBody("Some body");
    blogEntry.setDate(new Date());
  }

  /**
   * Tests the construction.
   */
  public void testConstruction() {
    assertNotNull(blogEntry.getId());
    assertEquals("A title", blogEntry.getTitle());
    assertEquals("Some body", blogEntry.getBody());

    assertNotNull(blogEntry.getCategories());
    assertEquals(0, blogEntry.getCategories().size());
    assertTrue(blogEntry.isCommentsEnabled());
    assertNotNull(blogEntry.getComments());
    assertEquals(0, blogEntry.getComments().size());
    assertNotNull(blogEntry.getTrackBacks());
    assertEquals(0, blogEntry.getTrackBacks().size());

    assertNotNull(blogEntry.getDate());
    assertNotNull(blogEntry.getAuthor());
    assertFalse(blogEntry.isAggregated());
  }

  /**
   * Tests that the root blog is setup correctly.
   */
  public void testGetRootBlog() {
    assertEquals(rootBlog, blogEntry.getRootBlog());
  }

  /**
   * Tests that the root is setup correctly.
   */
  public void testDailyBlog() {
    //assertEquals(dailyBlog, blogEntry.getDailyBlog());
  }

  /**
   * Tests for the id.
   */
  public void testId() {
    String id = "" + blogEntry.getDate().getTime();
    assertEquals(id, blogEntry.getId());
  }

  /**
   * Tests for the title.
   */
  public void testTitle() {
    blogEntry.setTitle("A new title");
    assertEquals("A new title", blogEntry.getTitle());
  }

  /**
   * Tests for the body.
   */
  public void testBody() {
    blogEntry.setBody("A new body");
    assertEquals("A new body", blogEntry.getBody());
  }

  /**
   * Tests for the excerpt.
   */
  public void testExcerpt() {
    blogEntry.setExcerpt("An excerpt");
    assertEquals("An excerpt", blogEntry.getExcerpt());
  }

  /**
   * Tests for a short excerpt.
   */
  public void testShortExcerpt() {
    blogEntry.setBody("Here is some text.");
    assertEquals("Here is some text.", blogEntry.getExcerptFromBody());
  }

  /**
   * Tests for a short excerpt.
   */
  public void testLongExcerpt() {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 252; i++) {
      buf.append("a");
    }

    // 252 character body
    blogEntry.setBody(buf.toString());
    assertEquals(buf.toString(), blogEntry.getExcerptFromBody());

    // 255 character body
    blogEntry.setBody(buf.toString() + "123");
    assertEquals(buf.toString() + "123", blogEntry.getExcerptFromBody());

    // over 255 characters and the body is trimmed to 252 + "..."
    blogEntry.setBody(buf.toString() + "1234");
    assertEquals(buf.toString() + "...", blogEntry.getExcerptFromBody());
  }

  /**
   * Tests for the categories.
   */
  public void testCategories() {
    assertEquals(0, blogEntry.getCategories().size());

    Category category = new Category("1", "One");
    blogEntry.addCategory(category);
    assertEquals(1, blogEntry.getCategories().size());
    assertTrue(blogEntry.getCategories().contains(category));

    // just check that we can't add null categories!
    blogEntry.addCategory(null);
    assertFalse(blogEntry.getCategories().contains(null));
  }

  /**
   * Tests for the "in category" check.
   */
  public void testInCategory() {
    Category category1 = new Category("1", "One");
    Category category2 = new Category("2", "Two");
    blogEntry.addCategory(category1);
    assertTrue(blogEntry.inCategory(category1));
    assertFalse(blogEntry.inCategory(category2));
    assertTrue(blogEntry.inCategory(null));

    blogEntry.addCategory(category2);
    assertTrue(blogEntry.inCategory(category1));
    assertTrue(blogEntry.inCategory(category2));
  }

  /**
   * Tests that all categories can be removed.
   */
  public void testAllCategoriesCanBeRemoved() {
    Category category1 = new Category("1", "One");
    Category category2 = new Category("2", "Two");
    blogEntry.addCategory(category1);
    blogEntry.addCategory(category2);

    blogEntry.removeAllCategories();
    assertEquals(0, blogEntry.getCategories().size());
  }

  /**
   * Tests for the date.
   */
  public void testDate() {
    assertNotNull(blogEntry.getDate());
    try {
      blogEntry.setDate(new Date());
      fail();
    } catch (RuntimeException re) {
      // this is fine - the date cannot be set after it has been initialised
    }
  }

  /**
   * Tests for the author.
   */
  public void testAuthor() {
    blogEntry.setAuthor("A new author");
    assertEquals("A new author", blogEntry.getAuthor());
  }

  /**
   * Tests the aggregated property.
   */
  public void testAggregated() {
    blogEntry.setOriginalPermalink("http://www.simongbrown.com/blog/2003/04/01.html#a123456789");
    assertTrue(blogEntry.isAggregated());

    blogEntry.setOriginalPermalink(null);
    assertFalse(blogEntry.isAggregated());

    blogEntry.setOriginalPermalink("");
    assertFalse(blogEntry.isAggregated());
  }

  /**
   * Tests the comments enabled property.
   */
  public void testCommentsEnabled() {
    assertTrue(blogEntry.isCommentsEnabled());
    blogEntry.setCommentsEnabled(false);
    assertFalse(blogEntry.isCommentsEnabled());
  }

  /**
   * Tests the links that will refer to a blog entry and its comments.
   */
  public void testLinks() {
    DecimalFormat format = new DecimalFormat("00");
    Calendar cal = rootBlog.getCalendar();
    cal.setTime(blogEntry.getDate());
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);
    assertEquals("http://www.yourdomain.com/blog/" +
        year + "/" + format.format(month) + "/" + format.format(day) + "/" +
        blogEntry.getId() + ".html", blogEntry.getPermalink());
    assertEquals("http://www.yourdomain.com/blog/" +
        year + "/" + format.format(month) + "/" + format.format(day) + "/" +
        blogEntry.getId() + ".html", blogEntry.getLocalPermalink());
    assertEquals("http://www.yourdomain.com/blog/" +
        blogEntry.getId() + ".html", blogEntry.getShortPermalink());
    assertEquals("http://www.yourdomain.com/blog/viewComments.action?entry=" + blogEntry.getId(), blogEntry.getCommentsLink());
    assertEquals("http://www.yourdomain.com/blog/viewTrackBacks.action?entry=" + blogEntry.getId(), blogEntry.getTrackBacksLink());
  }

  /**
   * Tests that a blog entry can correctly manage blog comments.
   */
  public void testComments() {
    Comment blogComment1, blogComment2;
    Calendar cal1, cal2;
    cal1 = rootBlog.getCalendar();
    cal1.set(Calendar.DAY_OF_MONTH, 1);
    cal2 = rootBlog.getCalendar();
    cal2.set(Calendar.DAY_OF_MONTH, 2);

    // check that there are no comments to start with
    assertEquals(0, blogEntry.getNumberOfComments());
    assertEquals(0, blogEntry.getComments().size());

    // now create a new comment
    blogComment1 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", cal1.getTime());
    assertNotNull(blogComment1);
    assertEquals("Re: " + blogEntry.getTitle(), blogComment1.getTitle());
    assertEquals("Body", blogComment1.getBody());
    assertEquals("Author", blogComment1.getAuthor());
    assertEquals("http://www.google.com", blogComment1.getWebsite());
    assertNotNull(blogComment1.getDate());
    assertEquals(blogEntry, blogComment1.getBlogEntry());

    // the comment hasn't been added yet
    assertEquals(0, blogEntry.getNumberOfComments());
    assertEquals(0, blogEntry.getComments().size());
    assertFalse(blogEntry.getComments().contains(blogComment1));

    // let's now add the comment
    blogEntry.addComment(blogComment1);
    assertEquals(1, blogEntry.getNumberOfComments());
    assertEquals(1, blogEntry.getComments().size());
    assertTrue(blogEntry.getComments().contains(blogComment1));

    // and now add another comment
    blogComment2 = blogEntry.createComment("Title 2", "Body 2", "Author 2", "me2@somedomain.com", "http://www.yahoo.com", "127.0.0.1", cal2.getTime());
    assertEquals("Title 2", blogComment2.getTitle());
    assertEquals(1, blogEntry.getNumberOfComments());
    assertTrue(blogEntry.getComments().contains(blogComment1));
    assertFalse(blogEntry.getComments().contains(blogComment2));
    blogEntry.addComment(blogComment2);
    assertEquals(2, blogEntry.getNumberOfComments());
    assertEquals(2, blogEntry.getComments().size());
    assertTrue(blogEntry.getComments().contains(blogComment1));
    assertTrue(blogEntry.getComments().contains(blogComment2));

    // check that we can't add the same comment more than once
    blogEntry.addComment(blogComment2);
    assertEquals(2, blogEntry.getNumberOfComments());

    // and now delete the comments
    blogEntry.removeComment(blogComment1.getId());
    assertFalse(blogEntry.getComments().contains(blogComment1));
    assertTrue(blogEntry.getComments().contains(blogComment2));
    assertEquals(1, blogEntry.getNumberOfComments());
    blogEntry.removeComment(blogComment2.getId());
    assertFalse(blogEntry.getComments().contains(blogComment1));
    assertFalse(blogEntry.getComments().contains(blogComment2));
    assertEquals(0, blogEntry.getNumberOfComments());
  }

  /**
   * Tests that nested comments can be accessed.
   */
  public void testNestedComments() {
    Comment comment1, comment2;
    Date date1 = new Date(0);
    Date date2 = new Date(1000);

    comment1 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", date1);
    blogEntry.addComment(comment1);

    comment2 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", date2);
    comment2.setParent(comment1);
    blogEntry.addComment(comment2);

    assertEquals(2, blogEntry.getComments().size());
    assertEquals(comment1, blogEntry.getComments().get(0));
    assertEquals(comment2, blogEntry.getComments().get(1));

    assertEquals(comment1, blogEntry.getComment(comment1.getId()));
    assertEquals(comment2, blogEntry.getComment(comment2.getId()));
  }

  /**
   * Tests that the number of comments reported is correct when some of those
   * comments are nested.
   */
  public void testNumberOfCommentsCorrectWhenNestedCommentsPresent() {
    Comment comment1, comment2;
    Calendar cal1, cal2;
    cal1 = rootBlog.getCalendar();
    cal1.set(Calendar.DAY_OF_MONTH, 1);
    cal2 = rootBlog.getCalendar();
    cal2.set(Calendar.DAY_OF_MONTH, 2);

    comment1 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", cal1.getTime());
    blogEntry.addComment(comment1);

    comment2 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", cal2.getTime());
    comment2.setParent(comment1);
    blogEntry.addComment(comment2);

    assertEquals(2, blogEntry.getNumberOfComments());
  }

  /**
   * Tests that removing a comment also removes all of its children.
   */
  public void testRemovingCommentRemovesAllChildren() {
    Comment comment1, comment2;
    Date date1 = new Date(0);
    Date date2 = new Date(1000);

    comment1 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", date1);
    blogEntry.addComment(comment1);

    comment2 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", date2);
    comment2.setParent(comment1);
    blogEntry.addComment(comment2);

    // now remove the top-level comment, and hopefully all of its children
    blogEntry.removeComment(comment1.getId());
    assertEquals(0, blogEntry.getNumberOfComments());
  }

  /**
   * Tests that a nested comment can be removed.
   */
  public void testNestedCommentCanBeRemoved() {
    Comment comment1, comment2;
    Date date1 = new Date(0);
    Date date2 = new Date(1000);

    comment1 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", date1);
    blogEntry.addComment(comment1);

    comment2 = blogEntry.createComment("", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", date2);
    comment2.setParent(comment1);
    blogEntry.addComment(comment2);

    // now remove the nested comment
    blogEntry.removeComment(comment2.getId());
    assertEquals(1, blogEntry.getNumberOfComments());
    assertNull(blogEntry.getComment(comment2.getId()));
  }

  /**
   * Tests that the permalink for a comment is correctly set.
   */
  public void testCommentPermalink() {
    Comment blogComment;

    blogComment = blogEntry.createComment("Title", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1");
    blogEntry.addComment(blogComment);
    assertEquals(blogEntry.getLocalPermalink() + "#comment" + blogComment.getId(), blogComment.getPermalink());
  }

  /**
   * Tests that a blog entry can correctly manage trackbacks.
   */
  public void testTrackBacks() {
    // check that there are no trackbacks to start with
    assertEquals(0, blogEntry.getNumberOfTrackBacks());
    assertEquals(0, blogEntry.getTrackBacks().size());
  }

  /**
   * Tests that a blog entry is cloneable.
   */
  public void testCloneableNormalBlogEntry() {
    blogEntry.setTitle("An old title");
    blogEntry.setBody("An old body");
    blogEntry.setAuthor("An old author");
    blogEntry.setType(BlogEntry.DRAFT);
    blogEntry.setTimeZone(TimeZone.getTimeZone("CET"));
    blogEntry.setCommentsEnabled(false);
    blogEntry.setTrackBacksEnabled(false);

    Category category1 = rootBlog.getBlogCategoryManager().getCategory("testCategory1");
    Category category2 = rootBlog.getBlogCategoryManager().getCategory("testCategory2");
    Category category3 = rootBlog.getBlogCategoryManager().getCategory("testCategory3");
    blogEntry.addCategory(category1);
    blogEntry.addCategory(category2);

    BlogEntry clonedBlogEntry = (BlogEntry)blogEntry.clone();

    assertEquals(blogEntry.getTitle(), clonedBlogEntry.getTitle());
    assertEquals(blogEntry.getBody(), clonedBlogEntry.getBody());
    assertEquals(blogEntry.getAuthor(), clonedBlogEntry.getAuthor());
    assertEquals(blogEntry.getPermalink(), clonedBlogEntry.getPermalink());
    assertEquals(blogEntry.inCategory(category1), clonedBlogEntry.inCategory(category1));
    assertEquals(blogEntry.inCategory(category2), clonedBlogEntry.inCategory(category2));
    assertEquals(blogEntry.inCategory(category3), clonedBlogEntry.inCategory(category3));
    assertEquals(blogEntry.getType(), clonedBlogEntry.getType());
    assertEquals(blogEntry.getTimeZone(), clonedBlogEntry.getTimeZone());
    assertFalse(clonedBlogEntry.isCommentsEnabled());
    assertFalse(clonedBlogEntry.isTrackBacksEnabled());
  }

  /**
   * Tests that a blog entry is cloneable.
   */
  public void testCloneableAggregatedBlogEntry() {
    blogEntry.setTitle("An old title");
    blogEntry.setBody("An old body");
    blogEntry.setAuthor("An old author");
    blogEntry.setOriginalPermalink("An old alternative permalink");

    Category category1 = rootBlog.getBlogCategoryManager().getCategory("testCategory1");
    Category category2 = rootBlog.getBlogCategoryManager().getCategory("testCategory2");
    Category category3 = rootBlog.getBlogCategoryManager().getCategory("testCategory3");
    blogEntry.addCategory(category1);
    blogEntry.addCategory(category2);

    BlogEntry clonedBlogEntry = (BlogEntry)blogEntry.clone();

    assertEquals(blogEntry.getTitle(), clonedBlogEntry.getTitle());
    assertEquals(blogEntry.getBody(), clonedBlogEntry.getBody());
    assertEquals(blogEntry.getAuthor(), clonedBlogEntry.getAuthor());
    assertEquals(blogEntry.getOriginalPermalink(), clonedBlogEntry.getOriginalPermalink());
    assertEquals(blogEntry.inCategory(category1), clonedBlogEntry.inCategory(category1));
    assertEquals(blogEntry.inCategory(category2), clonedBlogEntry.inCategory(category2));
    assertEquals(blogEntry.inCategory(category3), clonedBlogEntry.inCategory(category3));
  }

  public void testValidationForStaticPage() {
    blogEntry.setType(BlogEntry.STATIC_PAGE);

    ValidationContext context = new ValidationContext();
    blogEntry.validate(context);
    assertTrue("Name shouldn't be empty", context.hasErrors());

    context = new ValidationContext();
    blogEntry.setStaticName("someStoryName");
    blogEntry.validate(context);
    assertFalse(context.hasErrors());

    context = new ValidationContext();
    blogEntry.setStaticName("2004/someStoryName");
    blogEntry.validate(context);
    assertFalse(context.hasErrors());

    context = new ValidationContext();
    blogEntry.setStaticName("some-story-name");
    blogEntry.validate(context);
    assertFalse(context.hasErrors());

    context = new ValidationContext();
    blogEntry.setStaticName("someStoryName.html");
    blogEntry.validate(context);
    assertTrue("Name shouldn't contain punctuation", context.hasErrors());
  }

  /**
   * Tests that the next blog entry can be accessed.
   */
  public void testGetNextBlogEntry() {
    DailyBlog today = rootBlog.getBlogForToday();
    DailyBlog oneDayAgo = today.getPreviousDay();
    DailyBlog twoDaysAgo = today.getPreviousDay().getPreviousDay();
    BlogEntry b1 = twoDaysAgo.createBlogEntry();
    twoDaysAgo.addEntry(b1);
    BlogEntry b2 = oneDayAgo.createBlogEntry();
    oneDayAgo.addEntry(b2);
    BlogEntry b3 = today.createBlogEntry();
    today.addEntry(b3);

    assertNull(b3.getNextBlogEntry());
    assertEquals(b3, b2.getNextBlogEntry());
    assertEquals(b2, b1.getNextBlogEntry());
  }

  /**
   * Tests that the previous blog entry can be accessed.
   */
  public void testGetPreviousBlogEntry() {
    DailyBlog today = rootBlog.getBlogForToday();
    DailyBlog oneDayAgo = today.getPreviousDay();
    DailyBlog twoDaysAgo = today.getPreviousDay().getPreviousDay();
    BlogEntry b1 = twoDaysAgo.createBlogEntry();
    twoDaysAgo.addEntry(b1);
    BlogEntry b2 = oneDayAgo.createBlogEntry();
    oneDayAgo.addEntry(b2);
    BlogEntry b3 = today.createBlogEntry();
    today.addEntry(b3);

    assertNull(b1.getPreviousBlogEntry());
    assertEquals(b1, b2.getPreviousBlogEntry());
    assertEquals(b2, b3.getPreviousBlogEntry());
  }

  /**
   * Tests that for the timezone property.
   */
  public void testTimeZone() {
    // by default we should have the same timezone as the owning blog
    assertEquals(rootBlog.getTimeZone(), blogEntry.getTimeZone());

    // but it can also be overridden
    blogEntry.setTimeZone(TimeZone.getTimeZone("CET"));
    assertEquals(TimeZone.getTimeZone("CET"), blogEntry.getTimeZone());
  }

}