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
package net.sf.jboard.fwk.exception;




/**
 * This is the common superclass for all application exceptions. This class and
 * its subclasses support the chained exception facility that allows a root
 * cause Throwable to be wrapped by this class or one of its descendants. This
 * class also supports multiple exceptions via the exceptionList field.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.3 $
 */
public class BaseException extends Exception {

   

    /**
     * Creates a new BaseException object.
     *
     * @param rootCause the rootCause of the exception
     */
    public BaseException(String arg) {

        super(arg);

    }
	

}
