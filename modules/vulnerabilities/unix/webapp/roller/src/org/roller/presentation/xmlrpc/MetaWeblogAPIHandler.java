
package org.roller.presentation.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.roller.model.Roller;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.util.Utilities;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;


/**
 * Roller XML-RPC Handler for the MetaWeblog API.
 *
 * MetaWeblog API spec can be found at http://www.xmlrpc.com/metaWeblogApi
 *
 * @author David M Johnson
 */
public class MetaWeblogAPIHandler extends BloggerAPIHandler
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MetaWeblogAPIHandler.class);

    public MetaWeblogAPIHandler()
    {
        super();
    }

    //------------------------------------------------------------------------

    /**
     * Authenticates a user and returns the categories available in the blojsom
     *
     * @param blogid Dummy Value for Blojsom
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @throws Exception
     * @return
     */
    public Object getCategories(String blogid, String userid, String password)
                         throws Exception
    {
        mLogger.info("getCategories() Called =====[ SUPPORTED ]=====");
        mLogger.info("     BlogId: " + blogid);
        mLogger.info("     UserId: " + userid);

        WebsiteData website = validate(userid,password);

        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest();
            HttpServletRequest req = rreq.getRequest();
            String contextUrl =
                RollerContext.getRollerContext(req).getAbsoluteContextUrl(req);
            Roller roller = rreq.getRoller();

            Vector result = new Vector();
            WeblogManager weblogMgr = roller.getWeblogManager();

            List cats = weblogMgr.getWeblogCategories(website, false);

            for (Iterator wbcItr = cats.iterator(); wbcItr.hasNext();) {
				WeblogCategoryData category = (WeblogCategoryData) wbcItr.next();
				String name = category.getName();
				String ctx = contextUrl;

				Hashtable catDetails = new Hashtable(3);
				catDetails.put("description", name);

				String catUrl = ctx+"/page/"+userid+"?catname="+name;
				catUrl = Utilities.stringReplace(catUrl," ","%20");
				catDetails.put("htmlUrl", catUrl);

				String rssUrl = ctx+"/rss/"+userid+"?catname="+name;
				rssUrl = Utilities.stringReplace(catUrl," ","%20");
				catDetails.put("rssUrl",rssUrl);

				result.add(catDetails);
			}

            return result;
        }
        catch (Exception e)
        {
            String msg = "ERROR in MetaWeblogAPIHandler.getCategories";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }

    //------------------------------------------------------------------------

    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws org.apache.xmlrpc.XmlRpcException
     * @return
     */
    public boolean editPost(String postid, String userid, String password,
                            Hashtable struct, boolean publish)
                     throws Exception
    {
        mLogger.info("editPost() Called ========[ SUPPORTED ]=====");
        mLogger.info("     PostId: " + postid);
        mLogger.info("     UserId: " + userid);
        mLogger.info("    Publish: " + publish);

        validate(userid,password);

        Roller roller = RollerRequest.getRollerRequest().getRoller();

        Hashtable postcontent = struct;
        String description = (String)postcontent.get("description");
        String title = (String)postcontent.get("title");
        if (title == null) title = "";

        String cat = null;
        if ( postcontent.get("categories") != null )
        {
            Vector cats = (Vector)postcontent.get("categories");
            cat = (String)cats.elementAt(0);
        }
        mLogger.info("      Title: " + title);
        mLogger.info("   Category: " + cat);

        try
        {
            WeblogManager weblogMgr = roller.getWeblogManager();

            Timestamp current =
                new Timestamp(System.currentTimeMillis());

            WeblogEntryData entry =
                weblogMgr.retrieveWeblogEntry(postid);

            if ( !title.equals("") ) entry.setTitle(title);
            entry.setText(description);
            entry.setUpdateTime(current);
            entry.setPublishEntry(Boolean.valueOf(publish));

            if ( cat != null )
            {
                // Use first category specified by request
                WeblogCategoryData cd = weblogMgr.getWeblogCategory(cat, entry.getWebsite());
                entry.setCategory(cd);
            }

            entry.save();
            roller.commit();
            flushPageCache(userid);

            return true;
        }
        catch (Exception e)
        {
            String msg = "ERROR in MetaWeblogAPIHandler.editPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }

    //------------------------------------------------------------------------

    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws org.apache.xmlrpc.XmlRpcException
     * @return
     */
    public String newPost(String blogid, String userid, String password,
                          Hashtable struct, boolean publish)
                   throws Exception
    {
        mLogger.info("newPost() Called ===========[ SUPPORTED ]=====");
        mLogger.info("     BlogId: " + blogid);
        mLogger.info("     UserId: " + userid);
        mLogger.info("    Publish: " + publish);

        WebsiteData website = validate(userid,password);

        Hashtable postcontent = struct;
        String description = (String)postcontent.get("description");
        String title = (String)postcontent.get("title");
        if (title == null) title = "";

        String cat = null;
        if ( postcontent.get("categories") != null )
        {
            Vector cats = (Vector)postcontent.get("categories");
            cat = (String)cats.elementAt(0);
        }
        mLogger.info("      Title: " + title);
        mLogger.info("   Category: " + cat);

        try
        {
            Roller roller = RollerRequest.getRollerRequest().getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();

            Timestamp current = new Timestamp(System.currentTimeMillis());

            WeblogEntryData entry = new WeblogEntryData();
            entry.setTitle(title);
            entry.setText(description);
            entry.setPubTime(current);
            entry.setUpdateTime(current);
            entry.setWebsite(website);
            entry.setPublishEntry(Boolean.valueOf(publish));

            if ( cat != null )
            {
                // Use first category specified by request
                WeblogCategoryData cd = weblogMgr.getWeblogCategory(cat,website);
                entry.setCategory(cd);
            }
            else
            {
                // Use Blogger API category from user's weblog config
                entry.setCategory(website.getBloggerCategory());
            }

            entry.save();
            roller.commit();
            flushPageCache(userid);

/*
            RollerRequest rreq = RollerRequest.getRollerRequest();
            HttpServletRequest req = rreq.getRequest();
            String blogUrl = Utilities.escapeHTML(
                RollerContext.getRollerContext(req).getAbsoluteContextUrl(req)
                + "/page/" + userid);
			RollerXmlRpcClient.sendWeblogsPing(
				blogUrl, entry.getWebsite().getName());
*/
            return entry.getId();
        }
        catch (Exception e)
        {
            String msg = "ERROR in MetaWeblogAPIHandler.newPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }

    //------------------------------------------------------------------------

    /**
     *
     * @param postid
     * @param userid
     * @param password
     * @return
     * @throws Exception
     */
    public Object getPost(String postid, String userid, String password)
                   throws Exception
    {
        mLogger.info("getPost() Called =========[ SUPPORTED ]=====");
        mLogger.info("     PostId: " + postid);
        mLogger.info("     UserId: " + userid);

        validate(userid,password);

        try
        {
            Roller roller = RollerRequest.getRollerRequest().getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();
            WeblogEntryData entry = weblogMgr.retrieveWeblogEntry(postid);

            Hashtable result = new Hashtable();

            Vector cats = new Vector();
            cats.addElement(entry.getCategory().getName());

            result.put("categories", cats);
            result.put("dateCreated", entry.getPubTime());
            result.put("postid", entry.getId());
            result.put("title", entry.getTitle());
            result.put("description", entry.getText());
            result.put("userid", userid);

            return result;
        }
        catch (Exception e)
        {
            String msg = "ERROR in MetaWeblogAPIHandler.getPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }

    //------------------------------------------------------------------------

    /**
     *
     * @param blogid
     * @param userid
     * @param password
     * @param struct
     * @return
     * @throws Exception
     */
    public Object newMediaObject(String blogid, String userid, String password,
                                 Object struct) throws Exception
    {
        mLogger.info("newMediaObject() Called =[ UNSUPPORTED ]=====");
        mLogger.info("     BlogId: " + blogid);
        mLogger.info("     UserId: " + userid);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION,
                                  UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Get a list of recent posts for a category
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     * @throws XmlRpcException
     * @return
     */
    public Object getRecentPosts(String blogid, String userid,
                                 String password, int numposts)
                          throws Exception
    {
        return getRecentPosts("",blogid,userid,password,numposts);
    }
}
