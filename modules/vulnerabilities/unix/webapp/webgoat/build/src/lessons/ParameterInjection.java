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
public class ParameterInjection extends LessonAdapter
{

	private final static String DIR = "dir";


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
			ec.addElement(new StringElement("Enter path name: "));

			String dir = s.getParser().getRawParameter(DIR, "");
			Input input = new Input(Input.TEXT, DIR, dir.toString());
			ec.addElement(input);

			Element b = ECSFactory.makeButton("Execute!");
			ec.addElement(b);

			ec.addElement(exec("cmd.exe /c dir /b " + dir));
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}
		return (ec);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  command  Description of the Parameter
	 * @return          Description of the Return Value
	 */
	private Element exec(String command)
	{
		ExecResults er = Exec.execSimple(command);
		PRE p = new PRE().addElement(er.toString());
		return (p);
	}


	/**
	 *  Gets the hints attribute of the DirectoryScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("The application is taking your input and appending it to a system command.");
		hints.add("The ampersand(&) separates commands in the Windows 2000 command shell. In Unix the separator is typically a semi-colon(;)");
		hints.add("Try entering \'blah & netstat -a & ipconfig\'.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the DirectoryScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Parameter Injection", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the DirectoryScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(40);
	}


	/**
	 *  Gets the title attribute of the DirectoryScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Perform Parameter Injection");
	}


	/**
	 *  Gets the instructions attribute of the ParameterInjection object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Enter the pathname of the directory you want to list.";
		return (instructions);
	}

}

