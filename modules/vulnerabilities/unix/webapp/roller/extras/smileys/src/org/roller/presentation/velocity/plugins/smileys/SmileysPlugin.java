package org.roller.presentation.velocity.plugins.smileys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts ascii emoticons into HTML image tags.
 * 
 * @author lance.lavandowska
 * Created on Jun 8, 2004
 */
public class SmileysPlugin implements PagePlugin
{
    public String name = "Emoticons";
    
    private static String imgSize = "20";
    private Properties smileyDefs = new Properties();
    protected Pattern[] smileyPatterns = new Pattern[0];
    protected String[] imageTags = new String[0];

    private static Log mLogger = 
        LogFactory.getFactory().getInstance(SmileysPlugin.class);
    
    public SmileysPlugin()
    {
        try
        {
            smileyDefs.load(this.getClass().getResourceAsStream("smileys.properties"));
        }
        catch (Exception e)
        {
            mLogger.error("Unable to load smileys.properties", e);
        }
        mLogger.debug("SmileysPlugin instantiated.");
    }

    public String toString() { return name; }

    /* 
     * Find occurences of ascii emoticons and turn them into
     * HTML image pointers.
     * 
     * @see org.roller.presentation.velocity.PagePlugin#render(java.lang.String)
     */
    public String render(String text)
    {
        Matcher matcher = null;
        for (int i=0; i<smileyPatterns.length; i++)
        {
            matcher = smileyPatterns[i].matcher(text);
            text = matcher.replaceAll(imageTags[i]);
        }
        return text;
    }

    /* 
     * Convert the SmileyDefs into RegEx patterns and img tags for
     * later use.  Need an HttpServletRequest though so that we can
     * get the ServletContext Path.  But only do it once.
     * 
     * @see org.roller.presentation.velocity.PagePlugin#init(org.roller.presentation.RollerRequest, org.apache.velocity.context.Context)
     */
    public void init(RollerRequest rreq, Context ctx)
    {
        if (smileyPatterns.length < 1)
        {
            String contextPath = "";
            if (rreq != null && rreq.getRequest() != null) 
            {
                contextPath = rreq.getRequest().getContextPath();
            }
            Pattern[] tempP = new Pattern[smileyDefs.size()];
            String[] tempS = new String[smileyDefs.size()];
            //System.out.println("# smileys: " + smileyDefs.size());
            int count = 0;
            Enumeration enum1 = smileyDefs.propertyNames();
            while(enum1.hasMoreElements())
            {
                String smiley = (String)enum1.nextElement();
                String smileyAlt = htmlEscape(smiley);
                tempP[count] = Pattern.compile(regexEscape(smiley));
                tempS[count] = "<img src=\"" + 
                               contextPath + "/images/smileys/" + 
                               smileyDefs.getProperty(smiley, "smile.gif") +
                               "\" height=\"" + imgSize + "\" width=\"" +
                               imgSize + "\" alt=\"" + smileyAlt + 
                               "\" title=\"" + smileyAlt +"\">";
                //System.out.println(smiley + "=" + tempS[count]);
                count++;
            }
            smileyPatterns = tempP;
            imageTags = tempS;
        }
    }
    
    private String htmlEscape(String smiley) 
    {
        char[] chars = smiley.toCharArray();
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<chars.length; i++) 
        {
            if (chars[i] == '"')
            {
                buf.append("&quot;");
            }
            else if (chars[i] == '>')
            {
                buf.append("&gt;");
            }
            else if (chars[i] == '<')
            {
                buf.append("&lt;");
            }
            else
            {    
                buf.append(chars[i]);
            }
        }
        return buf.toString();    
    }

    /**
     * Some characters have to escaped with a backslash before
     * being compiled into a Regular Expression.
     * 
     * @param smiley
     * @return
     */
    private static char[] escape_regex = new char[] 
        {'-', '(', ')', '\\', '|', ':', '^', '$', '*', '+', '?', 
         '{', '}', '!', '=', '<', '>', '&', '[', ']' };
    private String regexEscape(String smiley)
    {
        char[] chars = smiley.toCharArray();
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<chars.length; i++) 
        {
            for (int x=0; x<escape_regex.length; x++)
            {
                if (escape_regex[x] == chars[i])
                {
                    buf.append("\\");
                    break;                    
                }
            }
            buf.append(chars[i]);
        }
        return buf.toString();
    }

    /* 
     * @see org.roller.presentation.velocity.PagePlugin#render(org.roller.pojos.WeblogEntryData, boolean)
     */
    public String render(WeblogEntryData entry, boolean skipFlag)
    {
        return render(entry.getText());
    }
}
