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
package net.sf.jboard.test.cactus;

import junit.framework.*;
import org.apache.cactus.ServletTestCase;
/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class TestSampleServlet extends ServletTestCase{

	/**
	 * Constructor.
	 */
	public TestSampleServlet() {
		super();

	}
	public TestSampleServlet(String theName)
	{
		super(theName);
	}

	public static void main(String[] theArgs)
	{
		junit.swingui.TestRunner.main(new String[] {TestSampleServlet.class.getName()});
	}

	public static Test suite()
	{
		return new TestSuite(TestSampleServlet.class);
	}

}
