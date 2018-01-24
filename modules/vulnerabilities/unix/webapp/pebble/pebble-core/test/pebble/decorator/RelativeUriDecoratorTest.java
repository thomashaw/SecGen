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
package pebble.decorator;

import pebble.blog.BlogEntry;
import pebble.blog.SimpleBlogTestCase;

/**
 * Tests for the RelativeUriDecorator class.
 *
 * @author    Simon Brown
 */
public class RelativeUriDecoratorTest extends SimpleBlogTestCase {

  private BlogEntryDecorator decorator;
  private BlogEntry blogEntry;
  private BlogEntryDecoratorContext context;

  private BlogEntryDecoratorChain chain;

  public void setUp() {
    super.setUp();

    blogEntry = rootBlog.getBlogForToday().createBlogEntry();
    decorator = new RelativeUriDecorator();
    chain = new BlogEntryDecoratorChain(null);
    context = new BlogEntryDecoratorContext();
    context.setBlogEntry(blogEntry);
  }

  /**
   * Tests that a relative URI pointing to an image is translated, in the body.
   */
  public void testImageUriInBody() throws Exception {
    blogEntry.setBody("Body - <img src=\"./images/someimage.jpg\">");
    decorator.decorate(chain, context);
    assertEquals("Body - <img src=\"http://www.yourdomain.com/blog/images/someimage.jpg\">", blogEntry.getBody());
  }

  /**
   * Tests that a relative URI pointing to a file is translated, in the body.
   */
  public void testFileUriInBody() throws Exception {
    blogEntry.setBody("Body - <a href=\"./files/someimage.zip\">");
    decorator.decorate(chain, context);
    assertEquals("Body - <a href=\"http://www.yourdomain.com/blog/files/someimage.zip\">", blogEntry.getBody());
  }

  /**
   * Tests that a relative URI pointing to an image is translated, in the excerpt.
   */
  public void testImageUriInExcerpt() throws Exception {
    blogEntry.setExcerpt("Excerpt - <img src=\"./images/someimage.jpg\">");
    decorator.decorate(chain, context);
    assertEquals("Excerpt - <img src=\"http://www.yourdomain.com/blog/images/someimage.jpg\">", blogEntry.getExcerpt());
  }

  /**
   * Tests that a relative URI pointing to a file is translated, in the excerpt.
   */
  public void testFileUriInExcerpt() throws Exception {
    blogEntry.setExcerpt("Excerpt - <a href=\"./files/someimage.zip\">");
    decorator.decorate(chain, context);
    assertEquals("Excerpt - <a href=\"http://www.yourdomain.com/blog/files/someimage.zip\">", blogEntry.getExcerpt());
  }

  /**
   * Tests that a relative URI pointing to an image is translated, in the excerpt.
   */
  public void testRelativeUrisNotChangedWhenMediaIsDesktopUI() throws Exception {
    context.setMedia(BlogEntryDecoratorContext.DESKTOP_UI);
    blogEntry.setBody("Body - <img src=\"./images/someimage.jpg\">");
    blogEntry.setExcerpt("Excerpt - <img src=\"./images/someimage.jpg\">");
    decorator.decorate(chain, context);
    assertEquals("Body - <img src=\"./images/someimage.jpg\">", blogEntry.getBody());
    assertEquals("Excerpt - <img src=\"./images/someimage.jpg\">", blogEntry.getExcerpt());
  }

}
