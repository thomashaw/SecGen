/*
 * Filename: AcronymsPlugin.java
 * 
 * Created on 22-Jun-04
 */
package org.roller.presentation.velocity.plugins.acronyms;

import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.PageData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

/**
 * Adds full text to pre-defined acronyms.
 * 
 * Example: HTML would become &lt;acronym title="Hyper Text Markup Language"&gt;HTML&lt;/acronym&gt; 
 * 
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.1 $
 */
public class AcronymsPlugin implements PagePlugin
{
	/**
	 * Logger
	 */
	private static final Log logger = LogFactory.getLog(AcronymsPlugin.class);

	/**
	 * Name of this Plugin.
	 */
	private String name = "Acronyms";
	
	/**
	 * User Manager to get to the Acronyms Page Data.
	 */
	private UserManager userMgr = null;

	/**
	 * Constructor
	 */
	public AcronymsPlugin()
	{
		super();
	}

	/**
	 * @see org.roller.presentation.velocity.PagePlugin#init(org.roller.presentation.RollerRequest, org.apache.velocity.context.Context)
	 */
	public void init(RollerRequest rreq, Context ctx)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("init( rreq = "+rreq+", ctx = "+ctx+" )");
		}
		
		try
		{
			userMgr = rreq.getRoller().getUserManager();
		}
		catch (RollerException e)
		{
			logger.error("Error initializing AcronymsPlugin.", e);
		}
	}

	/**
	 * @see org.roller.presentation.velocity.PagePlugin#render(org.roller.pojos.WeblogEntryData, boolean)
	 */
	public String render(WeblogEntryData entry, boolean skipFlag)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("render( entry = "+entry+", skipFlag = "+skipFlag+" )");
		}
		
		String text = entry.getText();
		
		// check skipper
		if (skipFlag)
		{
			return text;
		}

		PageData acronymsPage = null;
		try 
		{
			acronymsPage = userMgr.getPageByName(entry.getWebsite(), "_acronyms");
		} 
		catch(RollerException ex)
		{
			// log exception
			logger.error("Exception gettting '_acronyms' for website: "+entry.getWebsite().getName(), ex);
		}

		// no acronyms, no glory
		if (acronymsPage==null) 
		{
			return text;
		} 

		Properties acronyms = parseAcronymPage(acronymsPage);

		if (acronyms==null || acronyms.isEmpty()) {
			return text;
		}

		// match 'm!
		return matchAcronyms(text, acronyms);
	}

	/**
	 * This methods cannto be implemented because this plugin needs access to 
	 * the Website to get to a <code>_acronyms</code> page.
	 * 
	 * @see org.roller.presentation.velocity.PagePlugin#render(java.lang.String)
	 */
	public String render(String text)
	{
		return text;
	}

	/**
	 * @return this Page Plugin's name
	 */
	public String toString()
	{
		return name;
	}
	
	/**
	 * Iterates through the acronym properties and replaces matching 
	 * acronyms in the entry text with acronym html-tags.
	 * 
	 * @param text entry text
	 * @param acronyms user provided set of acronyms
	 * @return entry text with acronym explanations
	 */
	private String matchAcronyms(String text, Properties acronyms)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("matchAcronyms( text = "+text+", acronyms = "+acronyms+" )");
		}
		
		for (Iterator iter = acronyms.keySet().iterator(); iter.hasNext();)
		{
			String acronym = (String) iter.next();
			String regEx = regEx = "\\b" + acronym + "\\b";
			String link =
				"<acronym title=\""
					+ acronyms.getProperty(acronym)
					+ "\">"
					+ acronym
					+ "</acronym>";
			text = text.replaceAll(regEx, link);
		}
		return text;
	}

	/**
	 * Parse the Template of the provided PageData and turns it
	 * into a <code>Properties</code> collection.
	 * 
	 * @param acronymPage
	 * @return acronym properties (key = acronym, value= full text), empty if Template is empty
	 */
	private Properties parseAcronymPage(PageData acronymPage)
	{
		String rawAcronyms = acronymPage.getTemplate();
		
		if (logger.isDebugEnabled()) 
		{
			logger.debug("parsing _acronyms template: \n'"+rawAcronyms+"'");
		}
		
		String regex = "\n"; // end of line
		String[] lines = rawAcronyms.split(regex);

		Properties acronyms = new Properties();
		if (lines != null)
		{
			for (int i = 1; i < lines.length; i++)
			{
				int index = lines[i].indexOf('=');
				if (index > 0)
				{
					String key = lines[i].substring(0, index).trim();
					String value =
						lines[i].substring(index + 1, lines[i].length()).trim();
					acronyms.setProperty(key, value);
				}
			}
		}

		return acronyms;
	}

}
