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
package net.sf.jboard.model.forum.clients;

import net.sf.jboard.model.forum.dao.ForumDAOFactory;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * the mother of all the others client classes.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.9 $
 */
public class ForumBaseClient  {
	
private static Log logger = LogFactory.getLog(ForumBaseClient.class);
private static String daoImpl;

    /**
     * constructor.
     */
    public ForumBaseClient() {
        super();

    }
    /**
     * initialize the daoImplementation and his datasource reference.
     * @param daoImplementation
     * @param datasourceRef
     */
    public  void init(String daoImplementation){
    	daoImpl = "net.sf.jboard.model.forum.dao.Forum"+daoImplementation;
                  
        ForumDAOFactory.setDAO(daoImpl);
        
    }
    

}
