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
import net.sf.jboard.model.forum.dto.ForumCategory;
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
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * regroup the ForumCategory actions.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.14 $
 */
public class ForumCategoryDispatchAction extends DispatchAction implements ConstantsContext,ConstantsMessages {

    private static Log logger = LogFactory.getLog(ForumCategoryDispatchAction.class);

    /**
     * Constructor.
     */
    public ForumCategoryDispatchAction() {
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
     * @throws ClientException
     */
    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws ClientException, ViewException  {

        
        logger.debug(" create method in ForumCategoryDispatchAction");
	    DynaActionForm dyna = (DynaActionForm)form;
	    
        ForumFacadeInterface ffi = null;
        ForumCategory cat = null;
        ForumRoot root = null;
        
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
	
        cat = new ForumCategory();
		
		
			
        try{
        
            //transfer between ActionForm and the business DTO object.
            BeanUtils.copyProperties(cat, form);
        }catch(InvocationTargetException ex){
            logger.warn("BeanUtils error");
            throw new ViewException("errors_bean_utils");       
        }catch(IllegalAccessException ex){
            logger.warn("BeanUtils error");
            throw new ViewException("errors_bean_utils");   
        }
			logger.debug(" title=" + cat.getTitle());
			logger.debug(" description=" + cat.getDescription());
         root = ffi.createCategory(cat);
		 dyna.set("forumRoot",root);	
           
		 return mapping.findForward("createCategoryOK");

        
   
          
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
    public ActionForward beforeUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws  ViewException {

		logger.debug("begin method read de ForumCategoryDispatchAction");
		DynaActionForm dyna = (DynaActionForm)form;
        ForumFacadeInterface ffi = null;
        ForumCategory cat = null;
        Long catId = null;
        
		catId  = (Long)dyna.get("parentId");
		
		logger.debug("CatId ="+catId);
        ffi = ForumFacadeClientFactory.getForumFacadeClient();
		
        try {
			cat = ffi.readCategory(catId);
		} catch (NumberFormatException e) {
		   throw new ViewException("errors_dao");
			
		} catch (ClientException e) {
			throw new ViewException("errors_dao");
		}
		try {
			BeanUtils.copyProperties(dyna,cat);
            
            
		} catch (IllegalAccessException e1) {
			throw new ViewException("errors_bean_utils");
		} catch (InvocationTargetException e1) {
			throw new ViewException("errors_bean_utils");
		}
       
        return mapping.findForward("beforeUpdateCategoryOK");

    }

    /**
     * call the specific business client method to update the ForumCategory.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     *
     * @return ActionForward
     *
     * @throws ClientException
     */
    public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ClientException, ViewException {

        logger.debug("begin method update de ForumCategoryDispatchAction");
		DynaActionForm dyna = (DynaActionForm)form;
        ForumFacadeInterface ffi = null;
        ForumCategory cat = null;
        Long catId = null;
        ForumRoot root;
        ffi = ForumFacadeClientFactory.getForumFacadeClient();

        cat = new ForumCategory();
        
           try {
			BeanUtils.copyProperties(cat, form);
            catId  = (Long)dyna.get("parentId");
            cat.setId(catId);
		} catch (IllegalAccessException e) {
			throw new ViewException("errors_bean_utils");
		} catch (InvocationTargetException e) {
			throw new ViewException("errors_bean_utils");
		}
        

        try{
        
            root = ffi.updateCategory( cat);
        
        }catch(ClientException daoex){
        	throw new ViewException("errors_dao");
        }
        
        dyna.set("forumRoot",root);
        
        return mapping.findForward("updateCategoryOK");

    }

    /**
     * call the specific bussiness client method to delete the ForumCategory.
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
    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        logger.debug("begin method delete de ForumCategoryDispatchAction");
		
        DynaActionForm dyna = (DynaActionForm)form;
        ForumFacadeInterface ffi = null;
        ForumRoot root = null;

		Long CatId  = (Long)dyna.get("parentId");

        ffi = ForumFacadeClientFactory.getForumFacadeClient();

        root = ffi.deleteCategory(CatId);
		
        dyna.set("forumRoot",root);
        
        return mapping.findForward("deleteCategoryOK");

    }

}
