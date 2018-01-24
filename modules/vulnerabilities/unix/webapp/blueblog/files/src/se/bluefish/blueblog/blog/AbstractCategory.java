/*
 * Created on 2003-jul-03
 *
 */
package se.bluefish.blueblog.blog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.ImmutableException;
import se.bluefish.blueblog.repository.Repository;
import se.bluefish.blueblog.repository.RepositoryFactory;

/**
 * Helper class with some default implementations for the Category interface.
 * 
 * @author Robert Burén
 */
public abstract class AbstractCategory implements Category {

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#addNewBlog(se.bluefish.blueblog.blog.BlogData)
	 */
	public Blog addNewBlog(BlogData blogData)
		throws ImmutableException, BBException 
	{
		throw new ImmutableException("addNewBlog() not supported");
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#createSubCategory(java.lang.String, java.lang.String)
	 */
	public Category createSubCategory(String urlName, String name)
		throws ImmutableException, BBException 
	{
		throw new ImmutableException("createSubCategory() not supported.");
	}

	public abstract Blog getBlog(String blogId);

	public String getCompleteUrl() {
		return RepositoryFactory.getRootUrl()+'/'+getRepository().getOwner()+getUrlPath();
	}
	public abstract List getLinkList();
	public abstract String getName();
	public abstract Category getParent();
	
	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getParents()
	 */
	public List getParents() {
		List parents = new ArrayList();
		internalGetParents(parents);
		return parents;
	}

	/**
	 * This default implementation calls getRecentBlogs() with
	 * the same parameters on all subcategories, and returns a
	 * list with the <code>maxCount</code> most recent ones.
	 */
	public List getRecentBlogs(int maxCount, boolean recursive) {
		List allRecentBlogs = new ArrayList();
		Iterator iter = getSubCategories().iterator();
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
	public abstract Repository getRepository();
	
	public InputStream getResourceByName(String resourceName) {
		return null;
	}
	public abstract List getSubCategories();
	public abstract String getTemplateProfile();

	/**
	 * Calls getTotalNumberOfBlogs(<b>true</b>)
	 * 
	 * @see #getTotalNumberOfBlogs(boolean)
	 */
	public int getTotalNumberOfBlogs() {
		return getTotalNumberOfBlogs(true);
	}

	/**
	 * Implements a not-so-efficient default implementation, but
	 * it should return the right answer.
	 * 
	 * @see se.bluefish.blueblog.blog.Category#getTotalNumberOfBlogs(boolean)
	 */
	public int getTotalNumberOfBlogs(boolean recursive) {
		return getRecentBlogs(1000, recursive).size();
	}
	public abstract String getUrlPath();


	private void internalGetParents( List fillThisList ) {
		if( getParent() != null ) {
			((AbstractCategory)getParent()).internalGetParents( fillThisList );
			fillThisList.add( getParent() );
		}
	}
	
	/**
	 * Default: returns <b>true</b>.
	 * 
	 * @see se.bluefish.blueblog.blog.Category#isImmutable()
	 */
	public boolean isImmutable() {
		return true;
	}

}
