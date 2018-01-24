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
 * Tests for the Request class.
 *
 * @author    Simon Brown
 */
public class RequestTest extends TestCase {

  private Request url;

  public void setUp() {
    url = new Request("http://www.somedomain.com");
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
    url = new Request(s);
    assertEquals("http://www.somedomain.com", url.getTruncatedName());
  }

  public void testLongUrlIsTruncated() {
    String s = "http://www.somedomain.com/here/is/a/long/url/abcdefghijklmnopqrstuvwxyz012345678012345678901234567890123456789";
    url = new Request(s);
    assertEquals(s.substring(0, Request.NAME_LENGTH_LIMIT - 3) + "...", url.getTruncatedName());
  }

  public void testEmptyUrl() {
    url = new Request("");
    assertEquals("", url.getUrl());
    assertEquals("None", url.getName());
  }

  public void testNullUrl() {
    url = new Request(null);
    assertEquals(null, url.getUrl());
    assertEquals("None", url.getName());
  }

  public void testHashCode() {
    url = new Request(null);
    assertEquals(0, url.hashCode());
    url = new Request("http://www.somedomain.com");
    assertEquals("http://www.somedomain.com".hashCode(), url.hashCode());
  }

  public void testEquals() {
    Request url1 = new Request("http://www.somedomain.com");
    Request url2 = new Request("http://www.yahoo.com");
    Request url3 = new Request(null);
    Request url4 = new Request(null);

    assertFalse(url1.equals(null));
    assertFalse(url1.equals("http://www.somedomain.com"));
    assertTrue(url1.equals(url1));
    assertFalse(url1.equals(url2));
    assertFalse(url2.equals(url1));
    assertFalse(url1.equals(url3));
    assertFalse(url3.equals(url1));
    assertTrue(url3.equals(url4));
  }

  /**
   * Test that friendly names are used for news feeds.
   */
  public void testFriendlyNamesForNewsFeeds() {
    url = new Request("http://...rss.xml");
    assertEquals("RSS 2.0 feed", url.getName());
    url = new Request("http://...rss.xml?category=java");
    assertEquals("RSS 2.0 feed", url.getName());

    url = new Request("http://...feed.xml");
    assertEquals("RSS 2.0 feed", url.getName());
    url = new Request("http://...feed.xml?category=java");
    assertEquals("RSS 2.0 feed", url.getName());

    url = new Request("http://...feed.action");
    assertEquals("RSS 2.0 feed", url.getName());
    url = new Request("http://...feed.action?category=java");
    assertEquals("RSS 2.0 feed", url.getName());

    url = new Request("http://...rdf.xml");
    assertEquals("RDF 1.0 feed", url.getName());
    url = new Request("http://...rdf.xml?category=java");
    assertEquals("RDF 1.0 feed", url.getName());

    url = new Request("http://...atom.xml");
    assertEquals("Atom 0.3 feed", url.getName());
    url = new Request("http://...atom.xml?category=java");
    assertEquals("Atom 0.3 feed", url.getName());

    url = new Request("http://...rssWithCommentsAndTrackBacks.xml");
    assertEquals("RSS 2.0 feed, with comments and TrackBacks", url.getName());
    url = new Request("http://...rssWithCommentsAndTrackBacks.xml?category=java");
    assertEquals("RSS 2.0 feed, with comments and TrackBacks", url.getName());
  }

}
