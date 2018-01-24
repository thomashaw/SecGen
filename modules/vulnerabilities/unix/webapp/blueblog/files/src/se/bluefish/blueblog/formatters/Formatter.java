/*
 * Created on 2003-jun-09
 *
 */
package se.bluefish.blueblog.formatters;

/**
 * Base interface for Formatters, ie tools that transforms 
 * user-written text to screen-formatted html.
 * 
 * @author Robert Burén
 */
public interface Formatter {
	
	/**
	 * Formats the Blog content from raw text, as written by user,
	 * to formatted text (most likely html).
	 */
	public String formatBlogContent(String content);
	
	/**
	 * Formats the Blog title from raw text, as written by user,
	 * to formatted text (most likely html).
	 */
	public String formatBlogTitle(String title);
	
	/**
	 * The most generic text-formatting. For simple formatting,
	 * just implement this method, and let other methods call this one.
	 * 
	 * @param text to be formatted, as written by user
	 * @return formatted text (most likely in html)
	 */
	public String formatText(String text);
	/**
	 * A user-friendly display name of this Formatter instance.
	 */
	public String getName();
	public void setName(String n);
}
