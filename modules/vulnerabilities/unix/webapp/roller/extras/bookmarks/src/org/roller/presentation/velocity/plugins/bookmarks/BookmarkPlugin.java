/*
 * Created on Nov 2, 2003
 *
 */
package org.roller.presentation.velocity.plugins.bookmarks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.model.BookmarkManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author lance
 *
 */
public class BookmarkPlugin implements PagePlugin
{
    public String name = "Bookmark Linker";
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(BookmarkPlugin.class);
       
    BookmarkManager bMgr = null;
    
    public BookmarkPlugin()
    {
        mLogger.debug("BookmarkPlugin instantiated.");
    }
    
    public String toString() { return name; }

	/* (non-Javadoc)
	 * @see org.roller.presentation.velocity.PagePlugin#init(org.roller.presentation.RollerRequest, org.apache.velocity.context.Context)
	 */
	public void init(RollerRequest rreq, Context ctx)
	{  
        if (rreq != null)
        {    
            try
    		{
    			bMgr = rreq.getRoller().getBookmarkManager();
                
    		}
    		catch (RollerException e)
    		{
    			mLogger.error("Unable to reach BookmarkManager", e);
    		}
        }
	}

	/* 
     * This method cannot do it's intended job (since it cannot
     * read the current Entry) so it is to do no work!
     * 
     * (non-Javadoc)
	 * @see org.roller.presentation.velocity.PagePlugin#render(java.lang.String)
	 */
	public String render(String str)
	{
		return str;
	}
    
    public String render(WeblogEntryData entry, boolean skipFlag)
    {
        String text = entry.getText();
        try
		{
            FolderData root = bMgr.getRootFolder(entry.getWebsite());
            text = matchBookmarks(text, root);
			text = lookInFolders(text, root.getFolders());
		}
		catch (RollerException e)
		{
            // nothing much I can do, go with default "Weblog" value
            // could be RollerException or NullPointerException
		}
        return text;
    }

    /**
     * Recursively travel down Folder tree, attempting
     * to match up Bookmarks in each Folder.
     * 
     * @param text
     * @param folders
     * @return
     */
    private String lookInFolders(String text, Collection folders)
    {
        Iterator it = folders.iterator();
        while (it.hasNext())
        {
            FolderData folder = (FolderData)it.next();
            text = matchBookmarks(text, folder);
            
            try
            {
                if (!folder.getFolders().isEmpty())
                {
                    lookInFolders(text, folder.getFolders());
                }
            }
            catch (RollerException e)
            {
                mLogger.error("Error getting child Folders");
            }
        }
        return text;
    }

    private String matchBookmarks(String text, FolderData folder)
    {
        BookmarkData bookmark;
        String regEx;
        String link;
        Iterator bookmarks;
        bookmarks = folder.getBookmarks().iterator();
        while (bookmarks.hasNext())
        {
            bookmark = (BookmarkData)bookmarks.next();
            regEx = "\\b" + bookmark.getName() + "\\b";
            link = "<a href=\"" + bookmark.getUrl() + "\">" +
            bookmark.getName() + "</a>";
            text = text.replaceAll(regEx, link);
        }
        return text;
    }


}
