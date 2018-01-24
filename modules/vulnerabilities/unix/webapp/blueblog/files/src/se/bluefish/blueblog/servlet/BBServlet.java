package se.bluefish.blueblog.servlet;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;

import se.bluefish.blueblog.blog.Blog;
import se.bluefish.blueblog.blog.BlogData;
import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.repository.Repository;
import se.bluefish.blueblog.repository.RepositoryFactory;

/**
 * The BlueBlog dispatcher servlet.
 * 
 * @author Robert Burén
 */
public class BBServlet extends VelocityServlet {
	private ResourceBundle labels;

	/* (non-Javadoc)
	 * @see org.apache.velocity.servlet.VelocityServlet#getTemplate(java.lang.String)
	 */
	private Template getProfileTemplate(String profileName, String name)
		throws ResourceNotFoundException, ParseErrorException, Exception 
	{
		try {
			if( profileName != null ) {
				return getTemplate(profileName+"/"+name);
			} else {
				return getTemplate("default/"+name);
			}
		} catch( ResourceNotFoundException ex ) {
			return getTemplate("default/"+name);
		}
	}

	private Template getRootTemplate(HttpServletRequest request, Context context)
		throws ResourceNotFoundException, ParseErrorException, Exception {
		context.put("usernames", RepositoryFactory.getUsernames());
		context.put("pagetitle", labels.getString("title.root"));
		context.put("prefix", request.getContextPath()+request.getServletPath()+'/' );
		return getTemplate("root.vm");
	}	

