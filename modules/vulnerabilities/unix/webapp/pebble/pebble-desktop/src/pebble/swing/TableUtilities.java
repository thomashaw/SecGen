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
import javax.swing.table.TableColumn;
import java.awt.*;

public class TableUtilities {

  public static int setVisibleRowCount(JTable table, int visibleRowCount) {
    // to set the number of rows visible in a table, we must
    //
    // (1) find out the height of the header
    // (2) multiply the number of rows by the individual row height
    // (3) add it all up and set the preferred size on the viewport

    Dimension d = getPreferredScrollableViewportSize(table, visibleRowCount);

    table.setPreferredScrollableViewportSize(d);

    return d.height;
  }

  public static Dimension getPreferredScrollableViewportSize(JTable table, int visibleRowCount) {
    // to get the size of the viewport of a table, we must
    //
    // (1) find out the height of the header
    // (2) multiply the number of rows by the individual row height

    int headerHeight = table.getTableHeader().getHeight();
    int rowHeight = table.getRowHeight();
    int extra = table.getInsets().top + table.getInsets().bottom + 1;

    int preferredWidth = table.getWidth();
    int preferredHeight = headerHeight + (visibleRowCount * rowHeight) + extra;

    Dimension d = new Dimension(preferredWidth, preferredHeight);

    return d;
  }


  /**
   * Sets the preferred width of a table's columns based on the data in the first row.
   *
   * @param   table       the JTable to be sized
   * @return  int         the combined width of the table's columns
   */
  public static int setDefaultColumnWidth(JTable table) {
    int totalWidth = -1, preferredWidth = 0;
    if (table.getRowCount() > 0 && table.getColumnCount() > 0) {
      TableColumn column = table.getColumnModel().getColumn(0);
      // size the columns according to the data in the first row of the table
      Component comp = ((DefaultCellEditor) (table.getCellEditor(0, 0))).getComponent();
      FontMetrics fontMetrics = comp.getFontMetrics(comp.getFont());
      for (int i = 0; i < table.getColumnCount(); i++) {
        column = table.getColumnModel().getColumn(i);
        // set the preferred width to the width of the data in the cell or the table header text, whichever is wider
        preferredWidth = Math.max(fontMetrics.stringWidth(((table.getValueAt(0, i)).toString()) + 5), fontMetrics.stringWidth(((String) (table.getTableHeader().getColumnModel().getColumn(i).getHeaderValue())) + 5));
        column.setPreferredWidth(preferredWidth);
        totalWidth += preferredWidth;
      }
    }
    return totalWidth; // the combined widths of the columns or -1 if no data exists
  }

  /**
   * Scroll to the specified row of a table.
   *
   * @param   table       the JTable to be sized
   * @param   row         the row to scroll to
   * @throws  java.lang.IllegalArgumentException if 1) the table is null,
   *                                      2) the row is out of bounds,
   *                                      3) the table has not been added to a scrollable container
   */
  public static void scrollToRow(JTable table, int row) {
    if (table == null) {
      throw new IllegalArgumentException("JTable argument is null.");
    }
    if (row < 0 || row > table.getRowCount() - 1) {
      throw new IllegalArgumentException("Row argument is outside bound of rows in the JTable.");
    }
    Container c = table.getParent();
    // can only work with a scrollpane or a viewport
    JScrollPane sp = null;
    JViewport vp = null;
    if (c instanceof JScrollPane) {
      sp = (JScrollPane) c;
      vp = sp.getViewport();
    } else if (c instanceof JViewport) {
      vp = (JViewport) c;
    } else {
      throw new IllegalArgumentException("JTable is not contained by a JScrollPane.");
    }
    int rowHeight = table.getRowHeight();
    // top-left coord of current view
    java.awt.Point p1 = vp.getViewPosition();
    // find out where the row is that we want to scroll to
    java.awt.Rectangle rect = table.getCellRect(row, 0, true);
    java.awt.Point p = vp.toViewCoordinates(rect.getLocation());
    rect = new java.awt.Rectangle(p, rect.getSize());
    // scrollRectToVisible doesn't seem to work when the row is off the top
    // of the view so set the view position
    if (p.y < p1.y) {
      vp.setViewPosition(p);
    } else {
      vp.scrollRectToVisible(rect);
    }
  }

}
