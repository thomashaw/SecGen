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

package net.sf.jboard.model.reg.clients;

import javax.security.auth.Subject;

import javax.security.auth.login.LoginContext;

import net.sf.jboard.model.reg.dto.User;

/**
 * <strong>unique</strong> EntryPoint of the Forum Business part.
 * it regroups all the business Registration uses case in one location to reduce couplage
 * between the presentation and the business tiers.
 * it is the implementation of the <strong>facade</strong> design pattern.
 * it is the only <strong>public</strong>class in this package to reduce couplage 
 * with the presentation tiers.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class RegFacadeClient {

    private RegAuthenticationClient regAuth; 
    private LoginContext loginContext;
    
    /**
     * constructor.
     *
     */
    public RegFacadeClient(){
        
         regAuth = new RegAuthenticationClient();
    }
    
    
    /**
     * authenticate the user
     * @param username
     * @param password
     * @return result of the authentication process
     */
    public boolean authenticate(String username,String password) {
        

        RegAuthenticationClient regAuth = new RegAuthenticationClient();
        loginContext = regAuth.authenticate(username,password);
        if(loginContext != null){
            return true; 
        }
        return false;
        
    }

    /**
     * return the subject creatd by the loginModule.
     * @return Subject
     */
    public Subject getSubject() {
        
        return loginContext.getSubject();
        
    }
    /**
     * register the user in the authentication Datasource.
     * @return register Process result.
     */
    public boolean register(User user){
        
        return regAuth.register(user);
    }
}