	private Template getSingleBlogTemplate(
		HttpServletRequest request,
		Context context,
		String username,
		Category currentCategory,
		String blogId)
		throws ParseErrorException, Exception, ResourceNotFoundException 
	{
		Blog blog = currentCategory.getBlog(blogId);
		context.put("blog", blog);
		context.put("prefix", request.getContextPath()+request.getServletPath()+'/'+username );
		context.put("dateFormat", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
		String templateProfile=currentCategory.getTemplateProfile();
		return getProfileTemplate(templateProfile, "userblog.vm");
	}

	private Template getUserBlogTemplate(
		HttpServletRequest request,
		Context context,
		String blogpath,
		Repository repository,
		Category currentCategory)
		throws ParseErrorException, Exception, ResourceNotFoundException 
	{
		String username = repository.getOwner();
		context.put("pagetitle", labels.getString("title.userroot").replaceAll("\\{0\\}", repository.getName()));
		context.put("repository", repository);
		context.put("blogpath", blogpath);
		context.put("thisCategory", currentCategory);
		context.put("shortPrefix", request.getContextPath()+request.getServletPath()+'/' );
		context.put("prefix", request.getContextPath()+request.getServletPath()+'/'+username );
		// TODO fix a good (Locale-dependent?) instance of DateFormat here
		context.put("dateFormat", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
		context.put("labels", labels);
		String templateProfile=currentCategory.getTemplateProfile();
		return getProfileTemplate(templateProfile, "userblogs.vm");
	}

	
	/**
	 * The main request handler method -- corresponds to the
	 * service() method in an HttpServlet.
	 */
	public Template handleRequest( HttpServletRequest request,
									   HttpServletResponse response,
									   Context context )
	{
		long startTime = System.currentTimeMillis();
		String pathInfo = request.getPathInfo();
		if( pathInfo == null ) {
			pathInfo = "";
		}
		if( pathInfo.length() > 0 && pathInfo.charAt(0) == '/' ) {
			pathInfo = pathInfo.substring(1);
		}
		int indexOfSlash = pathInfo.indexOf('/');
		int indexOfQuestion = pathInfo.indexOf('?');
		String username = indexOfSlash >= 0 ? pathInfo.substring(0,indexOfSlash) :
			indexOfQuestion >= 0 ? pathInfo.substring(0, indexOfQuestion) : pathInfo;
		String blogpath = indexOfSlash < 0 ? "" : 
			indexOfQuestion >= 0 ? pathInfo.substring(indexOfSlash, indexOfQuestion) : 
			pathInfo.substring(indexOfSlash);
		try {
			if( username.equals("") ) {
				return getRootTemplate(request, context);
			} else {
				Repository repository = RepositoryFactory.findRepository(username);
				Category currentCategory = repository.getCategoryByPath(blogpath);
				if( currentCategory == null ) {
					// this is _probably_ a 404 error, but it just might be a 
					// resource request, so we'll check out the files in the "parent"
					boolean error404 = true;
					if( blogpath.charAt(blogpath.length()-1) != '/' ) {
						String parentpath = blogpath.substring(0, blogpath.lastIndexOf('/')+1);
						// try once more at getting the category
						currentCategory = repository.getCategoryByPath(parentpath);
						if( currentCategory != null ) {
							String resourceName = blogpath.substring(parentpath.length());
							InputStream is = currentCategory.getResourceByName(resourceName);
							if( is != null ) {
								response.setContentType(getServletContext().getMimeType(resourceName));
								OutputStream out = response.getOutputStream();
								byte[] buffer = new byte[100*1024]; // 100 kilobytes buffer
								int readlen = -1;
								while( (readlen = is.read(buffer)) != -1 ) {
									out.write(buffer,0,readlen);
								}
								out.close();
								error404 = false;
							}
						}
					}
					if( error404 ) {
						response.sendError(404);
					}
					return null;
				}
				String blogId = request.getParameter("blogId");
				if( blogId != null ) {
					// We have a single blog page request
					return getSingleBlogTemplate(
						request,
						context,
						username,
						currentCategory,
						blogId);
				}
				String newcat = request.getParameter("newcat");
				if( newcat != null ) {
					Category theNewCategory = currentCategory.createSubCategory(newcat, newcat);
					// TODO Test code below here (remove...)
					theNewCategory.addNewBlog(new BlogData("Test Title", "Test Content...\nWith 2 (TWO) Lines!"));
					// TODO Test code above here (remove...)
					response.sendRedirect(request.getRequestURI());
					return null;
				}
				String cmd = request.getParameter("cmd");
				if( cmd != null ) {
					if( cmd.equals("newblog") ) {
						context.put("labels", labels);
						context.put("actionUrl", "?cmd=saveBlog");
						return getProfileTemplate("default", "blogform.vm");
					} else if( cmd.equals("saveBlog") ) {
						String title = request.getParameter("title");
						String content = request.getParameter("content");
						BlogData bd = new BlogData(title, content);
						currentCategory.addNewBlog(bd); 
						response.sendRedirect(request.getRequestURI());
						return null;
					}
				}
				return getUserBlogTemplate(
					request,
					context,
					blogpath,
					repository,
					currentCategory);
			}
		} catch (Exception e) {
			getServletContext().log("BBServlet", e);
			return null;
		}
	}

	/**
	 * The main responsibility of this method is to properly 
	 * set up the RepositoryFactory singleton.
	 * 
	 * @see se.bluefish.blueblog.repository.RepositoryFactory
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		String repositoryConfigurationFileName = getServletConfig().getInitParameter("repositories");
		if( repositoryConfigurationFileName == null ) {
			repositoryConfigurationFileName = "repositories.xml"; // default config filename
		}
		repositoryConfigurationFileName = "/WEB-INF/"+repositoryConfigurationFileName;
		
		InputStream repositoryConfigInStream = getServletContext().getResourceAsStream(repositoryConfigurationFileName);
		try {
			// TODO I must get this URL from somewhere...:
			String rootUrl = "/bb"; 
			RepositoryFactory.init(rootUrl, getServletContext(), repositoryConfigInStream);
			repositoryConfigInStream.close();
		} catch (Exception e) {
			throw new ServletException(e);
		}
		labels = ResourceBundle.getBundle("labels");

		log("BBServlet.init completed");
	}
}
