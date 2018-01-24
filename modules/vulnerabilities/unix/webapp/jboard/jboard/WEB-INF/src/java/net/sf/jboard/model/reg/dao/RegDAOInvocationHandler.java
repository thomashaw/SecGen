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
package net.sf.jboard.model.reg.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

/**
 * @author Charles GAY
 *
 */
public class RegDAOInvocationHandler implements InvocationHandler{

    private RegDAOInterface regDaoInterface;

        /**
         * constructor.
         * @param fdi
         */
        public RegDAOInvocationHandler(RegDAOInterface rdi){
            regDaoInterface = rdi;
        
        }

	/**
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     * @param proxy
     * @param method
     * @param args
     * @return Object
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        Transaction tx;
        
        //begin transaction
        RegHibernateUtil.openSession();
        Session session = RegHibernateUtil.currentSession();
        
        tx = session.beginTransaction();
        regDaoInterface.setSession(session);
        
        try {
               
            result = method.invoke(regDaoInterface, args);
              
            tx.commit();
            
            
        } catch (InvocationTargetException ite) {
            tx.rollback();
                throw ite.getTargetException();
        }catch(Exception e){
            tx.rollback();
            e.printStackTrace();
            throw e;
            
        }finally{
          //close the hibernate session
          RegHibernateUtil.closeSession();
            
        }
        return result;
	}

}
