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


/**
 * a factory for the unique access point for the business part.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class ForumFacadeClientFactory {
    
   public static ForumFacadeInterface getForumFacadeClient(){
       
       ForumFacadeInterface ffi = new ForumFacadeClient();
       //ForumFacadeInvocationHandler ffih = new ForumFacadeInvocationHandler(new ForumFacadeClient());
       /*ForumFacadeInterface ffi =  (ForumFacadeInterface) Proxy.newProxyInstance(ForumFacadeClient.class.getClassLoader(),
                                                            new Class[] { ForumFacadeInterface.class },
                                                            ffih);*/
       
        return ffi;
    }

}
