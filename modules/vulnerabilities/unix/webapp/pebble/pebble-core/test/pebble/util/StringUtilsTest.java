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

import junit.framework.TestCase;

/**
 * Tests for the utilities in the StringUtils class.
 *
 * @author    Simon Brown
 */
public class StringUtilsTest extends TestCase {

  public void testTransformHTML() {
    assertNull(StringUtils.transformHTML(null));
    assertEquals("Here is some text", StringUtils.transformHTML("Here is some text"));
    assertEquals("Here is a &lt; symbol", StringUtils.transformHTML("Here is a < symbol"));
    assertEquals("Here is a &gt; symbol", StringUtils.transformHTML("Here is a > symbol"));
    assertEquals("&lt;a href=\"http://www.google.com\"&gt;Google&lt;/a&gt;", StringUtils.transformHTML("<a href=\"http://www.google.com\">Google</a>"));
  }

  public void testTransformToHTMLSubset() {
    assertNull(StringUtils.transformToHTMLSubset(null));
    assertEquals("Here is some text", StringUtils.transformToHTMLSubset("Here is some text"));
    assertEquals("Here is a &lt; symbol", StringUtils.transformToHTMLSubset("Here is a &lt; symbol"));
    assertEquals("Here is a &gt; symbol", StringUtils.transformToHTMLSubset("Here is a &gt; symbol"));
    assertEquals("Here is a <b> tag", StringUtils.transformToHTMLSubset("Here is a &lt;b&gt; tag"));
    assertEquals("Here is a <i> tag", StringUtils.transformToHTMLSubset("Here is a &lt;i&gt; tag"));
    assertEquals("Here is a <p> tag", StringUtils.transformToHTMLSubset("Here is a &lt;p&gt; tag"));
    assertEquals("Here is a </p> tag", StringUtils.transformToHTMLSubset("Here is a &lt;/p&gt; tag"));
    assertEquals("Here is a <p/> tag", StringUtils.transformToHTMLSubset("Here is a &lt;p/&gt; tag"));
    assertEquals("Here is a <p /> tag", StringUtils.transformToHTMLSubset("Here is a &lt;p /&gt; tag"));
    assertEquals("Here is a <br> tag", StringUtils.transformToHTMLSubset("Here is a &lt;br&gt; tag"));
    assertEquals("Here is a <br/> tag", StringUtils.transformToHTMLSubset("Here is a &lt;br/&gt; tag"));
    assertEquals("Here is a <br /> tag", StringUtils.transformToHTMLSubset("Here is a &lt;br /&gt; tag"));
    assertEquals("Here is a <pre> tag", StringUtils.transformToHTMLSubset("Here is a &lt;pre&gt; tag"));
    assertEquals("Here is a </pre> tag", StringUtils.transformToHTMLSubset("Here is a &lt;/pre&gt; tag"));
    assertEquals("Here is a <a href=\"http://www.google.com\">link</a> to Google", StringUtils.transformToHTMLSubset("Here is a &lt;a href=\"http://www.google.com\"&gt;link&lt;/a&gt; to Google"));
    assertEquals("Here is a <a href=\"http://www.google.com\">link</a> to Google and another <a href=\"http://www.google.com\">link</a>", StringUtils.transformToHTMLSubset("Here is a &lt;a href=\"http://www.google.com\"&gt;link&lt;/a&gt; to Google and another &lt;a href=\"http://www.google.com\"&gt;link&lt;/a&gt;"));
    assertEquals("Here is a <a href='http://www.google.com'>link</a> to Google", StringUtils.transformToHTMLSubset("Here is a &lt;a href='http://www.google.com'&gt;link&lt;/a&gt; to Google"));
    assertEquals("Here is a <a href='http://www.google.com'>link</a> to Google and another <a href='http://www.google.com'>link</a>", StringUtils.transformToHTMLSubset("Here is a &lt;a href='http://www.google.com'&gt;link&lt;/a&gt; to Google and another &lt;a href='http://www.google.com'&gt;link&lt;/a&gt;"));
    assertEquals("Here is a &lt;script&gt; tag", StringUtils.transformToHTMLSubset("Here is a &lt;script&gt; tag"));
  }

  public void testFilterNewLines() {
    assertNull(StringUtils.filterNewlines(null));
    assertEquals("Here is some text", StringUtils.filterNewlines("Here is some text"));
    assertEquals("Line 1", StringUtils.filterNewlines("Line 1\n"));
    assertEquals("Line 1Line2", StringUtils.filterNewlines("Line 1\nLine2"));
  }

}
