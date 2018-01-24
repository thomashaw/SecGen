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
package pebble.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;

/**
 * The "main" application window.
 *
 * @author	Simon Brown
 */
public abstract class ApplicationFrame extends JFrame {

  protected JMenuBar menubar;
  protected JToolBar toolbar;
  protected StatusBar statusbar;

  protected boolean showMenuBar = true;
  protected boolean showToolBar = true;
  protected boolean showStatusBar = true;

  protected ExitAction exitAction;

  /**
   * Default, no args constructor.
   */
  public ApplicationFrame() {
    this(true, true, true);
  }

  /**
   * Default, no args constructor.
   */
  public ApplicationFrame(boolean showMenuBar, boolean showToolBar, boolean showStatusBar) {
    this.setTitle(getApplicationName());

    this.showMenuBar = showMenuBar;
    this.showToolBar = showToolBar;
    this.showStatusBar = showStatusBar;

    initFrame();
    init();
    initUI();
  }

  protected void initFrame() {
    // init the exit action!
    this.exitAction = new ExitAction();

    // setup the menu
    this.menubar = new JMenuBar();

    if (showMenuBar) {
      initMenuBar();
      setJMenuBar(menubar);
    }

    // and the toolbar
    this.toolbar = new JToolBar();
    toolbar.setFloatable(false);

    if (showToolBar) {
      getContentPane().add(toolbar, BorderLayout.NORTH);
    }

    // and the statusbar
    this.statusbar = new StatusBar();

    if (showStatusBar) {
      getContentPane().add(statusbar, BorderLayout.SOUTH);
    }

    // and listeners required for this frame
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

    // put a blank panel in the frame with a lowered border
    JPanel p = new JPanel(new BorderLayout());
    //p.setBorder(BorderFactory.createLoweredBevelBorder());
    this.getContentPane().add(p, BorderLayout.CENTER);
  }

  /**
   * Override this method to perform any specific tasks before the frame is
   * opened such as showing a splash screen if required, or preloading
   * resources. It's one of the first methods called after construction.
   */
  protected void init() {
  }

  /**
   * Sets up the user interface for this frame.
   */
  protected abstract void initUI();

  /**
   * Sets up the actions that are needed for the toolbar and menu items.
   */
  protected void initActions() {
  }

  /**
   * Sets up the menubar for this frame.
   */
  protected void initMenuBar() {
  }

  /**
   * Creates the toolbar, which is then added to the top of this frame.
   */
  protected void initToolBar() {
  }

  /**
   * Adds a DMSAction to a specified menu - details of which are taken from
   * the DMSAction itself (e.g. name, mnemonic, ...).
   *
   * @param   menu        the JMenu to which the menu item is to be added
   * @param   action      the DMSAction to be added to "menu"
   * @return  the JMenuItem instance created by adding "action" to a JMenu
   */
  protected JMenuItem addActionToMenu(JMenu menu, final Action action) {
    final JMenuItem item = menu.add(action);

    //item.setMnemonic(action.getMnemonic());
    item.setIcon(null);

    return item;
  }

  /**
   * Adds a DMSAction to the frame toolbar - details of the toolbar button
   * are taken from the DMSAction (e.g. name, icon, ...).
   *
   * @param   action      the DMSAction to be added to the toolbar
   * @return  the JButton instance created by adding "action" to the toolbar
   */
  protected JButton addActionToToolBar(Action action) {
    JButton button;
    button = toolbar.add(action);

    button.setText("");
    //button.setToolTipText(action.getToolTipText());

    return button;
  }

  /**
   * Sets up the status bar, which is added to the bottom of this frame.
   */
  protected void initStatusBar() {
  }

  /**
   * Sets up any default listeners - in this implementation, enables this
   * frame to receive the "window closing" event.
   */
  protected void initListeners() {
  }

  /**
   * An extension of the standard processWindowEvent() method that
   * catches the "window closing" event. The canCloseWindow() method is
   * called and if that returns true, then the captured WindowEvent is allowed
   * to flow up the inheritance hierarchy. The exit() method is then called
   * to perform any last minute cleanup and shuts down the virtual machine.
   *
   * @param   e       the WindowEvent generated
   */
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {

      // the user has tried to close the frame so let's check
      // that this is OK to do now
      if (canCloseWindow()) {
        super.processWindowEvent(e);
        exit();
      }

    } else {
      // allow the event to pass on up the tree!
      super.processWindowEvent(e);
    }
  }

  /**
   * Called before the application frame is to be closed, and runs any
   * cleanup logic before closing.
   *
   * @return  true if the frame can continue to close,
   *          false if the frame should remain open
   */
  protected boolean canCloseWindow() {
    // do any checking in here!
    return true;
  }

  /**
   * Called just before the virtual machine is shutdown - contains any
   * application specific cleanup code such as removing temporary files, etc.
   */
  protected void exit() {
    this.dispose();

    // and stop the application
    System.exit(0);
  }

  /**
   * Returns the name of the application - used in the titlebar of this frame.
   *
   * @return  the name of the application
   */
  public abstract String getApplicationName();

  /**
   * An extension of Swing's AbstractAction that can be added to the "File"
   * menu - when selected, it checks that the window can be closed (by
   * calling the canCloseWindow()) and then the exit() method.
   */
  protected class ExitAction implements Action {

    public Object getValue(String s) {
      return null;
    }

    public void putValue(String s, Object o) {
    }

    public void setEnabled(boolean b) {
    }

    public boolean isEnabled() {
      return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    /**
     * Implementation of the ActionListener interface.
     *
     * @param   e       the ActionEvent generated
     */
    public void actionPerformed(ActionEvent e) {
      if (canCloseWindow()) {
        exit();
      }
    }
  }

  /**
   * Sets the message shown in the status bar.
   *
   * @param   s       the message to be shown in the status bar
   */
  public void setStatusBarMessage(String s) {
    statusbar.setStatusBarMessage(s);
  }

  /**
   * Resets the message in the status bar to it's default.
   */
  public void setDefaultStatusBarMessage() {
    statusbar.setDefaultStatusBarMessage();
  }

  /**
   * Sets the message in the status bar to it's waiting messsage.
   */
  public void setWaitStatusBarMessage() {
    statusbar.setWaitStatusBarMessage();
  }

  /**
   * Sets the mouse cursor to waiting.
   */
  public void setWaitCursor(String message) {
    setStatusBarMessage(message);
    CursorUtilities.setWaitCursor(this);
  }

  /**
   * Sets the mouse cursor to waiting.
   */
  public void setWaitCursor() {
    setWaitCursor("Busy");
  }

  /**
   * Sets the mouse cursor to the default.
   */
  public void setDefaultCursor() {
    setDefaultStatusBarMessage();
    CursorUtilities.setDefaultCursor(this);
  }

  protected void setTheme(final javax.swing.plaf.metal.MetalTheme t) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(t);
          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
          SwingUtilities.updateComponentTreeUI(ApplicationFrame.this);
        } catch (Exception e) {
          System.out.println("Error whilst setting theme.");
        }
      }
    });
  }
}

