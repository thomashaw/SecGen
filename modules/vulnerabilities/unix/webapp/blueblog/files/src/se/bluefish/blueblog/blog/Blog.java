package se.bluefish.blueblog.blog;

import java.util.Date;

/**
 * Single weblog entry.
 * 
 * @author Robert Burén
 */
public interface Blog extends Comparable {
	
	/**
	 * The Comparable interface method compareTo should compare 
	 * the lastUpdated field (recent blogs first in a list).
	 */
	public int compareTo(Object o);
	
	/**
	 * A string that can be used as a URL anchor
	 * <i>combined with the category</i> to id this blog entry.
	 */
	public String getBlogId();

	/**
	 * Returns the Category that this weblog belongs to.
	 */	
	public Category getCategory();
	
	/**
	 * When this blog entry was created.
	 */
	public Date   getCreatedDate();
	public String getFormattedContent();
	public String getFormattedTitle();

	/**
	 * When this blog entry was last updated.
	 */
	public Date getLastUpdated();
	
	/**
	 * When this blog entry was, or is going to be, published
	 * for the first time.
	 */
	public Date getPublishDate();
	
	/**
	 * Returns the content of this weblog entry, exactly as written.
	 */
	public String getRawContent();
	
	/**
	 * Returns the title of this weblog entry, exactly as written.
	 */
	public String getRawTitle();
}
