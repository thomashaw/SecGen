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

import java.lang.reflect.InvocationTargetException;

import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.fwk.exception.ViewException;
import net.sf.jboard.model.forum.clients.ForumFacadeClientFactory;
import net.sf.jboard.model.forum.clients.ForumFacadeInterface;
import net.sf.jboard.model.forum.dto.ForumThread;
import net.sf.jboard.model.forum.dto.ForumMessage;
import net.sf.jboard.struts.fwk.actions.ConstantsContext;
import net.sf.jboard.struts.fwk.actions.ConstantsMessages;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * regroup the ForumMessage actions.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.17 $
 */
public class ForumMessageDispatchAction extends DispatchAction implements ConstantsContext,ConstantsMessages{

    private static Log logger = LogFactory.getLog(ForumMessageDispatchAction.class);

    /**
     * Constructor.
     */
    public ForumMessageDispatchAction() {
        super();

    }

    /**
     * <code>create</code> a new <code>ForumCategory</code>. a ForumCategory is
     * a leaf just under the ForumRoot.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws ClientException
     * @throws ViewException
     */
    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException, ClientException  {

        DynaActionForm dyna = (DynaActionForm)form;
        
        
        ForumFacadeInterface ffi = null;
		ForumMessage msg = null;
		ForumThread ft = null;
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
		
		
		  msg = new ForumMessage();
		  
		  try {
			//transfer between ActionForm and the business DTO object.
			BeanUtils.copyProperties(msg, form);
			logger.debug("message="+msg.getMessage());
			logger.debug("ftId message="+msg.getParentId());
		  } catch (IllegalAccessException e) {
			  log.debug("beanUtils error forumthreadDispatchAction create method");
			  throw new ViewException("errors_bean_utils");
		  } catch (InvocationTargetException e) {
			  log.debug("beanUtils error forumthreadDispatchAction create method");
			  throw new ViewException("errors_bean_utils");
		  }
		  
          //create a message in the ForumThread and return th ForumThread updated
          ft = ffi.createMessage(msg);
    
          dyna.set("forumThread",ft);
    
    
     return mapping.findForward("createForumMessageOK");

    }
	/**
		 * return a ForumCategory.
		 *
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 *
		 * @return ActionForward
		 *
		 * @throws ViewException
		 */
		public ActionForward read(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException {

			logger.debug("begin method read de ForumCategoryDispatchAction");
			DynaActionForm dyna = (DynaActionForm)form;
			ForumFacadeInterface ffi = null;
			ForumMessage msg = null;
		
		
            ffi = ForumFacadeClientFactory.getForumFacadeClient();
			try{
				msg = ffi.readMessage((Long)dyna.get("id"));
				logger.debug("message="+msg.getMessage());
			}catch(ClientException e){
				logger.debug("problem");
				throw new ViewException("errors_dao");
			}
		   
		   try {
			BeanUtils.copyProperties(dyna,msg);
			} catch (IllegalAccessException e1) {
				throw new ViewException("errors_bean_utils");
			} catch (InvocationTargetException e1) {
				throw new ViewException("errors_bean_utils");
			}
		  
			return mapping.findForward("readForumMessageOK");

		}

    
    /**
     * update the message.
     *
     * @param mapping
     * @param form 
     * @param request 
     * @param response 
     *
     * @return ActionForward
     *
     */
    public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException {

      
        logger.debug("begin update method ForumMessage");
        
        DynaActionForm dyna = (DynaActionForm)form;
        
		ForumFacadeInterface ffi= null;
		ForumMessage msg = null;
        ForumThread ft = null;
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
		msg = new ForumMessage();
		try {
			BeanUtils.copyProperties(msg,form);
		} catch (IllegalAccessException e) {
			throw new ViewException("errors_bean_utils");
		} catch (InvocationTargetException e) {
			throw new ViewException("errors_bean_utils");
		}
		try {
            ft = ffi.updateMessage(msg);
		} catch (ClientException e1) {
			throw new ViewException("errors_dao");
		}
        
        dyna.set("forumThread",ft);
		logger.debug("before updateForumMessageOK");
        return mapping.findForward("updateForumMessageOK");

    }

    /**
     * delete the message.
     *
     * @param mapping 
     * @param form 
     * @param request 
     * @param response 
     *
     * @return ActionForward
     */
    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, DAOException  {
         
        logger.debug("begin delete method ForumMessage");
        DynaActionForm dyna = (DynaActionForm)form;
        ForumFacadeInterface ffi = null;
        ForumThread ft = null;
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
        try {
		     	ft = ffi.deleteMessage((Long)dyna.get("id"),(Long)dyna.get("parentId"));
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        dyna.set("forumThread",ft);
        
		return mapping.findForward("deleteForumMessageOK");

    }

}
