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
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import pebble.mobile.controller.UIController;
import pebble.mobile.model.Entry;
import pebble.mobile.util.EntryHandler;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: Jul 12, 2004
 * Time: 3:04:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntriesUI extends List implements CommandListener {

  private UIController uiController;
  private Command backCommand = new Command("Back", Command.BACK, 0);
  private Command refreshCommand = new Command("Refresh Entries", Command.SCREEN, 0);
  private Command editCommand = new Command("Edit", Command.SCREEN, 0);
  private Command deleteCommand = new Command("Delete", Command.SCREEN, 0);


  public EntriesUI(UIController uiController) {
    super(uiController.getString("ENTRIES_TITLE"), EXCLUSIVE);
    this.uiController = uiController;

    addCommand(backCommand);
    addCommand(editCommand);
    addCommand(deleteCommand);
    addCommand(refreshCommand);

    setCommandListener(this);
  }

  public void commandAction(Command command, Displayable displayable) {
    if (command == backCommand) {
      uiController.handleEvent(UIController.ENTRIES_BACK, displayable);
    } else if (command == refreshCommand) {
      uiController.handleEvent(UIController.ENTRIES_REFRESH, displayable);
    } else if (command == editCommand) {
      uiController.handleEvent(UIController.ENTRIES_EDIT, displayable);
    } else if (command == deleteCommand) {
      uiController.handleEvent(UIController.ENTRIES_DELETE, displayable);
    }
  }

  public void populate(Vector entries) {

    // remove all of the current entries
    while (size() > 0) {
      delete(size() - 1);
    }
    // this is a vector of hashtables
    // with the elements DATE_CREATED,USER_ID,POST_ID,CONTENT
    Hashtable entry;

    for (int ctr = 0; ctr < entries.size(); ctr++) {
      entry = (Hashtable) entries.elementAt(ctr);

      Entry e = EntryHandler.adaptEntry(entry);
      append(e.getEntryTitle(), null);

    }

  }
}
