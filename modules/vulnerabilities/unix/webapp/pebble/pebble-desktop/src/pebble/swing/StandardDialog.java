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

//import javax.help.HelpBroker;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This is an extension of the Swing JDialog, that can be extended further to
 * easily create a dialog box with some content (e.g. a table, a form, etc) and
 * a row of buttons at the bottom.
 * <br><br>
 * The <a href="http://java.sun.com/products/jlf/">Java Look and Feel Design Guidelines</a>
 * has recommendations for button positioning, spacing and border size to
 * dialogs. By using the methods defined in this class, it is very easy to build
 * consistent dialogs that conform to these recommendations.
 *
 * @author      Simon Brown
 * @author      James Yoxall (added extraInfo object and related methods)
 */
public abstract class StandardDialog extends JDialog implements LookAndFeelGuidelines {

  /** a panel used for the row of buttons located at the bottom right of the dialog */
  protected JPanel buttonRow;

  /**
   * Constructor with an existing JDialog as this dialog's owner.
   *
   * @param   owner       the owner (JDialog) of this dialog
   */
  public StandardDialog(JDialog owner) {
    super(owner);
  }

  /**
   * Constructor with an existing JFrame as this dialog's owner.
   *
   * @param   owner       the owner (JFrame) of this dialog
   */
  public StandardDialog(JFrame owner) {
    super(owner);
  }

  /**
   * Constructor with an existing JDialog as this dialog's owner.
   *
   * @param   owner       the owner (JDialog) of this dialog
   * @param   title       the titlebar text of this dialog
   * @param   modal       the modality of this dialog (true if model, false otherwise)
   */
  public StandardDialog(JDialog owner, String title, boolean modal) {
    super(owner);

    init(title, modal);
  }

  /**
   * Constructor with an existing JFrame as this dialog's owner.
   *
   * @param   owner       the owner (JFrame) of this dialog
   * @param   title       the titlebar text of this dialog
   * @param   modal       the modality of this dialog (true if model, false otherwise)
   */
  public StandardDialog(JFrame owner, String title, boolean modal) {
    super(owner);

    init(title, modal);
  }

