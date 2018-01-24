/*
 * Created on 2003-jul-05
 *
 */
package se.bluefish.blueblog.metarepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.repository.AbstractRepositoryManager;
import se.bluefish.blueblog.repository.Repository;
import se.bluefish.blueblog.repository.RepositoryContext;

/**
 * Type of RepositoryManager that gets its categories and blogs
 * from other repositories in the application, as specified in the
 * configuration file.
 * 
 * TODO: Specify configuration parameters.
 * 
 * @author Robert Burén
 */
public class MetaRepositoryManager extends AbstractRepositoryManager {
	public final String KEY_NAME = "name";
	public final String KEY_ROOTCATEGORY_PREFIX = "rootcategory";
	public final String KEY_ROOTCATEGORY_SUFFIX_NAME = "-name";
	public final String KEY_ROOTCATEGORY_SUFFIX_PATH = "-path";
	public final String KEY_TEMPLATE_PROFILE = "templateProfile";
	public final String KEY_USERNAME = "username";
	private MetaRepository repository; // there can be only one...
	
	private String username;

	public List getCategoriesByPath(String path) {
		List l = new ArrayList(1);
		Category cat = repository.getCategoryByPath(path);
		if( cat != null ) { 
			l.add(cat);
		}
		return l;
	}

	/**
	 * @see se.bluefish.blueblog.repository.RepositoryManager#getRepositoryFor(java.lang.String)
	 * @param un username
	 */
	public Repository getRepositoryFor(String un) {
		return un.equals(username) ? repository : null;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.RepositoryManager#getUsernames()
	 */
	public List getUsernames() {
		List l = new ArrayList(1);
		l.add(username);
		return l;
	}

	public void init(RepositoryContext context, Properties props)
		throws BBException 
	{
		username = props.getProperty(KEY_USERNAME);
		
		List paths = new ArrayList();
		List names = new ArrayList();
		for( int i=0; i<10; ++i ) {
			String path = props.getProperty(KEY_ROOTCATEGORY_PREFIX+i+KEY_ROOTCATEGORY_SUFFIX_PATH);
			if( path != null ) {
				paths.add(path);
				names.add(props.getProperty(KEY_ROOTCATEGORY_PREFIX+i+KEY_ROOTCATEGORY_SUFFIX_NAME));
			}
		}
		String name = props.getProperty(KEY_NAME);
		repository = new MetaRepository(username, name, paths, names);
		String template = props.getProperty(KEY_TEMPLATE_PROFILE);
		if( template != null ) {
			repository.setTemplateProfileName(template);
		}
	}
}
