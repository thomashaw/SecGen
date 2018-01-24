package org.snipsnap.snip;

/**
 * Wraps the SnipTokenizer into an analyzer
 *
 * @author stephan
 * @version $Id: SnipAnalyzer.java,v 1.4 2003/01/09 09:49:11 stephan Exp $
 **/

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;

public final class SnipAnalyzer extends Analyzer {

  public final TokenStream tokenStream(String field, Reader reader) {
    return new SnipTokenizer(field, reader);
  }
}
