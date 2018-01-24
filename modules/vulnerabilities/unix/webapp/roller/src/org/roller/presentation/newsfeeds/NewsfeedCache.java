package org.roller.presentation.newsfeeds;

//import net.sourceforge.flock.FlockFeedI;
//import net.sourceforge.flock.FlockResourceException;
//import net.sourceforge.flock.parser.FlockFeedFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.pojos.RollerConfig;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.RSSParser;

import java.io.IOException;
import java.net.URL;

/**
 * Returns parsed RSS feed by pulling one from a cache or by retrieving
 * and parging the specified feed using the Flock RSS parser.
 * 
 * @author Lance Lavandowska
 * @author Dave Johnson
 */
public class NewsfeedCache
{
	private static Log mLogger = 
		LogFactory.getFactory().getInstance(NewsfeedCache.class);
		
	/** Static singleton **/
	public static NewsfeedCache mInstance = null;
	
	/** Instance vars **/
	private RollerConfig mConfig = null;
	private GeneralCacheAdministrator mCacheAdmin = null;

    /** Constructor **/
    public NewsfeedCache(RollerConfig config)
    {
        mConfig = config;
        mCacheAdmin = new GeneralCacheAdministrator();
    }
    	
	/** static singleton retriever **/
	public static NewsfeedCache getInstance(RollerConfig config)
	{
		if (mInstance == null)
		{
            if (mLogger.isDebugEnabled())
            {
			    mLogger.debug("Instantiating new NewsfeedCache");
            }
            
			synchronized(NewsfeedCache.class)
			{
				mInstance = new NewsfeedCache( config );
			}
		}
		return mInstance;
	}
	
	/**
	 * Returns a Channel object for the supplied RSS newsfeed URL.
	 * @param feedUrl RSS newsfeed URL.
	 * @return FlockFeedI for specified RSS newsfeed URL.
	 */
	public ChannelIF getChannel(String feedUrl)
	{   
        ChannelIF channel = null;
        //FlockFeed feed = null;
		try
		{
            //FlockFeedFactory factory = new FlockFeedFactory();
	
            // If aggregator has been disable return null
            boolean enabled = mConfig.getEnableAggregator().booleanValue();
            if ( !enabled )
            {
                return null;
            }
            
			if ( mConfig.getRssUseCache().booleanValue() )
			{
                if (mLogger.isDebugEnabled())
                {
				    mLogger.debug("Newsfeed: use Cache for " + feedUrl);
                }
                
				boolean needsRefresh = false;
                try
				{
					// Get pre-parsed feed from the cache
                    channel = (ChannelIF)mCacheAdmin.getFromCache(
                        feedUrl, mConfig.getRssCacheTime().intValue());
                    //channel = (FlockFeedI)mCacheAdmin.getFromCache(
                        //feedUrl, mConfig.getRssCacheTime());
					
                    if (mLogger.isDebugEnabled())
                    {
                        mLogger.debug("Newsfeed: got from Cache");
                    }
				}
				catch (NeedsRefreshException nre)
				{
                    needsRefresh = true;
				}

                if (needsRefresh) 
                {
                    try
                    {
                        // Parse the feed
                        channel = RSSParser.parse(
                            new ChannelBuilder(), new URL(feedUrl));
                    }
                    catch (ParseException e1)
                    {
                        mLogger.info("Error parsing RSS: "+feedUrl);
                    }
                }
                    
                //channel = factory.createFeed(new URL(feedUrl));
                    
                // Store parsed feed in the cache
                mCacheAdmin.putInCache(feedUrl,  channel);
                mLogger.debug("Newsfeed: not in Cache");
			}
			else
			{
                if (mLogger.isDebugEnabled())
                {
				    mLogger.debug("Newsfeed: not using Cache for " + feedUrl);
                }
                try
                {
                    // Parse the feed
                    channel = RSSParser.parse(
                        new ChannelBuilder(), new URL(feedUrl));
                }
                catch (ParseException e1)
                {
                    mLogger.info("Error parsing RSS: "+feedUrl);
                }
			}
		}
		catch (IOException ioe)
		{
            if (mLogger.isDebugEnabled())
            {
			    mLogger.debug("Newsfeed: Unexpected exception",ioe);
            }
		}
//        catch (FlockResourceException e)
//        {
//            if (mLogger.isDebugEnabled())
//            {
//                mLogger.debug("Newsfeed: Flock parsing exception",e);
//            }
//        }
		return channel;
	}
}
