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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.blog.BlogManager;
import pebble.blog.SimpleBlog;

/**
 * A handler for the XML-RPC blogging APIs.
 *
 * @author    Simon Brown
 */
public abstract class AbstractAPIHandler {

  /** character used to separate blog and post IDs in multi-user mode */
  static final char BLOG_ID_SEPARATOR = '/';

  /**
   * A helper method to authenticate a username/password pair against the
   * properties for the specified SimpleBlog instance.
   *
   * @param blog      the SimpleBlog instance to test against
   * @param username  the username used for logging in via XML-RPC
   * @param password  the password used for logging in via XML-RPC
   * @return  true if the username/password will pass authentication
   *          for the specified blog, false otherwise
   */
  protected boolean authenticate(SimpleBlog blog, String username, String password) {
    return (blog.getXmlRpcUsername().equals(username) && blog.getXmlRpcPassword().equals(password));
  }

  /**
   * Gets the blog ID from a given String.
   * <br /><br />
   * In single-user mode, blog IDs are irrelevant since there is only one blog.
   * In multi-user mode, the post ID is composed of "blog ID/post ID"
   * (this is Pebble's way of uniquely identifying a blog entry across all
   * users' blogs).
   *
   * @param s   the String containing the post ID
   * @return  the post ID (blog entry ID)
   */
  protected String getBlogId(String s) {
    if (s == null) {
      return null;
    }

    if (BlogManager.getInstance().isMultiUser()) {
      int index = s.lastIndexOf(BLOG_ID_SEPARATOR);
      return s.substring(0, index);
    } else {
      // running in single user mode, there is only one blog
      // so the return value doesn't matter
      return "";
    }
  }

  /**
   * Gets the post ID (blog entry ID) from a given String.
   * <br /><br />
   * In single-user mode, post IDs
   * are specified as just the blog ID. In multi-user mode, the post ID
   * is composed of "blog ID/post ID" (this is Pebble's way of uniquely
   * identifying a blog entry across all users' blogs).
   *
   * @param s   the String containing the post ID
   * @return  the post ID (blog entry ID)
   */
  protected String getPostId(String s) {
    if (s == null) {
      return null;
    }

    if (BlogManager.getInstance().isMultiUser()) {
      int index = s.lastIndexOf(BLOG_ID_SEPARATOR);
      return s.substring(index+1);
    } else {
      return s;
    }
  }

  /**
   * Formats a post ID for the blogger client.
   *
   * @param blogid    the blog ID
   * @param postid    the post ID
   * @return  if running in multi-user mode, returns "blogid/postid",
   *          otherwise just returns "postid"
   */
  protected String formatPostId(String blogid, String postid) {
    if (BlogManager.getInstance().isMultiUser()) {
      return blogid + BLOG_ID_SEPARATOR + postid;
    } else {
      return postid;
    }
  }

}