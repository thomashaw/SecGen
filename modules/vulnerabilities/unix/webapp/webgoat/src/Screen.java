import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

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
public abstract class Screen
{

	/**
	 *  Description of the Field
	 */
	public static int MAIN_SIZE = 680;
	/**
	 *  Description of the Field
	 */
	public static int MENU_SIZE = 185;
	/**
	 *  Description of the Field
	 */
	public static int SCREEN_SIZE = MENU_SIZE + MAIN_SIZE;

	private Head head;
	private Html html;

	final static IMG logo = new IMG("images/aspectlogo-horizontal-small.jpg").setAlt("Aspect Security").setBorder(0).setHspace(0).setVspace(0);
	final static IMG logoWebGoat = new IMG("images/webgoat.gif").setWidth(MENU_SIZE - 5).setAlt("WebGoat").setBorder(0).setHspace(0).setVspace(0);


	/**
	 *  Constructor for the Screen object
	 */
	public Screen() { }


	/**
	 *  Constructor for the Screen object
	 *
	 * @param  s  Description of the Parameter
	 */
	public Screen(WebSession s)
	{
		setup(s);
	}



	/**
	 *  Description of the Method
	 *
	 * @param  token  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	protected static String convertMetachars(String token)
	{
		String result = token;

		int mci = 0;

		int mcLocation = 0;
		/*
		 *  meta char array
		 */
		char[] metaChar =
				{
				'&',
				'<',
				'>',
				'"',
				'\t',
				' ',
				'\n'
				};

		String[] htmlCode =
				{
				"&amp;",
				"&lt;",
				"&gt;",
				"&quot;",
				"    ",
				"&nbsp;",
				"<br>"
				};

		for (; mci < metaChar.length; mci += 1)
		{
			mcLocation = 0;

			while ((mcLocation = result.indexOf(metaChar[mci], mcLocation)) > -1)
			{
				result = result.substring(0, mcLocation) + htmlCode[mci] + result.substring(mcLocation + 1);
				mcLocation += 1;
			}
		}
		return (result);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected abstract Element createContent(WebSession s);


	/**
	 *  Gets the head attribute of the Screen object
	 *
	 * @return    The head value
	 */
	protected Head getHead()
	{
		return head;
	}



