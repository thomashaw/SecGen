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
package net.sf.jboard.struts.forum.actions;

import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.fwk.exception.ViewException;

import net.sf.jboard.model.forum.clients.ForumFacadeClientFactory;
import net.sf.jboard.model.forum.clients.ForumFacadeInterface;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.struts.fwk.actions.ConstantsContext;
import net.sf.jboard.struts.fwk.actions.ConstantsMessages;
import net.sf.jboard.struts.fwk.actions.JboardBaseAction;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * EntryPoint for the Forum business part.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.10 $
 */
public class ForumPanoramaAction extends JboardBaseAction implements ConstantsContext,ConstantsMessages{

    private static Log logger = LogFactory.getLog(ForumPanoramaAction.class);

    /**
     * Constructor for ForumAction.
     */
    public ForumPanoramaAction() {
        super();

    }

    /**
     * business logic execution place.
     *
     * @param mapping mapping
     * @param form form
     * @param request request
     * @param response response
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ClientException, ViewException {

		
        DynaActionForm dyna = (DynaActionForm)form;
        ForumRoot forumRoot = null;
        ForumFacadeInterface ffi = ForumFacadeClientFactory.getForumFacadeClient();
		
        
        try{
        
            forumRoot = ffi.getPanorama();
            //put in the request context the result embedded in the collection
        	//request.setAttribute(Constants.PANORAMA_OBJECT, forumRoot);
            
			dyna.set("forumRoot",forumRoot);
             
			 return mapping.findForward("panoramaOK");
				 
        }catch(ClientException cex){
			    logger.warn("error unknown");
		        throw cex;
        }
        
    }

   

}
