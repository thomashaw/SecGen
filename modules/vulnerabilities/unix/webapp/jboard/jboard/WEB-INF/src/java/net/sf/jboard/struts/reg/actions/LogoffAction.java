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

import net.sf.jboard.struts.fwk.actions.JboardBaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.jboard.filters.Constants;

/**
 * remove to the session the user <strong>subject</strong> object;
 * and create a new 'guest' user one, because <strong>all the users</strong> must be authenticated.
 * 
 *
 * @author $author$
 * @version $Revision: 1.7 $
 */
public  class LogoffAction extends JboardBaseAction implements Constants{

    /**
     * .
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
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)  {

        System.out.println("start logoffaction");

        // Extract attributes we will need
        HttpSession session = request.getSession();

        // Log off this user 
        session.removeAttribute(Constants.SUBJECT_ID);

  

        return (mapping.findForward("logoffOK"));

    }

}


