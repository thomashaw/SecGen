package se.bluefish.blueblog.repository;

import se.bluefish.blueblog.blog.Category;

/**
 * Represents all blog entries and blog categories for a specific
 * blog author (i.e. "user").
 * 
 * @author Robert Burén
 */
public interface Repository {

	/**
	 * Returns the Category corresponding to the path provided.
	 * Categories and subcategories are separated by the '/' character,
	 * e.g. "/Technology/Java". Returns <b>null</b> if no Category can be found
	 * matching the supplied path.
	 */
	public Category getCategoryByPath(String path);
	
	/**
	 * Name of the Repository.
	 */
	public String getName();
	
	/**
	 * Returns the username of the owner of this blog repository.
	 */
	public String getOwner();

	/**
	 * Method getRootCategory.
	 */
	public Category getRootCategory();
}
