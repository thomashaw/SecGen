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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import pebble.mobile.controller.Action;
import pebble.mobile.controller.UIController;
import pebble.mobile.model.Preferences;
import pebble.mobile.ui.PreferencesUI;
import pebble.mobile.util.GuiState;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: Jul 9, 2004
 * Time: 10:59:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class PreferencesSaveAction implements Action {

  public Displayable execute(Displayable displayable, UIController uiController) {
    // save the preferences and return to the main menu
    PreferencesUI form = (PreferencesUI) displayable;

    // get the values from the form
    String url = ((TextField) form.get(0)).getString().trim();
    String username = ((TextField) form.get(1)).getString().trim();
    String password = ((TextField) form.get(2)).getString().trim();
    String blogId = ((TextField) form.get(3)).getString().trim();

    // convert these into a byte array
    StringBuffer record = new StringBuffer("\"");
    record.append(url);
    record.append("|");
    record.append(username);
    record.append("|");
    record.append(password);
    record.append("|");
    record.append(blogId);
    record.append("\"");


    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    try {
      dos.writeUTF(record.toString());
      byte[] b = bos.toByteArray();
      // save these in the RMS
      try {

        RecordStore store = RecordStore.openRecordStore(Preferences.RECORD_STORE_NAME, true);
        // see if there is already a record, if so then delete it and add a new one
        RecordEnumeration enum = store.enumerateRecords(null, null, false);
        while (enum.hasNextElement()) {
          int id = enum.nextRecordId();
          store.deleteRecord(id);
        }

        store.addRecord(b, 0, b.length);

        Preferences prefs = new Preferences();
        prefs.populate(new String(b));
        GuiState.getInstance().setPreferences(prefs);

      } catch (RecordStoreException e) {
        System.out.println("In Preferences Save Action");
        e.printStackTrace();
      }
    } catch (IOException e) {
      System.out.println("In Preferences Save Action");
      e.printStackTrace();
    }

    // create a preferences object and put it in the GuIState


    return uiController.getMainForm();
  }
}
