/*
 * Created on 2003-jul-08
 *
 */
package se.bluefish.blueblog.formatters;

import java.util.Iterator;
import java.util.List;

/**
 * Maintains a List of other formatters, and calls their format*
 * methods in a sequence.
 * 
 * @author Robert Burén
 *
 */
public class ChainFormatter extends GenericFormatter {
	private static final int CONTENT = 2;
	private static final int TEXT    = 1;
	private static final int TITLE   = 3;
	private List formatters;
	
	/**
	 * The constructor takes the List of other Formatters as
	 * a parameter.
	 */
	public ChainFormatter(List formatters) {
		this.formatters = formatters;
	}

	private String doChain(String text, int method) {
		if( formatters == null ) {
			return text;
		}
		Iterator iter = formatters.iterator();
		while( iter.hasNext() ) {
			Formatter f = (Formatter)iter.next();
			switch( method ) {
				case TEXT:
					text = f.formatText(text);
					break;
				case CONTENT:
					text = f.formatBlogContent(text);
					break;
				case TITLE:
					text = f.formatBlogTitle(text);
					break;
			}
		}
		return text;
	}

	/**
	 * Calls the same method on each Formatter in the internal
	 * List in a sequence, returning the output from the last one.
	 */
	public String formatBlogContent(String content) {
		return doChain(content, CONTENT);
	}

	/**
	 * Calls the same method on each Formatter in the internal
	 * List in a sequence, returning the output from the last one.
	 */
	public String formatBlogTitle(String title) {
		return doChain(title, TITLE);
	}

	/**
	 * Calls the same method on each Formatter in the internal
	 * List in a sequence, returning the output from the last one.
	 */
	public String formatText(String text) {
		return doChain(text, TEXT);
	}
}
