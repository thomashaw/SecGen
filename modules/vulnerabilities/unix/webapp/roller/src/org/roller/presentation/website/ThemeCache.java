package org.roller.presentation.website;

import org.apache.commons.cache.Cache;
import org.apache.commons.cache.CacheSingleton;
import org.apache.commons.cache.LRUEvictionPolicy;
import org.apache.commons.cache.MemoryStash;
import org.apache.commons.cache.SimpleCache;
/**
 * Caches the Theme files to avoid repeated reading of the files from the
 * harddrive.
 * 
 * @author llavandowska
 */
public class ThemeCache
{
	private static ThemeCache INSTANCE = new ThemeCache();
	private static String cacheName = "ThemeFiles";
	private static Cache cache;

	/**
	 * How many objects to store in cache.
	 * @TODO Add configuration for maxObjects in theme cache
	 */
	private static int  maxObjects = 500;
	
	static {
		if(CacheSingleton.hasCache(cacheName)) 
		{
			cache = CacheSingleton.getCache(cacheName);
		}
		else 
		{
			cache = new SimpleCache( 
					new MemoryStash( maxObjects ), new LRUEvictionPolicy() );
			if(null != cache) 
			{
				CacheSingleton.putCache(cacheName, cache);
			}	
		}
	}
	
	/**
	 * How long until an object in cache expires.
	 * @TODO Add configuration for theme cache timeout
	 */
	private long expireInterval = 1000l*60*60*24; // 1 second * 1 min * 1 hr * 24 hours
	
	/**
	 * Should the PreviewResourceLoader cache the Template files.
	 * @TODO Add configuration for enabling theme template caching
	 */
	private static boolean cacheTemplateFiles = false;
		
	/** Private constructor to prevent outside instantiation **/
	private ThemeCache() { }

	/**
	 * Get Cache from Commons CacheSingleton.
	 * If one happens to not exist yet, make one.
	 */
	private Cache getCache()
	{
		return CacheSingleton.getCache(cacheName);
	}
		
	/**
	 * 
	 */
	public static ThemeCache getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * 
	 */
	public String putIntoCache(String themeName, String fileName, String template)
	{
		if (cacheTemplateFiles)
		{
			Cache cache = this.getCache();
	
			cache.store(themeName+":"+fileName, template,
				new Long(System.currentTimeMillis() + expireInterval), 
				null);
		}
		return template;

	}
	
	/**
	 * Null will be returned if there is a problem or if caching is "turned
	 * off".
	 */
	public String getFromCache(String themeName, String fileName)
	{
		if (!cacheTemplateFiles) return null;
		
		Cache cache = this.getCache();
		if (cache == null) return null;
		
		return (String) cache.retrieve(themeName+":"+fileName);
	}
	
	/**
	 * 
	 */
	public void removeFromCache(String themeName, String fileName)
	{
		if (!cacheTemplateFiles) return;
		
		Cache cache = this.getCache();
		cache.clear( themeName+":"+fileName );
	}

    
    /**
     * The list of files in a Theme is cached as a String[], the key being the
     * Theme location itself.
     * 
     * @param themeDir
     * @param fileNames
     * @return String[]
     */
    public String[] setFileList(String themeDir, String[] fileNames)
    {
		if (cacheTemplateFiles)
		{
        	Cache cache = this.getCache();

        	cache.store(themeDir, fileNames,
                new Long(System.currentTimeMillis() + expireInterval), 
                null);
		}
        return fileNames;

    }
    
    /**
     * The list of files in a Theme is cached as a String[], the key being the
     * Theme location itself.  If caching is turned off this will return null.
     * 
     * @param theme
     * @return String[]
     */
    public String[] getFileList(String themeDir)
    {
		if (!cacheTemplateFiles) return null;
		
        Cache cache = this.getCache();
        if (cache == null) return null;
        
        return (String[])cache.retrieve(themeDir);
    }
}
