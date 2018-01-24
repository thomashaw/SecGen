/*
 * Created on 2003-jul-11
 *
 */
package se.bluefish.blueblog.blog;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * Data class to hold data needed to create a 
 * new Blog instance.
 * 
 * @author Robert Burén
 */
public class BlogData {

	private static String constructSuggestedId(String str) {
		final int MAX_SUGGESTED_ID = 30;
		final String underscore = " \t";
		final String remove = "\"'^~{[]}@£$!?#¤%&/<>()´`*§|";
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < str.length() && sb.length() < MAX_SUGGESTED_ID; ++i ) {
			char c = str.charAt(i);
			if( underscore.indexOf(c) >= 0 ) {
				sb.append('_');
			} else if( remove.indexOf(c) >= 0 ) {
				//ignore these
			} else if( Character.isLetterOrDigit( c ) ) {
				sb.append( c );
			} else {
				// Hmm... what to do with the rest? For now, an underscore will do, again
				sb.append('_');
			}
		}
		return StringUtils.strip(sb.toString(), "_");
	}
	
	private String blogType = "text/plain";
	private String content;
	private Date created = new Date();
	private Date publish = new Date();
	private String suggestedId;
	private String title;

	public BlogData() {}
	
	public BlogData(String title, String content) {
		this.title = title;
		this.content = content;
		this.suggestedId = constructSuggestedId(title);
	}
	
	/**
	 * Mime type describing the text in the blog, for formatting purposes.
	 */
	public String getBlogType() {
		return blogType;
	}

	/**
	 * Blog contents.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Created date -- default value is when this 
	 * BlogData instance was created.
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Publish date -- default value is when this 
	 * BlogData instance was created.
	 */
	public Date getPublish() {
		return publish;
	}

	/**
	 * <i>Suggested</i> blogId for this Blog. The Category may choose
	 * to use this as a blogId if no such blogId already exist in the
	 * Category, otherwise, the Category will produce another, unique, blogId.
	 */
	public String getSuggestedId() {
		return suggestedId;
	}

	/**
	 * Title for this blog.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param string
	 */
	public void setBlogType(String string) {
		blogType = string;
	}

	/**
	 * @param string
	 */
	public void setContent(String string) {
		content = string;
	}

	/**
	 * @param date
	 */
	public void setCreated(Date date) {
		created = date;
	}

	/**
	 * @param date
	 */
	public void setPublish(Date date) {
		publish = date;
	}

	/**
	 * <i>Suggested</i> blogId for this Blog. The Category may choose
	 * to use this as a blogId if no such blogId already exist in the
	 * Category, otherwise, the Category will produce another, unique, blogId.
	 */
	public void setSuggestedId(String string) {
		suggestedId = string;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}
}
