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
public class UncheckedEmail extends LessonAdapter
{

	private final static String MESSAGE = "msg";
	private final static String TO = "to";


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		ElementContainer ec = new ElementContainer();
		Table table = new Table();
		try
		{

			String to = s.getParser().getRawParameter(TO, "");
			TR tr = new TR();
			tr.addElement(new TD().setAlign("RIGHT").addElement(ECSFactory.makeInput("TO: ", Input.TEXT, TO, to)));
			tr.addElement(new TD().setRowSpan(2).setAlign("LEFT").setVAlign("MIDDLE").addElement(ECSFactory.makeButton("Send!")));
			table.addElement(tr);

			tr = new TR();
			String message = s.getParser().getRawParameter(MESSAGE, "");
			tr.addElement(new TD().setAlign("RIGHT").addElement(ECSFactory.makeInput("Message: ", Input.TEXT, MESSAGE, convertMetachars(message))));
			table.addElement(tr);

			ec.addElement(table);
			ec.addElement(exec("cmd.exe /c sendmail " + to));

			if (to.length() > 0)
			{
				ec.addElement(new HR());
				ec.addElement(new Center().addElement(new B().addElement("You sent the following message to: " + to)));
				ec.addElement(new BR());
				ec.addElement(new StringElement(message));
			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			e.printStackTrace();
		}
		return (ec);
	}


// c:\winnt\system32\cmd.exe findstr /S /I password c:\
// c:\winnt\system32\cmd.exe /c dir & ipconfig \all
// c:\winnt\system32\command.exe /C dir & ipconfig \all


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
	 *  Gets the hints attribute of the EmailScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("Try sending an anonymous message to yourself.");
		hints.add("Try sending the message to multiple users");
		hints.add("Try sending fake obnoxious content");
		hints.add("Try inserting some html or javascript code in the message field");
		hints.add("Insert <A href=\"http://www.google.com\">Click here for Google\"</A> in the message field");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the EmailScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Unchecked Email", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the EmailScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(50);
	}


	/**
	 *  Gets the title attribute of the EmailScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Exploit Unchecked Email");
	}


	/**
	 *  Gets the instructions attribute of the UncheckedEmail object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Send your friend a message from our site!";
		return (instructions);
	}

}

