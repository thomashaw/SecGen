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

/**
 * @author  Artur Konrad Kulinski, Sony Poland IS Department
 *
 */
public class URLPermission extends java.security.Permission {
    
    /** Creates a new instance of URLPermission */
    public URLPermission(String name) {
        super(name);
    }
    
    public void checkGuard(Object object) {
    }
    
    public boolean equals(Object obj) {
        if((obj instanceof URLPermission) && ((URLPermission)obj).getName().equals(this.getName())){
        
            return true;
        } 
            return false;
    }
    
    public String getActions() {
        //we do not have any actions int this class;just allow or deny acces to URL
        return "";
    }
    
    public int hashCode() {
        return this.getName().hashCode(); //if we have any actions it should be: getName().hashCode() ^ acion_mask
    }
    
    public boolean implies(java.security.Permission permission) {
        if(!(permission instanceof URLPermission))
            return false;        
        String thisName = this.getName();
        String permName = permission.getName();
        if(this.getName().equals("*")) 
            return true;
        if(thisName.endsWith("*") && permName.startsWith(thisName.substring(0, thisName.lastIndexOf("*")))) {
            return true;
        }        
        if(thisName.equals(permName))
            return true;
        return false;            
    }
    
    public java.security.PermissionCollection newPermissionCollection() {
        return null;
    }
    
    public String toString() {
        return this.getClass().getName()+","+this.getName();
    }
    
    public class InnerClass extends java.security.PermissionCollection {
        
        private java.util.HashMap permissions = new java.util.HashMap();
        
        public void add(java.security.Permission permission) {
            //required by API 
            if(isReadOnly()) 
                throw new IllegalArgumentException("Read only collection !");
            //must be homogenous collection
            if(!(permission instanceof URLPermission)) 
                throw new IllegalArgumentException("Wrong type of permission !");
            //if egzists do nothin
            if(permissions.get(permission.getName())!=null)
                return;
            //if there is no ALLOW ALL permission in set 
            if(permissions.get("*") == null) {
                if(permission.getName().equals("*")) {
                    permissions.clear();
                    permissions.put(permission.getName(), permission);                    
                    return;
                }
                //if adding wildcard remove all weaker permissions
                if(permission.getActions().endsWith("*")) {
                    String wildcarded = permission.getName().substring(0, permission.getName().lastIndexOf("*"));
                    java.util.ArrayList toBeRemoved = new java.util.ArrayList();
                    java.util.Iterator iter = permissions.keySet().iterator();
                    while(iter.hasNext()){
                        String key = (String)iter.next();
                        String permName = ((URLPermission)permissions.get(key)).getName();
                        if(permName.startsWith(wildcarded)) {
                            toBeRemoved.add(permName);
                        }
                    }
                    for(int i=0; i<toBeRemoved.size(); i++) {
                        permissions.remove(toBeRemoved.get(i));
                    }                    
                    permissions.put(permission.getName(), permission);
                    toBeRemoved = null;
                    return;
                }
                //if permission without wildcard, look first for wildcarded if not found - add else skip
                java.util.Iterator iter = permissions.keySet().iterator();
                while(iter.hasNext()) {
                    String key = (String)iter.next();
                    if(key.endsWith("*")){
                        String wildcarded = key.substring(0, key.lastIndexOf("*"));                    
                        if(permission.getName().startsWith(wildcarded))
                            return;
                    }
                }
                permissions.put(permission.getName(), permission);
            }            
        }
        
        public java.util.Enumeration elements() {            
            //must use Hashtable becouse of old Collection framework used in this API
            java.util.Hashtable wrapper = new java.util.Hashtable(permissions);
            return wrapper.elements();
        }
        
        public boolean implies(java.security.Permission permission) {
            if(!(permission instanceof URLPermission))
                return false;        
            if(permissions.get("*") != null) 
                return true;
            if(permissions.get(permission.getName())!=null)
                return ((java.security.Permission )permissions.get(permission.getName())).implies(permission);
            java.util.Iterator iter = permissions.keySet().iterator();
            while(iter.hasNext()) {
                String key = (String)iter.next();
                if(key.endsWith("*")){
                    String wildcarded = key.substring(0, key.lastIndexOf("*"));                
                    if(permission.getName().startsWith(wildcarded))
                        return ((java.security.Permission)permissions.get(key)).implies(permission);
                }
            }
            return false;
        }        
    }        
    
}