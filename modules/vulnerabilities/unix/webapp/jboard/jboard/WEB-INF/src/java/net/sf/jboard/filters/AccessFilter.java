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
package net.sf.jboard.filters;

import java.io.IOException;


import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jboard.model.reg.clients.RegFacadeClient;


/**
 * this Filter handle the control access of all request.
 * 
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class AccessFilter implements Filter,Constants{


    
	/**
     * Filter initialization.
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterCfg) throws ServletException {
		
        
	}

	/**
     * the method which handle the request.
     * this method was inspired by the article on jaas published at <a href="http://www.mooreds.com/jaas.html">this</a> address.
     * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        HttpSession session = req.getSession();
        String login = null; 
        String password = null;
        RegFacadeClient auth = null;
        Subject subject = null;

        boolean authenticationResult = false;
        HttpSession sess;
        
        subject = (Subject)session.getAttribute(SUBJECT_ID);
        
        
        
        if(subject == null || req.getRequestURI().equals("/jboard/reg/LogonProcess.do") ){
            login = req.getParameter("login");
            password = req.getParameter("password");
            //all forum users must be authenticated
            //unless when the user send a wrong  login or/and password
            //=> he is redirected to the logonPage
            if(login==null || password == null ){
               login ="guest";
               password ="guest"; 
            }
                //try to authenticate
                auth = new RegFacadeClient();
                authenticationResult = auth.authenticate(login,password);
            
            //redirect to login page
            // because user is not authenticated
            if(!authenticationResult){
               
               res.sendRedirect("/jboard/SwitchToModule.do?prefix=/reg&page=/Logon.do&authenticationFailed=authentication%20Failed");
            }else{
                sess = req.getSession();
                //store the Subject in the session 
                sess.setAttribute(SUBJECT_ID,auth.getSubject());
                res.sendRedirect("/jboard/index.jsp");
            }
            
        }
        
            //user is authenticated 
            chain.doFilter(request,response);
            
            
            
            //@TODO implements authorizations
            /*if(AuthUtils.permitted(subject,permission)){
                //redirect to loginpage because user is not authorized
                //to access to this resource
                res.sendRedirect(logonPage);
            }else{
                // user is authorized
                chain.doFilter(request,response);
            }*/
        
        
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	
		
	}

}
