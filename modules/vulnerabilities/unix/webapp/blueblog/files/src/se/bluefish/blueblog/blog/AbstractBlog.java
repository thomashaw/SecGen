/*
 * Created on 2003-jun-09
 *
 */
package se.bluefish.blueblog.blog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class with some default implementations for the Blog interface.
 * 
 * @author Robert Burén
 */
public abstract class AbstractBlog implements Blog {
	private static final String DATEFORMAT="yyyy-MM-dd HH:mm:ss";

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		// TODO This can probably be made more efficient. I just don't get 
		// why the commented out version doesn't work.
		Blog other = (Blog)o;
		DateFormat df=new SimpleDateFormat(DATEFORMAT);
		int compValue =-1*df.format(getLastUpdated()).compareTo(df.format(other.getLastUpdated()));
		return compValue != 0 ? compValue : getBlogId().compareTo(other.getBlogId());
		
//		long diff = other.getLastUpdated().getTime() - this.getLastUpdated().getTime();
//		return diff != 0 ? (int)diff : getPermalinkAnchor().compareTo(other.getPermalinkAnchor());
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Blog#getPermalinkAnchor()
	 */
	public abstract String getBlogId();

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Blog#getCategory()
	 */
	public abstract Category getCategory();

	/**
	 * Default: returns lastUpdated.
	 * 
	 * @see #getLastUpdated()
	 * @see se.bluefish.blueblog.blog.Blog#getCreatedDate()
	 */
	public Date getCreatedDate() {
		return getLastUpdated();
	}

	/**
	 * Default: returns rawContent.
	 * 
	 * @see #getRawContent()
	 * @see se.bluefish.blueblog.blog.Blog#getFormattedContent()
	 */
	public String getFormattedContent() {
		return getRawContent();
	}

	/** 
	 * Default: returns rawTitle.
	 * 
	 * @see se.bluefish.blueblog.blog.Blog#getFormattedTitle()
	 * @see #getRawTitle()
	 */
	public String getFormattedTitle() {
		return getRawTitle();
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Blog#getLastUpdated()
	 */
	public abstract Date getLastUpdated();

	/**
	 * Default: returns lastUpdated.
	 * 
	 * @see #getLastUpdated()
	 * @see se.bluefish.blueblog.blog.Blog#getPublishDate()
	 */
	public Date getPublishDate() {
		return getLastUpdated();
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Blog#getRawContent()
	 */
	public abstract String getRawContent();

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Blog#getRawTitle()
	 */
	public abstract String getRawTitle();

}
