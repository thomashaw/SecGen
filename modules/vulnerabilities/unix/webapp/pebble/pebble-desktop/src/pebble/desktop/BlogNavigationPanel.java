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

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import pebble.blog.*;
import pebble.search.BlogSearcher;
import pebble.search.SearchException;
import pebble.search.SearchHit;
import pebble.search.SearchResults;
import pebble.swing.ApplicationFrame;

/**
 * ...
 * 
 * @author Simon Brown
 */
public class BlogNavigationPanel extends JPanel {

  private JSplitPane horizontalSplit;
  private JSplitPane verticalSplit;

  private JTree tree;
  private BlogEntryTable blogEntryTable;
  private BlogEntryPreviewPanel blogEntryPreviewPanel;

  private ApplicationFrame owner;

  private SimpleBlog blog;

  /**
   * Default, no args constructor.
   */
  public BlogNavigationPanel(ApplicationFrame owner) {
    blog = (SimpleBlog)BlogManager.getInstance().getBlog();

    initUI();

    this.owner = owner;
  }

  /**
   * Sets up the UI.
   */
  private void initUI() {
    blogEntryTable = new BlogEntryTable();

    horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JScrollPane scrollPane = new JScrollPane(blogEntryTable);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);

    verticalSplit.setTopComponent(panel);

    blogEntryPreviewPanel = new BlogEntryPreviewPanel();
    verticalSplit.setBottomComponent(blogEntryPreviewPanel);
    verticalSplit.setBorder(BorderFactory.createEmptyBorder());

    tree = new JTree(new BlogTreeModel(blog));
    tree.setRootVisible(true);
    scrollPane = new JScrollPane(tree);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    horizontalSplit.setLeftComponent(scrollPane);
    horizontalSplit.setRightComponent(verticalSplit);
    horizontalSplit.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

    setLayout(new BorderLayout());
    add(horizontalSplit, BorderLayout.CENTER);

    tree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent evt) {
            // Get all nodes whose selection status has changed
            TreePath[] paths = evt.getPaths();

            // Iterate through all affected nodes
            for (int i=0; i<paths.length; i++) {
                if (evt.isAddedPath(i)) {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                  Object o = node.getUserObject();
                  owner.setWaitCursor("Loading blog entries");
                  if (o instanceof MonthlyBlog) {
                    MonthlyBlog monthlyBlog = (MonthlyBlog)o;
                    blogEntryTable.setModel(new BlogEntryTableModel(monthlyBlog.getBlogEntries()));
                  } if (o instanceof SimpleBlog) {
                    SimpleBlog blog = (SimpleBlog)o;
                    blogEntryTable.setModel(new BlogEntryTableModel(blog.getRecentBlogEntries(10)));
                  } else if (o.equals("Drafts")) {
                    SimpleBlog blog = (SimpleBlog)BlogManager.getInstance().getBlog();
                    blogEntryTable.setModel(new BlogEntryTableModel(blog.getDraftBlogEntries()));
                  } else if (o.equals("Templates")) {
                    SimpleBlog blog = (SimpleBlog)BlogManager.getInstance().getBlog();
                    blogEntryTable.setModel(new BlogEntryTableModel(blog.getBlogEntryTemplates()));
                  }
                  owner.setDefaultCursor();
                }
            }
        }
    });
    tree.setRowHeight(tree.getRowHeight()+4);

    blogEntryTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          BlogEntryDialog dialog = new BlogEntryDialog(owner);
          dialog.setBlogEntry(blogEntryTable.getSelectedBlogEntry());
          dialog.setVisible(true);
        }
      }
    });

    blogEntryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        owner.setWaitCursor("Displaying blog entry");
        BlogEntry blogEntry = blogEntryTable.getSelectedBlogEntry();
        blogEntryPreviewPanel.setBlogEntry(blogEntry);
        owner.setDefaultCursor();
      }
    });

    blogEntryTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
          deleteSelectedBlogEntry();
        }
      }
    });
  }

  public void resizeSplitPanes() {
    verticalSplit.setDividerLocation(0.50);
    horizontalSplit.setDividerLocation(0.25);
  }

  public void showRecentBlogEntries() {
    tree.setModel(new BlogTreeModel(blog));
    tree.setSelectionRow(0);
    if (blogEntryTable.getRowCount() > 0) {
      blogEntryTable.getSelectionModel().setSelectionInterval(0, 0);
    }
  }

  public void findBlogEntries(String text) {
    try {
      owner.setWaitCursor("Performing search");
      BlogSearcher searcher = new BlogSearcher();
      SearchResults results = null;
      results = searcher.search(BlogManager.getInstance().getBlog(), text);
      results.sortByScoreDescending();
      Iterator it = results.getHits().iterator();
      List blogEntries = new ArrayList();
      while (it.hasNext()) {
        SearchHit hit = (SearchHit)it.next();
        blogEntries.add(blog.getBlogEntry(hit.getId()));
      }
      blogEntryTable.setModel(new BlogEntryTableModel(blogEntries));
    } catch (SearchException e1) {
      e1.printStackTrace();
    } finally {
      owner.setDefaultCursor();
    }
  }

  public void deleteSelectedBlogEntry() {
    BlogEntry blogEntry = blogEntryTable.getSelectedBlogEntry();
    if (blogEntry != null) {
      JOptionPane optionPane = new JOptionPane("Delete selected blog entry?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
      optionPane.setInitialValue(new Integer(JOptionPane.NO_OPTION));
      optionPane.createDialog(this, "Pebble deskblog");
      JDialog dialog = optionPane.createDialog(this, "Pebble deskblog");
      dialog.setVisible(true);
      int option = ((Integer)optionPane.getValue()).intValue();
      if (option == JOptionPane.YES_OPTION) {
        DailyBlog dailyBlog = blogEntry.getDailyBlog();
        try {
          blogEntry.remove();
          dailyBlog.removeEntry(blogEntry);
        } catch (BlogException be) {
          be.printStackTrace();
        }
      }
    }
  }

}
