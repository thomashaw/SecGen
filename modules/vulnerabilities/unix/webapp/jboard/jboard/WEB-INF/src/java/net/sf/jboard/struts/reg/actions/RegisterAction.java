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
package net.sf.jboard.struts.reg.actions;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.fwk.exception.ViewException;
import net.sf.jboard.model.reg.clients.RegFacadeClient;
import net.sf.jboard.model.reg.dto.User;
import net.sf.jboard.struts.fwk.actions.JboardBaseAction;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class RegisterAction extends JboardBaseAction{
    
    
    private static Log logger = LogFactory.getLog(RegisterAction.class);
    /**
     * Constructor for ForumAction.
     */
    public RegisterAction() {
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
        
        logger.debug(" start RegisterAction ");
        boolean result;
        DynaActionForm dyna = (DynaActionForm)form;
        RegFacadeClient regfacade = new RegFacadeClient();
        User user = new User();
        
        try {
            BeanUtils.copyProperties(user,dyna);
        } catch (IllegalAccessException e1) {
            throw new ViewException("errors_bean_utils");
        } catch (InvocationTargetException e1) {
            throw new ViewException("errors_bean_utils");
        }
       
        result = regfacade.register(user);
        
        if(result){
          return mapping.findForward("registerOK");
        }
        
        return mapping.findForward("registerKO");
        
    }
}
