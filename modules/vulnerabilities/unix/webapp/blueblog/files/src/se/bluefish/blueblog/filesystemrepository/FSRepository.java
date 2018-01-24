package se.bluefish.blueblog.filesystemrepository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import com.opensymphony.module.oscache.base.NeedsRefreshException;
import com.opensymphony.module.oscache.general.GeneralCacheAdministrator;

import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.repository.Repository;

/**
 * A Repository stored in a local file system.
 * 
 * @author Robert Burén
 *
 */
class FSRepository implements Repository {

	// TODO: the value below should come from a config file
	private static final int CACHE_REFRESH_TIME = 10; // 10 secs

	/**
	 * Default name of the settings directory.
	 */
	public static final String DIRNAME_SETTINGS = ".settings";
	
	/**
	 * Default name of the properties file for the Repository.
	 * The file is stored in the settings directory.
	 * 
	 * @see #DIRNAME_SETTINGS
	 */
	public static final String FILENAME_SETTINGS = "repository.properties";

	private Map blogTypes; // see same in FSRepositoryManager
	
	private GeneralCacheAdministrator cacheAdmin = GeneralCacheAdministrator.getInstance();
	private String name;
	private String ownerUsername;
	
	private File repositoryDirectory;
	private Category rootCategory;
	private File settingsDirectory;
	private File settingsFile;
	
	/**
	 * Constructor FSRepository.
	 * 
	 * @param owner username of owner (first part of blog URL)
	 * @param rootdir filesystem directory which is the root of the repository
	 * @param formatter to be used when formatting the Blogs
	 * @param blogfileendings array of file endings specifying blog files, e.g. {".txt",".text"}
	 * 
	 * @throws IOException when there is a file io problem
	 */
	public FSRepository(String owner, File rootdir, Map blogTypes) throws IOException {
		this.ownerUsername = owner;
		this.repositoryDirectory = rootdir;
		this.blogTypes = blogTypes;
		this.settingsDirectory = new File(this.repositoryDirectory.getPath()+File.separator+DIRNAME_SETTINGS);
		this.settingsFile = new File(this.settingsDirectory.getPath()+File.separator+FILENAME_SETTINGS);
		Properties props = getInitialProperties();
		this.name = props.getProperty("name");
	}

	/**
	 * @see se.bluefish.blueblog.repository.Repository#getCategoryByPath(String)
	 */
	public Category getCategoryByPath(String path) {
		final String CACHE_PREFIX = "FSCategory:";
		String cacheKeyValue = CACHE_PREFIX+getOwner()+'/'+path;
		if( path.length() < 1 || path.charAt(0) != '/' ) {
			// add starting slash if missing
			path = "/" + path;
		}
		path.replace('/', File.separatorChar);
		if( path.length() > 1 && path.endsWith("/") ) {
			// remove possible ending slash
			path = path.substring(0, path.length() - 1 );
		}
		
		// first check in the cache
		try {
			return (Category)cacheAdmin.getFromCache(cacheKeyValue, CACHE_REFRESH_TIME);
		} catch( NeedsRefreshException ex ) {
			// ignore the exception -- a new instance will be created
		}
		
		try {
			Category cat = null;
			if( path.equals("/") ) {
				// root category
				cat = new FSCategory(this, null, repositoryDirectory, blogTypes);
			} else {
				// some subcategory
				Category parent = getCategoryByPath(path.substring(0,path.lastIndexOf('/')));
				File catdir = new File(repositoryDirectory.getPath()+path);
				cat = new FSCategory(this, parent, catdir, blogTypes);
			}
			// put in the cache for later
			cacheAdmin.putInCache(cacheKeyValue, cat);
			return cat;
		} catch (IOException e) {
			return null;
		}
	}
	
	private Properties getDefaultProperties() {
		ResourceBundle defaults = ResourceBundle.getBundle("defaults");
		Properties props=new Properties();
		// TODO the handling of genitiv suffix is specific to Swedish locale (and not even that, completely)
		props.setProperty("name", defaults.getString("default.repositoryname").replaceAll("\\{0\\}", ownerUsername+"s"));
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
	
	/**
	 * Name of the Repository, ie name of the blog for this user.
	 * 
	 * @see se.bluefish.blueblog.repository.Repository#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see se.bluefish.blueblog.repository.Repository#getOwner()
	 */
	public String getOwner() {
		return ownerUsername;
	}

	/**
	 * @see se.bluefish.blueblog.repository.Repository#getRootCategory()
	 */
	public Category getRootCategory() {
		if( rootCategory == null ) {
			rootCategory = getCategoryByPath("/");
		}
		return rootCategory;
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
