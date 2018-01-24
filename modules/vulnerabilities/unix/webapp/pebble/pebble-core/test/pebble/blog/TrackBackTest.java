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



/**
 * Tests for the TrackBack class.
 *
 * @author    Simon Brown
 */
public class TrackBackTest extends SimpleBlogTestCase {

  private TrackBack trackback;

  public void setUp() {
    super.setUp();

    trackback = rootBlog.getBlogForToday().createBlogEntry().createTrackBack("Title", "Excerpt", "http://www.somedomain.com", "Some blog", "127.0.0.1");
  }

  /**
   * Test that a TrackBack instance can be created correctly.
   */
  public void testConstructionOfSimpleInstance() {
    assertNotNull(trackback);
    assertEquals("Title", trackback.getTitle());
    assertEquals("Excerpt", trackback.getExcerpt());
    assertEquals("http://www.somedomain.com", trackback.getUrl());
    assertEquals("Some blog", trackback.getBlogName());
    assertEquals("127.0.0.1", trackback.getIpAddress());
    assertNotNull(trackback.getDate());
    assertEquals(trackback.getDate().getTime(), trackback.getId());
    assertNotNull(trackback.getBlogEntry());
  }

  /**
   * Tests that the title is set appropriately.
   */
  public void testTitle() {
    assertEquals("Title", trackback.getTitle());
  }

  /**
   * Tests that the title defaults to the URL is not specified.
   */
  public void testTitleDefaultsToUrlWhenNotSpecified() {
    trackback.setTitle("");
    assertEquals("http://www.somedomain.com", trackback.getTitle());

    trackback.setTitle(null);
    assertEquals("http://www.somedomain.com", trackback.getTitle());
  }

  /**
   * Tests that the excerpt can't be null.
   */
  public void testExcerptNeverNull() {
    trackback.setExcerpt("");
    assertEquals("", trackback.getExcerpt());

    trackback.setExcerpt(null);
    assertEquals("", trackback.getExcerpt());
  }

  /**
   * Tests that the blog name can't be null.
   */
  public void testBlogNameNeverNull() {
    trackback.setBlogName("");
    assertEquals("", trackback.getBlogName());

    trackback.setBlogName(null);
    assertEquals("", trackback.getBlogName());
  }

  /**
   * Tests the permalink for a TrackBack.
   */
  public void testPermalink() {
    assertEquals(trackback.getBlogEntry().getPermalink() + "#trackback" + trackback.getId(), trackback.getPermalink());
  }

  /**
   * Tests that a TrackBack can be cloned.
   */
  public void testClone() {
    TrackBack clonedTrackBack = (TrackBack)trackback.clone();

    assertEquals(trackback.getTitle(), clonedTrackBack.getTitle());
    assertEquals(trackback.getExcerpt(), clonedTrackBack.getExcerpt());
    assertEquals(trackback.getUrl(), clonedTrackBack.getUrl());
    assertEquals(trackback.getBlogName(), clonedTrackBack.getBlogName());
    assertEquals(trackback.getIpAddress(), clonedTrackBack.getIpAddress());
    assertEquals(trackback.getDate(), clonedTrackBack.getDate());
    assertEquals(trackback.getId(), clonedTrackBack.getId());
    assertEquals(trackback.getBlogEntry(), clonedTrackBack.getBlogEntry());
  }

}
