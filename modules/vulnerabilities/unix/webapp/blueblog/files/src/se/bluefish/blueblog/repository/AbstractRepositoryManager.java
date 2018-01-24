/*
 * Created on 2003-jul-05
 *
 */
package se.bluefish.blueblog.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.blog.Category;

/**
 * Provides default implementation for a few RepositoryManager 
 * methods, to be used by concrete subclasses.
 * 
 * @author Robert Burén
 *
 */
public abstract class AbstractRepositoryManager implements RepositoryManager {

	/**
	 * Default: Steps through all repositories (ordered by
	 * username i.e. owner) and calls getRepositoryFor(path)
	 * on each Repository, and returns a list of all non-null
	 * responses.
	 */
	public List getCategoriesByPath(String path) {
		List categories = new ArrayList();
		List usernames = getUsernames();
		Iterator iter = usernames.iterator();
		while( iter.hasNext() ) {
			String username = (String)iter.next();
			Repository rep = getRepositoryFor(username);
			Category cat = rep.getCategoryByPath(path);
			if( cat != null ) {
				categories.add( cat );
			}
		}
		return categories;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.RepositoryManager#getRepositoryFor(java.lang.String)
	 */
	public abstract Repository getRepositoryFor(String username);

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.RepositoryManager#getUsernames()
	 */
	public abstract List getUsernames();

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.RepositoryManager#init(se.bluefish.blueblog.repository.RepositoryContext, java.util.Properties)
	 */
	public abstract void init(RepositoryContext context, Properties props)
		throws BBException;

}
