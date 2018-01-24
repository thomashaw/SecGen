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

import java.util.Calendar;

import pebble.blog.*;

/**
 * Tests for the BlogEntryComparator class.
 *
 * @author    Simon Brown
 */
public class BlogEntryComparatorTest extends SimpleBlogTestCase {

  public void testCompare() {
    BlogEntryComparator comp = new BlogEntryComparator();
    DailyBlog dailyBlog = rootBlog.getBlogForDay(2003, 04, 01);

    Calendar cal1 = rootBlog.getCalendar();
    cal1.set(Calendar.HOUR_OF_DAY, 9);
    Calendar cal2 = rootBlog.getCalendar();
    cal2.set(Calendar.HOUR_OF_DAY, 10);

    BlogEntry b1 = dailyBlog.createBlogEntry("Title 1", "Body 1", cal1.getTime());
    BlogEntry b2 = dailyBlog.createBlogEntry("Title 2", "Body 2", cal2.getTime());

    assertTrue(comp.compare(b1, b1) == 0);
    assertTrue(comp.compare(b1, b2) != 0);
    assertTrue(comp.compare(b1, b2) > 0);
    assertTrue(comp.compare(b2, b1) < 0);
  }

}
