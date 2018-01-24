/*
 * Created on Mar 5, 2004
 */
package org.roller.presentation.atom;

import org.osjava.atom4j.pojo.Content;
import org.osjava.atom4j.pojo.Entry;
import org.osjava.atom4j.pojo.Feed;
import org.osjava.atom4j.pojo.Generator;
import org.osjava.atom4j.pojo.Link;
import org.osjava.atom4j.pojo.Person;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author lance.lavandowska
 */
public abstract class AtomAssistant
{
    protected static String mBaseUrl = "";
    
    /*
     * Convert a Roller Website to an Atom Feed, and attach the
     * Atom Entries.
     */
    protected static Feed convertToAtomFeed(WebsiteData website, List rollerEntries) 
    {
        Feed feed = new Feed();
        
        Generator generator = new Generator();
        generator.setContent("RollerWeblogger");
        generator.setUrl("http://www.rollerweblogger.org");
        generator.setVersion(
            RollerContext.getRollerContext(
                // use RollerRequest.getServletContext !!
                RollerRequest.getRollerRequest().getServletContext() 
                    ).getRollerVersion());
        feed.setGenerator( generator );
        
        feed.setId( website.getId() ); //TODO: better ID?
        
        Content title = new Content();
        title.setText(website.getName());
        feed.setTitle(title);
        
        Content desc = new Content();
        desc.setText(website.getDescription());
        feed.setTagline(desc);

        List atomEntries = AtomAssistant.convertEntries(rollerEntries);
        feed.setEntries(atomEntries);
        
        return feed;
    }

    //------------------------------------------------------------------------
    /**
     * Convert a Roller WeblogEntryData into an Atom Entry.
     */
    protected static Entry convertToAtomEntry(WeblogEntryData weblogEntry)
    {
        UserData user = weblogEntry.getWebsite().getUser();
        Entry atomEntry = new Entry();
        atomEntry.setId( weblogEntry.getId() );
        atomEntry.setTitle( new Content() );
        atomEntry.getTitle().setText(weblogEntry.getTitle());
        atomEntry.setIssued( weblogEntry.getPubTime() );
        atomEntry.setCreated( weblogEntry.getPubTime() );
        atomEntry.setModified( weblogEntry.getUpdateTime() );
        
        Link altLink = new Link();
        altLink.setRel(Link.ALTERNATE);
        altLink.setHref(mBaseUrl + weblogEntry.getPermaLink());
        atomEntry.addLink(altLink);
        
        atomEntry.setAuthor( new Person() );
        atomEntry.getAuthor().setName( user.getFullName() );
        atomEntry.getAuthor().setUrl( mBaseUrl + "/" + user.getUserName() );
        atomEntry.getAuthor().setEmail( user.getEmailAddress() );
        
        atomEntry.setContent( new Content() );
        atomEntry.getContent().setText( weblogEntry.getText() );
        atomEntry.getContent().setMimeType( "application/xhtml+xml" );
        atomEntry.getContent().setLanguage( "en-us" );
        return atomEntry;
    }

    //------------------------------------------------------------------------
    /**
     * Convert a List of WeblogEntryData objects to a List of Atom Entries.
     * 
     * @param rollerEntries
     * @return
     */
    protected static List convertEntries(List rollerEntries)
    {
        // Convert Roller entries into Atom entries
        List entries = new ArrayList(rollerEntries.size());
        Iterator iter = rollerEntries.iterator();
        WeblogEntryData weblogEntry = null;
        while (iter.hasNext())
        {
            weblogEntry = (WeblogEntryData)iter.next();
            entries.add( AtomAssistant.convertToAtomEntry(weblogEntry) );
        }
        return entries;
    }

}
