
import java.util.*;
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
public abstract class LessonAdapter extends AbstractLesson
{

	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	protected Element createContent(WebSession s)
	{

		return new StringElement("Lesson Adapter default content");
	}


	/**
	 *  Gets the category attribute of the LessonAdapter object
	 *
	 * @return    The category value
	 */
	public String getCategory()
	{
		return "General";
	}


	/**
	 *  Gets the instructions attribute of the LessonAdapter object
	 *
	 * @return    The instructions value
	 */
	protected String getInstructions()
	{
		return null;
	}


	/**
	 *  Gets the hintCount attribute of the LessonAdapter object
	 *
	 * @return    The hintCount value
	 */
	protected int getHintCount()
	{
		return getHints().size();
	}


	/**
	 *  Fill in a minor hint that will help people who basically get it, but are stuck on somthing
	 *  silly.
	 *
	 * @return    The hint1 value
	 */
	protected List getHints()
	{
		List hints = new ArrayList();
		hints.add("There are no hints defined.");
		return hints;
	}



	/**
	 *  Gets the ranking attribute of the LessonAdapter object
	 *
	 * @return    The ranking value
	 */
	protected Integer getRanking()
	{
		return new Integer(1000);
	}



	/**
	 *  Fill in a descriptive title for this lesson
	 *
	 * @return    The title value
	 */
	protected String getTitle()
	{
		return "Untitled Lesson " + getScreenId();
	}

}

