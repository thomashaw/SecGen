/*
 * Created on 2003-jul-05
 *
 */
package se.bluefish.blueblog.metarepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.bluefish.blueblog.blog.AbstractCategory;
import se.bluefish.blueblog.blog.Blog;
import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.repository.Repository;

/**
 * @author Robert Burén
 *
 */
public class MetaRootCategory extends AbstractCategory {
	private String name;
	private MetaRepository repository;
	private Map subCategories = new HashMap(); // UrlPath -> MetaCategory
	private String templateProfileName;
	
	public MetaRootCategory(MetaRepository repository, String name) {
		this.repository = repository;
		this.name = name;
	}

	public void addSubMetaCategory(String path, String name) {
		MetaCategory cat = new MetaCategory(repository, this, path, name);
		subCategories.put( path, cat);
	}

	/**
	 * There are no Blogs in the MetaRootCategory.
	 */
	public Blog getBlog(String blogId) {
		return null;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getLinkList()
	 */
	public List getLinkList() {
		return new ArrayList();
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
		return null;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#getRepository()
	 */
	public Repository getRepository() {
		return repository;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getSubCategories()
	 */
	public List getSubCategories() {
		return new ArrayList( subCategories.values() );
	}

	public Category getSubCategoryByPath(String path) {
		return (Category)subCategories.get(path);
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getTemplateProfile()
	 */
	public String getTemplateProfile() {
		return templateProfileName;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getUrlPath()
	 */
	public String getUrlPath() {
		return "/";
	}

	/**
	 * @param template
	 */
	public void setTemplateProfile(String template) {
		templateProfileName = template;
	}

}
