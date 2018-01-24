import java.io.*;
import java.util.*;
import javax.servlet.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of the Open Web
 *  Application Security Project (http://www.owasp.org) This software package is published by OWASP
 *  under the GPL. You should read and accept the LICENSE before you use, modify and/or redistribute
 *  this software.
 *
 * @author     jwilliams@aspectsecurity.com
 * @created    November 6, 2002
 */
public class WelcomeScreen extends Screen
{
	/**
	 *  Constructor for the WelcomeScreen object
	 *
	 * @param  s  Description of the Parameter
	 */
	public WelcomeScreen(WebSession s)
	{
		setup(s);
	}


	/**
	 *  Constructor for the WelcomeScreen object
	 */
	public WelcomeScreen() { }


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		ElementContainer ec = new ElementContainer();

		Element b = ECSFactory.makeButton("Start the Course!");
		ec.addElement(new Center(b));

		return (ec);
	}


	/**
	 *  Gets the title attribute of the WelcomeScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("Welcome to the Penetration Testing Course");
	}


	/**
	 *  Gets the instructions attribute of the WelcomeScreen object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Enter your name and learn how HTTP really works!";
		return (instructions);
	}

}

