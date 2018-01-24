package net.eyde.personalblog.beans;

/*
 * Created on May 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Post {
	private String id; // the Hibernate identifier
	private String reference;
	private String title;
	private String category;
	private String content;
	private Date created;
	private Date modified;
	private Set comments;
	private Boolean emailComments;
    
	private int version; // the Hibernate version number

	/*
	private Post(){ 
	}
	*/
	public Post() {
		comments = new HashSet();
	}

	public Post(String title) {
		this.title = title;
		comments = new HashSet();
	}

	/**
	 * @return
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @return
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public Date getModified() {
		return modified;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param string
	 */
	public void setCategory(String string) {
		category = string;
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
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	/**
	 * @param date
	 */
	public void setModified(Date date) {
		modified = date;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}

	/**
	 * @return
	 */
	public Set getComments() {
		return comments;
	}

	/**
	 * @param set
	 */
	public void setComments(Set comments) {
		this.comments = comments;
	}

	public void addComment(Comment c) {
		comments.add(c);
	}

	public void removeComment(Comment c) {
		comments.remove(c);
	}

	/**
	 * @return
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param i
	 */
	public void setVersion(int i) {
		version = i;
	}
 
	/**
	 * @return
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @param string
	 */
	public void setReference(String string) {
		reference = string;
	}

	/**
	 * @return
	 */
	public Boolean getEmailComments() {
		return emailComments;
	}

	/**
	 * @param boolean1
	 */
	public void setEmailComments(Boolean boolean1) {
		emailComments = boolean1;
	}

}
