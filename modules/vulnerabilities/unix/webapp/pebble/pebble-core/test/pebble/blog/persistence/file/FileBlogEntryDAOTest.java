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
package pebble.blog.persistence.file;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import pebble.blog.*;
import pebble.blog.persistence.BlogEntryDAO;
import pebble.util.FileUtils;

/**
 * Tests for the FileBlogEntryDAO class.
 *
 * @author    Simon Brown
 */
public class FileBlogEntryDAOTest extends SimpleBlogTestCase {

  private BlogEntryDAO dao= new FileBlogEntryDAO();

  public void testLoadBlogEntryFomFile() throws Exception {

    File source = new File("./1081203335000.xml");
    File destination = new File(TEST_BLOG_LOCATION, "2004/04/05/");
    destination.mkdirs();
    FileUtils.copyFile(source, new File(destination, "1081203335000.xml"));

    DailyBlog dailyBlog = rootBlog.getBlogForDay(2004, 04, 05);
    Category category1 = rootBlog.getBlogCategoryManager().addCategory("category1", "Category 1");
    Category category2 = rootBlog.getBlogCategoryManager().addCategory("category2", "Category 2");
    Collection blogEntries = dao.getBlogEntries(dailyBlog);
    SimpleDateFormat sdf = new SimpleDateFormat(FileBlogEntryDAO.NEW_PERSISTENT_DATETIME_FORMAT);

    assertEquals(1, blogEntries.size());

    BlogEntry blogEntry = (BlogEntry)blogEntries.iterator().next();

    // test that the blog entry properties were loaded okay
    assertEquals("Blog entry title", blogEntry.getTitle());
    assertEquals("<p>Blog entry excerpt.</p>", blogEntry.getExcerpt());
    assertEquals("<p>Blog entry body.</p>", blogEntry.getBody());
    assertEquals(1081203335000L, blogEntry.getDate().getTime());
    assertEquals("simon", blogEntry.getAuthor());
    assertTrue(blogEntry.isCommentsEnabled());
    assertTrue(blogEntry.isTrackBacksEnabled());
    assertEquals(2, blogEntry.getCategories().size());
    assertTrue(blogEntry.getCategories().contains(category1));
    assertTrue(blogEntry.getCategories().contains(category2));

    // now test the comments were loaded okay
    List comments = blogEntry.getComments();
    assertEquals(2, comments.size());
    Comment comment1 = (Comment)comments.get(0);
    assertEquals("Comment title 1", comment1.getTitle());
    assertEquals("<p>Comment 1.</p>", comment1.getBody());
    assertEquals("Comment author 1", comment1.getAuthor());
    assertEquals("me@author1.com", comment1.getEmail());
    assertEquals("http://www.author1.com", comment1.getWebsite());
    assertEquals("127.0.0.1", comment1.getIpAddress());
    assertEquals(sdf.parse("05 Apr 2004 23:27:30:0 +0100"), comment1.getDate());

    Comment comment2 = (Comment)comments.get(1);
    assertEquals("Re: " + blogEntry.getTitle(), comment2.getTitle());
    assertEquals("<p>Comment 2.</p>", comment2.getBody());
    assertEquals("Comment author 2", comment2.getAuthor());
    assertEquals("me@author2.com", comment2.getEmail());
    assertEquals("http://www.author2.com", comment2.getWebsite());
    assertEquals("192.168.0.1", comment2.getIpAddress());
    assertEquals(sdf.parse("05 Apr 2004 23:31:00:0 +0100"), comment2.getDate());

    // now test the Trackbacks were loaded okay
    List trackBacks = blogEntry.getTrackBacks();
    assertEquals(2, trackBacks.size());
    TrackBack trackBack1 = (TrackBack)trackBacks.get(0);
    assertEquals("TrackBack title 1", trackBack1.getTitle());
    assertEquals("TrackBack body 1.", trackBack1.getExcerpt());
    assertEquals("http://www.author1.com/entry", trackBack1.getUrl());
    assertEquals("Blog name 1", trackBack1.getBlogName());
    assertEquals("127.0.0.1", trackBack1.getIpAddress());
    assertEquals(sdf.parse("06 Apr 2004 07:09:24:0 +0100"), trackBack1.getDate());

    TrackBack trackBack2 = (TrackBack)trackBacks.get(1);
    assertEquals("TrackBack title 2", trackBack2.getTitle());
    assertEquals("TrackBack body 2.", trackBack2.getExcerpt());
    assertEquals("http://www.author2.com/entry", trackBack2.getUrl());
    assertEquals("Blog name 2", trackBack2.getBlogName());
    assertEquals("192.168.0.1", trackBack2.getIpAddress());
    assertEquals(sdf.parse("06 Apr 2004 07:09:24:0 +0100"), trackBack2.getDate());
  }

}
