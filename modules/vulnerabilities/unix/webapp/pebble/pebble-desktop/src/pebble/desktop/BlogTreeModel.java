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
import java.util.Collection;
import java.util.Iterator;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import pebble.blog.Blog;
import pebble.blog.SimpleBlog;
import pebble.blog.YearlyBlog;
import pebble.blog.MonthlyBlog;

/**
 * ...
 * 
 * @author Simon Brown
 */
public class BlogTreeModel extends DefaultTreeModel {

  private DefaultMutableTreeNode root;
  private SimpleBlog blog;

  public BlogTreeModel(SimpleBlog blog) {
    super(new DefaultMutableTreeNode(blog));
    root = (DefaultMutableTreeNode)getRoot();
    this.blog = blog;
    initModel();
  }

  private void initModel() {
    List years = blog.getYearlyBlogs();
    Iterator it = years.iterator();
    while (it.hasNext()) {
      YearlyBlog yearlyBlog = (YearlyBlog)it.next();
      DefaultMutableTreeNode yearNode = new DefaultMutableTreeNode(yearlyBlog);
      root.add(yearNode);

      MonthlyBlog[] months = yearlyBlog.getMonthlyBlogs();
      for (int i = 0; i < months.length; i++) {
        DefaultMutableTreeNode monthNode = new DefaultMutableTreeNode(months[i]);
        yearNode.add(monthNode);
      }
    }

    // uncomment this when deskblog supports templates
    //root.add(new DefaultMutableTreeNode("Templates"));
  }

}
