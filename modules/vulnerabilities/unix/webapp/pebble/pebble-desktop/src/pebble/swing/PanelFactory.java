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

/**
 * An easy to use factory that can create a number of panels that follow the
 * suggestions made in the Java Look and Feel Guidelines.
 *
 * @author      Simon Brown
 */
public class PanelFactory implements LookAndFeelGuidelines {

  /**
   * Creates a panel containing the specified buttons, all at the
   * same width.
   *
   * @param   buttons     the array of JButtons to be added to the panel
   * @return  an instance of JPanel containing the specified buttons
   */
  public static JPanel createHorizontalButtonPanel(JButton buttons[]) {
    JPanel p;
    JPanel q;

    p = new JPanel(new GridLayout(1, buttons.length, HORIZONTAL_BUTTON_SPACING, 0));

    for (int i = 0; i < buttons.length; i++) {
      p.add(buttons[i]);
    }

    q = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
    q.add(p);

    return q;
  }

  /**
   * Creates a panel containing the specified buttons, all at the
   * same width.
   *
   * @param   buttons     the array of JButtons to be added to the panel
   * @return  an instance of JPanel containing the specified buttons
   */
  public static JPanel createVerticalButtonPanel(JButton buttons[]) {
    return createVerticalButtonPanel(buttons, SwingConstants.TOP);
  }

  /**
   * Creates a panel containing the specified buttons, all at the
   * same width.
   *
   * @param   buttons     the array of JButtons to be added to the panel
   * @return  an instance of JPanel containing the specified buttons
   */
  public static JPanel createVerticalButtonPanel(JButton buttons[], int position) {

    if (position == SwingConstants.CENTER) {
      GridBagPanel p = new GridBagPanel(buttons.length, 1);

      for (int i = 0; i < buttons.length; i++) {
        p.addToEndOfRow(buttons[i], false, false);
      }

      return p;
    } else {
      JPanel p = new JPanel(new GridLayout(buttons.length, 1, 0, VERTICAL_BUTTON_SPACING));

      for (int i = 0; i < buttons.length; i++) {
        p.add(buttons[i]);
      }

      JPanel q = new JPanel(new BorderLayout());

      switch (position) {
        case SwingConstants.BOTTOM:
          q.add(p, BorderLayout.SOUTH);
          break;
        default :
          q.add(p, BorderLayout.NORTH);
          break;
      }

      return q;
    }
  }
}
