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
import java.io.File;
import java.io.StringReader;
import java.text.DateFormat;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;

import pebble.blog.Blog;
import pebble.blog.BlogEntry;
import pebble.blog.BlogManager;
import pebble.decorator.BlogEntryDecoratorManager;
import pebble.decorator.BlogEntryDecoratorContext;

/**
 * ...
 * 
 * @author Simon Brown
 */
public class BlogEntryPreviewPanel extends JPanel {

  private JEditorPane htmlPane;

  /**
   * Default, no args constructor.
   */
  public BlogEntryPreviewPanel() {
    initUI();
  }

  /**
   * Sets up the UI.
   */
  private void initUI() {
    Blog blog = BlogManager.getInstance().getBlog();
    htmlPane = new JEditorPane("text/html; charset=" + blog.getCharacterEncoding(), "");
    htmlPane.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(htmlPane);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
  }

  public void setBlogEntry(BlogEntry blogEntry) {
    if (blogEntry != null) {
      BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
      decoratorContext.setView(BlogEntryDecoratorContext.DETAIL_VIEW);
      decoratorContext.setMedia(BlogEntryDecoratorContext.DESKTOP_UI);
      blogEntry = BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext);
      try {
        String blogDir = blogEntry.getRootBlog().getRoot();
        String content = blogEntry.getBody();
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        //content = content.replaceAll("src=\"\\./images/", "src=\"file://" + blog.getRoot() + "/images/");
        content = content.replaceAll("src=\"\\./", "src=\"");
        content = "<h1>" + blogEntry.getTitle() + "</h1>" + content;
        content = content + "<p><h6>Posted by " + blogEntry.getAuthor() + " on " + dateFormat.format(blogEntry.getDate()) + "</h6></p>";
        content = content.replaceAll("/>", ">");
        content = "<html><head><base href=\"" + new File(blogDir, "index.html").toURL() + "\"><style type=\"text/css\">body { font: Lucida Grande, Verdana, Arial; } h1 { font-size: 16px; } h6 { color: gray; font-weight: bold; font-size: 9px; }</style></head><body>" + content + "</body></html>";
        HTMLDocument doc = new HTMLDocument();
        doc.setBase(new File(blogDir, "index.html").toURL());
        htmlPane.setContentType("text/html");
        htmlPane.read(new StringReader(content), doc);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    } else {
      htmlPane.setText("");
    }
  }

}
