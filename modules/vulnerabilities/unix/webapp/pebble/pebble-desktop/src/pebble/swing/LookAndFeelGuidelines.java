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

/**
 * The <a href="http://java.sun.com/products/jlf/">Java Look and Feel Design Guidelines</a>
 * has recommendations for user interface designs.
 * <br><br>
 * This interface is really just a single place to keep constants representing
 * the various values for button spacing, component spacing, margins, borders, etc.
 * <br><br>
 * Implementing this interface is just an easy way to reference the constants
 * defined in this class!
 *
 * @author      Simon Brown
 */
public interface LookAndFeelGuidelines {

  /** the horizontal margin between the button text and the edge of the button */
  public final static int HORIZONTAL_BUTTON_MARGIN = 12;

  /** the horizontal spacing between command buttons in a row */
  public final static int HORIZONTAL_BUTTON_SPACING = 5;
  /** the vertical spacing between command buttons in a row */
  public final static int VERTICAL_BUTTON_SPACING = 5;

  /** the spacing between the content of a dialog and the top of the dialog */
  public final static int DIALOG_OUTSIDE_BORDER_TOP = 12;
  /** the spacing between the content of a dialog and the left of the dialog */
  public final static int DIALOG_OUTSIDE_BORDER_LEFT = 12;
  /** the spacing between the content of a dialog and the bottom of the dialog */
  public final static int DIALOG_OUTSIDE_BORDER_BOTTOM = 11;
  /** the spacing between the content of a dialog and the right of the dialog */
  public final static int DIALOG_OUTSIDE_BORDER_RIGHT = 11;
  /** the spacing between the content of a dialog and the row of command buttons */
  public final static int DIALOG_CONTENT_BUTTONS_VERTICAL_GAP = 17;

  /** the spacing between the content of a dialog and the top of the panel */
  public final static int PANEL_OUTSIDE_BORDER_TOP = 6;
  /** the spacing between the content of a dialog and the left of the panel */
  public final static int PANEL_OUTSIDE_BORDER_LEFT = 6;
  /** the spacing between the content of a dialog and the bottom of the panel */
  public final static int PANEL_OUTSIDE_BORDER_BOTTOM = 5;
  /** the spacing between the content of a dialog and the right of the panel */
  public final static int PANEL_OUTSIDE_BORDER_RIGHT = 5;

  /** the spacing between the content of a splash screen and the top of the splash screen */
  public final static int SPLASH_OUTSIDE_BORDER_TOP = 12;
  /** the spacing between the content of a splash screen and the left of the splash screen */
  public final static int SPLASH_OUTSIDE_BORDER_LEFT = 12;
  /** the spacing between the content of a splash screen and the bottom of the splash screen */
  public final static int SPLASH_OUTSIDE_BORDER_BOTTOM = 12;
  /** the spacing between the content of a splash screen and the right of the splash screen */
  public final static int SPLASH_OUTSIDE_BORDER_RIGHT = 12;

  /** the horizontal spacing between components in a form */
  public final static int HORIZONTAL_COMPONENT_SPACING = 5;
  /** the vertical spacing between components (and rows of components) in a form */
  public final static int VERTICAL_COMPONENT_SPACING = 5;
  /** the horizontal spacing between a label and the component it is associated with */
  public final static int LABEL_COMPONENT_SPACING = 12;

  public static final int HORIZONTAL_COMPONENT_SEPARATOR_SPACING = 11;
  public static final int VERTICAL_COMPONENT_SEPARATOR_SPACING = 11;
}
