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
import javax.security.auth.login.LoginException;

import net.sf.jboard.fwk.exception.DAOException;
import net.sf.jboard.model.reg.dao.RegDAOFactory;
import net.sf.jboard.model.reg.dao.RegDAOInterface;
import net.sf.jboard.model.reg.dto.User;
import net.sf.jboard.security.PassiveCallbackHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tagish.auth.Utils;

/**
 * Entry Point for the Authentification Mechanism.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class RegAuthenticationClient extends RegBaseClient{

    private static Log logger = LogFactory.getLog(RegAuthenticationClient.class);

    private LoginContext loginContext;
   public RegAuthenticationClient(){
    
 
   }
   /**
    * @return authentification state. true if the user has been authenticated succesfully.
    */
   public LoginContext authenticate(String username,String password) {
    
       try{
           //if we want to have a subject with special credentials (public and private)
           //we should use another LoginContext constructor
           //or add some credentials with  loginModules (a better way)
           loginContext = new LoginContext("jboard",new PassiveCallbackHandler(username,password));
           
       }catch(LoginException le) {
             logger.error("loginContext cannot be created.loginException "+le.getMessage());
             System.err.println("LoginContext cannot be created. "+ le.getMessage());
             return null;
       
       } catch(SecurityException se) {
             logger.error("loginContext cannot be created.SecurityException "+se.getMessage());
             se.printStackTrace();
             
             System.err.println("LoginContext cannot be created. "+ se.getMessage());
             return null;
       }
       
       try{
           loginContext.login();
           return loginContext;
       }catch(LoginException le){
           //authentication failed
           logger.error("authentication failed.LoginException "+le.getMessage());
           System.err.println("authentication failed.LoginException "+ le.getMessage());
           return null;
       }
       
   }
   
   /**
    * retrieve the subject from the loginContext;
    * @return authenticated Subject.
    */
   public Subject getSubject(){
       
       return loginContext.getSubject();
   }
   
   /**
    * register the User Object which handle registration parameters like pseudonyl, icq identification number etc...
    * @param user
    * @return registerProcess result.
    */
   public boolean register(User user){
       
       boolean alreadyPresent;
       RegDAOInterface rdi = RegDAOFactory.getDAO();
       try {
           alreadyPresent  = rdi.userAlreadyExists(user);
          if(!alreadyPresent){
           user.setPassword(String.valueOf(Utils.cryptPassword(user.getPassword().toCharArray())));
           rdi.register(user);
           
           return true;
          }else{
           return false;
          }
        } catch (DAOException e) {
             logger.warn(" registration failed ");
             return false;
        }catch(Exception ex){
            logger.warn(" crypting failed ");
            return false;
        }
       
   }

}
