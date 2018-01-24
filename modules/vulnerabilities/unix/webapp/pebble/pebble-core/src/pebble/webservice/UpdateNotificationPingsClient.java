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
import org.apache.xmlrpc.AsyncCallback;
import org.apache.xmlrpc.XmlRpcClient;
import pebble.blog.SimpleBlog;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * A simple client to ping (notify) sites like weblogs.com when this blog has
 * been updated.
 *
 * @author    Simon Brown
 */
public class UpdateNotificationPingsClient {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(UpdateNotificationPingsClient.class);

  /** the name of the method to call via XML-RPC */
  private static final String WEBLOGS_METHOD_NAME = "weblogUpdates.ping";

  /**
   * Sends a sendUpdateNotificationPing to weblogs.com indicating this the specified blog has
   * recently been updated.
   *
   * @param blog    the Blog representing the updated blog
   */
  public void sendUpdateNotificationPing(SimpleBlog blog) {
    try {
      Collection websites = blog.getUpdateNotificationPingsAsCollection();
      Iterator it = websites.iterator();
      while (it.hasNext()) {
        String website = (String)it.next();
        log.debug("Pinging " + website);
        XmlRpcClient xmlrpc = new XmlRpcClient(website);
        Vector params = new Vector();
        params.addElement(blog.getName());
        params.addElement(blog.getUrl());
        xmlrpc.executeAsync(WEBLOGS_METHOD_NAME, params, new UpdateNotificationPingsAsyncCallback());
      }
    } catch (IOException ioe) {
      log.error(ioe.getMessage(), ioe);
    }
  }

  /**
   * A callback class used to log the result/error message.
   */
  class UpdateNotificationPingsAsyncCallback implements AsyncCallback {

    /**
     * Called if the XML-RPC was successful.
     *
     * @param o         the resulting Object
     * @param url       the original URL
     * @param method    the original method name
     */
    public void handleResult(Object o, URL url, String method) {
      Hashtable result = (Hashtable)o;
      if (result != null) {
        log.debug("Result of calling " + method + " at " + url + " was " + result.get("flerror") + ", " + result.get("message"));
      }
    }

    /**
     * Called if the XML-RPC was not successful.
     *
     * @param e         the resulting Exception
     * @param url       the original URL
     * @param method    the original method name
     */
    public void handleError(Exception e, URL url, String method) {
      log.error("Exception when calling " + method + " at " + url, e);
    }
  }

}