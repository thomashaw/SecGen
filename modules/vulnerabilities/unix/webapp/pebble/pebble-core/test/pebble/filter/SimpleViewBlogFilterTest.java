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

import javax.servlet.ServletException;

import pebble.Constants;
import pebble.AuthenticatedUser;
import pebble.blog.*;
import pebble.mock.*;

/**
 * Tests for the ViewBlogFilter class.
 *
 * @author    Simon Brown
 */
public class SimpleViewBlogFilterTest extends SimpleBlogTestCase {

  private ViewBlogFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  public void setUp() {
    super.setUp();

    filter = new ViewBlogFilter();
    request = new MockHttpServletRequest();
    request.setContextPath("/somecontext");
    request.setRequestUri("/somecontext/");
    response = new MockHttpServletResponse();
  }

  public void testSimpleUrlsForSingleUserBlog() throws Exception {
    // test a short uri
    request.setRequestUri("/somecontext");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewHomePage.action", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());

    // now a slightly longer uri (trailing / character)
    request.setRequestUri("/somecontext/");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewHomePage.action", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRssUrlForSingleUserBlog() throws Exception {
    // test the URL that points to the RSS feed for the blog
    request.setRequestUri("/somecontext/rss.xml");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/feed.action?flavor=rss20", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testRdfUrlForSingleUserBlog() throws Exception {
    // test the URL that points to the RDF feed for the blog
    request.setRequestUri("/somecontext/rdf.xml");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/feed.action?flavor=rdf", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testAtomUrlForSingleUserBlog() throws Exception {
    // test the URL that points to the Atom feed for the blog
    request.setRequestUri("/somecontext/atom.xml");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/feed.action?flavor=atom", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testIncorrectRssUrlForSingleUserBlog() throws Exception {
    // test the URL that points to the RSS feed for the blog
    request.setRequestUri("/somecontext/viewFeeds.action/rss.xml");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewFeeds.action/rss.xml", dispatcher.getUri());
  }

  public void testRandomUrlForSingleUserBlog() throws Exception {
    // test a random uri that doesn't point to anything special
    request.setRequestUri("/somecontext/somerandompage.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/somerandompage.html", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testImageUrlForSingleUserBlog() throws Exception {
    // test a uri that points to an image located within the blog
    request.setRequestUri("/somecontext/images/myImage.jpg");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/file.action?type=" + FileMetaData.BLOG_IMAGE + "&name=/myImage.jpg", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testFileUrlForSingleUserBlog() throws Exception {
    // test a uri that points to an image located within the blog
    request.setRequestUri("/somecontext/files/myFile.zip");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/file.action?type=" + FileMetaData.BLOG_FILE + "&name=/myFile.zip", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testMonthlyUrlForSingleUserBlog() throws Exception {
    // test a url to request a whole month
    request.setRequestUri("/somecontext/2003/11.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewMonthlyBlog.action?year=2003&month=11", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testDailyUrlForSingleUserBlog() throws Exception {
    // test a url to request a whole day
    request.setRequestUri("/somecontext/2003/11/24.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewDailyBlog.action?year=2003&month=11&day=24", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testPermalinkUrlsForSingleUserBlog() throws Exception {
    // test a url to request a single blog entry (long)
    request.setRequestUri("/somecontext/2003/11/24/1234567890123.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    assertEquals(null, request.getAttribute(Constants.CURRENT_CATEGORY_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewBlogEntry.action?entryId=1234567890123", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());

    // test a url to request a single blog entry (short)
    request.setRequestUri("/somecontext/1234567890123.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    assertEquals(null, request.getAttribute(Constants.CURRENT_CATEGORY_KEY));
    dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewBlogEntry.action?entryId=1234567890123", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testStaticPermalinkUrlsForSingleUserBlog() throws Exception {
    // test a url to request a "static" permalink
    request.setRequestUri("/somecontext/pages/some-story.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    assertEquals(null, request.getAttribute(Constants.CURRENT_CATEGORY_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/viewStaticPage.action?name=some-story", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testUrlEndingWithSlashForSingleUserBlog() throws Exception {
    // test a url to request a single blog entry (long)
    request.setRequestUri("/somecontext/2003/");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/2003/", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

  public void testUnauthenticated() throws Exception {
    request.setUserPrincipal(null);
    filter.doFilter(request, response, null);
    assertNull(request.getSession().getAttribute(Constants.AUTHENTICATED_USER));
  }

  public void testAuthenticatedWithNoRoles() throws Exception {
    request.setUserPrincipal(new MockPrincipal("simon"));
    filter.doFilter(request, response, null);

    // and also check that an AuthenticatedUser object has been created
    AuthenticatedUser user = (AuthenticatedUser)request.getSession().getAttribute(Constants.AUTHENTICATED_USER);
    assertEquals("simon", user.getName());
    assertFalse(user.isBlogOwner());
    assertFalse(user.isBlogContributor());
  }

  public void testAuthenticatedBlogOwner() throws Exception {
    MockPrincipal principal = new MockPrincipal("simon");
    principal.addRole(Constants.BLOG_OWNER_ROLE);
    request.setUserPrincipal(principal);
    filter.doFilter(request, response, null);

    // and also check that an AuthenticatedUser object has been created
    AuthenticatedUser user = (AuthenticatedUser)request.getSession().getAttribute(Constants.AUTHENTICATED_USER);
    assertEquals("simon", user.getName());
    assertTrue(user.isBlogOwner());
    assertFalse(user.isBlogContributor());
  }

  public void testAuthenticatedBlogContributor() throws Exception {
    MockPrincipal principal = new MockPrincipal("simon");
    principal.addRole(Constants.BLOG_CONTRIBUTOR_ROLE);
    request.setUserPrincipal(principal);
    filter.doFilter(request, response, null);

    // and also check that an AuthenticatedUser object has been created
    AuthenticatedUser user = (AuthenticatedUser)request.getSession().getAttribute(Constants.AUTHENTICATED_USER);
    assertEquals("simon", user.getName());
    assertFalse(user.isBlogOwner());
    assertTrue(user.isBlogContributor());
  }

  public void testCategoryPermalink() throws Exception {
    rootBlog.getBlogCategoryManager().addCategory("category1", "Category 1");
    request.setRequestUri("/somecontext/categories/category1.html");
    filter.doFilter(request, response, null);
    assertEquals(rootBlog, request.getAttribute(Constants.BLOG_KEY));
    MockRequestDispatcher dispatcher = (MockRequestDispatcher)request.getRequestDispatcher();
    assertEquals("/changeCategory.action?category=category1", dispatcher.getUri());
    assertTrue(dispatcher.wasForwarded());
  }

}
