
package org.roller.presentation.velocity.plugins.textile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

/**
 * @author David M Johnson
 */
public class TextilePlugin implements PagePlugin
{
    public String name = "Textile Formatter";
    
    private net.sf.textile4j.Textile mTextile = new net.sf.textile4j.Textile();
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(TextilePlugin.class);
    
    public TextilePlugin()
    {
        mLogger.debug("Textile Plugin instantiated.");
    }
    
    public String toString() { return name; }
    
    /** 
     * Put plugin into the page context so templates may access it.
     */
    public void init(RollerRequest rreq, Context ctx)
    {
        ctx.put("textileRenderer",this);
    }
    
    /** 
     * Convert an input string that contains text that uses the Textile
     * syntax to an output string in HTML format.
     * @param src Input string that uses Textile syntax
     * @return Output string in HTML format.
     */
    public String render( String src )
    {
        return mTextile.process(src);
    }
    
    public String render( WeblogEntryData entry, boolean skipFlag )
    {
        return render( entry.getText() );
    }
}
