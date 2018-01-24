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

import junit.framework.TestCase;
import pebble.logging.CountedUrl;

/**
 * Tests for the Referer class.
 *
 * @author    Simon Brown
 */
public class RefererTest extends TestCase {

  private Referer url;

  public void setUp() {
    url = new Referer("http://www.somedomain.com");
  }

  public void testConstruction() {
    assertEquals("http://www.somedomain.com", url.getName());
    assertEquals("http://www.somedomain.com", url.getUrl());
    assertEquals(1, url.getCount());
  }

  public void testIncrementingCount() {
    assertEquals(1, url.getCount());
    url.incrementCount();
    assertEquals(2, url.getCount());
  }

  public void testShortUrlIsNotTruncated() {
    String s = "http://www.somedomain.com";
    url = new Referer(s);
    assertEquals("http://www.somedomain.com", url.getTruncatedName());
  }

  public void testLongUrlIsTruncated() {
    String s = "http://www.somedomain.com/here/is/a/long/url/abcdefghijklmnopqrstuvwxyz012345678012345678901234567890123456789";
    url = new Referer(s);
    assertEquals(s.substring(0, Referer.NAME_LENGTH_LIMIT - 3) + "...", url.getTruncatedName());
  }

  public void testEmptyUrl() {
    url = new Referer("");
    assertEquals("", url.getUrl());
    assertEquals("None", url.getName());
  }

  public void testNullUrl() {
    url = new Referer(null);
    assertEquals(null, url.getUrl());
    assertEquals("None", url.getName());
  }

  public void testHashCode() {
    url = new Referer(null);
    assertEquals(0, url.hashCode());
    url = new Referer("http://www.somedomain.com");
    assertEquals("http://www.somedomain.com".hashCode(), url.hashCode());
  }

  public void testEquals() {
    Referer url1 = new Referer("http://www.somedomain.com");
    Referer url2 = new Referer("http://www.yahoo.com");
    Referer url3 = new Referer(null);
    Referer url4 = new Referer(null);

    assertFalse(url1.equals(null));
    assertFalse(url1.equals("http://www.somedomain.com"));
    assertTrue(url1.equals(url1));
    assertFalse(url1.equals(url2));
    assertFalse(url2.equals(url1));
    assertFalse(url1.equals(url3));
    assertFalse(url3.equals(url1));
    assertTrue(url3.equals(url4));
  }

  public void testFriendlyNamesForGoogleSearchUrls() {
    url = new Referer("http://www.google.com");
    assertEquals("Google : ", url.getName());
    url = new Referer("http://www.GOOGLE.com");
    assertEquals("Google : ", url.getName());
    url = new Referer("http://www.google.com.au");
    assertEquals("Google : ", url.getName());
    url = new Referer("http://www.google.com?q=some+search+term");
    assertEquals("Google : some search term", url.getName());
    url = new Referer("http://www.google.com?q=some+search+term&abc=123");
    assertEquals("Google : some search term", url.getName());
    url = new Referer("http://www.google.com?abc=123&q=some+search+term&xyz=456");
    assertEquals("Google : some search term", url.getName());
    url = new Referer("http://www.google.com?q=%22some+search+term%22");
    assertEquals("Google : \"some search term\"", url.getName());
  }

}
