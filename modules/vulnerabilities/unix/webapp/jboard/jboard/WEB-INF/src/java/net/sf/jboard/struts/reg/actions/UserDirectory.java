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
package net.sf.jboard.struts.reg.actions;

import java.io.FileOutputStream;

/**
 */
import java.io.IOException;

import java.util.Enumeration;
import java.util.Properties;


/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.3 $
 */
public class UserDirectory {

    /**
                                         *
                                         */
    private static final String UserDirectoryFile = "resources/users.properties";

    /**
                                         *
                                         */
    private static final String UserDirectoryHeader = "${user}=${password}";

    /**
                                         *
                                         */
    private static UserDirectory userDirectory = null;

    /**
                                         *
                                         */
    private static Properties p;

    /**
                                         *
                                         */
    private UserDirectory() throws UserDirectoryException {

        System.out.println("dans constructeur");

        java.io.InputStream i = null;

        p = null;
        i = this.getClass().getClassLoader().getResourceAsStream(UserDirectoryFile);

        if(null == i) {

            throw new UserDirectoryException();

        } else {

            try {

                p = new Properties();
                p.load(i);
                i.close();

            } catch(java.io.IOException e) {

                p = null;
                System.out.println(e.getMessage());
                throw new UserDirectoryException();

            } finally {

                i = null;

            }

        }

        // end else
    }

    // end UserDirectory

    /**
                                         *
                                         */
    public static UserDirectory getInstance() throws UserDirectoryException {

        System.out.println("dans getinstance");

        if(null == userDirectory) {

            System.out.println("userDirectory est null creation instance");
            userDirectory = new UserDirectory();

        }

        return userDirectory;

    }

    /**
     * Transform id so that it will match any conventions used by user
     * directory. The default implementation forces the id to uppercase. Does
     * <b>not</b> expect the userId to be null and will throw a NPE if it is.
     *
     * @param userId DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String fixId(String userId) {

        return userId.toUpperCase();

    }

    /**
                                         *
                                         */
    public boolean isValidPassword(String userId, String password) {

        System.out.println("user=" + userId);
        System.out.println("pwd=" + password);

        // no null passwords
        if(null == password)

            return false;

        // conform userId to uppercase
        String _userId = fixId(userId);

        // no passwords for non-users
        if(! isUserExist(_userId))

            return false;

        // does password match user's password
        return (password.equals(getPassword(_userId)));

    }

    /**
                                         *
                                         */
    public boolean isUserExist(String userId) {

        // no null users
        if(null == userId)

            return false;

        // if not null, it's a user
        return ! (null == p.getProperty(userId));

    }

    /**
                                         *
                                         */
    public String getPassword(String userId) {

        return p.getProperty(userId);

    }

    /**
                                         *
                                         */
    public Enumeration getUserIds() {

        return p.propertyNames();

    }

    /**
                                         *
                                         */
    public void setUser(String userId, String password) throws UserDirectoryException {

        // no nulls
        if((null == userId) || (null == password)) {

            throw new UserDirectoryException();

        }

        try {

            // conform userId to uppercase when stored
            p.put(fixId(userId), password);
            p.store(new FileOutputStream(UserDirectoryFile), UserDirectoryHeader);

        } catch(IOException e) {

            throw new UserDirectoryException();

        }

    }

}


// end UserDirectory
