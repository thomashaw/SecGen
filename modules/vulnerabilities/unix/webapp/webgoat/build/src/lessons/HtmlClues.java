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
public class HtmlClues extends FailOpenAuthentication
{

	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	private boolean backdoor(WebSession s)
	{
		String username = s.getParser().getRawParameter(USERNAME, "");
		String password = s.getParser().getRawParameter(PASSWORD, "");
		return (username.equals("admin") && password.equals("adminpw"));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		ElementContainer ec = new ElementContainer();
		try
		{
			ec.addElement(new Comment("FIXME admin:adminpw"));
			ec.addElement(new Comment("Use Admin to regenerate database"));

			if (backdoor(s))
			{
				s.setMessage("BINGO -- admin authenticated");
				ec.addElement(super.makeUser(s, "jblow", "PARAMETERS"));
			}
			else
			{
				ec.addElement(super.createContent(s));
			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
		}
		return (ec);
	}


	/**
	 *  Gets the hints attribute of the CluesScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("You can view the HTML source by selecting 'view source' in the browser menu.");
		hints.add("There are lots of clues in the HTML");
		hints.add("Search for the word HIDDEN, look at URLs, look for comments.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the CluesScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("HTML Clues", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the CluesScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(30);
	}


	/**
	 *  Gets the title attribute of the CluesScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Discover Clues in the HTML");
	}


	/**
	 *  Gets the instructions attribute of the HtmlClues object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Please enter a username and password to login.";
		return (instructions);
	}

}

