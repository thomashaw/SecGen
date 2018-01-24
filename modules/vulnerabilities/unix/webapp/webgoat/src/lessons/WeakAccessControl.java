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
public class WeakAccessControl extends LessonAdapter
{

	private final static String FILE = "File";


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
			ec.addElement(new P().addElement("Enter filename: "));
			String file = s.getParser().getRawParameter(FILE, "");

			Input input = new Input(Input.TEXT, FILE, file);
			ec.addElement(input);

			Element b = ECSFactory.makeButton("Go!");
			ec.addElement(b);

			if (file.length() > 0)
			{
				s.setMessage("Access allowed");
				if (file.indexOf("..") == -1)
				{
					ec.addElement(new P().addElement("Normal file contents"));
				}
				else
				{
					ec.addElement(new P().addElement("TOP SECRET file contents"));
				}
			}
			else
			{
				s.setMessage("Access denied");
			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}

		return (ec);
	}


	/**
	 *  Gets the hints attribute of the AccessControlScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("The server looks for files in c:\temp");
		hints.add("Most operating systems allow special characters in the path.");
		hints.add("Try ../topsecret.txt");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the AccessControlScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Weak Access Control", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the AccessControlScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(120);
	}


	/**
	 *  Gets the title attribute of the AccessControlScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Bypass a Weak Access Control Scheme");
	}


	/**
	 *  Gets the instructions attribute of the WeakAccessControl object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = ".";
		return (instructions);
	}

}

