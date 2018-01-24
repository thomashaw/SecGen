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
package pebble.desktop;

import java.util.List;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import pebble.blog.BlogEntry;
import pebble.swing.AlternatingRowTableCellRenderer;
import pebble.swing.RightAlignedDateTableCellRenderer;

/**
 * ...
 * 
 * @author Simon Brown
 */
public class BlogEntryTable extends JTable {

  public BlogEntryTable() {
    super();
  }

  public void setModel(TableModel model) {
    super.setModel(model);

    configureTable();
  }

  private void configureTable() {
    if (getModel() instanceof BlogEntryTableModel) {
      setRowSelectionAllowed(true);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
      getColumnModel().getColumn(0).setCellRenderer(new AlternatingRowTableCellRenderer());
      int tableWidth = getWidth();
      getColumnModel().getColumn(0).setPreferredWidth((tableWidth*66)/100);
      getColumnModel().getColumn(1).setCellRenderer(new RightAlignedDateTableCellRenderer());
      getColumnModel().getColumn(1).setPreferredWidth((tableWidth*33)/100);
      setRowHeight(24);
      setShowGrid(false);
    }
  }

  public BlogEntry getSelectedBlogEntry() {
    BlogEntryTableModel model = (BlogEntryTableModel)getModel();
    return model.getBlogEntry(getSelectedRow());
  }

  public void setBlogEntries(List blogEntries) {
    BlogEntryTableModel model = (BlogEntryTableModel)getModel();
    model.setBlogEntries(blogEntries);
  }

}
