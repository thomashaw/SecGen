package se.bluefish.blueblog.blog;

import java.io.InputStream;
import java.util.List;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.ImmutableException;
import se.bluefish.blueblog.repository.Repository;

/**
 * A Category is a container for weblog entries. Think of it as
 * a directory, containing files (weblog entries) and other directories.
 * 
 * @author Robert Burén
 */
public interface Category {
	/**
	 * Creates a new Blog in this Category.
	 * 
	 * @param blogData the data used to create the Blog
	 * @throws ImmutableException if this is an immutable category
	 * @throws BBException if the Blog couldn't be created
	 * @return the newly created Blog instance
	 * 
	 * @see #isImmutable()
	 * @see Blog
	 * @see BlogData
	 */
	public Blog addNewBlog(BlogData blogData) throws ImmutableException, BBException;
	
	/**
	 * Creates a new sub-category to this category.
	 * 
	 * @param urlName name to be used to identify the 
	 *        new category in the URL. Think "directory name".
	 * @param name display name of the new category
	 * @return the newly created category
	 * @throws ImmutableException if this Category is immutable
	 * @throws BBException if the Category could not be created
	 * 
	 * @see #isImmutable()
	 */
	public Category createSubCategory(String urlName, String name) throws ImmutableException, BBException;

	/**
	 * Returns the Blog corresponding to the specified blogId, 
	 * or <b>null</b> if no such Blog can be found.
	 */
	public Blog getBlog(String blogId);

	/**
	 * Returns the complete URL for this Category, 
	 * starting with a '/' character.
	 */
	public String getCompleteUrl();
	
	/**
	 * Returns a list of Link instances, belonging to this Category.
	 * If no links belongs to this Category, the links for the parent
	 * category may be returned (but this is up to the implementer of 
	 * this interface).
	 */
	public List getLinkList();
	
	/**
	 * Returns the name of this Category.
	 */
	public String getName();
	
	/**
	 * Returns parent category, or null if this is the root category.
	 */
	public Category getParent();
	
	/**
	 * Returns a List of Categories that are parents to this one. 
	 * The first element in the list is the root category.
	 */
	public List getParents();
	
	/**
	 * Returns a list of the most recent weblog entries (Blog instances).
	 * 
	 * @param maxCount highest number of Blog instances returned.
	 * @param recursive true if the list should include recent blogs from subcategories
	 * @see se.bluefish.blueblog.blog.Blog
	 */
	public List getRecentBlogs(int maxCount, boolean recursive);
	
	/**
	 * Returns the Repository this Category belongs to.
	 */
	public Repository getRepository();

	/**
	 * Gets a resource, i.e. some type of information that is
	 * not a Category or a Blog, like an image or a zip file.
	 * 
	 * @param resourceName think "file name"
	 * @return an open InputStream connected to the resource, 
	 *         or <b>null</b> if no resource with the specified
	 *         name can be found
	 */
	public InputStream getResourceByName(String resourceName);
	
	/**
	 * Returns a list of Category instances that are 
	 * subcategories to this one. No subcategories
	 * equals empty list.
	 */	
	public List getSubCategories();

	/**
	 * Returns template profile name, or null for default.
	 */
	public String getTemplateProfile();

	/**
	 * Should call <tt>getTotalNumberOfBlogs(<b>true</b>) internally.
	 */
	public int getTotalNumberOfBlogs();
	
	/**
	 * Returns the total number of Blogs in this Category
	 * (and, if <tt>recursive</tt> is set to <b>true</b>,
	 * its sub-categories).
	 */
	public int getTotalNumberOfBlogs(boolean recursive);
	
	/**
	 * Returns the part of the URL that points to this category.
	 * Starts with a '/' character. Root category returns the string "/".
	 */
	public String getUrlPath();
	
	/**
	 * True if it isn't possible to add, remove or modify any 
	 * properties of this Category, nor of any of its 
	 * sub-categories or any Blogs belonging to them.
	 */
	public boolean isImmutable();
}
