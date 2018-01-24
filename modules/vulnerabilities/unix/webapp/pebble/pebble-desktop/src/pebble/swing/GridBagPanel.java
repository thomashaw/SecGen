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
 * An extension of JPanel with it's layout already set to GridBagLayout.
 * <br><br>
 * This has been created to take the "pain" out of creating professional
 * looking user interfaces using the GridBagLayout, which also conform
 * to the recommendations made in the Java Look and Feel Design Guidelines.
 *
 * @author      Simon Brown
 */
public class GridBagPanel extends JPanel implements LookAndFeelGuidelines {

  /** the grid bag constraints used internally within this panel */
  private GridBagConstraints constraints;

  /** the grid bag layout used internally within this panel */
  private GridBagLayout layout;

  /** a counter used to keep track of the current "grid x" position when adding components */
  private int gridx = 0;
  /** a counter used to keep track of the current "grid y" position when adding components */
  private int gridy = 0;

  /** the number of "rows" that the grid bag layout consists of */
  private int rows = 1;
  /** the number of "columns" that the grid bag layout consists of */
  private int columns = 2;

  /**
   * Constructor for this panel, allowing the number of rows and columns
   * in the underlying grid bag layout to be specified.
   *
   * @param   rows        the number of rows in the layout
   * @param   columns     the number of columns in the layout
   */
  public GridBagPanel(int rows, int columns) {
    this.rows = rows;
    this.columns = columns;

    init();
    initUI();
    initListeners();
  }

  /**
   * Sets up the layout and constraints for this panel.
   */
  private void init() {
    constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;

    layout = new GridBagLayout();

    setLayout(layout);
  }

  /**
   * Placeholder for any code to set up the user interface - override this!
   */
  protected void initUI() {
  }

  /**
   * Placeholder for any code to set up the listeners - override this!
   */
  protected void initListeners() {
  }

  /**
   * Groups of components in a form should be separated by "extra" space
   * between the rows - this method allows that space to be inserted.
   */
  public void addSeparator() {
    JLabel label = new JLabel();

    constraints.gridx = 0;
    constraints.gridy = gridy++;

    constraints.weightx = 1;
    constraints.gridwidth = columns;
    constraints.gridheight = 1;
    constraints.insets = new Insets(0, 0, 6, 0);

    add(label, constraints);
  }

  /**
   * A convenient way of adding a label and component pair to the grid bag
   * layout, conforming to the Java Look and Feel Design Guidelines.
   * <br><br>
   * For any grid baggers out there, the label will be zero weighted whilst
   * the component will be given a weight of 1 to allow it to grow horizontally.
   *
   * @param   labelText       the text for the label to be associated with
   *                          "component"
   * @param   component       the JComponent (e.g. textfield, combo, ...) to
   *                          be added
   */
  public JLabel addRow(String labelText, JComponent component) {
    JLabel label = addLabel(labelText);

    addToEndOfRow(component, true, false);

    return label;
  }

  /**
   * A convenient way of adding a label and component pair to the grid bag
   * layout, conforming to the Java Look and Feel Design Guidelines.
   * <br><br>
   * For any grid baggers out there, the label will be zero weighted whilst
   * the component will be given a weight of 1 to allow it to grow horizontally.
   *
   * @param   labelText       the text for the label to be associated with
   *                          "component"
   * @param   component       the JComponent (e.g. textfield, combo, ...) to
   *                          be added
   * @param   mnemonic        the single character mnemonic to be diaplayed on
   *                          the label to allow the user to jump straight to
   *                          component with a keystroke
   */
  public JLabel addRow(String labelText, JComponent component, char mnemonic) {
    JLabel label = addLabel(labelText);

    label.setLabelFor(component);
    label.setDisplayedMnemonic(mnemonic);

    addToEndOfRow(component, true, false);

    return label;
  }

  /**
   * Adds a label to the current row in the layout. This is used by the addRow
   * methods, but can also be called to add a single label if necessary.
   *
   * @param   labelText       the text for the label to be added
   */
  public JLabel addLabel(String labelText) {
    JLabel label;

    if (labelText.length() > 0) {
      label = new JLabel(labelText + ":", SwingConstants.LEFT);
    } else {
      label = new JLabel("", SwingConstants.LEFT);
    }

    label.setVerticalAlignment(SwingConstants.TOP);

    if ((gridy + 1) == rows) {
      constraints.insets = new Insets(0, 0, 0, LABEL_COMPONENT_SPACING - HORIZONTAL_COMPONENT_SPACING);
    } else {
      constraints.insets = new Insets(0, 0, VERTICAL_COMPONENT_SPACING, LABEL_COMPONENT_SPACING - HORIZONTAL_COMPONENT_SPACING);
    }

    constraints.gridx = gridx++;
    constraints.gridy = gridy;

    // if we've reached the end of the row (e.g. gridx is now 2 in a 2 column grid)
    // - reset gridx back to 0
    // - increment gridy (representing the next row)
    if (gridx == columns) {
      gridx = 0;
      gridy++;
    }

    constraints.weightx = 0;
    constraints.weighty = 0;

    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    add(label, constraints);

    return label;
  }

