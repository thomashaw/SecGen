/*
 * Created on Mar 10, 2004
 */
package org.roller.presentation;

import org.roller.util.Blacklist;

import java.util.TimerTask;

/**
 * @author lance.lavandowska
 */
public class BlacklistUpdateTask extends TimerTask
{    
    public BlacklistUpdateTask()
    {
        // load Blacklist from file
        Blacklist.getBlacklist();
        
        // now have it check for an update
        Blacklist.checkForUpdate();
    }

    public void run() 
    {
        // try reading new def from URL
        Blacklist.checkForUpdate();
    }
}
