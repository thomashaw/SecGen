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
public class HiddenFieldTampering extends LessonAdapter
{

	/**
	 *  Constructor for the HiddenFieldScreen object
	 */
	public HiddenFieldTampering() { }


	private final static String PRICE = "Price";


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
			String price = s.getParser().getRawParameter(PRICE, "");

			if (price.length() == 0)
			{
				ec.addElement(new P().addElement("Confirm purchase: "));
				ec.addElement(new B("46 inch HDTV (model KTV-551)"));
				Input input = new Input(Input.HIDDEN, PRICE, "4999.99");
				ec.addElement(input);

				ec.addElement(new BR());
				Element b = ECSFactory.makeButton("Purchase");
				ec.addElement(b);
			}
			else
			{
				ec.addElement(new P().addElement("Your total price is:"));
				ec.addElement(new B("$" + price.toString()));
				ec.addElement(new BR());

				ec.addElement(new P().addElement("This amount will be charged to your credit card immediately."));
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
	 *  Gets the hints attribute of the HiddenFieldScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("This application is using hidden fields to transmit price information to the server.");
		hints.add("Use a program to intercept and change the value in the hidden field.");
		hints.add("Use Achilles to change the price of the TV from 4999.99 to 9.99.");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the HiddenFieldScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		return makeMenuItem("Hidden Field Tampering", WebSession.SCREEN, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the HiddenFieldScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(110);
	}


	/**
	 *  Gets the title attribute of the HiddenFieldScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("How to Exploit Hidden Fields");
	}


	/**
	 *  Gets the instructions attribute of the HiddenFieldTampering object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Click below to confirm your purchase.";
		return (instructions);
	}

}

