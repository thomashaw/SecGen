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
package pebble.logging;

/**
 * Represents a visited or referer URL along with a count of how many times
 * that URL has been accessed/referred from.
 *
 * @author    Simon Brown
 */
public abstract class CountedUrl {

  /** the maximum length of the name */
  public static final int NAME_LENGTH_LIMIT = 80;

  /** the URL as a String */
  private String url;

  /** the displayable name for the URL */
  private String name;

  /** the number of times that the url has been accessed/referred from */
  private int count;

  /**
   * Creates a new CountedUrl representing the specified url.
   *
   * @param url   the url as a String
   */
  public CountedUrl(String url) {
    setUrl(url);
    this.count = 1;
  }

  /**
   * Gets the underlying url.
   *
   * @return    the url as a String
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the underlying url.
   *
   * @param url   the url as a String
   */
  protected void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets a name representation of the url. This is just the url, but truncated
   * to a maximum number of characters.
   *
   * @return    a String
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name    the name as a String
   */
  protected void setName(String name) {
    this.name = name;
  }

  /**
   * Gets a name representation of the url. This is just the url, but truncated
   * to a maximum number of characters.
   *
   * @return    a String
   */
  public String getTruncatedName() {
    String s = getName();
    if (s.length() <= NAME_LENGTH_LIMIT) {
      return s;
    } else {
      return s.substring(0, NAME_LENGTH_LIMIT - 3) + "...";
    }
  }

  /**
   * Gets the count associated with this url.
   *
   * @return    the count as an int
   */
  public int getCount() {
    return count;
  }

  /**
   * Adds one to the count.
   */
  public synchronized void incrementCount() {
    count++;
  }

  /**
   * Implementation of the hashCode() method.
   *
   * @return  the hashcode of the underlying url
   */
  public int hashCode() {
    return url == null ? 0 : url.hashCode();
  }

  /**
   * Determines whether this object is equal to another.
   *
   * @param o   the object to test against
   * @return  true if the specified object is the same as this one (i.e. the
   *          underlying urls match, false otherwise
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CountedUrl)) return false;

    CountedUrl cUrl = (CountedUrl)o;

    if (url == null && cUrl.getUrl() == null) {
      return true;
    } else {
      return (url != null && url.equals(cUrl.getUrl()));
    }
  }

}