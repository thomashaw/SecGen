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

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.3 $
 */
public final class Constants {

    /** The package name for this application. */
    public static final String Package = "net.sf.jboard";

    /**
     * The session scope attribute under which the Username for the currently
     * logged in user is stored.
     */
    public static final String USER_KEY = "user";

    /** The token that represents a nominal outcome in an ActionForward. */
    public static final String SUCCESS = "success";

    /** The token that represents the logon activity in an ActionForward. */
    public static final String LOGON = "logon";

    /** The token that represents the welcome activity in an ActionForward. */
    public static final String WELCOME = "welcome";

    /** The value to indicate debug logging. */
    public static final int DEBUG = 1;

    /** The value to indicate normal logging. */
    public static final int NORMAL = 0;

}
