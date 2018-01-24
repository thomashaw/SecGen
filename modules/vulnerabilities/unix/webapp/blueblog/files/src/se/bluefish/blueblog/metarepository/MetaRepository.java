/*
 * Created on 2003-jul-05
 *
 */
package se.bluefish.blueblog.metarepository;

import java.util.List;

import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.repository.Repository;

/**
 * @author Robert Burén
 *
 */
public class MetaRepository implements Repository {
	private String name = "?";
	private String owner;
	private MetaRootCategory rootCategory;

	public MetaRepository( String owner, String name, List paths, List names ) {
		this.owner = owner;
		this.name = name;
		rootCategory = new MetaRootCategory( this, name );
		for( int i=0; i<paths.size(); ++i ) {
			String path = (String)paths.get(i);
			String catname = path; // default value
			if( names.size() > i && names.get(i) != null ) {
				catname = (String)names.get(i);
			}
			rootCategory.addSubMetaCategory( path, catname );
		}
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.Repository#getCategoryByPath(java.lang.String)
	 */
	public Category getCategoryByPath(String path) {
		if( path.length() < 1 || path.charAt(0) != '/' ) {
			// add starting slash if missing
			path = "/" + path;
		}
		if( path.equals("/") ) {
			// root category
			return rootCategory;
		} else {
			if( path.endsWith("/") ) {
				// remove possible ending slash
				path = path.substring(0, path.length() - 1 );
			}
			return rootCategory.getSubCategoryByPath(path);
		}
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.Repository#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.Repository#getOwner()
	 */
	public String getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.Repository#getRootCategory()
	 */
	public Category getRootCategory() {
		return rootCategory;
	}

	/**
	 * @param template
	 */
	public void setTemplateProfileName(String template) {
		rootCategory.setTemplateProfile(template);
	}

}
