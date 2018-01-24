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
import java.awt.event.*;

/**
 * This is an extension of the Swing JFrame, that can be extended further to
 * easily create a frame with some content (e.g. a table, a form, etc) and
 * a row of buttons at the bottom.
 * <br><br>
 * The <a href="http://java.sun.com/products/jlf/">Java Look and Feel Design Guidelines</a>
 * has recommendations for button positioning, spacing and border size to
 * dialogs. By using the methods defined in this class, it is very easy to build
 * consistent frames that conform to these recommendations.
 *
 * @author      Simon Brown
 */
public abstract class StandardFrame extends JFrame implements LookAndFeelGuidelines {

  /** a panel used for the row of buttons located at the bottom right of the dialog */
  protected JPanel buttonRow;

  /**
   * Constructor with an existing JDialog as this dialog's owner.
   *
   * @param   title       the titlebar text of this dialog
   */
  public StandardFrame(String title) {
    init(title);
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
   */
  private void init(String title) {
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

    // and enable the dialog process it's window closing event if necessary
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

    pack();
    center();

    this.setTitle(title);
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
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size = this.getSize();
    this.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
  }

  /**
   * Sets up where the focus should be when the window is opened.
   */
  private final void initFocus() {
    final JComponent c = getFirstFocusableComponent();

    // if the dialog doesn't have a component that it wants to get
    // focus by default,
    if (c == null) {
      return;
    }

    addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        // and try to set the default focus for this component
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            c.requestFocus();
          }
        });
      }
    });
  }

  /**
   * Sets up the default button for this dialog.
   */
  private final void initDefaultButton() {
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
    buttonRow = new JPanel(new GridLayout(1, buttons.length, HORIZONTAL_BUTTON_SPACING, 0));

    for (int i = 0; i < buttons.length; i++) {
      addButton(buttons[i]);
    }

    JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
    p.add(buttonRow);
    p.setBorder(BorderFactory.createEmptyBorder(DIALOG_CONTENT_BUTTONS_VERTICAL_GAP - DIALOG_OUTSIDE_BORDER_BOTTOM, DIALOG_OUTSIDE_BORDER_LEFT, DIALOG_OUTSIDE_BORDER_BOTTOM, DIALOG_OUTSIDE_BORDER_RIGHT));

    this.getContentPane().add(p, BorderLayout.SOUTH);
  }

  /**
   * A convenience method used internally to add an individual button to the
   * button row.
   *
   * @param   button      the button to be added to the button row
   */
  private void addButton(JButton button) {
    Insets margin = button.getMargin();
    margin.left = HORIZONTAL_BUTTON_MARGIN;
    margin.right = HORIZONTAL_BUTTON_MARGIN;
    button.setMargin(margin);

    buttonRow.add(button);
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
