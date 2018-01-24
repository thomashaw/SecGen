/*
 * Created on May 19, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.beans;

import java.util.Date;


/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Referrer {
    private String id; // the Hibernate identifier
    private Date date;
    private String referrer;
    private int counter;
    private Date created;
    private int version; // the Hibernate version number

    /**
     * @return
     */
    public int getCounter() {
        return counter;
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
    public Date getDate() {
        return date;
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
    public String getReferrer() {
        return referrer;
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
    public void setCounter(int i) {
        counter = i;
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
    public void setDate(Date date) {
        this.date = date;
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
    public void setReferrer(String string) {
        referrer = string;
    }

    /**
     * @param i
     */
    public void setVersion(int i) {
        version = i;
    }
}
