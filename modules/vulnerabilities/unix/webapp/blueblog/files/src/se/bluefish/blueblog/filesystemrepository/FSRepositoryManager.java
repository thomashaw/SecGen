package se.bluefish.blueblog.filesystemrepository;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.opensymphony.module.oscache.base.NeedsRefreshException;
import com.opensymphony.module.oscache.general.GeneralCacheAdministrator;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.formatters.Formatter;
import se.bluefish.blueblog.formatters.GenericFormatter;
import se.bluefish.blueblog.repository.AbstractRepositoryManager;
import se.bluefish.blueblog.repository.Repository;
import se.bluefish.blueblog.repository.RepositoryContext;

/**
 * Handles several Repositories in the file system. The directories
 * that work as roots for the repositories are specified with a
 * parameter, "rootpattern", in the configuration file. The
 * rootpattern should contain the pattern <tt>${user}</tt> to
 * indicate where the username should be in the path. 
 * 
 * @author Robert Burén
 */
public class FSRepositoryManager extends AbstractRepositoryManager {
	
	// TODO: the value below should come from a config file
	private static final int CACHE_REFRESH_TIME = 10; // 10 secs
	public static final String KEY_BLOGTYPE_PREFIX = "blogtype";
	public static final String KEY_ROOTPATTERN = "rootpattern";
	public static final String KEY_USERNAME = "${user}";
	private String blogrootpattern;
	private Map blogTypes = new HashMap(); // mime type (String) -> Formatter instance
	
	private GeneralCacheAdministrator cacheAdmin = GeneralCacheAdministrator.getInstance();	
	private RepositoryContext context;
	
	public FSRepositoryManager() {}

	/**
	 * @see se.bluefish.blueblog.repository.RepositoryManager#getRepositoryFor(String)
	 */
	public Repository getRepositoryFor(String username) {
		final String CACHE_PREFIX = "FSRepository:";
		String cacheKeyValue = CACHE_PREFIX+username;
		// first, we check the cache
		try { 
			return (Repository)cacheAdmin.getFromCache(cacheKeyValue, CACHE_REFRESH_TIME);
		} catch( NeedsRefreshException ex ) {
			// ignore the exception -- a new Repository instance will be created
		}
		
		try {
			String path=StringUtils.replace(blogrootpattern, KEY_USERNAME, username);
			File rootdir = new File(path);
			if( rootdir.exists() && rootdir.isDirectory() ) {
				Repository rep = new FSRepository(username, rootdir, blogTypes);
				cacheAdmin.putInCache(cacheKeyValue, rep);
				return rep;
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List getUsernames() {
		ArrayList usernames = new ArrayList();
		
		String pathPrefix = blogrootpattern.substring(0,blogrootpattern.indexOf(KEY_USERNAME));
		final String pathPostfix = blogrootpattern.substring(pathPrefix.length()+KEY_USERNAME.length());
		File rootPath = new File(pathPrefix);
		File[] userDirs = rootPath.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if( file.isDirectory() ) {
					File blogrootdir = new File(file.getPath()+pathPostfix);
					return blogrootdir.isDirectory();
				} else {
					return false;
				}
			}
		});
		for( int i=0; i<userDirs.length; ++i ) {
			usernames.add( userDirs[i].getName() );
		}
		Collections.sort(usernames);
		return usernames;
	}

	/* (non-Javadoc)
	 * @see se.bluefish.blueblog.repository.RepositoryManager#init(java.util.Properties)
	 */
	public void init(RepositoryContext ctxt, Properties props) throws BBException {
		context = ctxt;
		blogrootpattern = props.getProperty(KEY_ROOTPATTERN);
		for( int i=0; i<10; ++i ) {
			String blogTypeStr = props.getProperty(KEY_BLOGTYPE_PREFIX+i);
			if( blogTypeStr != null ) {
				String[] blogTypeStrArray = blogTypeStr.split(":");
				String mimeType = blogTypeStrArray.length > 0 ? blogTypeStrArray[0] : "text/plain";
				String formatterName = blogTypeStrArray.length > 1 ? blogTypeStrArray[1] : null;
				Formatter formatter = 
					formatterName != null ? 
					context.getFormatter(formatterName) :
					new GenericFormatter();
				blogTypes.put(mimeType, formatter);
			}
		}
	}
}
