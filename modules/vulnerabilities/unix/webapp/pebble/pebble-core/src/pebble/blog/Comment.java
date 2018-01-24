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

import java.util.*;

import pebble.util.StringUtils;

/**
 * Represents a blog comment.
 *
 * @author    Simon Brown
 */
public class Comment extends BlogEntryResponse {

  /** the body of the comment */
  private String body;

  /** the name of the author */
  private String author;

  /** the author's e-mail address */
  private String email;

  /** the author's website */
  private String website;

  /** the parent comment, if applicable */
  private Comment parent;

  /** the collection of nested comments */
  private List comments = new ArrayList();

  /**
   * Creates a new comment with the specified properties.
   *
   * @param title        the comment title
   * @param body        the comment body
   * @param author      the name of the author
   * @param website     the author's website
   * @param ipAddress the IP address of the author
   * @param date        the date that this comment was left
   * @param blogEntry   the owning blog entry
   */
  Comment(String title, String body, String author, String email, String website, String ipAddress, Date date, BlogEntry blogEntry) {
    super(title, ipAddress, date, blogEntry);

    setBody(body);
    setAuthor(author);
    setEmail(email);
    setWebsite(website);
  }

  /**
   * Gets the body of this comment.
   *
   * @return    the body of this comment as a String
   */
  public String getBody() {
    return body;
  }

  /**
   * Gets the content of this response.
   *
   * @return a String
   */
  public String getContent() {
    return getBody();
  }

  /**
   * Gets the body of this comment, truncated.
   *
   * @return    the body of this comment as a String
   */
  public String getTruncatedBody() {
    if (body == null) {
      return "";
    } else if (body.length() <= 64) {
      return body;
    } else {
      return body.substring(0, 61) + "...";
    }
  }

  /**
   * Sets the title of this comment.
   *
   * @param   title    the title of this comment as a String
   */
  public void setTitle(String title) {
    if (title == null || title.length() == 0) {
      if (blogEntry != null) {
        this.title = "Re: " + blogEntry.getTitle();
      } else {
        this.title = "Comment";
      }
    } else {
      this.title = title;
    }
  }

  /**
   * Sets the body of this comment.
   *
   * @param   body    the body of this comment as a String
   */
  public void setBody(String body) {
    if (body == null || body.length() == 0) {
      this.body = null;
    } else {
      // strip all HTML out of the author name, website and comment body as it
      // may contain dodgy script
      this.body = body;
    }
  }

  /**
   * Gets the name of the author.
   *
   * @return    the name of the author as a String
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Sets the author of this blog comment. If an author isn't specified,
   * the author is set to be "Anonymous".
   *
   * @param author    the name of the author
   */
  public void setAuthor(String author) {
    if (author == null || author.length() == 0) {
      this.author = "Anonymous";
    } else {
      this.author = author;
    }
  }

  /**
   * Gets the author's e-mail address.
   *
   * @return    the author's e-mail address as a String
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the author's e-mail address.
   *
   * @param email   the e-mail address
   */
  public void setEmail(String email) {
    if (email == null || email.length() == 0) {
      this.email = null;
    } else {
      this.email = StringUtils.transformHTML(email);
    }
  }

  /**
   * Gets the author's website.
   *
   * @return    the author's website as a String
   */
  public String getWebsite() {
    return website;
  }

  /**
   * Sets the author's website.
   *
   * @param website   the website url
   */
  public void setWebsite(String website) {
    website = StringUtils.transformHTML(website);
    if (website == null || website.length() == 0) {
      this.website = null;
    } else if (
        !website.startsWith("http://") &&
        !website.startsWith("https://") &&
        !website.startsWith("ftp://") &&
        !website.startsWith("mailto:")) {
      this.website = "http://" + website;
    } else {
      this.website = website;
    }
  }

  /**
   * Gets the permalink for this comment.
   *
   * @return  a URL as a String
   */
  public String getPermalink() {
    if (blogEntry != null) {
      return blogEntry.getLocalPermalink() + "#comment" + getId();
    } else {
      return "";
    }
  }

  /**
   * Gets the owning comment, if this comment is nested.
   *
   * @return    a Comment instance, or null if this comment isn't nested
   */
  public Comment getParent() {
    return this.parent;
  }

  /**
   * Sets the owning comment.
   *
   * @param parent    the owning Comment instance
   */
  public void setParent(Comment parent) {
    this.parent = parent;
  }

  /**
   * Gets the number of parents that this comment has.
   *
   * @return  the number of parents as an int
   */
  public int getNumberOfParents() {
    int count = 0;
    Comment c = getParent();
    while (c != null) {
      count++;
      c = c.getParent();
    }

    return count;
  }

  /**
   * Adds a child comment.
   *
   * @param comment   the Comment to add
   */
  void addComment(Comment comment) {
    if (comment != null && !comments.contains(comment)) {
      comments.add(comment);
      comment.setParent(this);
    }
  }

  /**
   * Removes a child comment.
   *
   * @param comment   the Comment to be removed
   */
  void removeComment(Comment comment) {
    if (comment != null && comments.contains(comment)) {
      comments.remove(comment);
      comment.setParent(null);
    }
  }

  /**
   * Gets a list of comments, in the order that they were left.
   *
   * @return  a List of Comment instances
   */
  public List getComments() {
    return Collections.unmodifiableList(comments);
  }

  /**
   * Creates and returns a copy of this object.
   *
   * @return a clone of this instance.
   * @see Cloneable
   */
  public Object clone() {
    Comment comment = new Comment(title, body, author, email, website, ipAddress, date, blogEntry);
    comment.setParent(parent);
    Iterator it = getComments().iterator();
    while (it.hasNext()) {
      Comment nestedComment = (Comment)it.next();
      comment.addComment((Comment)nestedComment.clone());
    }

    return comment;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Comment)) {
      return false;
    }

    Comment comment = (Comment)o;
    return (getId() == comment.getId() && blogEntry.getId().equals(comment.getBlogEntry().getId()));
  }

  public int hashCode() {
    return blogEntry.getId().hashCode();
  }


}