  /**
   * Initialisation specific to this dialog, it sets out a template of
   * methods to be called and the order in which they are called, i.e.
   *
   *  init(), init(extraInfo), initUI(), initListeners(), initFocus(),
   *  initDefaultButton(), initHelp()
   *
   * This method is called by the two constructors in this class.
   *
   * @param   title       the titlebar text of this dialog
   * @param   modal       the modality of this dialog (true if model, false otherwise)
   */
  protected final void init(String title, boolean modal) {
    init();

    initUI();
    initListeners();
    initFocus();
    initDefaultButton();

    // allow the Escape key to be used to close the dialog
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // the user has tried to close the frame so let's check
        // that this is OK to do now
        if (canCloseWindow()) {
          close();
        }
      }
    };

    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // and Ctrl-M to open the memory usage dialog
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setWaitCursor();
        //MemoryUsageDialog.getInstance();
        setDefaultCursor();
      }
    };

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK);
    getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // and enable the dialog process it's window closing event if necessary
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

    this.setTitle(title);

    this.setModal(modal);

    pack();
    center();
  }

  /**
   * An extension of the standard processWindowEvent() method that
   * catches the "window closing" event. The canCloseWindow() method is
   * called and if that returns true, then the captured WindowEvent is allowed
   * to flow up the inheritance hierarchy. The exit() method is then called
   * to perform any last minute cleanup.
   *
   * @param   e       the WindowEvent generated
   */
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {

      // the user has tried to close the frame so let's check
      // that this is OK to do now
      if (canCloseWindow()) {
        super.processWindowEvent(e);
        close();
      }

    } else {
      // allow the event to pass on up the tree!
      super.processWindowEvent(e);
    }
  }

  /**
   * Called before the dialog is closed, and runs any cleanup logic before closing.
   *
   * @return  true if the dialog can continue to close,
   *          false if the dialog should remain open
   */
  protected boolean canCloseWindow() {
    // do any checking in here!
    return true;
  }

  /**
   * Called just before the dialog is closed - contains any cleanup code.
   */
  protected void close() {
    this.dispose();
  }

  /**
   * Centers the dialog on screen!
   */
  protected void center() {
    Window w = this.getOwner();

    if (w != null && w.isVisible()) {
      Point location = w.getLocation();
      Dimension size = w.getSize();
      setLocation(location.x + (size.width - this.getSize().width) / 2, location.y + (size.height - this.getSize().height) / 2);
    } else {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = this.getSize();
      this.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
    }
  }

  /**
   * An extension of the pack() method that ensures that the dialog is kept
   * completely inside the bounds of the screen.
   */
  public void pack() {
    boolean changed = false;

    // perform packing!
    super.pack();

    // now check the dialog is completely on the screen!
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size = this.getSize();

    // first check width
    if (size.width > screen.width) {
      size.width = screen.width;
      changed = true;
    }

    // and then height
    if (size.height > screen.height) {
      size.height = screen.height;
      changed = true;
    }

    // and reset dialog size
    if (changed) {
      setSize(size);
    }
  }

  /**
   * Sets up where the focus should be when the window is opened.
   */
  protected final void initFocus() {
    final JComponent c = getFirstFocusableComponent();

    // if the dialog doesn't have a component that it wants to get
    // focus by default,
    if (c == null) {
      return;
    }

    WindowAdapter adpater = new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        // and try to set the default focus for this component
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            c.requestFocus();
          }
        });

        removeWindowListener(this);
      }
    };

    addWindowListener(adpater);
  }

  /**
   * Sets up the default button for this dialog.
   */
  protected final void initDefaultButton() {
    getRootPane().setDefaultButton(getDefaultButton());
  }

  /**
   * Override this method for any initialisation before the user interface
   * and listeners are set up - this is the first method called after
   * construction. For example, maybe instantiating the user interface
   * components before initUI() is called.
   */
  protected void init() {
  }

  /**
   * Override this method for any initialisation before the user interface
   * and listeners are set up - this is the first method called after
   * construction. For example, maybe instantiating the user interface
   * components before initUI() is called.
   * <p>
   * This version of init returns back to the initialisation process the extraInfo
   * passed on the constructor;
   */
  protected void init(Object extraInfo) {
  }

  /**
   * Implement this method to perform any set up of the user interface
   * components on this dialog.
   */
  protected abstract void initUI();

  /**
   * Implement this method to add any listeners to components on the dialog.
   */
  protected abstract void initListeners();

  /**
   * A dialog consists of a row of command buttons in the bottom right of
   * the dialog, and some content in the center. This method sets the content
   * of the dialog and creates the borders required so that the dialog
   * conforms to the Java Look and Feel Design Guidelines.
   *
   * @param   content     the JComponent that is the content of this dialog
   *                      (anything from a single textfield to a complex panel)
   */
  public final void setContent(JComponent content) {
    JPanel p = new JPanel(new BorderLayout());

    p.add(content, BorderLayout.CENTER);
    p.setBorder(BorderFactory.createEmptyBorder(DIALOG_OUTSIDE_BORDER_TOP, DIALOG_OUTSIDE_BORDER_LEFT, DIALOG_OUTSIDE_BORDER_BOTTOM, DIALOG_OUTSIDE_BORDER_RIGHT));

    this.getContentPane().add(p, BorderLayout.CENTER);
  }

  /**
   * A dialog consists of a row of command buttons in the bottom right of
   * the dialog, and some content in the center. This method creates the row
   * of buttons and the borders required so that the dialog conforms to the
   * Java Look and Feel Design Guidelines.
   *
   * @param   buttons     an array of JButtons to be added to this dialog
   */
  public final void setButtons(JButton buttons[]) {
    buttonRow = PanelFactory.createHorizontalButtonPanel(buttons);
    buttonRow.setBorder(BorderFactory.createEmptyBorder(DIALOG_CONTENT_BUTTONS_VERTICAL_GAP - DIALOG_OUTSIDE_BORDER_BOTTOM, DIALOG_OUTSIDE_BORDER_LEFT, DIALOG_OUTSIDE_BORDER_BOTTOM, DIALOG_OUTSIDE_BORDER_RIGHT));
    this.getContentPane().add(buttonRow, BorderLayout.SOUTH);
  }

  /**
   * Allows the default button of the dialog to be set - this is just a
   * shortcut for calling the setDefaultButton method on the root pane.
   *
   * @param   button      the JButton to be the default
   */
  public final void setDefaultButton(JButton button) {
    this.getRootPane().setDefaultButton(button);
  }

  /**
   * Returns a reference to the button that is to be set as the default on the
   * dialog's rootpane.
   *
   * @return  the JButton to be the default on the rootpane
   */
  public abstract JButton getDefaultButton();

  /**
   * Called on initialisation to determine which component on the dialog
   * should get focus when the dialog is opened.
   * <br><br>
   * Implement this to return a reference to a component on the dialog, or
   * to return null to "not specify".
   *
   * @return  the JComponent that should receive focus when the dialog is
   *          first opened
   */
  protected abstract JComponent getFirstFocusableComponent();

  /**
   * Sets the mouse cursor to waiting.
   */
  public void setWaitCursor() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  /**
   * Sets the mouse cursor to the default.
   */
  public void setDefaultCursor() {
    setCursor(Cursor.getDefaultCursor());
  }
}
