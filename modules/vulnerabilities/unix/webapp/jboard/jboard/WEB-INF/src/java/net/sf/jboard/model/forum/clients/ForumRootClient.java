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

import net.sf.jboard.fwk.exception.*;
import net.sf.jboard.model.forum.dao.ForumDAOFactory;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.struts.fwk.actions.ConstantsMessages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Client class which communicate with action (business delegate design
 * pattern).
 * the visibility of this class is reduces to package.
 * the only public class is ForumFacadeClient.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.2 $
 */
 public class ForumRootClient extends ForumBaseClient implements ConstantsMessages{

    private static Log logger = LogFactory.getLog(ForumCategoryClient.class);
    
    /**
     * constructor.
     *
     */
    protected ForumRootClient(){
        
    }

    /**
     * retrieve an overview of the forum.
     *
     * @return ForumRoot
     * @throws ClientException
     * @throws DAOException
     */
    public ForumRoot getPanorama() throws ClientException {

        logger.debug("debut méthode getPanorama de ForumRootClient");

        ForumRoot panorama = null;
       
        try {
			panorama = ForumDAOFactory.getDAO().readPanorama();
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        logger.debug("fin méthode createCategory de ForumRootClient");

        return panorama;

    }

}
