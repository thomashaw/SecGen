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
import java.util.MissingResourceException;

/**
 * An easy to use factory that can create a button using the CommonUIResources
 * class to obtain information about labels and mnemonics.
 *
 * @author      Simon Brown
 */
public class ButtonFactory implements LookAndFeelGuidelines {

  /**
   * Factory method that creates an instance of JButton, prepared with the
   * appropriate label and mnemonic.
   *
   * @param   buttonType      a resource constant from CommonUIResources or
   *                          any subclass (i.e. a constant used in the
   *                          resource bundle)
   * @return  an instance of JButton with the appropriate label and mnemonic
   */
  public static JButton createButton(String buttonText) {
    JButton button = new JButton();

    // first set the label
    button.setText(buttonText);

    // and set the margins (as defined in the Java Look and Feel Design Guidelines)
    // (except if it's the "..." button we are creating as it should be a thin button)
    if (!buttonText.equals("...")) {
      Insets margin = button.getMargin();
      margin.left = HORIZONTAL_BUTTON_MARGIN;
      margin.right = HORIZONTAL_BUTTON_MARGIN;
      button.setMargin(margin);
    }

    return button;
  }

  /**
   * Factory method that creates an instance of JButton, prepared with the
   * appropriate label and mnemonic.
   *
   * @param   buttonType      a resource constant from CommonUIResources or
   *                          any subclass (i.e. a constant used in the
   *                          resource bundle)
   * @param   showMnemonic    true if the button's mnemonic (if it has one)
   *                          should be shown, false otherwise
   * @return  an instance of JButton with the appropriate label and mnemonic
   */
  public static JButton createButton(String buttonText, char mnemonic) {
    JButton button = createButton(buttonText);

    // and then try and set the mnemonic - this is in a separate try-catch
    // block because they may not be a mnemonic
    button.setMnemonic(mnemonic);

    return button;
  }
}