  /**
   * Adds a component to the end of the current row - specifying whether it
   * should be remain the same size or be expandable with the container
   * it is in.
   *
   * @param   component                   the JComponent to be added to the end of the
   *                                      current row
   * @param   horizontallyExpandable      true if the component should expand horizontally
   *                                      with the surrounding container, false otherwise
   * @param   verticallyExpandable        true if the component should expand vertically
   *                                      with the surrounding container, false otherwise
   */
  public void addToEndOfRow(JComponent component, boolean horizontallyExpandable, boolean verticallyExpandable) {
    constraints.gridx = gridx;
    constraints.gridy = gridy;

    int leftInsets;
    int bottomInsets;

    if ((gridy + 1) == rows) {
      bottomInsets = 0;
    } else {
      bottomInsets = VERTICAL_COMPONENT_SPACING;
    }

    if (gridx == 0) {
      leftInsets = 0;
    } else {
      leftInsets = HORIZONTAL_COMPONENT_SPACING;
    }

    constraints.insets = new Insets(0, leftInsets, bottomInsets, 0);

    constraints.gridwidth = columns - gridx;
    constraints.gridheight = 1;

    if (horizontallyExpandable) {
      constraints.weightx = 1;
    } else {
      constraints.weightx = 0;
    }

    if (verticallyExpandable) {
      constraints.weighty = 1;
    } else {
      constraints.weighty = 0;
    }

    // - reset gridx back to 0
    // - increment gridy (representing the next row)
    gridx = 0;
    gridy++;

    add(component, constraints);
  }

  /**
   * Adds a component to the current row - specifying whether it
   * should be remain the same size or be expandable with the container
   * it is in.
   *
   * @param   component                   the JComponent to be added to the current row
   * @param   horizontallyExpandable      true if the component should expand horizontally
   *                                      with the surrounding container, false otherwise
   * @param   verticallyExpandable        true if the component should expand vertically
   *                                      with the surrounding container, false otherwise
   */
  public void addToRow(JComponent component, boolean horizontallyExpandable, boolean verticallyExpandable) {
    constraints.gridx = gridx;
    constraints.gridy = gridy;

    int leftInsets;
    int bottomInsets;

    if ((gridy + 1) == rows) {
      bottomInsets = 0;
    } else {
      bottomInsets = VERTICAL_COMPONENT_SPACING;
    }

    if (gridx == 0) {
      leftInsets = 0;
    } else {
      leftInsets = HORIZONTAL_COMPONENT_SPACING;
    }

    constraints.insets = new Insets(0, leftInsets, bottomInsets, 0);

    // if we've reached the end of the row (e.g. gridx is now 2 in a 2 column grid)
    // - reset gridx back to 0
    // - increment gridy (representing the next row)
    if (gridx == columns) {
      gridx = 0;
      gridy++;
    }

    gridx++;

    if (horizontallyExpandable) {
      constraints.weightx = 1;
    } else {
      constraints.weightx = 0;
    }

    if (verticallyExpandable) {
      constraints.weighty = 1;
    } else {
      constraints.weighty = 0;
    }

    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    add(component, constraints);
  }

  /**
   * Getter for the constraints object - should it be needed!
   *
   * @return  the constraints object in the grid bag layout
   */
  public GridBagConstraints getConstraints() {
    return this.constraints;
  }

  /**
   * Adds a JComponent to the grid bag layout using the specified constraints
   * object. Mainly used internally, but public if more flexibility is
   * required.
   *
   * @param   component       the JComponent to be added
   * @param   constraints     the constraints object to be associated with the
   *                          component when added
   */
  public void add(JComponent component, GridBagConstraints constraints) {
    layout.setConstraints(component, constraints);
    add(component);
  }

  /**
   * Getter for the current grid x position in the grid bag layout.
   *
   * @return  the current grid x position in the grid bag layout
   */
  public int getCurrentGridX() {
    return gridx;
  }

  /**
   * Getter for the current grid y position in the grid bag layout.
   *
   * @return  the current grid y position in the grid bag layout
   */
  public int getCurrentGridY() {
    return gridy;
  }
}
