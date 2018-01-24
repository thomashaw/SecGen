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
package pebble.mobile.ui;

import java.util.Hashtable;

import javax.microedition.lcdui.*;

import pebble.mobile.controller.UIController;
import pebble.mobile.model.Entry;
import pebble.mobile.util.EntryHandler;

/**
 *
 */
public class NewEntryUI extends Form implements CommandListener {
  private UIController uiController;
  private Command postCommand = new Command("Post", Command.SCREEN, 0);
  //private Command saveCommand = new Command("Save as Draft", Command.SCREEN, 0);
  private Command backCommand = new Command("Back", Command.BACK, 0);
  private String editingId;

  private TextField title = new TextField("Title", "", 100, TextField.ANY);
  private TextField category = new TextField("Category", "", 50, TextField.ANY);
  private TextField text = new TextField("Text", "", 1000, TextField.ANY);


  public NewEntryUI(UIController uiController) {
    super(uiController.getString("NEW_ENTRY_TITLE"));
    this.uiController = uiController;
    append(title);
    append(category);
    append(text);

    addCommand(backCommand);
    addCommand(postCommand);
    //addCommand(saveCommand);
    setCommandListener(this);
  }

  public void commandAction(Command command, Displayable displayable) {
    if (command == postCommand) {
      uiController.handleEvent(UIController.NEW_ENTRY_POST, this);
    } else {
      uiController.handleEvent(UIController.NEW_ENTRY_BACK, this);
    }
  }

  public String getEntryData() {
    return text.getString();
  }

  public String getEntryTitle() {
    return title.getString();
  }

  public String getEntryCategory() {
    return category.getString();
  }

  public void populate(Hashtable entryMap) {

    Entry entry = EntryHandler.adaptEntry(entryMap);
    editingId = entry.getEntryId();

    title.setString(entry.getEntryTitle());
    category.setString(entry.getEntryCategory());
    text.setString(entry.getEntryContent());
  }

  public String getEditingId() {
    return editingId;
  }

}
