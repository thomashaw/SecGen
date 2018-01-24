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
package pebble.mobile.controller;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import pebble.mobile.controller.action.*;
import pebble.mobile.model.Preferences;
import pebble.mobile.ui.MainUI;
import pebble.mobile.ui.PreferencesUI;
import pebble.mobile.util.GuiState;


//todo: generic BACK action
//todo: ditch splash screen and put the logo on the main page.
//todo: save as draft
//todo: help
//todo: image upload

public class UIController implements CommandListener {

  private MIDlet midlet;
  private Display display;

  private Command exitCommand = new Command("Exit", Command.EXIT, 0);
  private MainUI mainForm;

  // Start Event IDs
  public static final int PREFS_OPEN = 0;
  public static final int PREFS_BACK = 1;
  public static final int PREFS_SAVE = 2;
  public static final int NEW_ENTRY_POST = 3;
  public static final int NEW_ENTRY_BACK = 4;
  public static final int NEW_ENTRY_OPEN = 5;
  public static final int ENTRIES_OPEN = 6;
  public static final int ENTRIES_BACK = 7;
  public static final int ENTRIES_REFRESH = 8;
  public static final int ENTRIES_EDIT = 9;
  public static final int ENTRIES_DELETE = 10;

  // End event IDs

  public UIController(MIDlet midlet) {
    this.midlet = midlet;
    this.display = Display.getDisplay(midlet);

    // start the application by displaying a splash screen
    //display.setCurrent(new SplashScreenUI(this));



    //Thread t = new Thread(new Runnable() {
    //    public void run() {

    //    }
    //});
    //t.start();

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {

//        }
    mainForm = new MainUI(this);
    Preferences prefs = loadPreferences();
    if (prefs.isSet()) {
      display.setCurrent(mainForm);
    } else {
      display.setCurrent(new PreferencesUI(this));
    }
  }

  public void commandAction(Command command, Displayable displayable) {
    if (command == exitCommand) {
      midlet.notifyDestroyed();
    }
  }

  public String getString(String key) {
    return midlet.getAppProperty(key);
  }

  public Command getExitCommand() {
    return exitCommand;
  }

  /**
   * Handle and event from the
   *
   * @param eventId
   * @param displayable
   */
  public void handleEvent(int eventId, Displayable displayable) {
    // get the appropriate event and forward the displayable to it. This will return a
    // new displayable (pre-populated?)
    Action action = null;
    switch (eventId) {
      case PREFS_OPEN:
        action = new PreferencesOpenAction();
        break;
      case PREFS_BACK:
        action = new PreferencesBackAction();
        break;
      case PREFS_SAVE:
        action = new PreferencesSaveAction();
        break;
      case NEW_ENTRY_BACK:
        action = new NewEntryBackAction();
        break;
      case NEW_ENTRY_POST:
        action = new NewEntryPostAction();
        break;
      case NEW_ENTRY_OPEN:
        action = new NewEntryOpenAction();
        break;
      case ENTRIES_OPEN:
        action = new EntriesOpenAction();
        break;
      case ENTRIES_BACK:
        action = new EntriesBackAction();
        break;
      case ENTRIES_REFRESH:
        action = new EntriesRefreshAction();
        break;
      case ENTRIES_EDIT:
        action = new EntriesEditAction();
        break;
      case ENTRIES_DELETE:
        action = new EntriesDeleteAction();
        break;
      default:
        // do nothing, action remains null
    }

    // Execute the action and forward on to the appropriate displayable
    if (action != null) {
      forwardToScreen(action.execute(displayable, this));
    }
  }

  /**
   * Show the specified screen
   */
  public void forwardToScreen(Displayable displayable) {
    if (displayable != null) {
      display.setCurrent(displayable);
    }
  }

  public Displayable getMainForm() {
    return mainForm;
  }

  private Preferences loadPreferences() {
    Preferences prefs = new Preferences();

    try {
      RecordStore recordStore = RecordStore.openRecordStore(Preferences.RECORD_STORE_NAME, true);
      if (recordStore.getNumRecords() > 0) {
        RecordEnumeration enum = recordStore.enumerateRecords(null, null, false);
        String record = new String(recordStore.getRecord(enum.nextRecordId()));
        prefs.populate(record);
        GuiState.getInstance().setPreferences(prefs);
      }

    } catch (RecordStoreException e) {
      System.out.println("In Preferences Open Action");
      e.printStackTrace();
    }

    return prefs;
  }

  public Display getDisplay() {
    return display;
  }

}
