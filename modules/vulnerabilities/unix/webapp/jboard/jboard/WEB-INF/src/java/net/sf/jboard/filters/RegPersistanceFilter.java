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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;
import net.sf.jboard.model.reg.dao.RegHibernateUtil;

/**
 * @author Charles GAY
 *
 */
public class RegPersistanceFilter implements Filter {

public static final String HIBERNATE_SESSION_FACTORY_PARAM ="hibernateSessionFactory";
private Log logger = LogFactory.getLog(ForumPersistanceFilter.class);

	/**
     * method called at webapp startup.
     * it initialize the Hibernate Session Factory.
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		
        String sessionFactory = filterConfig.getInitParameter(HIBERNATE_SESSION_FACTORY_PARAM);
        RegHibernateUtil.init(sessionFactory);

	}

	/**
     * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain)
		throws IOException, ServletException {
		
        
        try {
			RegHibernateUtil.openSession();
		} catch (HibernateException e) {
            logger.fatal(" persisitence session is not opened " + e.getMessage());
		}
        
        try{
          chain.doFilter(request,response);
        }finally{
            
            try {
				RegHibernateUtil.closeSession();
			} catch (HibernateException e1) {
                logger.fatal(" persisitence session is not closed " + e1.getMessage());
			}
        }
        

	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
