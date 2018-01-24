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
package pebble.mobile.controller.action;

import java.util.Vector;

import javax.microedition.lcdui.*;

import org.kxmlrpc.XmlRpcClient;
import pebble.mobile.controller.Action;
import pebble.mobile.controller.UIController;
import pebble.mobile.model.Preferences;
import pebble.mobile.ui.NewEntryUI;
import pebble.mobile.util.GuiState;

/**
 *
 */
public class NewEntryPostAction implements Action {
  private static final String APP_KEY = "MoPebble";
  private static final String HANDLER = "blogger";

  public Displayable execute(Displayable displayable, UIController uiController) {

    ((Screen) displayable).setTicker(new Ticker("Posting blog entry..."));

    // todo: move all of this to the EntriesHandler
    // todo : add confirmation here

    Alert response = new Alert("Response", null, null, AlertType.INFO);
    response.setTimeout(Alert.FOREVER);


    Preferences prefs = GuiState.getInstance().getPreferences();

    NewEntryUI form = (NewEntryUI) displayable;
    String data = getTitleAndCategory(form);
    data += form.getEntryData();


    String editingId = form.getEditingId();
    try {
      XmlRpcClient xmlrpc = new XmlRpcClient(prefs.getUrl());
      Vector params = new Vector();
      params.addElement(APP_KEY);
      if (editingId != null) {
        params.addElement(editingId);
      } else {
        params.addElement(prefs.getBlogId());
      }
      params.addElement(prefs.getUserName());
      params.addElement(prefs.getPassword());
      params.addElement(data);
      params.addElement(new Boolean(true));

      String methodName;
      if (editingId != null) {
        Object success = xmlrpc.execute(HANDLER + ".editPost", params);
        if (((Boolean) success).booleanValue()) {
          response.setString("Entry Updated Succesfully");
        } else {
          response.setString("Entry Update Failed!");
        }
      } else {
        Object postId = xmlrpc.execute(HANDLER + ".newPost", params);
        response.setString("Success, post ID : " + postId.toString());
      }

    } catch (Exception ex) {
      response.setString(ex.toString());
      ex.printStackTrace(); // DEBUG
    }
    ((Screen) displayable).setTicker(new Ticker(""));


    return response;
  }

  private String getTitleAndCategory(NewEntryUI form) {

    // get the title field and create the correct format for a pebble title
    StringBuffer titleAndCategory = new StringBuffer("<title>");
    titleAndCategory.append(form.getEntryTitle());
    titleAndCategory.append("</title>");

    titleAndCategory.append("<category>");
    titleAndCategory.append(form.getEntryCategory());
    titleAndCategory.append("</category>");

    return titleAndCategory.toString();

  }
}
