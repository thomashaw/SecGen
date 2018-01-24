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
package net.sf.jboard.security;

import java.security.AccessControlException;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * this class was inspired by the article on jaas published at <a href="http://www.mooreds.com/jaas.html">this</a> address.
 *
 */
public class AuthUtils {
       
       static public boolean permitted(Subject subj, final Permission p) {
          final SecurityManager sm;
          if (System.getSecurityManager() == null) {
              sm = new SecurityManager();
          } else {
              sm = System.getSecurityManager();
          }
          try {
              Subject.doAsPrivileged(subj, new PrivilegedExceptionAction() {
                  public Object run() { 
                      sm.checkPermission(p);
                      // the 'null' tells the SecurityManager to consider this resource access
                      //in an isolated context, ignoring the permissions of code currently
                      //on the execution stack.
                      return null;
                  }
                  },null);
              return true;
          } catch (AccessControlException ace) {
              return false;
          } catch (PrivilegedActionException pae) {
              return false;
          }
      }
}