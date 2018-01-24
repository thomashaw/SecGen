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
import java.util.Vector;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import pebble.swing.StandardDialogWithClose;
import pebble.swing.StandardDialogWithOKCancel;
import pebble.swing.GridBagPanel;
import pebble.blog.BlogManager;
import pebble.blog.SimpleBlog;
import pebble.blog.Category;

/**
 * ...
 * 
 * @author Simon Brown
 */
public class CategoryDialog extends StandardDialogWithOKCancel {

  private Category category;
  private JTextField idTextField, nameTextField;

  /**
   * Constructor with an existing JFrame as this dialog's owner.
   *
   * @param owner the owner (JFrame) of this dialog
   */
  public CategoryDialog(JDialog owner) {
    super(owner, "Category", true);
  }

  /**
   * Implement this method to perform any set up of the user interface
   * components on this dialog.
   */
  protected void initUI() {
    GridBagPanel gbp = new GridBagPanel(2, 2);
    idTextField = new JTextField(20);
    idTextField.setEditable(false);
    nameTextField = new JTextField(20);
    gbp.addRow("ID", idTextField);
    gbp.addRow("Name", nameTextField);

    setContent(gbp);
  }

  public void setCategory(Category category) {
    this.category = category;

    idTextField.setText(category.getId());
    nameTextField.setText(category.getName());
  }

  /**
   * Called when the ok button is clicked on the UI - override if necessary.
   */
  protected void okButtonClicked() {
    category.setName(nameTextField.getText());
    SimpleBlog blog = (SimpleBlog)BlogManager.getInstance().getBlog();
    blog.getBlogCategoryManager().addCategory(category);
    close();
  }

}
