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
import net.sf.jboard.fwk.exception.ViewException;
import net.sf.jboard.model.forum.clients.ForumFacadeClientFactory;
import net.sf.jboard.model.forum.clients.ForumFacadeInterface;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumRoot;
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
 * regroup the ForumCategory actions.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.16 $
 */
public class ForumItemDispatchAction extends DispatchAction implements ConstantsContext,ConstantsMessages{

    private static Log logger = LogFactory.getLog(ForumItemDispatchAction.class);

    /**
     * Constructor.
     */
    public ForumItemDispatchAction() {
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
     *
     * @return ActionForward
     *
     * @throws ViewException
     */
    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws ViewException  {

        logger.debug("begin create de ForumItemDispatchAction");
		
        DynaActionForm dyna=  (DynaActionForm)form;
        
        ForumFacadeInterface ffi = null;
        ForumItem fit = null;
        ForumRoot root = null;
        
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
        fit = new ForumItem();

        //transfer between ActionForm and the business DTO object.
        try {
			BeanUtils.copyProperties(fit, form);
		} catch (IllegalAccessException e) {
			log.warn("beanUtils error forumItemDispatchAction create method");
			throw new ViewException("errors_bean_utils");
		} catch (InvocationTargetException e) {
			log.warn("beanUtils error forumItemDispatchAction create method");
			throw new ViewException("errors_bean_utils");
			
		}
		fit.setId(null);
        logger.debug(" title=" + fit.getTitle());
        logger.debug(" description=" + fit.getDescription());
		logger.debug(" catId=" + fit.getParentId());
        //on crée une catégorie dans le forum en passant en parametre
        // l'objet catégorie rempli par l'utilisateur
        try {
			root = ffi.createForumItem(fit);
		} catch (ClientException e1) {
			log.warn("beanUtils error forumItemDispatchAction create method");
			throw new ViewException("errors_bean_utils");
		}
        
		dyna.set("forumRoot",root);
        
        
        return mapping.findForward("createForumItemOK");

    }

    /**
     * call in the business side, the DAO client to retrieve the <code>ForumItem</code> object.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     *
     * @return ActionForward
     *
     * @throws Exception
     */
    public ActionForward read(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ClientException, ViewException  {

        logger.debug(" read ForumItemDispatchAction");
		DynaActionForm dyna = (DynaActionForm)form;

        ForumFacadeInterface ffi= null;
        ForumItem fit = null;
		
        ffi = ForumFacadeClientFactory.getForumFacadeClient();

        fit = (ForumItem) ffi.readForumItem((Long)dyna.get("id"));
        dyna.set("forumItem",fit);
        return mapping.findForward("readForumItemOK");

    }
    /**
     * call in the business side, the DAO client to retrieve the <code>ForumItem</code> object.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     *
     * @return ActionForward
     *
     * @throws Exception
     */
    public ActionForward beforeUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ClientException, ViewException  {

        logger.debug(" beforeUpdate ForumItemDispatchAction");
        DynaActionForm dyna = (DynaActionForm)form;

        ForumFacadeInterface ffi= null;
        ForumItem fit = null;
    
        ffi = ForumFacadeClientFactory.getForumFacadeClient();

   
        fit = (ForumItem) ffi.readForumItem((Long)dyna.get("id"));
        logger.debug("title="+fit.getTitle());
        logger.debug("description="+fit.getDescription());
        //put in the request context the result embedded in the collection
        try {
			BeanUtils.copyProperties(dyna,fit);
		} catch (IllegalAccessException e) {
            throw new ViewException("errors_bean_utils");
		} catch (InvocationTargetException e) {
            throw new ViewException("errors_bean_utils");
		}
        return mapping.findForward("beforeUpdateForumItemOK");

    }
    /**
     * update the forumItem.
     *
     * @param ActionMapping mapping 
     * @param ActionForm form
     * @param HttpServletRequest request
     * @param HttpServletResponse response
     *
     * @return ActionForward

     */
    public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ViewException  {
                   
            DynaActionForm dyna = (DynaActionForm)form;
            ForumItem fit = null;
            ForumFacadeInterface ffi = null;
            ForumRoot root = null;
            
            ffi = ForumFacadeClientFactory.getForumFacadeClient();
            fit = new ForumItem();
            try {
				BeanUtils.copyProperties(fit,dyna);
			} catch (IllegalAccessException e1) {
                throw new ViewException("errors_bean_utils");
			} catch (InvocationTargetException e1) {
                throw new ViewException("errors_bean_utils");
			}
        
            try {
                root = ffi.updateForumItem(fit);
			} catch (ClientException e) {
                //@TODO enhance exception handling                            
				throw new ViewException("");
			}
               
            dyna.set("forumRoot",root);
            logger.debug("end update method ForumItemDispatchAction");

            return mapping.findForward("updateForumItemOK");

    }

    /**
     * delete the forumItem.
     *
     * @param mapping 
     * @param form 
     * @param request 
     * @param response 
     *
     * @return ActionForward
     *
     * @throws Exception
     */
    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)  {

        logger.debug("begin delete method   ForumItemDispatchAction");
		DynaActionForm dyna = (DynaActionForm)form;
        ForumRoot root = null;
        ForumFacadeInterface ffi = null;
		ffi = ForumFacadeClientFactory.getForumFacadeClient();
		try {
			root = ffi.deleteForumItem((Long)dyna.get("id"));
		}catch (ClientException e) {
            //@TODO enhance exception handling                    
			new ViewException("");
		}
        
        dyna.set("forumRoot",root);
        logger.debug("end delete method ForumItemDispatchAction");

		return mapping.findForward("deleteForumItemOK");

    }

}
