package org.roller.presentation.velocity;

import org.apache.commons.cache.Cache;
import org.apache.commons.cache.CacheSingleton;
import org.apache.commons.cache.GroupMapImpl;
import org.apache.commons.cache.LRUEvictionPolicy;
import org.apache.commons.cache.MemoryStash;
import org.apache.commons.cache.SimpleCache;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This is a simple template file loader that loads 
 * "preview" templates
 * from a HashMap instance instead of plain files.
 *
 * Ideally this would use some smarter caching
 * (re-implement with Commons-Cache?) so that the
 * templates put here could expire in case the user
 * forgot/neglected to clear their "work area".
 *
 * There is no configuration.
 *
 * @author <a href="mailto:lance@brainopolis.com">Lance Lavandowska</a>
 * @version $Id: PreviewResourceLoader.java,v 1.15 2003/10/16 16:16:44 mraible Exp $
*/
public class PreviewResourceLoader extends ResourceLoader
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(PreviewResourceLoader.class);

	private static String cacheName = "PreviewCache";
	
	/**
	 * number of objects to store in the cache, older objects
	 * are pushed out (acts like LRU) if maxObjects reached.
	 * @TODO Add configuration for this maxObject in preview cache
	 */
	private static int  maxObjects = 500;

	/**
	 * time objects are in cache before they expire - default one hour.
	 * Configuration parameter is in seconds which we convert
	 * to milliseconds.
	 * @TODO Add configuration for preview cache timeout
	 */
	private static int  time = 60 * 60 * 1000;
	
    public void init( ExtendedProperties configuration)
    {
    }

    public boolean isSourceModified(Resource resource)
    {
        return true;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     *  @param name name of template
     *  @return InputStream containing template
    */
    public InputStream getResourceStream( String name )
    throws ResourceNotFoundException
    {
        if (name == null || name.length() == 0)
        {
            throw new ResourceNotFoundException(
                "Need to specify a template name!");
        }

        if (mLogger.isDebugEnabled())
        {
		    mLogger.debug("PreviewResourceLoader.getResourceStream(" + name + ")");
        }
        
        try
        {
			String template = PreviewResourceLoader.getTemplate(name);
			if (template != null && mLogger.isDebugEnabled())
			{
                mLogger.debug("PreviewResourceLoader found resource " + name);
		    }
            return new ByteArrayInputStream( template.getBytes() );
        }
        catch (NullPointerException npe)
        {
        	// to be expected if resource is not in cache
        	throw new ResourceNotFoundException("Resource not found in PreviewResourceLoader");
        }
        catch (Exception e)
        {
            String msg = "PreviewResourceLoader Error: " +
                "problem trying to load resource " + name + ": " + e.getMessage();
            if (mLogger.isDebugEnabled()) 
            {
                mLogger.debug( msg);
            }
            Runtime.error(msg );
            throw new ResourceNotFoundException (msg);
        }
    }
		
	/**
	 * Get the cache for the PreviewServlet
	 */ 
	public static Cache getCache()
	{
		synchronized(cacheName)
		{
			if(CacheSingleton.hasCache(cacheName))
			{
				return CacheSingleton.getCache(cacheName);
			}
		}

		SimpleCache cache = new SimpleCache(
				new MemoryStash( maxObjects ), 
				new LRUEvictionPolicy(),
				null,
				new GroupMapImpl() );
		if(null != cache)
		{
			CacheSingleton.putCache(cacheName, cache);
			return cache;
		}

		return null;
	}
	 
    /**
     * Set the temporary Template into memory.
     */
    public static void setTemplate(String id, String template, String username)
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("PreviewResourceLoader.setTemplate(" 
    			+ id + ", template)");
        
        }
		Cache cache = PreviewResourceLoader.getCache();
		cache.store(id,
				template,
				new Long(System.currentTimeMillis() + 
					PreviewResourceLoader.time),
				null,
				username); // this allows us to clear the
				// whole bunch for this user at once.
    }
    
    /**
     * Get the temporary Template from the Map.
     */    
    public static String getTemplate(String id)
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("PreviewResourceLoader.getTemplate(" 
    			+ id + ")");
        }        
		Cache cache = PreviewResourceLoader.getCache();
    	return (String) cache.retrieve( id );
    }
    
    /**
     * Remove the temporary Template from the Map.
     */    
    public static void clearTemplate(String id)
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("PreviewResourceLoader.clearTemplate(" 
			    + id + ")");
        }
		Cache cache = PreviewResourceLoader.getCache();
		cache.clear( id );
    }
    
    /**
     * Clear all templates for this user.
     * 
     * @param username
     */
    public static void clearAllTemplates(String username)
    {
		PreviewResourceLoader.getCache().clearGroup( username );
    }
}
