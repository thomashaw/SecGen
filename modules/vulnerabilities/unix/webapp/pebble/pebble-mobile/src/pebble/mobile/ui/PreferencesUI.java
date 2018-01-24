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

import javax.microedition.lcdui.*;

import pebble.mobile.controller.UIController;
import pebble.mobile.model.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: Jul 7, 2004
 * Time: 10:36:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class PreferencesUI extends Form implements CommandListener {

  private Command backCommand = new Command("Back", Command.BACK, 0);
  private Command saveCommand = new Command("Save", Command.SCREEN, 0);

  private TextField url = new TextField("Blog URL", "http://www.", 100, TextField.URL);
  private TextField userName = new TextField("Username", "", 10, TextField.ANY);
  private TextField password = new TextField("Password", "", 10, TextField.PASSWORD);
  private TextField blogId = new TextField("Blog ID", "blog", 10, TextField.ANY);

  private UIController uiController;

  public PreferencesUI(UIController uiController) {
    super(uiController.getString("PREFERENCES_TITLE"));
    this.uiController = uiController;
    addCommand(backCommand);
    addCommand(saveCommand);
    setCommandListener(this);

    append(url);
    append(userName);
    append(password);
    append(blogId);

  }

  public void commandAction(Command command, Displayable displayable) {
    if (command == backCommand) {
      uiController.handleEvent(UIController.PREFS_BACK, displayable);
    } else if (command == saveCommand) {
      uiController.handleEvent(UIController.PREFS_SAVE, displayable);
    }
  }

  public void populateFields(Preferences prefs) {

    url.setString(prefs.getUrl());
    userName.setString(prefs.getUserName());
    password.setString(prefs.getPassword());
    blogId.setString(prefs.getBlogId());


  }


}
