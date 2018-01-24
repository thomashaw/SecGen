/*
 * Created on 2003-jul-08
 *
 */
package se.bluefish.blueblog.formatters;

/**
 * Sample formatter: exchanges the word "<tt>Bluefish</tt>" with the text
 * <tt>&lt;a href="http://bluefish.se">Bluefish&lt;/a></tt>
 * 
 * @author Robert Burén
 */
public class BluefishLinker extends GenericFormatter {
	public String formatBlogContent(String content) {
		return content.replaceAll("Bluefish", "<a href=\"http://bluefish.se\">Bluefish</a>");
	}
}
