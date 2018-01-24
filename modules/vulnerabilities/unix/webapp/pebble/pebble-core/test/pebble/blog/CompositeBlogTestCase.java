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

import junit.framework.TestCase;
import pebble.blog.persistence.DAOFactory;
import pebble.blog.persistence.mock.MockDAOFactory;

/**
 * Superclass for test cases that make uses of composite blogs.
 *
 * @author    Simon Brown
 */
public abstract class CompositeBlogTestCase extends PebbleTestCase {

  protected static final File TEST_BLOG_LOCATION = new File(System.getProperty("java.io.tmpdir"), "pebble");

  protected CompositeBlog blog;
  protected SimpleBlog blog1, blog2;

  public void setUp() {
    DAOFactory.setConfiguredFactory(new MockDAOFactory());

    blog = new CompositeBlog(TEST_BLOG_LOCATION.getAbsolutePath());
    //blog.setUrl("blog1");

    BlogManager.getInstance().setBaseUrl("http://www.yourdomain.com/blog/");
    BlogManager.getInstance().setMultiUser(true);
    BlogManager.getInstance().setBlog(blog);

    // and set up some simple blogs
    blog1 = new SimpleBlog(TEST_BLOG_LOCATION.getAbsolutePath());
    blog1.setId("blog1");
    blog.addBlog(blog1);
    blog2 = new SimpleBlog(TEST_BLOG_LOCATION.getAbsolutePath());
    blog2.setId("blog2");
    blog.addBlog(blog2);
  }

}