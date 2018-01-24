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
package pebble.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of utility methods for manipulating strings.
 *
 * @author    Simon Brown
 */
public final class StringUtils {

  /**
   * Filters out characters that have meaning within JSP and HTML, and
   * replaces them with "escaped" versions.
   *
   * @param s   the String to filter
   * @return  the filtered String
   */
  public static String transformHTML(String s) {

    if (s == null) {
      return null;
    }

    StringBuffer buf = new StringBuffer(s.length());

    // loop through every character and replace if necessary
    int length = s.length();
    for (int i = 0; i < length; i++) {
      switch (s.charAt(i)) {
        case '<':
          buf.append("&lt;");
          break;
        case '>':
          buf.append("&gt;");
          break;
        default :
          buf.append(s.charAt(i));
      }
    }

    return buf.toString();
  }

  /**
   * Transforms the given String into a subset of HTML displayable on a web
   * page. The subset includes &lt;b&gt;, &lt;i&gt;, &lt;p&gt;, &lt;br&gt;,
   * &lt;pre&gt; and &lt;a href&gt; (and their corresponding end tags).
   *
   * @param s   the String to transform
   * @return    the transformed String
   */
  public static String transformToHTMLSubset(String s) {

    if (s == null) {
      return null;
    }

    // bold tags
    s = s.replaceAll("&lt;[bB]&gt;", "<b>");
    s = s.replaceAll("&lt;/[bB]&gt;", "</b>");

    // italic tags
    s = s.replaceAll("&lt;[iI]&gt;", "<i>");
    s = s.replaceAll("&lt;/[iI]&gt;", "</i>");

    // line break tags
    s = s.replaceAll("&lt;[bB][rR]&gt;", "<br>");
    s = s.replaceAll("&lt;[bB][rR]/&gt;", "<br/>");
    s = s.replaceAll("&lt;[bB][rR] /&gt;", "<br />");

    // paragraph tags
    s = s.replaceAll("&lt;[pP]&gt;", "<p>");
    s = s.replaceAll("&lt;/[pP]&gt;", "</p>");
    s = s.replaceAll("&lt;[pP]/&gt;", "<p/>");
    s = s.replaceAll("&lt;[pP] /&gt;", "<p />");

    // preformatted tags
    s = s.replaceAll("&lt;[pP][rR][eE]&gt;", "<pre>");
    s = s.replaceAll("&lt;/[pP][rR][eE]&gt;", "</pre>");

    // HTTP links
    s = s.replaceAll("&lt;/[aA]&gt;", "</a>");
    Pattern p = Pattern.compile("&lt;a href=[\"']http://\\S*[\"']&gt;");
    Matcher m = p.matcher(s);

    while (m.find()) {
      int start = m.start();
      int end = m.end();
      String link = s.substring(start, end);
      link = "<" + link.substring(4, link.length() - 4) + ">";
      s = s.substring(0, start) + link + s.substring(end, s.length());
      m = p.matcher(s);
    }

    return s;
  }

  /**
   * Filters out newline characters.
   *
   * @param s   the String to filter
   * @return  the filtered String
   */
  public static String filterNewlines(String s) {

    if (s == null) {
      return null;
    }

    StringBuffer buf = new StringBuffer(s.length());

    // loop through every character and replace if necessary
    int length = s.length();
    for (int i = 0; i < length; i++) {
      switch (s.charAt(i)) {
        case '\n':
          break;
        default :
          buf.append(s.charAt(i));
      }
    }

    return buf.toString();
  }

}