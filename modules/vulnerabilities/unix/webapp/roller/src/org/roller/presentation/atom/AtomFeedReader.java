/*
 * Created on Apr 2, 2004
 */
package org.roller.presentation.atom;

import org.apache.commons.digester.Digester;
import org.osjava.atom4j.reader.FeedReader;

import java.io.InputStream;

/**
 * @author lance.lavandowska
 */
public class AtomFeedReader extends FeedReader
{
    /**
     * @param xml
     */
    public AtomFeedReader(String xml)
    {
        super(xml);
    }

    /**
     * @param in
     */
    public AtomFeedReader(InputStream in)
    {
        super(in);
    }
    
    public void parse()
    {
        super.parse();
    }

    /**
     * Configure digester to parse Entries, based on the parent path.
     * 
     * @param digester
     * @param parent
     */
    protected void configureEntryDigester(Digester digester, String parent )
    {
        String entryPath = parent + "entry";
        digester.addObjectCreate(entryPath, AtomEntry.class.getName());
        
        configureContentDigester(digester, entryPath + "/title", "setTitle", "</title>");
        
        digester.addCallMethod(entryPath + "/issued",   "setIssued", 0);
        digester.addCallMethod(entryPath + "/created",  "setCreated", 0);
        digester.addCallMethod(entryPath + "/modified", "setModified", 0);
        digester.addCallMethod(entryPath + "/id",       "setId", 0);
        
        configureLinkDigester(digester, entryPath + "/link");
        
        configurePersonDigester(digester, entryPath + "/author", "setAuthor");

        configurePersonDigester(digester, entryPath + "/contributor", "addContributor");
        
        configureContentDigester(digester, entryPath + "/content", "setContent", "</content>");
        configureContentDigester(digester, entryPath + "/content/content", "addContent", "</content>");
        
        configureContentDigester(digester, entryPath + "/summary", "setSummary", "</summary>");
        
        /* new elements for Roller's version of Atom Entry */
        digester.addCallMethod(entryPath + "/subject", "addCategory", 0);
        digester.addCallMethod(entryPath + "/annotate", "setAnnotation", 0);
        
        digester.addSetNext(entryPath, "addEntry",  AtomEntry.class.getName());
    }
}
