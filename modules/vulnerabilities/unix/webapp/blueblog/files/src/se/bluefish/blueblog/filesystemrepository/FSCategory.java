package se.bluefish.blueblog.filesystemrepository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.opensymphony.module.oscache.base.NeedsRefreshException;
import com.opensymphony.module.oscache.general.GeneralCacheAdministrator;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.ImmutableException;
import se.bluefish.blueblog.blog.AbstractCategory;
import se.bluefish.blueblog.blog.Blog;
import se.bluefish.blueblog.blog.BlogData;
import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.blog.Link;
import se.bluefish.blueblog.formatters.Formatter;
import se.bluefish.blueblog.formatters.GenericFormatter;
import se.bluefish.blueblog.repository.Repository;
import se.bluefish.blueblog.repository.RepositoryFactory;

/**
 * An FSCategory maps to a file system folder.
 * 
 * @author Robert Burén
 */
class FSCategory extends AbstractCategory {
	
	private static class FileNameComparator implements Comparator {
		/**
		 * @see java.util.Comparator#compare(Object, Object)
		 */
		public int compare(Object arg0, Object arg1) {
			File f1=(File)arg0;
			File f2=(File)arg1;
			return f1.getName().compareTo(f2.getName());
		}

	}

	// TODO: the value below should come from a config file
	private static final int CACHE_REFRESH_TIME = 10; // 10 secs

	public static final String FILENAME_SETTINGS = "category.properties";

	private static final String getMimeType( String filename ) {
		return RepositoryFactory.getMimeType(filename);
	}

	private Map blogTypes; // see same in FSRepositoryManager
	
	private GeneralCacheAdministrator cacheAdmin = GeneralCacheAdministrator.getInstance();
	
	private File categoryDirectory;
	private List linklist;

	private String name;

	private Category parent;
	private Properties properties = new Properties();
	private FSRepository repository;
	private File settingsDirectory;
	private File settingsFile;
	private String templateProfileName;
	private String urlPath;
	
