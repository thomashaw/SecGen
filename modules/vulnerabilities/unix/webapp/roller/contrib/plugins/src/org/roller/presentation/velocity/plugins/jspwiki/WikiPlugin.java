
package org.roller.presentation.velocity.plugins.jspwiki;

import com.ecyrd.jspwiki.FileUtil;
import com.ecyrd.jspwiki.TranslatorReader;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * Wiki plugin using the JSPWiki WikiEngine. If you want Wiki links to point 
 * to your JSPWiki, then edit the jspwiki.properties fiel in Roller's WEB-INF
 * directory and set the jspwiki.baseURL appropriately. For example, if your
 * Wiki is on your localhost in the Servlet Context named 'wiki' then you'd
 * set jspwiki.baseURL like so:<br />
 * <br />
 *  jspwiki.baseURL=http://local:8080/wiki/<br />
 * 
 * @author David M Johnson
 */
public class WikiPlugin implements PagePlugin
{
    public String name = "JSPWiki Syntax";
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(WikiPlugin.class);

    WikiEngine mWikiEngine = null;
    WikiContext mWikiContext = null;
    WikiPage mWikiPage = new WikiPage("dummyPage");
    
    public WikiPlugin()
    {
        mLogger.debug("JSPWiki WikiPlugin instantiated.");
    }
    
    public String toString() { return name; }
    
    /** 
     * Put plugin into the page context so templates may access it.
     */
    public void init(RollerRequest rreq, Context ctx)
    {
        try
        {
            if (mWikiEngine == null)
            {
                if (rreq != null && rreq.getPageContext() != null)
                {
                    mWikiEngine = WikiEngine.getInstance(
                        rreq.getPageContext().getServletConfig());
                }
            }
            if (mWikiContext == null && mWikiEngine != null)
            {
                mWikiContext = new WikiContext( mWikiEngine, mWikiPage );
            }

            if (mWikiContext != null && mWikiEngine != null)
            {
                ctx.put("wikiPlugin",this);
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR initializing WikiPlugin",e);
        }
    }
    
    /** 
     * Convert an input string that contains text that uses JSPWiki
     * syntax to an output string in HTML format.
     * @param src Input string that uses JSPWiki syntax
     * @return Output string in HTML format.
     */
    public String render( String src )
    {
        String ret = null;        
        try
        {
            StringReader reader = new StringReader(src);            
            TranslatorReader tr = new TranslatorReader( mWikiContext, reader );
            ret = FileUtil.readContents( tr );        
        }
        catch (Exception e)
        {
            mLogger.error("ERROR rendering Wiki text",e);
        }
        return ret;
    }
    
    public String render( WeblogEntryData entry, boolean skipFlag)
    {
        return render(entry.getText());
    }
    
    /** Return URL to the Wiki page for a weblog entry, CamelCase style */
    public String makeCamelCaseWikiLink( WeblogEntryData wd, String prefix )
    {
        StringBuffer sb = new StringBuffer();
        StringTokenizer toker = new StringTokenizer(wd.getAnchor(),"_");
        while ( toker.hasMoreTokens() )
        {
            String token = toker.nextToken();
            sb.append( token.substring(0,1).toUpperCase() );
            sb.append( token.substring(1) );
        }
        return mWikiEngine.getBaseURL()+"Wiki.jsp?page="+prefix+sb.toString();
    }
    
    /** Return URL to the Wiki page for a weblog entry, spacey style */
    public String makeSpacedWikiLink( WeblogEntryData wd, String prefix )
    {
        StringBuffer sb = new StringBuffer();
        StringTokenizer toker = new StringTokenizer(wd.getAnchor(),"_");
        while ( toker.hasMoreTokens() )
        {
            sb.append( toker.nextToken() );
            if ( toker.hasMoreTokens() ) sb.append("%20");
        }
        return mWikiEngine.getBaseURL()+"Wiki.jsp?page="+prefix+sb.toString();
    }
}
