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
public class EscapeMarkupDecoratorTest extends SimpleBlogTestCase {

  private BlogEntryDecorator decorator;
  private BlogEntryDecoratorChain chain;
  private BlogEntryDecoratorContext context;
  private BlogEntry blogEntry;

  public void setUp() {
    super.setUp();

    decorator = new EscapeMarkupDecorator();
    blogEntry = rootBlog.getBlogForToday().createBlogEntry();
    chain = new BlogEntryDecoratorChain(null);
    context = new BlogEntryDecoratorContext();
    context.setBlogEntry(blogEntry);
  }

  /**
   * Tests that text in the body between <escape> tags is escaped.
   */
  public void testEscapeTextInBody() throws Exception {
    blogEntry.setBody(null);
    decorator.decorate(chain, context);
    assertEquals("", blogEntry.getBody());

    blogEntry.setBody("");
    decorator.decorate(chain, context);
    assertEquals("", blogEntry.getBody());

    blogEntry.setBody("Here is some <b>HTML</b>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some <b>HTML</b>.", blogEntry.getBody());

    blogEntry.setBody("Here is some <escape><b>escaped HTML</b></escape>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some &lt;b&gt;escaped HTML&lt;/b&gt;.", blogEntry.getBody());

    blogEntry.setBody("Here is some <escape><b>escaped HTML</b></escape> and <escape><i>some more</i></escape>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some &lt;b&gt;escaped HTML&lt;/b&gt; and &lt;i&gt;some more&lt;/i&gt;.", blogEntry.getBody());

    blogEntry.setBody("Here is some <escape><b>escaped\n" +
        "HTML</b></escape>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some &lt;b&gt;escaped\nHTML&lt;/b&gt;.", blogEntry.getBody());

    blogEntry.setBody("abc <escape></escape> def");
    decorator.decorate(chain, context);
    assertEquals("abc <escape></escape> def", blogEntry.getBody());
  }

  /**
   * Tests that text in the excerpt between <escape> tags is escaped.
   */
  public void testEscapeTextInExcerpt() throws Exception {
    blogEntry.setExcerpt(null);
    decorator.decorate(chain, context);
    assertEquals("", blogEntry.getExcerpt());

    blogEntry.setExcerpt("");
    decorator.decorate(chain, context);
    assertEquals("", blogEntry.getExcerpt());

    blogEntry.setExcerpt("Here is some <b>HTML</b>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some <b>HTML</b>.", blogEntry.getExcerpt());

    blogEntry.setExcerpt("Here is some <escape><b>escaped HTML</b></escape>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some &lt;b&gt;escaped HTML&lt;/b&gt;.", blogEntry.getExcerpt());

    blogEntry.setExcerpt("Here is some <escape><b>escaped HTML</b></escape> and <escape><i>some more</i></escape>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some &lt;b&gt;escaped HTML&lt;/b&gt; and &lt;i&gt;some more&lt;/i&gt;.", blogEntry.getExcerpt());

    blogEntry.setExcerpt("Here is some <escape><b>escaped\n" +
        "HTML</b></escape>.");
    decorator.decorate(chain, context);
    assertEquals("Here is some &lt;b&gt;escaped\nHTML&lt;/b&gt;.", blogEntry.getExcerpt());

    blogEntry.setExcerpt("abc <escape></escape> def");
    decorator.decorate(chain, context);
    assertEquals("abc <escape></escape> def", blogEntry.getExcerpt());
  }

}
