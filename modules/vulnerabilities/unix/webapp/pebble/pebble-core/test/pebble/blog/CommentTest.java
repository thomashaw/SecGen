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

import java.util.Date;
import java.util.Calendar;

/**
 * Tests for the Comment class.
 *
 * @author    Simon Brown
 */
public class CommentTest extends SimpleBlogTestCase {

  private Comment comment;

  public void setUp() {
    super.setUp();

    comment = rootBlog.getBlogForToday().createBlogEntry().createComment("Title", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1");
  }

  /**
   * Test that a Comment instance can be created correctly.
   */
  public void testConstructionOfSimpleInstance() {
    assertNotNull(comment);
    assertEquals("Title", comment.getTitle());
    assertEquals("Body", comment.getBody());
    assertEquals("Author", comment.getAuthor());
    assertEquals("me@somedomain.com", comment.getEmail());
    assertEquals("http://www.google.com", comment.getWebsite());
    assertEquals("127.0.0.1", comment.getIpAddress());
    assertNotNull(comment.getDate());
    assertEquals(comment.getDate().getTime(), comment.getId());
    assertNotNull(comment.getBlogEntry());
  }

  /**
   * Tests that the author name is properly escaped and set.
   */
  public void testAuthor() {
    assertEquals("Author", comment.getAuthor());

    // blank or null author name defaults to "Anonymous"
    comment.setAuthor("");
    assertEquals("Anonymous", comment.getAuthor());
    comment.setAuthor(null);
    assertEquals("Anonymous", comment.getAuthor());

    // for security, special HTML characters aren't removed
    // (they are rendered out at runtime)
    comment.setAuthor("<Author>");
    assertEquals("<Author>", comment.getAuthor());
  }

  /**
   * Tests that the e-mail address is properly escaped and set.
   */
  public void testEmailAddress() {
    assertEquals("me@somedomain.com", comment.getEmail());

    // blank or null e-mail defaults to null
    comment.setEmail("");
    assertEquals(null, comment.getEmail());
    comment.setEmail(null);
    assertEquals(null, comment.getEmail());

    // for security, special HTML characters are removed
    comment.setEmail("<me@somedomain.com>");
    assertEquals("&lt;me@somedomain.com&gt;", comment.getEmail());
  }

  /**
   * Tests that the website is properly escaped and set.
   */
  public void testWebsite() {
    assertEquals("http://www.google.com", comment.getWebsite());

    // blank or null website name defaults to null
    comment.setWebsite("");
    assertEquals(null, comment.getWebsite());
    comment.setWebsite(null);
    assertEquals(null, comment.getWebsite());

    // for security, special HTML characters are removed
    comment.setWebsite("http://<www.google.com>");
    assertEquals("http://&lt;www.google.com&gt;", comment.getWebsite());

    // anything websites are also checked for known prefixes and "http://"
    // is prepended if missing
    comment.setWebsite("http://www.google.com");
    assertEquals("http://www.google.com", comment.getWebsite());
    comment.setWebsite("https://www.google.com");
    assertEquals("https://www.google.com", comment.getWebsite());
    comment.setWebsite("ftp://www.google.com");
    assertEquals("ftp://www.google.com", comment.getWebsite());
    comment.setWebsite("mailto://www.google.com");
    assertEquals("mailto://www.google.com", comment.getWebsite());
    comment.setWebsite("www.google.com");
    assertEquals("http://www.google.com", comment.getWebsite());
  }

  /**
   * Tests that the body is properly escaped and only allowable HTML included.
   */
  public void testBody() {
    comment.setBody("");
    assertEquals(null, comment.getBody());
    comment.setBody(null);
    assertEquals(null, comment.getBody());

    comment.setBody("Here is some text");
    assertEquals("Here is some text", comment.getBody());

    // for security, special HTML characters aren't removed
    // (they are rendered out at runtime)
    comment.setBody("Here is a < symbol");
    assertEquals("Here is a < symbol", comment.getBody());

    comment.setBody("Here is a > symbol");
    assertEquals("Here is a > symbol", comment.getBody());

    comment.setBody("Here is a <b> tag");
    assertEquals("Here is a <b> tag", comment.getBody());

    comment.setBody("Here is a <i> tag");
    assertEquals("Here is a <i> tag", comment.getBody());

    comment.setBody("Here is a <p> tag");
    assertEquals("Here is a <p> tag", comment.getBody());

    comment.setBody("Here is a </p> tag");
    assertEquals("Here is a </p> tag", comment.getBody());

    comment.setBody("Here is a <br> tag");
    assertEquals("Here is a <br> tag", comment.getBody());

    comment.setBody("Here is a <pre> tag");
    assertEquals("Here is a <pre> tag", comment.getBody());

    comment.setBody("Here is a </pre> tag");
    assertEquals("Here is a </pre> tag", comment.getBody());

    comment.setBody("Here is a <a href=\"http://www.google.com\">link</a> to Google");
    assertEquals("Here is a <a href=\"http://www.google.com\">link</a> to Google", comment.getBody());

    comment.setBody("Here is a <script> tag");
    assertEquals("Here is a <script> tag", comment.getBody());
  }

  /**
   * Tests that the date can never be null.
   */
  public void testDate() {
    assertNotNull(comment.getDate());

    comment.setDate(new Date());
    assertNotNull(comment.getDate());

    comment.setDate(null);
    assertNotNull(comment.getDate());
  }

  /**
   * Tests that the title is set when an owning blog entry is present.
   */
  public void testTitleTakenFromOwningBlogEntryWhenNotSpecified() {
    BlogEntry entry = rootBlog.getBlogForToday().createBlogEntry();
    entry.setTitle("My blog entry title");
    comment = entry.createComment(null, "", "", "", "", "");
    assertEquals("Re: My blog entry title", comment.getTitle());
    comment = entry.createComment("", "", "", "", "", "");
    assertEquals("Re: My blog entry title", comment.getTitle());
  }

  /**
   * Tests the number of parents is 0 by default.
   */
  public void testNumberOfParentsIsZeroByDefault() {
    assertEquals(0, comment.getNumberOfParents());
  }

  /**
   * Tests that the number of parents is correct when comments are nested.
   */
  public void testNumberOfParentsIsCorrectWhenNested() {
    comment.setParent(rootBlog.getBlogForToday().createBlogEntry().createComment("", "", "", "", "", ""));
    assertEquals(1, comment.getNumberOfParents());
  }

  /**
   * Tests that adding a null comment doesn't cause an NPE.
   */
  public void testAddingNullCommentDoesntCauseException() {
    comment.addComment(null);
  }

  /**
   * Tests that removing a null comment doesn't cause an NPE.
   */
  public void testRemovingNullCommentDoesntCauseException() {
    comment.removeComment(null);
  }

  /**
   * Tests for the truncated body.
   */
  public void testTruncatedBody() {
    comment.setBody(null);
    assertEquals("", comment.getTruncatedBody());

    comment.setBody("1234567890");
    assertEquals("1234567890", comment.getTruncatedBody());

    comment.setBody("1234567890123456789012345678901234567890123456789012345678901234567890");
    assertEquals("1234567890123456789012345678901234567890123456789012345678901...", comment.getTruncatedBody());
  }

  /**
   * Tests that a comment can be cloned.
   */
  public void testClone() {
    // todo
  }

  /**
   * Test the equals() method.
   */
  public void testEquals() {
    assertTrue(comment.equals(comment));

    Calendar cal = rootBlog.getCalendar();
    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
    Comment comment2 = rootBlog.getBlogForToday().createBlogEntry().createComment("Title", "Body", "Author", "me@somedomain.com", "http://www.google.com", "127.0.0.1", cal.getTime());
    assertFalse(comment.equals(comment2));

  }

}
