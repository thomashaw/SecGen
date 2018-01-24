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
package pebble.comparator;

import java.util.Date;

import pebble.blog.BlogEntry;
import pebble.blog.DailyBlog;
import pebble.blog.SimpleBlogTestCase;
import pebble.blog.Comment;

/**
 * Tests for the BlogEntryResponseByDateComparator class.
 *
 * @author    Simon Brown
 */
public class BlogEntryResponseByDateComparatorTest extends SimpleBlogTestCase {

  public void testCompare() {
    BlogEntryResponseByDateComparator comp = new BlogEntryResponseByDateComparator();
    DailyBlog dailyBlog = rootBlog.getBlogForToday();
    BlogEntry b1 = dailyBlog.createBlogEntry();

    Comment c1 = b1.createComment("", "", "", "", "", "", new Date(1000));
    Comment c2 = b1.createComment("", "", "", "", "", "", new Date(2000));

    assertTrue(comp.compare(c1, c1) == 0);
    assertTrue(comp.compare(c1, c2) != 0);
    assertTrue(comp.compare(c1, c2) > 0);
    assertTrue(comp.compare(c2, c1) < 0);
  }

}
