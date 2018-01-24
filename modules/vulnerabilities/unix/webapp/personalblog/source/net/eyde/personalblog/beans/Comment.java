package net.eyde.personalblog.beans;

/*
 * Created on May 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.util.Date;


/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Comment {
    private String id; // the Hibernate identifier
    private String name;
    private String email;
    private String url;
    private String content;
    private String emoticon;
    private Date created;

    //private String post_id;
    private Post post;
    private int version; // the Hibernate version number

    public Comment() {
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
    public String getEmail() {
        return email;
    }

    /**
     * @return
     */
    public String getEmoticon() {
        return emoticon;
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
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public String getUrl() {
        return url;
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
    public void setEmail(String string) {
        email = string;
    }

    /**
     * @param string
     */
    public void setEmoticon(String string) {
        emoticon = string;
    }

    /**
     * @param string
     */
    public void setId(String string) {
        id = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @param string
     */
    public void setUrl(String string) {
        url = string;
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

    //	public String getPost_id() {
    //		return post_id;
    //	}

    /**
     * @param string
     */

    //	public void setPost_id(String string) {
    //		post_id = string;
    //	}

    /**
     * @return
     */
    public Post getPost() {
        return post;
    }

    /**
     * @param post
     */
    public void setPost(Post post) {
        this.post = post;
    }
}
