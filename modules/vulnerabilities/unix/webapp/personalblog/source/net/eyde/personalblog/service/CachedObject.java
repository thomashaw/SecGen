//---------------------------------------------------------
// Application: DVD Lib
// Author     : Matthew Payne
// File       : CachedObject.java
//
// Copyright 2003 Telerx Inc
// Generated at Wed Mar 26 12:08:34 EST 2003
// using Karapan Sapi Struts Generator
// Visit http://www.javanovic.com
//---------------------------------------------------------

package net.eyde.personalblog.service;

import java.util.*;

public class CachedObject {

    public Object object = null;
    private Date dateofExpiration = null;
    private String identifier = null;
    private Date lastAccessTime = new Date();
    private long numAccess = 1;
    private int size;

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public CachedObject(Object obj, String id, int minutesToLive) {
        this.object = obj;
        this.identifier = id;

        size = objectSize(obj);
        lastAccessTime = new Date();
        // minutesToLive of 0 means it lives on indefinitely.
        if (minutesToLive != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastAccessTime);
            cal.add(Calendar.MINUTE, minutesToLive);
            dateofExpiration = cal.getTime();
        }
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public boolean isExpired() {
        // Remember if the minutes to live is zero then it lives forever!
        if (dateofExpiration != null && dateofExpiration.before(new Date())) {
            return true;
        }
        return false;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object getObject() {
        return object;
    }

    public Date getDateofExpiration() {
        return (this.dateofExpiration);
    }

    public Date getLastAccessTime() {
        return (this.lastAccessTime);
    }

    public long getNumAccess() {
        return (this.numAccess);
    }

    public long getSize() {
        return (this.size);
    }

    public double getMixCost() {
        long milis = new Date().getTime() - lastAccessTime.getTime();
        if (milis == 0) {
            milis = 1;
        }
        return (double) numAccess / (double) milis / (double) size;
    }

    public double getLRUCost() {
        long milis = new Date().getTime() - lastAccessTime.getTime();
        if (milis == 0) {
            milis = 1;
        }
        return 1.0 / (double) milis;
    }

    public double getLFUCost() {
        return (double) numAccess;
    }

    public void incNumAccess() {
        numAccess++;
    }

    public boolean equals(Object o2) {
        try {
            String key2 = ((CachedObject) o2).getIdentifier();
            return identifier.equals(key2);
        } catch (Exception e) {
            return false;
        }
    }

    private static int objectSize(Object o) {
        try {
            int size = ((List) o).size();
            return size + 1;
        } catch (Exception e) {
            return 1;
        }
    }

}
