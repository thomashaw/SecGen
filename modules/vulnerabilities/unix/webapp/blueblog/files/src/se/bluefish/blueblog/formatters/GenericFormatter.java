/*
 * Created on 2003-jun-09
 *
 */
package se.bluefish.blueblog.formatters;

/**
 * Default implementation of some methods from the Formatter 
 * interface. The default behaviour of this class is to pass
 * input text through unformatted.
 * 
 * @author Robert Burén
 */
public class GenericFormatter implements Formatter {
	protected String name;
	
	public GenericFormatter() {
		name = this.getClass().getName();
	}

	public GenericFormatter(String name) {
		this.name = name;
	}

	/**
	 * Default: calls formatText() to do the formatting.
	 * 
	 * @see #formatText(String)
	 * @see se.bluefish.blueblog.formatters.Formatter#formatBlogContent(java.lang.String)
	 */
	public String formatBlogContent(String content) {
		return formatText(content);
	}

	/**
	 * Default: calls formatText() to do the formatting.
	 * 
	 * @see #formatText(String)
	 * @see se.bluefish.blueblog.formatters.Formatter#formatBlogTitle(java.lang.String)
	 */
	public String formatBlogTitle(String title) {
		return formatText(title);
	}
	
	/**
	 * Default: returns the text parameter unchanged.
	 *  
	 * @see se.bluefish.blueblog.formatters.Formatter#formatText(java.lang.String)
	 */
	public String formatText(String text) {
		return text;
	}
	
	/**
	 * Default: returns class name.
	 * 
	 * @see se.bluefish.blueblog.formatters.Formatter#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.formatters.Formatter#setName(java.lang.String)
	 */
	public void setName(String n) {
		name = n;
	}
}
