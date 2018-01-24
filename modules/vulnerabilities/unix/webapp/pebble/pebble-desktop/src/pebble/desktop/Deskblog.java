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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.TimeZone;

import javax.swing.*;

import pebble.blog.BlogEntry;
import pebble.blog.BlogManager;
import pebble.blog.SimpleBlog;
import pebble.blog.Blog;
import pebble.blog.persistence.DAOFactory;
import pebble.blog.persistence.file.FileDAOFactory;
import pebble.swing.ApplicationFrame;
import pebble.swing.SplashScreen;
import pebble.swing.PlatformUtils;

/**
 * Starting point for the Pebble deskblog client.
 * 
 * @author Simon Brown
 */
public class Deskblog extends ApplicationFrame {

  private static String blogDir;

  private BlogNavigationPanel blogNavigationPanel;

  /**
   * Sets up the user interface for this frame.
   */
  protected void initUI() {
    // keep the user occupied while we do some setup :-)
    SplashScreen splash = new SplashScreen(createPanelForSplashScreen());
    splash.setVisible(true);

    initBlog();

    blogNavigationPanel = new BlogNavigationPanel(this);

    getContentPane().add(blogNavigationPanel);
    setSize(800, 600);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);
    setVisible(true);

    blogNavigationPanel.resizeSplitPanes();
    blogNavigationPanel.showRecentBlogEntries();

    // job is done - close the splash screen
    splash.setVisible(false);
  }

  private void initBlog() {
    BlogManager.getInstance().setBlogDir(blogDir);
    DAOFactory.setConfiguredFactory(new FileDAOFactory());
    SimpleBlog blog = new SimpleBlog(BlogManager.getInstance().getBlogDir());
    // reset the timezone to the local timezone, since blog entries
    // are added locally
    blog.setProperty(Blog.TIMEZONE_KEY, TimeZone.getDefault().getID());
    BlogManager.getInstance().setBlog(blog);
  }

  /**
   * Sets up the menubar for this frame.
   */
  protected void initMenuBar() {
    JMenuItem item;
    JMenu file = new JMenu("File");

    file.add(item = new JMenuItem("New Blog Entry..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleBlog blog = (SimpleBlog)BlogManager.getInstance().getBlog();
        BlogEntry blogEntry = blog.getBlogForToday().createBlogEntry();
        blogEntry.setBody("<p></p>");
        BlogEntryDialog dialog = new BlogEntryDialog(Deskblog.this);
        dialog.setBlogEntry(blogEntry);
        dialog.setVisible(true);
        blogNavigationPanel.showRecentBlogEntries();
      }
    });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    file.add(item = new JMenuItem("Delete"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        blogNavigationPanel.deleteSelectedBlogEntry();
      }
    });

    file.addSeparator();

//    file.add(item = new JMenuItem("Categories..."));
//    item.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        JDialog dialog = new CategoriesDialog(Deskblog.this);
//        dialog.setVisible(true);
//      }
//    });
//
//    file.addSeparator();

    file.add(item = new JMenuItem("Find..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String text = JOptionPane.showInputDialog("Text to find");
        blogNavigationPanel.findBlogEntries(text);
      }
    });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    menubar.add(file);

    if (!PlatformUtils.isMacOSX()) {
      file.addSeparator();
      file.add(item = new JMenuItem("Exit"));
      item.addActionListener(exitAction);
      item.setMnemonic('x');
    }
  }

  /**
   * Helper method to create the panel used in the splash screen.
   *
   * @return  a configured JPanel instance
   */
  private JPanel createPanelForSplashScreen() {
    JPanel panel = new JPanel(new BorderLayout());
    URL imageUrl = Thread.currentThread().getContextClassLoader().getResource("pebble.jpg");
    panel.add(new JLabel(new ImageIcon(imageUrl), SwingConstants.CENTER), BorderLayout.CENTER);
    panel.add(new JLabel("Blogging tools written in Java", SwingConstants.CENTER), BorderLayout.SOUTH);
    panel.setBorder(BorderFactory.createEmptyBorder());
    panel.setBackground(Color.WHITE);

    return panel;
  }

  /**
   * Returns the name of the application - used in the titlebar of this frame.
   *
   * @return the name of the application
   */
  public String getApplicationName() {
    return "Pebble deskblog";
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    if (args.length > 0) {
      blogDir = args[0];
    } else {
      blogDir = "${user.home}/blog";
    }
    new Deskblog();
  }

}