	/**
	 * Creates a File System Blog Category, which is contained in a
	 * file system directory, supplied in an argument.
	 * 
	 * @throws IOException if <code>categoryDirectory</code> is invalid (doesn't exist, 
	 *                     or is not a readable directory) 
	 */
	public FSCategory(FSRepository repository, Category parent, File categoryDirectory, Map blogTypes) throws IOException {
		if( categoryDirectory == null || !categoryDirectory.exists() 
			|| !categoryDirectory.isDirectory() || !categoryDirectory.canRead() ) 
		{
			String path = categoryDirectory == null ? "null" : categoryDirectory.getPath();
			throw new IOException("Invalid categoryDirectory: "+path);			
		}
		this.repository = repository;
		this.parent = parent;
		this.categoryDirectory = categoryDirectory;
		this.blogTypes = blogTypes;
		this.settingsDirectory = new File(categoryDirectory.getPath()+File.separator+FSRepository.DIRNAME_SETTINGS);
		this.settingsFile      = new File(settingsDirectory.getPath()+File.separator+FILENAME_SETTINGS);
		Properties props = getInitialProperties();
		this.name = props.getProperty("name");
		this.templateProfileName = props.getProperty("template");
		this.linklist = getLinkList( props );
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#addNewBlog(se.bluefish.blueblog.blog.BlogData)
	 */
	public Blog addNewBlog(BlogData blogData)
		throws ImmutableException, BBException 
	{
		// check that the blogType is known
		Formatter formatter = (Formatter)blogTypes.get(blogData.getBlogType());
		if( formatter == null ) {
			throw new BBException("Unknown blog type: "+blogData.getBlogType());
		}
		// TODO: the blog should go in the cache here!
		return FSBlog.createNewBlog(this, blogData, formatter);
	}


	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#createSubCategory(java.lang.String, java.lang.String)
	 */
	public Category createSubCategory(String urlName, String name)
		throws ImmutableException, BBException 
	{
		File dir = new File(categoryDirectory.getPath()+File.separator+urlName);
		if( dir.exists() ) {
			throw new BBException("Directory "+dir.getPath()+" can't be created - path already exists");
		}
		dir.mkdir();
		return getRepository().getCategoryByPath(getUrlPath()+dir.getName());
	}


	public List getAllBlogs(boolean recursive) throws IOException {
		final String CACHE_PREFIX = "AllBlogs:";
		String cacheKeyValue = CACHE_PREFIX+getRepository().getOwner()+getUrlPath();
		// first check the cache
		try {
			return (List)cacheAdmin.getFromCache(cacheKeyValue, CACHE_REFRESH_TIME);
		} catch (NeedsRefreshException e1) {
			// ignore exception -- we'll create the list below
		}
		
		File[] files = categoryDirectory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if( !file.isFile() ) {
					return false;
				}
				return blogTypes.containsKey(getMimeType(file.getName()));
			}
		});
		List list = new ArrayList(files.length);
		for( int i = 0; i < files.length; ++i ) {
			try {
				Formatter formatter = (Formatter)blogTypes.get(getMimeType(files[i].getName()));
				if( formatter == null ) {
					formatter = new GenericFormatter();
				}
				// TODO: possible optimization here: cache individual blogs?
				list.add(new FSBlog(this, files[i], formatter));
			} catch (IOException e) {
				// ignore current blog entry and continue with next 
			}
		}
		if( recursive ) {
			List subs = getSubCategories();
			Iterator iter = subs.iterator();
			while( iter.hasNext() ) {
				FSCategory c = (FSCategory)iter.next();
				list.addAll(c.getAllBlogs(true));
			}
		}
		Collections.sort(list);
		cacheAdmin.putInCache(cacheKeyValue, list);
		return list;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#getBlog(java.lang.String)
	 */
	public Blog getBlog(final String blogId) {
		final String CACHE_PREFIX="Blog:";
		String cacheKeyString = CACHE_PREFIX+getRepository().getOwner()+getUrlPath()+"?blogId="+blogId;
		// check the cache first
		try {
			return (Blog)cacheAdmin.getFromCache(cacheKeyString, CACHE_REFRESH_TIME);
		} catch (NeedsRefreshException e1) {
			// ignore exception -- we'll create an instance below
		}
		
		File[] files = categoryDirectory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if( !file.isFile() ) {
					return false;
				}
				return blogTypes.containsKey(getMimeType(file.getName()))
					&& file.getName().startsWith(blogId+'.');
			}
		});
		List list = new ArrayList(files.length);
		for( int i = 0; i < files.length; ++i ) {
			try {
				Formatter formatter = (Formatter)blogTypes.get(getMimeType(files[i].getName()));
				if( formatter == null ) {
					formatter = new GenericFormatter();
				}
				list.add(new FSBlog(this, files[i], formatter));
			} catch (IOException e) {
				// ignore current blog entry and continue with next 
			}
		}
		if( list.size() > 0 ) {
			Collections.sort(list);
			Blog b = (FSBlog)list.get(0);
			cacheAdmin.putInCache(cacheKeyString, b);
			return b; 
		} else {
			return null;
		}
	}

	/**
	 * A javabean-compatible property. Returns all blogs, recursively.
	 */
	public List getBlogs() {
		try {
			return getAllBlogs(true);
		} catch (IOException e) {
			return new ArrayList();
		}
	}

	File getCategoryDirectory() {
		return categoryDirectory;
	}

	private Properties getDefaultProperties() {
		Properties props=new Properties();
		props.setProperty("name", categoryDirectory.getName());
		return props;
	}
	
	private Properties getInitialProperties() throws IOException {
		if( settingsFile.exists() ) {
			Properties props=new Properties();
			BufferedInputStream in=new BufferedInputStream(new FileInputStream(settingsFile));
			props.load(in);
			in.close();
			return props;
		} else {
			return setupDefaultSettings();
		}
	}

	public List getLinkList() {
		if( linklist != null && linklist.size() > 0 ) {
			return linklist;
		}
		if( parent != null ) {
			return parent.getLinkList();
		}
		return new ArrayList();
	}

	/**
	 * @param props
	 * @return
	 */
	private List getLinkList(Properties props) {
		String separator = props.getProperty("link.separator", "\\|");
		ArrayList keys = Collections.list(props.keys());
		Collections.sort(keys);
		ArrayList list = new ArrayList();
		Iterator keysiter = keys.iterator();
		while( keysiter.hasNext() ) {
			String key = (String)keysiter.next();
			if( key.matches("link\\.\\d+") ) {
				String[] temp = props.getProperty(key).split(separator);
				if( temp.length < 2 ) {
					continue; // ignore invalid link values
				}
				String name = temp[0];
				String url = temp[1];
				try {
					Link link = new Link(name, new URI(url));
					list.add(link);
				} catch (URISyntaxException e) {
					// ignore invalid urls
				}
			}
		}
		return list;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Category#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Category#getParent()
	 */
	public Category getParent() {
		return parent;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Category#getRecentBlogs(int,boolean)
	 */
	public List getRecentBlogs(int maxCount, boolean recursive) {
		List list = null;
		try {
			list = getAllBlogs(recursive);
		} catch (IOException e) {
			list = new ArrayList();
		}
		if( list.size() <= maxCount ) {
			return list;
		} else {
			ArrayList retval=new ArrayList(maxCount);
			Iterator iter=list.iterator();
			for( int i=0; i<maxCount && iter.hasNext(); ++i ) {
				retval.add(iter.next());
			}
			return retval;
		}
	}

	/**
	 * @see se.bluefish.blueblog.blog.AbstractCategory#getRepository()
	 */
	public Repository getRepository() {
		return repository;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.Category#getResourceByName(java.lang.String)
	 */
	public InputStream getResourceByName(String resourceName) {
		try {
			File resourceFile = new File(categoryDirectory, resourceName);
			return new BufferedInputStream(new FileInputStream(resourceFile));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * @see se.bluefish.blueblog.blog.Category#getSubCategories()
	 */
	public List getSubCategories() {
		File[] dirs = categoryDirectory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() && !file.getName().equals(FSRepository.DIRNAME_SETTINGS);
			}
		});
		// sort dirs by name
		Arrays.sort(dirs, new FileNameComparator());
		
		List list = new ArrayList(dirs.length);
		for( int i = 0; i < dirs.length; ++i ) {
			// Delegating to the Repository to get the Categories for us.
			// This gives the Repository a chance to cache them.
			Category cat = getRepository().getCategoryByPath(getUrlPath()+dirs[i].getName());
			if( cat != null ) {
				list.add( cat);
			}
		}
		return list;
	}
	
	public String getTemplateProfile() {
		if( templateProfileName != null ) {
			return templateProfileName;
		}
		if( parent != null ) {
			return parent.getTemplateProfile();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#getTotalNumberOfBlogs(boolean)
	 */
	public int getTotalNumberOfBlogs(boolean recursive) {
		try {
			return getAllBlogs(recursive).size();
		} catch (IOException e) {
			return super.getTotalNumberOfBlogs(recursive);
		}
	}

	/**
	 * @see se.bluefish.blueblog.blog.Category#getUrlPath()
	 */
	public String getUrlPath() {
		if( urlPath == null ) {
			urlPath = parent == null ? "/" : parent.getUrlPath()+categoryDirectory.getName()+"/";
		}
		return urlPath;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.blog.AbstractCategory#isImmutable()
	 */
	public boolean isImmutable() {
		return false;
	}

	private Properties setupDefaultSettings() throws IOException {
		if( !settingsDirectory.exists() && !settingsDirectory.mkdir() ) {
			throw new IOException("Couldn't create directory "+settingsDirectory.getPath());
		}
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(settingsFile));
		Properties props = getDefaultProperties();
		props.store(out, "Generated default values");
		out.close();
		return props;
	}

}
