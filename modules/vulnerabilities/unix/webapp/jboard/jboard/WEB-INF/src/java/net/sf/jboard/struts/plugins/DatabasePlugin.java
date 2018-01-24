/*
jboard is a java bulletin board.
version $Name:  $
http://sourceforge.net/projects/jboard/
Copyright (C) 2003 Charles GAY
This file is part of jboard.
jboard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

jboard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jboard; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.sf.jboard.struts.plugins;



import net.sf.jboard.model.forum.clients.ForumBaseClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import javax.servlet.ServletException;


/**
 * this class is launched when the application parse the struts-config.xml file.
 * it initialize the Data storage for the entire application.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.7 $
 */
public class DatabasePlugin implements PlugIn {

   private static Log logger = LogFactory.getLog(DatabasePlugin.class);
	
   //datasource reference dynamically set by struts via struts-config.xml
   private String forumDatasourceName ;
   private String forumDaoImpl ;
   private ForumBaseClient fbc;
    /**
     * @see org.apache.struts.action.PlugIn#destroy()
     */
    public void destroy() {

        
    }

    /** 
     * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet, org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet arg0, ModuleConfig arg1) throws ServletException {

       
        try {
             fbc = new ForumBaseClient();
             fbc.init(forumDaoImpl);
			 logger.info("forumDatasourceName="+forumDatasourceName);
			 logger.info("forumDaoImpl="+forumDaoImpl);
			
        } catch(Exception ex) {

            System.err.println("Can't initialize data storage system.  DatabasePlugin Error: " + ex.getMessage());
            System.out.println("Can't initialize data storage system.  DatabasePlugin Error: " + ex.getMessage());

        }

    }


/**
 * @return
 */
public String getForumDatasourceName() {
	return forumDatasourceName;
}

/**
 * @param string
 */
public void setForumDatasourceName(String string) {
	forumDatasourceName = string;
}

/**
 * @return
 */
public String getForumDaoImpl() {
	return forumDaoImpl;
}

/**
 * @param string
 */
public void setForumDaoImpl(String string) {
	forumDaoImpl = string;
}

}
