/*
 * Created on 2003-maj-05
 *
 */
package se.bluefish.blueblog.blog;

import java.io.Serializable;
import java.net.URI;

/**
 * A link, for the link list (bookmarks) for this weblog.
 * 
 * @author Robert Burén
 *
 */
public class Link implements Serializable, Comparable {
	private String name;
	private URI uri;
	
	public Link(String name, URI uri) {
		this.name=name;
		this.uri=uri;
	}
	
	/**
	 * Natural order: compare the name property.
	 */
	public int compareTo(Object o) {
		return name.compareTo(((Link)o).name);
	}

	/**
	 * Display name of the Link.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The actual URI (URL) of the link.
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * String representation is html link, ie
	 * &lt;a href="<i>uri</i>"><i>name</i>&lt;/a>
	 */
	public String toString() {
		return "<a href=\""+uri+"\">"+name+"</a>";
	}
}