	/**
	 *  Fill in a descriptive title for this lesson
	 *
	 * @return    The title value
	 */
	protected abstract String getTitle();



	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	protected Element makeCSS()
	{
		StringElement css = new StringElement();

		css.addElement("<style type=\"text/css\">");
		css.addElement("label { font-size: 10pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("input { font-size: 8pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("button { font-size: 8pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("select { font-size: 8pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("textarea { font-size: 8pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("body { font-size: 10pt; color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("h1 { font-size: 14pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("h2 { font-size: 12pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("table { font-size: 10pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement("div { font-size: 10pt;  color:#000088; font-family: Arial, Verdana, Helvetica }");
		css.addElement(".menu {font-family:Verdana; color:black; font-size:7pt; font-weight:bold }");
		css.addElement(".submenu {font-family:Verdana; color:darkred; font-size:7pt; font-weight:bold }");
		css.addElement("</style>");

		return (css);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeContentHeader(WebSession s)
	{
		ElementContainer ec = new ElementContainer();
		return (ec);
	}



	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	protected Element makeFooter()
	{
		P p = new P().addElement("Sponsored by Aspect Security.");

		ElementContainer ec = new ElementContainer();
		ec.addElement(new Center().addElement(p));
		ec.addElement(new P());

		return (ec);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeLeft(WebSession s)
	{
		Table t = new Table().setWidth("100%").setCellSpacing(0).setCellPadding(0).setBorder(0);
		return (t);
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	protected Element makeLogo()
	{
		return (logo);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	protected TD makeMenuCategory(String text)
	{
		return (new TD().setWidth("100%").addElement(new Font().setColor(HtmlColor.WHITE).addElement(new B().addElement(text))));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text   Description of the Parameter
	 * @param  name   Description of the Parameter
	 * @param  value  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	protected TD makeMenuItem(String text, String name, String value)
	{
		Element link = ECSFactory.makeLink(new Font().setColor(HtmlColor.WHITE).addElement(text).toString(), name, value);
		TD cell = new TD().setWidth("100%");
		cell.addElement(new Font().setColor(HtmlColor.WHITE).addElement("-"));
		cell.addElement(link);
		TR row = new TR().addElement(cell);
		return (cell);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text   Description of the Parameter
	 * @param  name   Description of the Parameter
	 * @param  value  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	protected TD makeMenuItem(String text, String name, boolean value)
	{
		return (makeMenuItem(text, name, new Boolean(value).toString()));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text   Description of the Parameter
	 * @param  name   Description of the Parameter
	 * @param  value  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	protected TD makeMenuItem(String text, String name, int value)
	{
		return (makeMenuItem(text, name, Integer.toString(value)));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  height  Description of the Parameter
	 * @param  width   Description of the Parameter
	 * @return         Description of the Return Value
	 */
	protected TD makeMenuPad(int height, int width)
	{
		TD td = new TD();
		if (height > 0)
		{
			td.setHeight(height);
		}
		if (width > 0)
		{
			td.setWidth(width);
		}
		return (td);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  height  Description of the Parameter
	 * @param  width   Description of the Parameter
	 * @return         Description of the Return Value
	 */
	protected TR makeMenuSpacer(int height, int width)
	{
		TD td = new TD();
		if (height > 0)
		{
			td.setHeight(height);
		}
		if (width > 0)
		{
			td.setWidth(width);
		}
		TR row = new TR().addElement(td);
		return (row);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeMessages(WebSession s)
	{
		if (s == null)
		{
			return (new StringElement(""));
		}
		Font f = new Font().setColor(HtmlColor.RED);
		String message = s.getMessage();
		f.addElement(message);
		return (f);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  title  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	protected Element makeTop(String title)
	{
		return (new H1(title));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  out  Description of the Parameter
	 */
	public void output(PrintWriter out)
	{
		// format output -- then send to printwriter
		// otherwise we're doing way too much SSL encryption work
		out.print(html.toString());
	}



	/**
	 *  Description of the Method
	 *
	 * @param  x  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected static String pad(int x)
	{
		StringBuffer sb = new StringBuffer();
		if (x < 10)
		{
			sb.append(" ");
		}
		if (x < 100)
		{
			sb.append(" ");
		}
		sb.append(x);
		return (sb.toString());
	}



	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 */
	public void setup(WebSession s)
	{
		head = new Head();
		head.addElement(new Title(getTitle()));
		head.addElement(new StringElement("<meta name=\"Author\" content=\"Jeff Williams\">"));
		head.addElement(new StringElement("<link rev=\"made\" href=\"mailto:jeff.williams@aspectsecurity.com\">"));
		head.addElement(makeCSS());

		// call createContent first so messages will go somewhere
		Form form = new Form("attack", Form.POST).setName("form").setEncType("");
		form.addElement(wrapForm(s));
		TD lowerleft = new TD().setHeight("100%").setWidth(MENU_SIZE).setBgColor("#000066").setVAlign("top").setAlign("center").addElement(makeLeft(s));
		TD lowerright = new TD().setHeight("100%").setVAlign("top").setWidth(MAIN_SIZE).setAlign("left").addElement(form);
		TD upperright = new TD().setWidth(MAIN_SIZE).setVAlign("top").setAlign("center").addElement(makeTop(getTitle()));
		TD upperleft = new TD().setWidth(MENU_SIZE).setBgColor("#000066").setVAlign("top").setAlign("center").addElement(new Font().setColor(HtmlColor.WHITE).addElement(logoWebGoat));
		TD ourSpaceLowerLeft = new TD().setHeight(100).setWidth(MENU_SIZE).setBgColor("#000066").setVAlign("center").setAlign("center").addElement("&nbsp");
		TD ourSpaceLowerRight = new TD().setVAlign("center").setAlign("center").addElement(makeFooter()).addElement(makeLogo());

		TR row1 = new TR().addElement(upperleft).addElement(upperright);
		TR row2 = new TR().addElement(lowerleft).addElement(lowerright);
		TR row3 = new TR().addElement(ourSpaceLowerLeft).addElement(ourSpaceLowerRight);

		Table layout = new Table().setHeight("100%").setBgColor(HtmlColor.WHITE).setWidth(SCREEN_SIZE).setCellSpacing(0).setCellPadding(0).setBorder(0);
		if (s.isColor())
		{
			layout.setBgColor(HtmlColor.RED);
			layout.setBorder(1);
		}
		layout.addElement(row1);
		layout.addElement(row2);
		layout.addElement(row3);

		Body body = new Body();
		body.addAttribute("leftmargin", "0");
		body.addAttribute("topmargin", "0");
		body.addAttribute("bottommargin", "0");
		body.addAttribute("rightmargin", "0");
		body.addAttribute("marginheight", "0");
		body.addAttribute("marginwidth", "0");
		body.setBgColor("#FFFFFF");
		body.setBackground("images/back2.GIF");
		body.setLink("#67001D");
		body.setVlink("#67001D");
		body.setAlink("#DF0031");
		body.setText("#000000");

		body.addElement(layout);

		html = new Html();
		html.addElement(head);
		html.addElement(body);

		// FIXME:  This should move to Lesson
		String htmlcode = form.toString();
		if (s.showHtml())
		{
			lowerright.addElement(new HR());
			String code = this.convertMetachars(htmlcode);
			Element codeElement = new StringElement(AbstractLesson.readFromFile(new BufferedReader(new StringReader(code)), true));
			lowerright.addElement(codeElement);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element wrapForm(WebSession s)
	{
		if (s == null)
		{
			return new StringElement("Invalid Session");
		}

		Table container = new Table().setWidth("100%").setCellSpacing(10).setCellPadding(0).setBorder(0);
		if (s.isColor())
		{
			container.setBorder(0);
		}
		container.addElement(new TR().addElement(new TD().addElement(makeContentHeader(s))));
		container.addElement(new TR().addElement(new TD().addElement(makeMessages(s))));
		container.addElement(new TR().addElement(new TD().addElement(createContent(s))));
		container.addElement(new TR());
		return (container);
	}
}

