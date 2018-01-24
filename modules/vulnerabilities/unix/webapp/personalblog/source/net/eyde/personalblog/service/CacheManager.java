//---------------------------------------------------------
// Application: Personal Blog
// Author     : Matthew Payne
// File       : CacheManager.java
// Date       : July 10, 2003
//---------------------------------------------------------

package net.eyde.personalblog.service;

import net.eyde.personalblog.service.CachedObject;

import java.util.*;

class CostComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        return ((Double) o1).compareTo((Double) o2);
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }
}

public class CacheManager {
    public final static int LRU = 0;
    public final static int LFU = 1;
    public final static int MIX = 2;

    private HashMap cacheHashMap = new HashMap();
    private int CACHE_CAPACITY = 100;
    private int TRESHOLD = 90;
    private int purgeAlgorithm;

    private long hitCount = 0;
    private long missCount = 0;

    public CacheManager(int purgeAlgorithm, int cacheCapacity, int treshold) {
        this.purgeAlgorithm = purgeAlgorithm;
        CACHE_CAPACITY = cacheCapacity;
        TRESHOLD = treshold;
    }

    public CacheManager(int purgeAlgorithm) {
        this.purgeAlgorithm = purgeAlgorithm;
    }

    public synchronized Object getCache(String identifier) {
        CachedObject cachedObject = (CachedObject) cacheHashMap.get(identifier);
        Object obj = null;

        if (cachedObject == null) {
            missCount++;
        } else if (cachedObject.isExpired()) {
            cacheHashMap.remove(identifier);
            missCount++;
        } else {
            cachedObject.incNumAccess();
            cachedObject.setLastAccessTime(new Date());
            hitCount++;
            obj = cachedObject.getObject();
        }
        return obj;
    }

    public synchronized void invalidate(String identifier) {
        cacheHashMap.remove(identifier);
    }

    public long getHitCount() {
        return hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public long getCurrentCacheSize() {
        return cacheHashMap.size();
    }

    public synchronized void putCache(Object object, String id, int minutesToLive) {
        CachedObject cachedObject = new CachedObject(object, id, minutesToLive);
        if (cacheHashMap.size() == CACHE_CAPACITY) {
            sweep();
        }
        cacheHashMap.put(id, cachedObject);
    }

    public synchronized void sweep() {
        TreeMap costTreeMap = new TreeMap(new CostComparator());
        for (Iterator i = cacheHashMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            CachedObject cachedObject = (CachedObject) entry.getValue();
            if (cachedObject.isExpired()) {
                cacheHashMap.remove(entry.getKey());
            } else {
                double cost = 0.0;
                switch (purgeAlgorithm) {
                    case LFU:
                        cost = cachedObject.getLFUCost();
                        break;
                    case LRU:
                        cost = cachedObject.getLRUCost();
                        break;
                    default:
                        cost = cachedObject.getMixCost();
                }
                costTreeMap.put(new Double(cost), entry.getKey());
            }
        }

        // delete to treshold
        for (int i = cacheHashMap.size(); i > TRESHOLD; i--) {
            Object kk = costTreeMap.firstKey();
            Object k = costTreeMap.get(kk);
            cacheHashMap.remove(k);
            costTreeMap.remove(kk);
        }
    }

    public void clearCache() {
        hitCount = 0;
        missCount = 0;
        cacheHashMap.clear();
    }

    public static String createKey(String[] keys) {
        StringBuffer newKey = new StringBuffer("");
        for (int i = 0; i < keys.length; i++)
            newKey.append(keys[i]).append("/");
        return newKey.toString();
    }
}

