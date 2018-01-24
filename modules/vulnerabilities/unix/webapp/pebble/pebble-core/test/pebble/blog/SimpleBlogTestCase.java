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
 * Superclass for test cases that make uses of simple blogs.
 *
 * @author    Simon Brown
 */
public abstract class SimpleBlogTestCase extends PebbleTestCase {

  protected SimpleBlog rootBlog;

  public void setUp() {
    DAOFactory.setConfiguredFactory(new MockDAOFactory());
    rootBlog = new SimpleBlog(TEST_BLOG_LOCATION.getAbsolutePath());
    rootBlog.setUrl("");
    Theme theme = new Theme(rootBlog, "custom", new File(System.getProperty("java.io.tmpdir"), "pebble").getAbsolutePath());
    rootBlog.setEditableTheme(theme);

    BlogManager.getInstance().setBaseUrl("http://www.yourdomain.com/blog/");
    BlogManager.getInstance().setMultiUser(false);
    BlogManager.getInstance().setBlog(rootBlog);
  }

}