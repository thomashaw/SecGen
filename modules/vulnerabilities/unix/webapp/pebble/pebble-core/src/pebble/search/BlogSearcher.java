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

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.Analyzer;
import pebble.blog.Blog;
import pebble.blog.SimpleBlog;
import pebble.blog.CompositeBlog;
import pebble.PebbleProperties;

/**
 * Provides a convenient wrapper around Lucene to perform searches on
 * blog entries.
 */
public class BlogSearcher {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(BlogSearcher.class);

  public SearchResults search(SimpleBlog blog, String queryString) throws SearchException {

    log.debug("Performing search : " + queryString);

    SearchResults searchResults = new SearchResults();
    searchResults.setQuery(queryString);

    if (queryString != null && queryString.length() > 0) {
      Searcher searcher = null;

      try {
        searcher = new IndexSearcher(blog.getIndexDirectory());
        Query query = QueryParser.parse(queryString, "blogEntry", getAnalyzer(blog));
        Hits hits = searcher.search(query);

        for (int i = 0; i < hits.length(); i++) {
          Document doc = hits.doc(i);
          SearchHit result = new SearchHit(
              doc.get("id"),
              doc.get("permalink"),
              doc.get("title"),
              doc.get("excerpt"),
              DateField.stringToDate(doc.get("date")),
              hits.score(i));
          searchResults.add(result);
        }
      } catch (ParseException pe) {
        pe.printStackTrace();
        searchResults.setMessage("Sorry, but there was an error. Please try another search");
      } catch (Exception e) {
        e.printStackTrace();
        throw new SearchException(e.getMessage());
      } finally {
        if (searcher != null) {
          try {
            searcher.close();
          } catch (IOException e) {
            // can't do much now! ;-)
          }
        }
      }
    }

    return searchResults;
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

  public SearchResults search(CompositeBlog blog, String queryString) throws SearchException {

    SearchResults searchResults = new SearchResults();
    searchResults.setQuery(queryString);

    // only search over public blogs
    Iterator it = blog.getPublicBlogs().iterator();
    while (it.hasNext()) {
      SimpleBlog simpleBlog = (SimpleBlog)it.next();
      searchResults.add(search(simpleBlog, queryString));
    }

    return searchResults;
  }

  public SearchResults search(Blog blog, String queryString) throws SearchException {
    if (blog instanceof SimpleBlog) {
      return search((SimpleBlog)blog, queryString);
    } else {
      return search((CompositeBlog)blog, queryString);
    }
  }

}