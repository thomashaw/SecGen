package se.bluefish.blueblog.repository;

import java.util.List;
import java.util.Properties;

import se.bluefish.blueblog.BBException;

/**
 * Handles a collection of Repository instances. 
 * The RepositoryManager class is responsible for
 * knowing about how, and accessing, the Repository
 * instances. Concrete RepositoryManagers can choose to
 * access Repositories from e.g. the filesystem, a 
 * database, remote EJBs etc.
 * 
 * @author Robert Burén
 */
public interface RepositoryManager {

	/**
	 * Returns a List of all Category instances with the same
	 * path (from all Repositories handled by this RepositoryManager).
	 */
	public List getCategoriesByPath(String path);
	
	/**
	 * Returns the weblog repository for a particular user, or null if that
	 * username is unknown by this RepositoryManager.
	 */
	public Repository getRepositoryFor( String username );
	
	/**
	 * Returns a list of the usernames (as Strings) that this 
	 * RepositoryManager manages.
	 */
	public List getUsernames();
	
	/**
	 * Initializes this RepositoryManager instance. Called by the RepositoryFactory on startup.
	 * 
	 * @param context additional info that this RepositoryManager may take advantage of
	 * @param props properties defining this instance
	 * @throws BBException if this instance is initialized more than once
	 */
	public void init( RepositoryContext context, Properties props ) throws BBException;
}
