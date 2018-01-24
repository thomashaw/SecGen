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
import net.sf.jboard.model.forum.dto.ForumItem;
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
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * regroup the ForumCategory actions.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.21 $
 */
public class ForumThreadDispatchAction extends DispatchAction implements ConstantsContext,ConstantsMessages{

    private static Log logger = LogFactory.getLog(ForumThreadDispatchAction.class);

    /**
     * Constructor.
     */
    public ForumThreadDispatchAction() {
        super();

    }

    /**
     * <code>create</code> a new <code>ForumThread</code>. 
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     *
     * @return
     *
     * @throws ViewException
     * @throws ClientException
     */
    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException, ClientException {

		        logger.debug("begin méthod create  ForumThread");
				DynaActionForm dyna = (DynaActionForm)form;

				ForumFacadeInterface ffi = null;
				ForumThread ft = null;
                ForumItem fit = null;
                ForumMessage msg = null;
                
                ffi = ForumFacadeClientFactory.getForumFacadeClient();
				ft = new ForumThread();
				msg = new ForumMessage();

				//transfer between ActionForm and the business DTO object.
				try {
					BeanUtils.copyProperties(ft, form);
					BeanUtils.copyProperties(msg, form);
                    
                    
				} catch (IllegalAccessException e) {
					log.debug("beanUtils error forumthreadDispatchAction create method");
					throw new ViewException("errors_bean_utils");
				} catch (InvocationTargetException e) {
					log.debug("beanUtils error forumthreadDispatchAction create method");
					throw new ViewException("errors_bean_utils");
				}
		        
		       
		       
				//create the ForumThread object
				//and retrieve the forumItem 
				//create a forumThread and his attached first message
				fit = ffi.createForumThread(ft,msg); 
                dyna.set("forumItem",fit);
                dyna.set("id",fit.getId());
                dyna.set("parentId",fit.getParent().getId());


        return mapping.findForward("createForumThreadOK");

    }

    /**
     * call the business client to retrieve from the DAO abstraction, the <code>ForumThread</code> item.
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
    public ActionForward read(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException  {

        logger.debug(" read ForumThreadDispatchAction");
        DynaActionForm dyna = (DynaActionForm)form;
 
        ForumFacadeInterface ffi = null;
        

        ffi = ForumFacadeClientFactory.getForumFacadeClient();

        ForumThread ft = null;
		logger.debug("ftId actionForm="+dyna.get("id"));
		
        // business delegate
        try {
            
			ft = (ForumThread) ffi.readForumThread((Long)dyna.get("id"));
		}catch(ClientException daoex){
            logger.error("DAO ERROR");
            throw new ViewException("errors_dao");       
		}
	
        dyna.set("forumThread",ft);

        return mapping.findForward("readForumThreadOK");

    }
    /**
     * call the business client to retrieve from the DAO abstraction, the <code>ForumThread</code> item.
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
    public ActionForward beforeUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException {

        logger.debug(" beforeUpdate ForumThreadDispatchAction");
            DynaActionForm dyna = (DynaActionForm)form;
 
            ForumFacadeInterface ffi = null;
        

            ffi = ForumFacadeClientFactory.getForumFacadeClient();

            ForumThread ft = null;
            
    
        // business delegate
        try {
			ft = (ForumThread) ffi.readForumThread((Long)dyna.get("id"));
		}  catch (ClientException e1) {
            logger.error("DAO ERROR");
            throw new ViewException("errors_dao");  
		}
        logger.debug(" after business delegate forumThreadDispatchAction read");
        //put in the request context the result embedded in the collection
        try {
			BeanUtils.copyProperties(dyna,ft);
		} catch (IllegalAccessException e) {
            throw new ViewException("errors_dao");
		} catch (InvocationTargetException e) {
            throw new ViewException("errors_dao");
        }

        return mapping.findForward("beforeUpdateForumThreadOK");

    }
    /**
     * call the business client to update via the DAO layer the ForumThread.
     *
     * @param mapping 
     * @param form 
     * @param request 
     * @param response 
     *
     * @return ActionForward
     */
    public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException {

        logger.debug("begin update  ForumThread");
        DynaActionForm dyna = (DynaActionForm)form;
        ForumFacadeInterface ffi = null;
        ForumThread ft = null;
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
        ft = new ForumThread();
        ForumItem fit;
        try {
			BeanUtils.copyProperties(ft,dyna);
		} catch (IllegalAccessException e1) {
            throw new ViewException("errors_bean_utils");
		} catch (InvocationTargetException e1) {
            throw new ViewException("errors_bean_utils");
		}
       
        try {
            fit = ffi.updateForumThread(ft);
		} catch (ClientException e) {
            //@TODO enhance exception handling            
			throw new ViewException("");
		}
        
        dyna.set("forumItem",fit);
        
        logger.debug("end update  ForumThread");
        return mapping.findForward("updateForumThreadOK");

    }

    /**
     * delete a forumThread.
     *
     * @param mapping 
     * @param form 
     * @param request 
     * @param response 
     *
     * @return ActionForward
     *
     */
    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException, ClientException  {

        logger.debug("begin delete method ForumThread");
        DynaActionForm dyna = (DynaActionForm)form;
        
        ForumFacadeInterface ffi = null;
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
        ForumItem fit = null;
        
        try{
            fit = ffi.deleteForumThread((Long)dyna.get("id"),(Long)dyna.get("parentId"));
        
        } catch (NumberFormatException e) {
            //@TODO enhance exception handling            
			throw new ClientException("");
		}
        
        dyna.set("forumItem",fit);
        
        return mapping.findForward("deleteForumThreadOK");

    }

}
