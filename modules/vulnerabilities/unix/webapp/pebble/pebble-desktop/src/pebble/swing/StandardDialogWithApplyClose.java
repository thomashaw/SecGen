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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An extension of StandardDialog in order that simple dialogs with content
 * and a pair of "Apply" and "Close" buttons can be implemented quicker.
 *
 * @author      Simon Brown
 */
public abstract class StandardDialogWithApplyClose extends StandardDialog {

  private JButton applyButton;
  private JButton closeButton;

  /**
   * Constructor with an existing JDialog as this dialog's owner.
   *
   * @param   owner       the owner (JDialog) of this dialog
   */
  public StandardDialogWithApplyClose(JDialog owner) {
    super(owner);
  }

  /**
   * Constructor with an existing JFrame as this dialog's owner.
   *
   * @param   owner       the owner (JFrame) of this dialog
   */
  public StandardDialogWithApplyClose(JFrame owner) {
    super(owner);
  }

  /**
   * Constructor with an existing JDialog as this dialog's owner.
   *
   * @param   owner       the owner (JDialog) of this dialog
   * @param   title       the titlebar text of this dialog
   * @param   modal       the modality of this dialog (true if model, false otherwise)
   */
  public StandardDialogWithApplyClose(JDialog owner, String title, boolean modal) {
    super(owner, title, modal);

    this.pack();
  }

  /**
   * Constructor with an existing JFrame as this dialog's owner.
   *
   * @param   owner       the owner (JFrame) of this dialog
   * @param   title       the titlebar text of this dialog
   * @param   modal       the modality of this dialog (true if model, false otherwise)
   */
  public StandardDialogWithApplyClose(JFrame owner, String title, boolean modal) {
    super(owner, title, modal);

    this.pack();
  }

  /**
   * Instantiates the ok and cancel buttons (the labels are taken from the
   * CommonUIResources object) and sets up the button row for
   * this dialog. The OK button is the default button.
   *
   * The Java Look and Feel Design Guidelines suggest that OK and Cancel
   * buttons do not have mnemonics - so this implementation doesn't!
   */
  protected void init() {
    applyButton = ButtonFactory.createButton("Apply");
    closeButton = ButtonFactory.createButton("Close");

    JButton buttons[] = {applyButton, closeButton};
    setButtons(buttons);
  }

  /**
   * A default implementation, adding a simple listener to the cancel button
   * that calls the dispose() method on this dialog.
   *
   * Override this method if you need more complex behaviour in your cancel
   * button (and some functionality on Apply!).
   */
  protected void initListeners() {
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeButtonClicked();
      }
    });

    applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyButtonClicked();
      }
    });
  }

  /**
   * Called when the ok button is clicked on the UI - override if necessary.
   */
  protected void applyButtonClicked() {
  }

  /**
   * Called when the cancel button is clicked on the UI - override if necessary.
   */
  protected void closeButtonClicked() {
    if (canCloseWindow()) {
      close();
    }
  }

  /**
   * A getter for the apply button so that subclasses can use it for adding
   * a listener to, for example.
   *
   * @return  a reference to the ok button used on this dialog
   */
  protected final JButton getApplyButton() {
    return this.applyButton;
  }

  /**
   * A getter for the close button so that subclasses can use it for adding
   * a listener to, for example.
   *
   * @return  a reference to the cancel button used on this dialog
   */
  protected final JButton getCloseButton() {
    return this.closeButton;
  }

  protected JComponent getFirstFocusableComponent() {
    return getApplyButton();
  }

  /**
   * Returns a reference to the button that is to be set as the default on the
   * dialog's rootpane.
   *
   * @return  the JButton to be the default on the rootpane
   */
  public JButton getDefaultButton() {
    return getApplyButton();
  }
}
