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
package net.sf.jboard.model.forum.clients;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.jboard.model.forum.dao.ForumHibernateUtil;

/**
 * InvocationHandler to manage <strong>transaction</strong> during facade method calls by the view tiers.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class ForumFacadeInvocationHandler implements InvocationHandler{

    private ForumFacadeClient ffc;
    
    /**
     * constructor.
     * @param facade
     */
    public  ForumFacadeInvocationHandler(ForumFacadeClient facade){
        
        super();
        ffc = facade;
    }



    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try {
              //open the hibernate session
              ForumHibernateUtil.openSession();
               
              result = method.invoke(ffc, args);
        } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
        }finally{
               //close the hibernate session
               ForumHibernateUtil.closeSession();
            
        }
        return result;
    }


}
