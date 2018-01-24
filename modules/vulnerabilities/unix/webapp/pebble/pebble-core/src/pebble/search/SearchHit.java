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
package pebble.search;

import java.util.Date;

/**
 * A container for the results of a search (a search hit).
 *
 * @author    Simon Brown
 */
public class SearchHit {

  /** the id of the blog entry that matched the search query */
  private String id;

  /** the permalink of the blog entry that matched the search query */
  private String permalink;

  /** the title of the blog entry that matched the search query */
  private String title;

  /** the excerpt of the blog entry that matched the search query */
  private String excerpt;

  /** the date of the blog entry */
  private Date date;

  /** the score of the matched */
  private float score;

  /**
   * Creates a new instance with the specified message.
   */
  public SearchHit(String id, String permalink, String title, String excerpt, Date date, float score) {
    this.id = id;
    this.permalink = permalink;
    this.title = title;
    this.excerpt = excerpt;
    this.date = date;
    this.score = score;
  }

  /**
   * Gets the id for the matching blog entry.
   *
   * @return  the id as a String
   */
  public String getId() {
    return this.id;
  }

  /**
   * Gets the permalink for the matching blog entry.
   *
   * @return  a permalink as a String
   */
  public String getPermalink() {
    return this.permalink;
  }

  /**
   * Gets the title for the matching blog entry.
   *
   * @return  a title as a String
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the excerpt for the matching blog entry.
   *
   * @return  a excerpt as a String
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * Gets the date of the matching blog entry.
   *
   * @return  the date as a Date
   */
  public Date getDate() {
    return this.date;
  }

  /**
   * Gets the score for the matching blog entry.
   *
   * @return  a score as a float
   */
  public float getScore() {
    return this.score;
  }

}
