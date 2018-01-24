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
package pebble.mobile.model;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: Jul 12, 2004
 * Time: 11:10:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class Preferences {

  public static final String RECORD_STORE_NAME = "pebblePreferences";

  private String url;
  private String userName;
  private String password;
  private String blogId;


  public Preferences() {
  }

  public String getUrl() {
    return url;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getBlogId() {
    return blogId;
  }

  public void populate(String fields) {

    System.out.println("fields = " + fields);
    int index = fields.indexOf('"');

    int index1 = fields.indexOf("|", index);
    url = fields.substring(index + 1, index1);

    index = index1 + 1;
    index1 = fields.indexOf("|", index);
    userName = fields.substring(index, index1);

    index = index1 + 1;
    index1 = fields.indexOf("|", index);
    password = fields.substring(index, index1);

    index = index1 + 1;
    index1 = fields.indexOf('"', index);
    blogId = fields.substring(index, index1);

  }

  /**
   * Determines whether the preferences have been set.
   *
   * @return true if they have been set, false otherwise
   */
  public boolean isSet() {
    return url != null && url.length() > 0;
  }

}
