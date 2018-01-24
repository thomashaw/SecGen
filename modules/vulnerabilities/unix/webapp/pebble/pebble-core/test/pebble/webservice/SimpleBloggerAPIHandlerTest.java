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
package pebble.webservice;

import java.util.*;

import pebble.blog.*;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Tests for the BloggerAPIHandler class, when using a simple blog.
 *
 * @author    Simon Brown
 */
public class SimpleBloggerAPIHandlerTest extends SimpleBlogTestCase {

  private BloggerAPIHandler handler = new BloggerAPIHandler();

  public void setUp() {
    super.setUp();

    rootBlog.setProperty(SimpleBlog.XML_RPC_ENABLED_KEY, "true");
    rootBlog.setProperty(SimpleBlog.XML_RPC_USERNAME_KEY, "username");
    rootBlog.setProperty(SimpleBlog.XML_RPC_PASSWORD_KEY, "password");
  }

  public void testXmlRpcNotEnabled() {
    rootBlog.setProperty(SimpleBlog.XML_RPC_ENABLED_KEY, "false");
    try {
      handler.getPost("", "123", "", "");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("XML-RPC not enabled for this blog.", xmlrpce.getMessage());
    }
  }

  public void testAuthenticationFailure() {
    rootBlog.setProperty(SimpleBlog.XML_RPC_USERNAME_KEY, "username");
    rootBlog.setProperty(SimpleBlog.XML_RPC_PASSWORD_KEY, "someIncorrectPassword");
    try {
      handler.getUserInfo("", "username", "password");
      fail();
    } catch (XmlRpcAuthenticationException xmlrpcae) {
    } catch (XmlRpcException xmlrpce) {
      fail();
    }
    try {
      handler.deletePost("", "123", "username", "password", true);
      fail();
    } catch (XmlRpcAuthenticationException xmlrpcae) {
    } catch (XmlRpcException xmlrpce) {
      fail();
    }
    try {
      handler.editPost("", "123", "username", "password", "content", true);
      fail();
    } catch (XmlRpcAuthenticationException xmlrpcae) {
    } catch (XmlRpcException xmlrpce) {
      fail();
    }
    try {
      handler.getPost("", "123", "username", "password");
      fail();
    } catch (XmlRpcAuthenticationException xmlrpcae) {
    } catch (XmlRpcException xmlrpce) {
      fail();
    }
    try {
      handler.getRecentPosts("", "", "username", "password", 10);
      fail();
    } catch (XmlRpcAuthenticationException xmlrpcae) {
    } catch (XmlRpcException xmlrpce) {
      fail();
    }
    try {
      handler.newPost("", "", "username", "password", "content", true);
      fail();
    } catch (XmlRpcAuthenticationException xmlrpcae) {
    } catch (XmlRpcException xmlrpce) {
      fail();
    }
  }

  public void testAuthenticationSuccess() {
    try {
      handler.deletePost("", "123", "username", "password", true);
    } catch (XmlRpcAuthenticationException xmlrpcae) {
      fail();
    } catch (XmlRpcException xmlrpce) {
    }
    try {
      handler.editPost("", "123", "username", "password", "content", true);
    } catch (XmlRpcAuthenticationException xmlrpcae) {
      fail();
    } catch (XmlRpcException xmlrpce) {
    }
    try {
      handler.getPost("", "123", "username", "password");
    } catch (XmlRpcAuthenticationException xmlrpcae) {
      fail();
    } catch (XmlRpcException xmlrpce) {
    }
    try {
      handler.getRecentPosts("", "", "username", "password", 10);
    } catch (XmlRpcAuthenticationException xmlrpcae) {
      fail();
    } catch (XmlRpcException xmlrpce) {
    }
    try {
      handler.getUsersBlogs("", "username", "password");
    } catch (XmlRpcAuthenticationException xmlrpcae) {
      fail();
    } catch (XmlRpcException xmlrpce) {
    }
    try {
      handler.newPost("", "", "username", "password", "<title>title</title>content", true);
    } catch (XmlRpcAuthenticationException xmlrpcae) {
      fail();
    } catch (XmlRpcException xmlrpce) {
    }
  }

  public void testGetRecentPostsFromEmptyBlog() {
    try {
      Vector posts = handler.getRecentPosts("appkey", "", "username", "password", 3);
      assertTrue(posts.isEmpty());
    } catch (Exception e) {
      fail();
    }
  }

