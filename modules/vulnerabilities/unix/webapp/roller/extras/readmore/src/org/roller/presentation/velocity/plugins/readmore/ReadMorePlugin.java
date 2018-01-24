/*
 * Created on Nov 2, 2003
 *
 */
package org.roller.presentation.velocity.plugins.readmore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;
import org.roller.util.Utilities;

/**
 * @author lance
 *
 */
public class ReadMorePlugin implements PagePlugin
{
    public String name = "Read More Summary";
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(ReadMorePlugin.class);
       
    String ctxPath = "";
    UserManager uMgr = null;
    
    public ReadMorePlugin()
    {
        mLogger.debug("ReadMorePlugin instantiated.");
    }
    
    public String toString() { return name; }

	/* (non-Javadoc)
	 * @see org.roller.presentation.velocity.PagePlugin#init(org.roller.presentation.RollerRequest, org.apache.velocity.context.Context)
	 */
	public void init(RollerRequest rreq, Context ctx)
	{
        if (rreq != null)
        {    
    		ctxPath = rreq.getRequest().getContextPath();        
            try
    		{
    			uMgr = rreq.getRoller().getUserManager();
    		}
    		catch (RollerException e)
    		{
    			mLogger.error("Unable to reach UserManager", e);
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
        if (skipFlag) 
            return entry.getText();
        
        String result = Utilities.truncateNicely(entry.getText(), 240, 260, "... ");
        
        // if the result is shorter, we need to add "Read More" link
        if (result.length() < entry.getText().length())
        {
            String pageLink = "Weblog";
            if (uMgr != null)
            {
                try
				{
					pageLink = uMgr.retrievePage(
                        entry.getWebsite().getDefaultPageId()
                            ).getLink();
				}
				catch (Exception e)
				{
					// nothing much I can do, go with default "Weblog" value
                    // could be RollerException or NullPointerException
				}
            }
            
            if (result.endsWith("</p>")) result += "<p>";
            
            String link = "<a href=\"" + ctxPath + "/comments/" + 
                entry.getWebsite().getUser().getUserName() + 
                "/" + pageLink + "/" + entry.getAnchor() + 
                "\">Read More</a></p>";
            
            result += link;
        }
        return result;
    }

}
