
import java.net.*;
import java.util.*;
import javax.servlet.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of the Open Web
 *  Application Security Project (http://www.owasp.org) This software package is published by OWASP
 *  under the GPL. You should read and accept the LICENSE before you use, modify and/or redistribute
 *  this software.
 *
 * @author     jwilliams@aspectsecurity.com
 * @created    November 6, 2002
 */
public class Course
{

	private List lessons = new ArrayList();


	/**
	 *  Description of the Method
	 *
	 * @param  fileName  Description of the Parameter
	 * @param  path      Description of the Parameter
	 * @param  ext       Description of the Parameter
	 * @return           Description of the Return Value
	 */
	private String clean(String fileName, String path, String ext)
	{
		fileName = fileName.trim();

		// check if file is a directory
		if (fileName.endsWith("/"))
		{
			return fileName;
		}

		// check if file is a class file
		if (!fileName.endsWith(ext))
		{
			return null;
		}

		// Strip off the leading path info
		fileName = fileName.substring(path.length(), fileName.length() - ext.length());

		return fileName;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  path        Description of the Parameter
	 * @param  context     Description of the Parameter
	 * @param  courseName  Description of the Parameter
	 * @param  lesson      Description of the Parameter
	 */
	private void findCourseResource(AbstractLesson lesson, ServletContext context, String path, String courseName)
	{
		Set files = context.getResourcePaths(path);
		Iterator fileIter = files.iterator();
		while (fileIter.hasNext())
		{
			String className = clean((String) fileIter.next(), path, ".java");
			if (className == null)
			{
				continue;
			}
			else if (className.length() != 1 && className.endsWith("/"))
			{
				findCourseResource(lesson, context, className, courseName);
			}
			else
			{
				if (className.endsWith(courseName))
				{
					lesson.setSourceURL(path + courseName + ".java");
				}
			}
		}
	}


	/**
	 *  Gets the categories attribute of the Course object
	 *
	 * @return    The categories value
	 */
	public List getCategories()
	{
		List categories = new ArrayList();
		Iterator iter = lessons.iterator();
		while (iter.hasNext())
		{
			AbstractLesson lesson = (AbstractLesson) iter.next();
			categories.add(lesson.getCategory());
		}
		return categories;
	}


	/**
	 *  Gets the firstLesson attribute of the Course object
	 *
	 * @return    The firstLesson value
	 */
	public int getFirstLesson()
	{
		Collections.sort(lessons);
		return ((AbstractLesson) lessons.get(0)).getScreenId();
	}


	/**
	 *  Gets the lesson attribute of the Course object
	 *
	 * @param  lessonId  Description of the Parameter
	 * @param  role      Description of the Parameter
	 * @return           The lesson value
	 */
	public AbstractLesson getLesson(int lessonId, String role)
	{
		Iterator iter = lessons.iterator();
		while (iter.hasNext())
		{
			AbstractLesson lesson = (AbstractLesson) iter.next();
			if (lesson.getScreenId() == lessonId)
			{
				return lesson;
			}
		}
		return null;
	}


	/**
	 *  Gets the lesson attribute of the Course object
	 *
	 * @param  lessonId  Description of the Parameter
	 * @return           The lesson value
	 */
	public AbstractLesson getLesson(int lessonId)
	{
		Iterator iter = lessons.iterator();
		while (iter.hasNext())
		{
			AbstractLesson lesson = (AbstractLesson) iter.next();
			if (lesson.getScreenId() == lessonId)
			{
				return lesson;
			}
		}
		return null;
	}


	/**
	 *  Gets the lessons attribute of the Course object
	 *
	 * @param  category  Description of the Parameter
	 * @return           The lessons value
	 */
	public List getLessons(String category)
	{
		List lessonList = new ArrayList();
		Iterator iter = lessons.iterator();
		while (iter.hasNext())
		{
			AbstractLesson lesson = (AbstractLesson) iter.next();
			if (lesson.getCategory().equals(category))
			{
				lessonList.add(lesson);
			}
		}
		Collections.sort(lessonList);
		return lessonList;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  path     Description of the Parameter
	 * @param  context  Description of the Parameter
	 */
	public void loadCourses(ServletContext context, String path)
	{
		Set files = context.getResourcePaths(path);
		Iterator fileIter = files.iterator();
		while (fileIter.hasNext())
		{
			String className = clean((String) fileIter.next(), path, ".class");
			if (className == null)
			{
				continue;
			}
			else if (className.length() != 1 && className.endsWith("/"))
			{
				loadCourses(context, className);
			}
			else
			{
				try
				{
				        String classNames[] = {
					    "ChallengeScreen", "DatabaseXss", "BufferOverflow", "CommandInjection", "ParameterInjection", 
					    "DatabaseXss", "DOS_Login", "Encoding", "ForcedBrowsing", "ForgetPassword", "HiddenFieldTampering", 
					    "HttpBasics", "JavaScriptValidation", "PathBasedAccessControl", "ProductAdminScreen", "ReflectedXSS",
					    "RefreshDBScreen", "RemoteAdminFlaw", "ReportCardScreen", "RoleBasedAccessControl", "SummaryReportCardScreen", 
					    "ThreadSafetyProblem", "UncheckedEmail", "UserAdminScreen", "ViewDatabase", "WeakAuthenticationCookie"
					};
					int i = 10;
				        className = classNames[i];
					Object possibleLesson = Class.forName(className).newInstance();
					if (possibleLesson instanceof AbstractLesson)
					{
						AbstractLesson lesson = (AbstractLesson) possibleLesson;
						findCourseResource(lesson, context, "/", className);
						lessons.add(possibleLesson);

					}
				}
				catch (Exception e)
				{
					// only care about lessons
				}
			}
		}
	}

}