  public void testGetRecentPosts() {
    try {
      Calendar cal1 = rootBlog.getCalendar();
      cal1.set(Calendar.HOUR_OF_DAY, 2);
      Calendar cal2 = rootBlog.getCalendar();
      cal2.set(Calendar.HOUR_OF_DAY, 3);
      Calendar cal3 = rootBlog.getCalendar();
      cal3.set(Calendar.HOUR_OF_DAY, 4);
      Calendar cal4 = rootBlog.getCalendar();
      cal4.set(Calendar.HOUR_OF_DAY, 5);

      DailyBlog today = rootBlog.getBlogForToday();
      BlogEntry entry1 = today.createBlogEntry("title1", "body1", cal1.getTime());
      today.addEntry(entry1);
      BlogEntry entry2 = today.createBlogEntry("title2", "body2", cal2.getTime());
      today.addEntry(entry2);
      BlogEntry entry3 = today.createBlogEntry("title3", "body3", cal3.getTime());
      today.addEntry(entry3);
      BlogEntry entry4 = today.createBlogEntry("title4", "body4", cal4.getTime());
      today.addEntry(entry4);
      Vector posts = handler.getRecentPosts("appkey", "", "username", "password", 3);

      assertFalse(posts.isEmpty());
      assertEquals(3, posts.size());
      Hashtable ht = (Hashtable)posts.get(0);
      assertEquals(entry4.getId(), ht.get(BloggerAPIHandler.POST_ID));
      assertEquals("<title>title4</title><category></category>body4", ht.get(BloggerAPIHandler.CONTENT));
      ht = (Hashtable)posts.get(1);
      assertEquals(entry3.getId(), ht.get(BloggerAPIHandler.POST_ID));
      assertEquals("<title>title3</title><category></category>body3", ht.get(BloggerAPIHandler.CONTENT));
      ht = (Hashtable)posts.get(2);
      assertEquals(entry2.getId(), ht.get(BloggerAPIHandler.POST_ID));
      assertEquals("<title>title2</title><category></category>body2", ht.get(BloggerAPIHandler.CONTENT));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testGetPost() {
    try {
      DailyBlog today = rootBlog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();
      entry.setTitle("title");
      entry.setBody("body");
      entry.setAuthor("simon");
      today.addEntry(entry);

      Hashtable post = handler.getPost("appkey", entry.getId(), "username", "password");
      assertEquals("<title>title</title><category></category>body", post.get(BloggerAPIHandler.CONTENT));
      assertEquals(entry.getAuthor(), post.get(BloggerAPIHandler.USER_ID));
      assertEquals(entry.getDate(), post.get(BloggerAPIHandler.DATE_CREATED));
      assertEquals(entry.getId(), post.get(BloggerAPIHandler.POST_ID));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testGetPostWithCategory() {
    try {
      DailyBlog today = rootBlog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();
      entry.setTitle("title");
      entry.setBody("body");
      entry.setAuthor("simon");
      entry.addCategory(new Category("java", "Java"));
      today.addEntry(entry);

      Hashtable post = handler.getPost("appkey", entry.getId(), "username", "password");
      assertEquals("<title>title</title><category>java</category>body", post.get(BloggerAPIHandler.CONTENT));
      assertEquals(entry.getAuthor(), post.get(BloggerAPIHandler.USER_ID));
      assertEquals(entry.getDate(), post.get(BloggerAPIHandler.DATE_CREATED));
      assertEquals(entry.getId(), post.get(BloggerAPIHandler.POST_ID));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testGetPostWithIdThatDoesntExist() {
    String postid = "1234567890123";
    try {
      handler.getPost("appkey", postid, "username", "password");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + postid + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testGetPostWithNullId() {
    String postid = null;
    try {
      handler.getPost("appkey", postid, "username", "password");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + null + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testNewPostWithTitleAndCategory() {
    try {
      Category category = rootBlog.getBlogCategoryManager().addCategory("aCategory", "A Category");

      String postid = handler.newPost("appkey", "", "username", "password", "<title>Title</title><category>aCategory</category><p>Content</p>", true);

      BlogEntry entry = (BlogEntry)rootBlog.getRecentBlogEntries(1).get(0);
      assertEquals(entry.getId(), postid);
      assertEquals("Title", entry.getTitle());
      assertTrue(entry.inCategory(category));
      assertEquals("<p>Content</p>", entry.getBody());
      assertEquals("username", entry.getAuthor());

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testNewPostWithTitleAndCategories() {
    try {
      Category category1 = rootBlog.getBlogCategoryManager().addCategory("category1", "Category 1");
      Category category2 = rootBlog.getBlogCategoryManager().addCategory("category2", "Category 2");

      String postid = handler.newPost("appkey", "", "username", "password", "<title>Title</title><category>category1, category2</category><p>Content</p>", true);

      BlogEntry entry = (BlogEntry)rootBlog.getRecentBlogEntries(1).get(0);
      assertEquals(entry.getId(), postid);
      assertEquals("Title", entry.getTitle());
      assertTrue(entry.inCategory(category1));
      assertTrue(entry.inCategory(category2));
      assertEquals("<p>Content</p>", entry.getBody());
      assertEquals("username", entry.getAuthor());

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testNewPostWithoutTitle() {
    try {
      String postid = handler.newPost("appkey", "", "username", "password", "<p>Content</p>", true);

      BlogEntry entry = (BlogEntry)rootBlog.getRecentBlogEntries(1).get(0);
      assertEquals(entry.getId(), postid);
      assertEquals("", entry.getTitle());
      assertEquals("<p>Content</p>", entry.getBody());
      assertEquals("username", entry.getAuthor());

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testEditPost() {
    try {
      DailyBlog today = rootBlog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();
      entry.setTitle("title");
      entry.setBody("body");
      today.addEntry(entry);
      boolean result = handler.editPost("appkey", entry.getId(), "username", "password", "<title>Title</title><p>Content</p>", true);

      assertTrue(result);
      assertEquals("Title", entry.getTitle());
      assertEquals("<p>Content</p>", entry.getBody());
      assertEquals("username", entry.getAuthor());

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testEditPostWithNullId() {
    String postid = null;
    try {
      handler.editPost("appkey", postid, "username", "password", "<title>Title</title><p>Content</p>", true);
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + postid + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testEditPostWithIdThatDoesntExist() {
    String postid = "1234567890123";
    try {
      handler.editPost("appkey", postid, "username", "password", "<title>Title</title><p>Content</p>", true);
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + postid + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testDeletePost() {
    try {
      DailyBlog today = rootBlog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();
      entry.setTitle("title");
      entry.setBody("body");
      entry.setAuthor("simon");
      today.addEntry(entry);

      assertTrue(today.hasEntries());
      boolean result = handler.deletePost("appkey", entry.getId(), "username", "password", true);
      assertTrue("deletePost() returned false instead of true", result);
      assertFalse(today.hasEntries());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testDeletePostWithIdThatDoesntExist() {
    String postid = "1234567890123";
    try {
      handler.deletePost("appkey", postid, "username", "password", true);
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + postid + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testDeletePostWithNullId() {
    String postid = null;
    try {
      handler.deletePost("appkey", postid, "username", "password", true);
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + null + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testGetUserInfo() {
    try {
      Hashtable userInfo = handler.getUserInfo("appkey", "username", "password");
      assertEquals("username", userInfo.get("userid"));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testGetUsersBlogs() {
    try {
      Vector blogs = handler.getUsersBlogs("appkey", "username", "password");
      assertEquals(1, blogs.size());

      Hashtable blog = (Hashtable)blogs.get(0);
      assertEquals("http://www.yourdomain.com/blog/", blog.get("url"));
      assertEquals("blog", blog.get("blogid"));
      assertEquals("My blog", blog.get("blogName"));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testGetTemplate() {
    try {
      handler.getTemplate("appkey", "blogid", "username", "password", "templateType");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("getTemplate is not supported by Pebble.", xmlrpce.getMessage());
    }
  }

  public void testSetTemplate() {
    try {
      handler.setTemplate("appkey", "blogid", "username", "password", "template", "templateType");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("setTemplate is not supported by Pebble.", xmlrpce.getMessage());
    }
  }

  public void testAddCategory() {
    try {
      DailyBlog today = rootBlog.getBlogForToday();
      BlogEntry entry = today.createBlogEntry();
      today.addEntry(entry);
      rootBlog.getBlogCategoryManager().addCategory("aCategory", "A Category");

      boolean result = handler.addCategory("appkey", entry.getId(), "username", "password", "aCategory");
      assertTrue("Category wasn't added", result);
      assertTrue(entry.inCategory(rootBlog.getBlogCategoryManager().getCategory("aCategory")));

      result = handler.addCategory("appkey", entry.getId(), "username", "password", "aNonExistentCategory");
      assertFalse("Category was added", result);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testAddCategoryWithIdThatDoesntExist() {
    String postid = "1234567890123";
    try {
      handler.addCategory("appkey", postid, "username", "password", "aCategory");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + postid + " was not found.", xmlrpce.getMessage());
    }
  }

  public void testAddCategoryWithNullId() {
    String postid = null;
    try {
      handler.addCategory("appkey", postid, "username", "password", "aCategory");
      fail();
    } catch (XmlRpcException xmlrpce) {
      assertEquals("Blog entry with ID of " + null + " was not found.", xmlrpce.getMessage());
    }
  }

}
