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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * Represents a referer URL along with a count of how many times
 * it has been refered from.
 *
 * @author    Simon Brown
 */
public class Referer extends CountedUrl {

  /** regular expression to pull out the query from a Google referer */
  private static final Pattern GOOGLE_QUERY_STRING_PATTERN = Pattern.compile("[?&]q=[^&]+&*");

  /** the prefix for all Google referers */
  private static final String GOOGLE_PREFIX = "http://www.google.";

  /**
   * Creates a new instance representing the specified url.
   *
   * @param url   the url as a String
   */
  public Referer(String url) {
    super(url);
  }

  protected void setUrl(String url) {
    super.setUrl(url);

    if (url == null || url.length() == 0) {
      setName("None");
    } else if (url.length() > GOOGLE_PREFIX.length() &&
        url.substring(0, GOOGLE_PREFIX.length()).equalsIgnoreCase(GOOGLE_PREFIX)) {
      Matcher m = GOOGLE_QUERY_STRING_PATTERN.matcher(url);

      String query = "";
      if (m.find()) {
        int start = m.start();
        int end = m.end();
        query = url.substring(start+3, end);
        if (query.endsWith("&")) {
          query = query.substring(0, query.length()-1);
        }
        try {
          query = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
      }

      setName("Google : " + query);
    } else {
      setName(url);
    }
  }

}