import java.io.*;
import java.net.*;
import java.sql.*;
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
public class ChallengeScreen extends LessonAdapter
{

	/**
	 *  Description of the Field
	 */
	protected final static String CREDIT = "Credit";
	/**
	 *  Description of the Field
	 */
	protected final static String FILE = "File";
	/**
	 *  Description of the Field
	 */
	protected final static String MESSAGE = "Message";
	/**
	 *  Description of the Field
	 */
	protected final static String PARAM = "p";
	/**
	 *  Description of the Field
	 */
	protected final static String PASSWORD = "Password";
	/**
	 *  Description of the Field
	 */
	protected final static String USER = "s";
	/**
	 *  Description of the Field
	 */
	protected final static String USERNAME = "Username";

	private static Connection connection = null;

	private static String orig = null;
	private String pass = "goodbye";

	// store messages in a folder
	// pass them through the SSI filter
	// exec commands in the messages allows defacing
	// be creative!
	private Random random = new Random();

	private String user = "youaretheweakestlink";


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{
		try
		{
			int stage = s.getChallengeStage();
			switch (stage)
			{
				case 1:
					return (doStage1(s));
				case 2:
					return (doStage2(s));
				case 3:
					return (doStage3(s));
				case 4:
					return (doStage4(s));
				case 5:
					return (doStage5(s));
				case 6:
					return (doStage6(s));
				default:
					throw new Exception("Invalid stage");
			}
		}
		catch (Exception e)
		{
			s.setMessage("Error generating " + this.getClass().getName());
			System.out.println(e);
			e.printStackTrace();
		}
		return (new StringElement(""));
	}


	// get username and password
	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element doStage1(WebSession s)
		throws Exception
	{
		this.phoneHome(s, "User: " + user + " --> " + "Pass: " + pass);
		String username = s.getParser().getStringParameter(USERNAME, "");
		String password = s.getParser().getStringParameter(PASSWORD, "");
		if (username.equals(user) && password.equals(pass))
		{
			s.setMessage("Welcome to stage 2 -- get credit card numbers!");
			s.setChallengeStage(2);
			return (doStage2(s));
		}

		s.setMessage("Invalid login");

		StringElement label1 = new StringElement("User Name: ");
		StringElement label2 = new StringElement("Password: ");
		Input input1 = new Input(Input.TEXT, USERNAME, "");
		Input input2 = new Input(Input.PASSWORD, PASSWORD, "");
		Element b = ECSFactory.makeButton("Login");

		ElementContainer ec = new ElementContainer();
		ec.addElement(new P().addElement(label1).addElement(input1));
		ec.addElement(new P().addElement(label2).addElement(input2));
		ec.addElement(new P().addElement(b));
		Input input = new Input(Input.HIDDEN, USER, "101");
		ec.addElement(input);
		return (ec);
	}


