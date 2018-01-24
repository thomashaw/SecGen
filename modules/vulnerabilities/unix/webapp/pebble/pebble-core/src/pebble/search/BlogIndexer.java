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
package pebble.search;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import pebble.blog.*;
import pebble.PebbleProperties;

/**
 * Wraps up the functionality to index blog entries. This is really just
 * a convenient wrapper around Lucene.
 *
 * @author    Simon Brown
 */
public class BlogIndexer {

  /** the log used by this class */
  private static final Log log = LogFactory.getLog(BlogIndexer.class);

  /**
   * Allows an entire root blog to be indexed. In other words, this indexes
   * all of the blog entries within the specified root blog.
   *
   * @param rootBlog    the Blog instance to index
   */
  public void index(SimpleBlog rootBlog) {
    try {
      log.debug("Creating index for all blog entries in " + rootBlog.getName());
      Analyzer analyzer = getAnalyzer(rootBlog);
      IndexWriter writer = new IndexWriter(rootBlog.getIndexDirectory(), analyzer, true);

      // here's where we loop through every single blog entry
      // first years, then months, then days and finally blog entries
      Collection yearlyBlogs = rootBlog.getYearlyBlogs();
      Iterator it = yearlyBlogs.iterator();
      while (it.hasNext()) {
        YearlyBlog yearly = (YearlyBlog)it.next();
        MonthlyBlog[] months = yearly.getMonthlyBlogs();
        for (int i = 0; i < months.length; i++) {
          Collection dailyBlogs = months[i].getDailyBlogs();
          Iterator kt = dailyBlogs.iterator();
          while (kt.hasNext()) {
            DailyBlog daily = (DailyBlog)kt.next();
            Collection blogEntries = daily.getEntries();
            Iterator lt = blogEntries.iterator();
            while (lt.hasNext()) {
              BlogEntry blogEntry = (BlogEntry)lt.next();
              index(blogEntry, writer);
            }
          }
        }
      }

      writer.close();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Allows a single blog entry to be (re)indexed. If the entry is already
   * indexed, this method deletes the previous index before adding the new
   * one.
   *
   * @param blogEntry   the BlogEntry instance to index
   */
  public void index(BlogEntry blogEntry) {
    try {
      SimpleBlog rootBlog = blogEntry.getRootBlog();
      Analyzer analyzer = getAnalyzer(rootBlog);

      // create an index if one doesn't already exist
      if (!IndexReader.indexExists(rootBlog.getIndexDirectory())) {
        index(rootBlog);
      }

      // first delete the blog entry from the index (if it was there)
      removeIndex(blogEntry);

      IndexWriter writer = new IndexWriter(rootBlog.getIndexDirectory(), analyzer, false);
      index(blogEntry, writer);
      writer.close();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Gets the Analyzer implementation to use.
   *
   * @return  an Analyzer instance
   * @throws Exception
   */
  private Analyzer getAnalyzer(SimpleBlog blog) throws Exception {
    Class c = Class.forName(blog.getLuceneAnalyzer());
    return (Analyzer)c.newInstance();
  }

  /**
   * Removes the index for a single blog entry to be removed.
   *
   * @param blogEntry   the BlogEntry instance to be removed
   */
  public void removeIndex(BlogEntry blogEntry) {
    try {
      SimpleBlog rootBlog = blogEntry.getRootBlog();

      // create an index if one doesn't already exist
      if (!IndexReader.indexExists(rootBlog.getIndexDirectory())) {
        index(rootBlog);
      }

      log.debug("Attempting to delete index for " + blogEntry.getTitle());
      IndexReader reader = IndexReader.open(rootBlog.getIndexDirectory());
      Term term = new Term("id", blogEntry.getId());
      log.debug("Deleted " + reader.delete(term) + " document(s) from the index");
      reader.close();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Helper method to index an individual blog entry. This assumes that the
   * root blog has already been indexed.
   *
   * @param blogEntry   the BlogEntry instance to index
   * @param writer      the IndexWriter to index with
   */
  private void index(BlogEntry blogEntry, IndexWriter writer) {
    try {
      log.debug("Indexing " + blogEntry.getTitle());
      Document document = new Document();
      document.add(Field.Keyword("id", blogEntry.getId()));
      document.add(Field.Text("title", blogEntry.getTitle()));
      document.add(Field.Keyword("permalink", blogEntry.getPermalink()));
      document.add(Field.UnIndexed("date", DateField.dateToString(blogEntry.getDate())));
      document.add(Field.UnStored("body", blogEntry.getBody()));

      Iterator it = blogEntry.getCategories().iterator();
      Category category;
      while (it.hasNext()) {
        category = (Category)it.next();
        document.add(Field.Text("category", category.getName()));
      }

      if (blogEntry.getAuthor() != null) {
        document.add(Field.Text("author", blogEntry.getAuthor()));
      }

      // build up one large string with all searchable content
      // i.e. entry title, entry body and all comment bodies
      StringBuffer searchableContent = new StringBuffer();
      searchableContent.append(blogEntry.getTitle());
      searchableContent.append(" ");
      searchableContent.append(blogEntry.getBody());
      searchableContent.append(" ");
      it = blogEntry.getComments().iterator();
      while (it.hasNext()) {
        Comment comment = (Comment)it.next();
        searchableContent.append(comment.getBody());
        searchableContent.append(" ");
      }

      // join the title and body together to make searching on them both easier
      document.add(Field.UnStored("blogEntry", searchableContent.toString()));

      writer.addDocument(document);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

}

