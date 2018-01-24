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
package net.sf.jboard.model.forum.dao;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

/**
 * @author an hibernate contributor.
 * this class comes from the hibernate reference file.
 *
 */


public class ForumHibernateUtil {

    private static  SessionFactory sessionFactory;


    /**
    * Holds the current hibernate session.
    */
    private static ThreadLocal hibernateSessionHolder = new ThreadLocal(); 
    
    /**
     * 
     * @return boolean initialization result
     */
    public static void init(String hibernateConfigFile){
        try {
               sessionFactory = new Configuration().configure(hibernateConfigFile).buildSessionFactory();
        } catch (HibernateException ex) {
            throw new RuntimeException("Exception building SessionFactory: " + ex.getMessage(), ex);
        }
        
    }
    
    /**
     * retrieve the hibernate current Session.
     * @return Session hibernate Session
     * @throws HibernateException
     */
    public static Session currentSession() throws HibernateException {
          
        Session s = (Session) hibernateSessionHolder.get();
        
        return s;
    }
    /**
     * open the hibernate session.
     * @throws HibernateException
     */
    public static void openSession()throws HibernateException{
        
        hibernateSessionHolder.set(sessionFactory.openSession());
    }
    /**
     * close the hibernate current session.
     * @throws HibernateException
     */
    public static void closeSession() throws HibernateException {
        Session s = (Session) hibernateSessionHolder.get();
        
        hibernateSessionHolder.set(null);
        if (s != null){
           
            s.close();
        }
    }
}