	// get creditcards from database
	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element doStage2(WebSession s)
		throws Exception
	{
		if (connection == null)
		{
			connection = DatabaseUtilities.makeConnection(s);
		}
		Statement statement = connection.createStatement();

		String user = s.getParser().getRawParameter(USER, "");
		String query = "SELECT cc_type, cc_number from user_data WHERE userid = '" + user + "'";

		ResultSet results = statement.executeQuery(query);
		Vector v = new Vector();
		while (results.next())
		{
			String type = results.getString("cc_type");
			String num = results.getString("cc_number");
			v.addElement(type + "-" + num);
		}

		if (v.size() > 2)
		{
			s.setMessage("Welcome to stage 3 -- deface the site");
			s.setChallengeStage(3);
			return (doStage3(s));
		}

		StringElement label1 = new StringElement("Select Credit Card: ");
		Element p = ECSFactory.makePulldown(CREDIT, v, "");
		Element b = ECSFactory.makeButton("Buy Now!");

		ElementContainer ec = new ElementContainer();
		ec.addElement(new P().addElement(label1).addElement(p));
		ec.addElement(new P().addElement(b));
		Input input = new Input(Input.HIDDEN, USER, "101");
		ec.addElement(input);

		return (ec);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element doStage3(WebSession s)
		throws Exception
	{
		String origpath = s.getContext().getRealPath("index.html");
		ElementContainer ec = new ElementContainer();
		try
		{
			// initialize original text
			if (orig == null)
			{
				orig = getFileText(new BufferedReader(new FileReader(origpath)), false);
			}

			// get current text and compare
			String defaced = getFileText(new BufferedReader(new FileReader(origpath)), false);
			if (!orig.equals(defaced))
			{
				s.setMessage("CONGRATULATIONS");
				s.setChallengeStage(4);
				return (doStage4(s));
			}

			// show index.html text
			ec.addElement(new H1().addElement("Original Text"));
			ec.addElement(new P().addElement(orig));
			ec.addElement(new HR());
			ec.addElement(new H1().addElement("Defaced Text"));
			ec.addElement(new P().addElement(defaced));
			ec.addElement(new HR());

			// save any message into a file
			try
			{
				String message = s.getParser().getRawParameter(MESSAGE, "");
				if (message.length() > 0)
				{
					String filename = s.getContext().getRealPath("messages");
					File msgFile = new File(filename, "Message" + Math.abs(random.nextInt()) + ".msg");
					msgFile.createNewFile();
					FileWriter fw = new FileWriter(msgFile);
					fw.write(message);
					fw.close();
				}
			}
			catch (Exception e)
			{
				System.out.println(e);
				e.printStackTrace();
			}

			// get the selected file and handle any SSI tags
			ec.addElement(new H1().addElement("Current Message"));
			try
			{
				String file = s.getParser().getRawParameter(FILE, "");
				String currentmsg = s.getContext().getRealPath("messages/" + file);
				String text = getFileText(new BufferedReader(new FileReader(currentmsg)), false);
				String results = handleIncludes(text);
				ec.addElement(new P().addElement(text));
				if (results != null)
				{
					ec.addElement(new PRE().addElement(results));
				}
			}
			catch (Exception e)
			{
				ec.addElement(new P().addElement("No message selected"));
			}
			ec.addElement(new HR());

			// show a list of files
			ec.addElement(new H1().addElement("Message List"));
			try
			{
				String tempdir = s.getContext().getRealPath("messages");
				File f = new File(tempdir);
				File[] files = f.listFiles();
				for (int loop = 0; loop < files.length; loop++)
				{
					File mf = files[loop];
					ec.addElement(new Div().addElement(ECSFactory.makeLink(mf.getName(), FILE, mf.getName())));
				}
			}
			catch (Exception e)
			{
				System.out.println(e);
				e.printStackTrace();
			}
			ec.addElement(new HR());

			// show a new message window
			ec.addElement(new H1().addElement("New Message"));
			TextArea ta = new TextArea(MESSAGE, 5, 60);
			ec.addElement(ta);
			Element b = ECSFactory.makeButton("Add this message!");
			ec.addElement(new P().addElement(b));
		}
		catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
		return (ec);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element doStage4(WebSession s)
		throws Exception
	{

		/*
		 *  if ( parameter.length() > 0 )
		 *  {
		 *  String cmd = "c:\\winnt\\system32\\cmd.exe /c " + parameter;
		 *  ExecResults er = Exec.execSimple( cmd );
		 *  }
		 *  StringElement label1 = new StringElement( "Enter zip code: " );
		 *  Element b = ECSFactory.makeButton( "GO!" );
		 *  Input input = new Input( Input.TEXT, PARAM, parameter.toString() );
		 */
		return (new H1().addElement("Thanks for coming! Please remember that you will be caught and fired if you use these techniques for evil."));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element doStage5(WebSession s)
		throws Exception
	{
		return (new StringElement("not yet"));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s              Description of the Parameter
	 * @return                Description of the Return Value
	 * @exception  Exception  Description of the Exception
	 */
	protected Element doStage6(WebSession s)
		throws Exception
	{
		return (new StringElement("not yet"));
	}


	/**
	 *  Gets the hints attribute of the ChallengeScreen object
	 *
	 * @return    The hints value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("Nice try -- it's a CHALLENGE!");
		hints.add("Seriously, no hints -- it's a CHALLENGE!");
		hints.add("Come on -- give it a rest!");
		return hints;
	}


	/**
	 *  Gets the menuItem attribute of the ChallengeScreen object
	 *
	 * @return    The menuItem value
	 */
	protected Element getMenuItem()
	{
		String makeLinkHack = WebSession.CHALL + "=true&" + WebSession.SCREEN;
		return makeMenuItem("Start Challenge", makeLinkHack, getScreenId());
	}


	/**
	 *  Gets the ranking attribute of the ChallengeScreen object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(130);
	}



	/**
	 *  Gets the title attribute of the ChallengeScreen object
	 *
	 * @return    The title value
	 */
	public String getTitle()
	{
		return ("The CHALLENGE!");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	protected String handleIncludes(String text)
	{
		// SSI format is <!--#command tag1="value1" tag2="value2" -->
		// e.g. <!--#exec tag1="dir" -->
		int start = text.indexOf("<!--");
		int stop = text.indexOf(" -->");
		if (start < 0 || stop < 0)
		{
			return null;
		}

		String tag = text.substring(start + 4, stop);

		ExecResults er = null;
		if (tag.substring(0, 5).equals("#exec"))
		{
			String command = tag.substring(12, tag.length() - 1);
			String cmd = "cmd.exe /c " + command;
			System.out.println("       Executing: " + cmd);
			er = Exec.execSimple(cmd);
		}
		if (er != null)
		{
			return (er.toString());
		}
		return (null);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element makeClues(WebSession s)
	{
		return new StringElement("Clues not Available :)");
	}



	/**
	 *  Description of the Method
	 *
	 * @param  s        Description of the Parameter
	 * @param  message  Description of the Parameter
	 */
	protected void phoneHome(WebSession s, String message)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(s.getRequest().getRemoteHost());
			DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length());
			DatagramSocket sock = new DatagramSocket();
			sock.connect(addr, 1234);
			System.out.println("      Sending message to " + sock.getInetAddress());
			sock.send(dp);
			sock.close();
		}
		catch (Exception e)
		{
			System.out.println("Couldn't phone home");
			e.printStackTrace();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s        Description of the Parameter
	 * @param  message  Description of the Parameter
	 */
	protected void sendMessage(Socket s, String message)
	{
		try
		{
			OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
			osw.write(message);
		}
		catch (Exception e)
		{
			System.out.println("Couldn't write " + message + " to " + s);
			e.printStackTrace();
		}
	}


	/**
	 *  Constructor for the ChallengeScreen object
	 *
	 * @param  s  Description of the Parameter
	 */
	public void start(WebSession s)
	{
		s.enableClues(false);
		setup(s);
	}


	/**
	 *  Gets the instructions attribute of the ChallengeScreen object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		String instructions = "Your mission is to break the authentication scheme, steal all the credit cards from the database, and then deface the website. You will have to use many of the techniques you have learned in the other lessons. One hint, the last stage of the challenge is based on Server-Side Includes (SSI) and you'll need to do some research.";
		return (instructions);
	}

}

