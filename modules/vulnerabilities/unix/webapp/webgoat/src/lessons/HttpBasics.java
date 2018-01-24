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
public class HttpBasics extends LessonAdapter
{

	private final static String PERSON = "person";



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
			ec.addElement(new StringElement("Enter your name: "));

			StringBuffer person = new StringBuffer(s.getParser().getStringParameter(PERSON, ""));
			person.reverse();

			Input input = new Input(Input.TEXT, PERSON, person.toString());
			ec.addElement(input);

			Element b = ECSFactory.makeButton("Go!");
			ec.addElement(b);
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}
		return (ec);
	}



	/**
	 *  Gets the hints attribute of the HelloScreen object
	 *
	 * @return    The hints value
	 */
	public List getHints()
	{
		List hints = new ArrayList();
		hints.add("Type in your name and press 'go'");
		hints.add("Turn on Show Parameters or other features and press 'apply clues'");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the HelloScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Http Basics", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the HelloScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(10);
	}


	/**
	 *  Gets the title attribute of the HelloScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("Http Basics");
	}

}

