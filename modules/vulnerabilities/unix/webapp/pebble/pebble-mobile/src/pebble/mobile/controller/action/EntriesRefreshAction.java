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

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.Ticker;

import pebble.mobile.controller.Action;
import pebble.mobile.controller.UIController;
import pebble.mobile.ui.EntriesUI;
import pebble.mobile.util.EntryHandler;
import pebble.mobile.util.GuiState;

/**
 * Created by IntelliJ IDEA.
 * User: sam
 * Date: Jul 12, 2004
 * Time: 6:00:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntriesRefreshAction implements Action {
  public Displayable execute(Displayable displayable, UIController uiController) {

    ((Screen) displayable).setTicker(new Ticker("Getting blog entries..."));
    Vector entries = EntryHandler.getInstance().getEntries(GuiState.getInstance().getPreferences());
    GuiState.getInstance().setEntries(entries);
    ((EntriesUI) displayable).populate(entries);
    ((Screen) displayable).setTicker(null);

    return displayable;
  }
}
