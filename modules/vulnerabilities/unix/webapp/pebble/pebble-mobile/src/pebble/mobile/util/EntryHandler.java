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
package pebble.mobile.util;

import java.util.Hashtable;
import java.util.Vector;

import org.kxmlrpc.XmlRpcClient;
import pebble.mobile.model.Entry;
import pebble.mobile.model.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: Jul 12, 2004
 * Time: 9:50:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntryHandler {

  private static final String APP_KEY = "Pebble moblog";
  private static final String HANDLER = "blogger";

  private static final String TITLE_ELEMENT = "<title>";
  private static final String TITLE_ELEMENT_END = "</title>";

  private static final String CATEGORY_ELEMENT = "<category>";
  private static final String CATEGORY_ELEMENT_END = "</category>";


  private static EntryHandler _instance = new EntryHandler();

  public static EntryHandler getInstance() {
    return _instance;
  }

  public Vector getEntries(Preferences prefs) {
    // todo: check that this is not empty

    XmlRpcClient xmlrpc = new XmlRpcClient(prefs.getUrl());
    Vector params = new Vector();
    params.addElement(APP_KEY);
    params.addElement(prefs.getBlogId());
    params.addElement(prefs.getUserName());
    params.addElement(prefs.getPassword());

    // todo: make this configurable
    params.addElement(new Integer(5));


    Vector entries = null;
    try {
      // this is a vector of hashtables
      // with the elements DATE_CREATED,USER_ID,POST_ID,CONTENT
      System.out.println("Executing " + HANDLER + ".getRecentPosts");
      entries = (Vector) xmlrpc.execute(HANDLER + ".getRecentPosts", params);
      System.out.println(entries);

    } catch (Exception e) {

    }

    return entries;
  }

  public boolean deleteEntry(int selectedIndex, Preferences prefs) {

    Vector entries = GuiState.getInstance().getEntries();
    Hashtable entry = (Hashtable) entries.elementAt(selectedIndex);

    XmlRpcClient xmlrpc = new XmlRpcClient(prefs.getUrl());
    Vector params = new Vector();
    params.addElement(APP_KEY);
    params.addElement(entry.get("postid"));
    params.addElement(prefs.getUserName());
    params.addElement(prefs.getPassword());
    params.addElement(new Boolean(true));


    try {
      // this is a vector of hashtables
      // with the elements DATE_CREATED,USER_ID,POST_ID,CONTENT
      Boolean success = (Boolean) xmlrpc.execute(HANDLER + ".deletePost", params);

      return success.booleanValue();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;

  }

  public Object postEntry(Entry entry) {
    // todo: do the posting here
    return null;
  }

  public static Entry adaptEntry(Hashtable entryMap) {
    // create and entry object from the hashmap
    Entry entry = new Entry();
    entry.setEntryId(String.valueOf(entryMap.get("postid")));

    String content = String.valueOf(entryMap.get("content"));

    System.out.println("content = " + content);
    System.out.println("entry = " + entryMap);

    int pos = content.indexOf(TITLE_ELEMENT) + TITLE_ELEMENT.length();
    int pos2 = content.indexOf(TITLE_ELEMENT_END);
    String title = content.substring(pos, pos2);
    entry.setEntryTitle(title);

    pos = content.indexOf(CATEGORY_ELEMENT, pos2);
    if (pos > 0) {
      pos2 = content.indexOf(CATEGORY_ELEMENT_END);
      String category = content.substring(pos, pos2);
      entry.setEntryCategory(category);

      pos2 += CATEGORY_ELEMENT_END.length();
    } else {
      pos2 += TITLE_ELEMENT_END.length();
    }
    entry.setEntryContent(content.substring(pos2, content.length()));

    return entry;
  }

}
