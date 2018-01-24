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
import java.util.Iterator;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import pebble.blog.*;
import pebble.swing.GridBagPanel;
import pebble.swing.StandardDialogWithOKCancel;
import pebble.swing.CursorUtilities;

/**
 * ...
 * 
 * @author Simon Brown
 */
public class BlogEntryDialog extends StandardDialogWithOKCancel {

  private BlogEntry blogEntry;
  private JTextField titleTextField;
  private JTextArea bodyTextArea;
  private JTabbedPane tabbedPane;
  private BlogEntryPreviewPanel blogEntryPreviewPanel;

  /**
   * Constructor with an existing JFrame as this dialog's owner.
   *
   * @param owner the owner (JFrame) of this dialog
   */
  public BlogEntryDialog(JFrame owner) {
    super(owner, "Blog Entry", true);
  }

  /**
   * Implement this method to perform any set up of the user interface
   * components on this dialog.
   */
  protected void initUI() {
    tabbedPane = new JTabbedPane();
    GridBagPanel gbp = new GridBagPanel(2, 2);
    titleTextField = new JTextField(20);
    bodyTextArea = new JTextArea(20, 50);
    bodyTextArea.setLineWrap(true);
    SimpleBlog blog = (SimpleBlog)BlogManager.getInstance().getBlog();

    gbp.addRow("Title", titleTextField);
    JScrollPane scrollPane = new JScrollPane(bodyTextArea);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    gbp.addLabel("Body");
    gbp.addToEndOfRow(scrollPane, true, true);

    /*
    List categories = blog.getBlogCategoryManager().getCategories();
    JPanel categoriesPanel = new JPanel();
    categoriesPanel.setLayout(new GridLayout((categories.size()+1)/2, 2));
    Iterator it = categories.iterator();
    while (it.hasNext()) {
      Category category = (Category)it.next();
      JCheckBox categoryCB = new JCheckBox(category.getName());
      //categoryCB.setSelected(blogEntry.inCategory(category));
      categoriesPanel.add(categoryCB);
    }

    gbp.addRow("Category", categoriesPanel);
    */

    tabbedPane.add("Edit", gbp);

    blogEntryPreviewPanel = new BlogEntryPreviewPanel();
    tabbedPane.add("Preview", blogEntryPreviewPanel);
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (tabbedPane.getSelectedIndex() == 1) {
          BlogEntry blogEntryCopy = (BlogEntry)blogEntry.clone();
          populateBlogEntry(blogEntryCopy);
          blogEntryPreviewPanel.setBlogEntry(blogEntryCopy);
        }
      }
    });

    setContent(tabbedPane);
  }

  public BlogEntry getBlogEntry() {
    return this.blogEntry;
  }

  public void setBlogEntry(BlogEntry blogEntry) {
    this.blogEntry = blogEntry;

    titleTextField.setText(blogEntry.getTitle());
    bodyTextArea.setText(blogEntry.getBody());
  }

  private void populateBlogEntry(BlogEntry blogEntry) {
    blogEntry.setTitle(titleTextField.getText());
    blogEntry.setBody(bodyTextArea.getText());
    blogEntry.setAuthor(System.getProperty("user.name"));
  }

  /**
   * Called when the ok button is clicked on the UI - override if necessary.
   */
  protected void okButtonClicked() {
    CursorUtilities.setWaitCursor(this);
    SimpleBlog blog = (SimpleBlog)BlogManager.getInstance().getBlog();
    populateBlogEntry(blogEntry);

    try {
      switch (blogEntry.getType()) {
        case BlogEntry.NEW :
          blog.getBlogForToday().addEntry(blogEntry);
          blogEntry.store();
          break;
        case BlogEntry.TEMPLATE :
          blogEntry = blog.getBlogForToday().createBlogEntry();
          blog.getBlogForToday().addEntry(blogEntry);
          blogEntry.store();
          break;
        case BlogEntry.DRAFT :
          BlogEntry draftBlogEntry = blogEntry;
          blogEntry = blog.getBlogForToday().createBlogEntry();
          blog.getBlogForToday().addEntry(blogEntry);
          blogEntry.store();
          draftBlogEntry.remove();
          break;
        default :
          blogEntry.store();
          break;
      }

      // and close this dialog
      close();
    } catch (BlogException e) {
      e.printStackTrace();
    }
  }

  /**
   * Called when the cancel button is clicked on the UI - override if necessary.
   */
  protected void cancelButtonClicked() {
    if (canCloseWindow()) {
      this.blogEntry = null;
      close();
    }
  }

  /**
   * Called before the dialog is closed, and runs any cleanup logic before closing.
   *
   * @return true if the dialog can continue to close,
   *         false if the dialog should remain open
   */
  protected boolean canCloseWindow() {
    boolean modified = false;
    modified |= !titleTextField.getText().equals(blogEntry.getTitle());
    modified |= !bodyTextArea.getText().equals(blogEntry.getBody());

    if (modified) {
      return JOptionPane.showConfirmDialog(this, "Save changes?", "Pebble deskblog", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION;
    } else {
      return true;
    }
  }

}
