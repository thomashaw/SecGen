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
package pebble.filter;

import pebble.Constants;
import pebble.blog.*;
import pebble.mock.*;

/**
 * Tests for the ViewBlogFilter class.
 *
 * @author    Simon Brown
 */
public class CompositeViewBlogFilterTest extends CompositeBlogTestCase {

  private ViewBlogFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  public void setUp() {
    super.setUp();

    filter = new ViewBlogFilter();
    request = new MockHttpServletRequest();
    request.setContextPath("/somecontext");
    response = new MockHttpServletResponse();
  }

  public void testSimpleUrlsForMultiUserBlog() throws Exception {
    // test a short uri
    request.setRequestUri("/somecontext");
    filter.doFilter(request, response, null);
    assertEquals(blog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());

    // now a slightly longer uri (trailing / character)
    request.setRequestUri("/somecontext/");
    filter.doFilter(request, response, null);
    assertEquals(blog, request.getAttribute(Constants.BLOG_KEY));
    dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRequestAMultiUserBlog() throws Exception {
    // and also that we can access blog2
    request.setRequestUri("/somecontext/blog2/");
    filter.doFilter(request, response, null);
    assertEquals(blog2, (request.getAttribute(Constants.BLOG_KEY)));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/blog2/viewHomePage.action", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRequestAMultiUserBlogWithoutTrailingSlash() throws Exception {
    request.setRequestUri("/somecontext/blog1");
    filter.doFilter(request, response, null);
    assertEquals(blog1, (request.getAttribute(Constants.BLOG_KEY)));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/blog1/viewHomePage.action", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testStaticPermalinkUrlsForSingleUserBlog() throws Exception {
    request.setRequestUri("/somecontext/blog1/pages/some-story.html");
    filter.doFilter(request, response, null);
    assertEquals(blog1, request.getAttribute(Constants.BLOG_KEY));
    assertEquals(null, request.getAttribute(Constants.CURRENT_CATEGORY_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/blog1/viewStaticPage.action?name=some-story", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRssUrlForSingleUserBlog() throws Exception {
    request.setRequestUri("/somecontext/blog1/rss.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog1, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/blog1/feed.action?flavor=rss20", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRdfUrlForSingleUserBlog() throws Exception {
    request.setRequestUri("/somecontext/blog1/rdf.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog1, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/blog1/feed.action?flavor=rdf", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testAtomUrlForSingleUserBlog() throws Exception {
    request.setRequestUri("/somecontext/blog1/atom.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog1, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/blog1/feed.action?flavor=atom", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRssUrlForCompositeBlog() throws Exception {
    request.setRequestUri("/somecontext/rss.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/feed.action?flavor=rss20", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRdfUrlForCompositeBlog() throws Exception {
    request.setRequestUri("/somecontext/rdf.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/feed.action?flavor=rdf", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testAtomUrlForCompositeBlog() throws Exception {
    request.setRequestUri("/somecontext/atom.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/feed.action?flavor=atom", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testIncorrectRssUrlForCompositeBlog() throws Exception {
    request.setRequestUri("/somecontext/viewFeeds.action/rss.xml");
    filter.doFilter(request, response, null);
    assertEquals(blog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewFeeds.action/rss.xml", dispatcher.getUri());
  }

}
