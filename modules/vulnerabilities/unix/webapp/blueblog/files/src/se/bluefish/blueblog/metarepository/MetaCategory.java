/*
 * Created on 2003-jul-05
 *
 */
package se.bluefish.blueblog.metarepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import se.bluefish.blueblog.blog.AbstractCategory;
import se.bluefish.blueblog.blog.Blog;
import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.repository.Repository;
import se.bluefish.blueblog.repository.RepositoryFactory;
import se.bluefish.blueblog.repository.RepositoryManager;

/**
 * "Virtual" Category that references blogs from other Repositories.
 * 
 * @author Robert Burén
 *
 */
public class MetaCategory extends AbstractCategory {
	private String name;
	private MetaRootCategory parent; 
	private String path;
	private MetaRepository repository;
	
	public MetaCategory(MetaRepository repository, MetaRootCategory parent, String path, String name) {
		this.repository=repository;
		this.path=path;
		this.name=name;
		this.parent=parent;
	}
	
	public Blog getBlog(String blogId) {
		// TODO Can we return a Blog here?
		return null;
	}
	
	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getLinkList()
	 */
	public List getLinkList() {
		return new ArrayList();
	}

	/**
	 * Returns list of "virtual" (or "meta") subcategories, 
	 * ie categories that this metacategory is "pointing to".
	 * 
	 * @see se.bluefish.blueblog.blog.Category#getSubCategories()
	 */
	private List getMetaCategories() {
		Iterator iter = RepositoryFactory.getRepositoryManagers().iterator();
		List categories = new ArrayList();
		while( iter.hasNext() ) {
			RepositoryManager mgr = (RepositoryManager)iter.next();
			if( ! (mgr instanceof MetaRepositoryManager) ) {
				List cats = mgr.getCategoriesByPath(path);
				categories.addAll(cats);
			}
		}
		return categories;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getParent()
	 */
	public Category getParent() {
		return parent;
	}

	public List getRecentBlogs(int maxCount, boolean recursive) {
		List allRecentBlogs = new ArrayList();
		Iterator iter = getMetaCategories().iterator();
		while( iter.hasNext() ) {
			Category cat = (Category)iter.next();
			allRecentBlogs.addAll(cat.getRecentBlogs(maxCount, recursive));
		}
		Collections.sort(allRecentBlogs);
		if( allRecentBlogs.size() > maxCount ) {
			allRecentBlogs.subList(maxCount, allRecentBlogs.size()).clear();
		}
		return allRecentBlogs;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#getRepository()
	 */
	public Repository getRepository() {
		return repository;
	}
	
	public List getSubCategories() {
		return new ArrayList();
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getTemplateProfile()
	 */
	public String getTemplateProfile() {
		return parent.getTemplateProfile();
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getUrlPath()
	 */
	public String getUrlPath() {
		return path;
	}

}
