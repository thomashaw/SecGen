/*
 * Created on 2003-jun-09
 *
 */
package se.bluefish.blueblog.formatters;

/**
 * Transforms file linebreaks to html linebreaks.
 * 
 * @author Robert Burén
 */
public class SimpleText extends GenericFormatter {

	/**
	 * Removes all linebreaks. Defers the rest of the 
	 * formatting to formatText(). 
	 * 
	 * @see #formatText(String)
	 * @see se.bluefish.blueblog.formatters.GenericFormatter#formatBlogTitle(java.lang.String)
	 */
	public String formatBlogTitle(String title) {
		return formatText( title.replaceAll("\\n", "").replaceAll("\\r", "") );
	}

	/**
	 * Adds html linebreak tag (&lt;br/>) before each 
	 * linebreak in the string.
	 * 
	 * @see se.bluefish.blueblog.formatters.Formatter#formatText(java.lang.String)
	 */
	public String formatText(String text) {
		String formatted = text.replaceAll("\\n", "<br/>\n").replaceAll("\\r", "");
		return formatted;
	}

}
