package se.bluefish.blueblog.repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.formatters.ChainFormatter;
import se.bluefish.blueblog.formatters.Formatter;

/**
 * Singleton keeping track of all users and blogs in this application.
 * 
 * @author Robert Burén
 *
 */
public class RepositoryFactory {

	/**
	 * corresponds to the repository element in the XML config file
	 */
	private static class RepositoryConfig {
		public String manager;
		public Properties parameters = new Properties();
	}

	/**
	 * corresponds to the repository-manager element in the XML config file
	 */
	private static class RepositoryManagerConfig {
		public String className;
		public String description;
		public String name;
	}
	private static RepositoryFactory instance = null;

	/**
	 * Method findRepository.
	 * @param username
	 * @return Repository responsible for the given username, or null 
	 *          if no such repository is found
	 */
	public static Repository findRepository(String username) {
		Iterator iter = instance.repositoryManagers.iterator();
		while( iter.hasNext() ) {
			RepositoryManager mgr = (RepositoryManager)iter.next();
			Repository rep = mgr.getRepositoryFor(username);
			if( rep != null ) {
				return rep;
			}
		}
		return null;
	}

	/**
	 * Help method -- asks the servlet context for the mime
	 * type of a file (based on its filename).
	 * <br/><br/>
	 * Returns the MIME type of the specified file, or <b>null</b> 
	 * if the MIME type is not known. The MIME type is determined 
	 * by the configuration of the servlet container, and may be 
	 * specified in a web application deployment descriptor.
	 * <br/><br/> 
	 * Common MIME types are "<tt>text/html</tt>" and "<tt>image/gif</tt>".
	 * 
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	public static String getMimeType(String filename) {
		if( instance == null || instance.servletContext == null) {
			return null;
		}
		return instance.servletContext.getMimeType(filename);
	}

	public static List getRepositoryManagers() {
		return instance.repositoryManagers;
	}

	/**
	 * Returns a list of usernames (strings) known by all RepositoryManagers.
	 */
	public static List getUsernames() {
		ArrayList list=new ArrayList();
		Iterator iter = instance.repositoryManagers.iterator();
		while( iter.hasNext() ) {
			RepositoryManager mgr = (RepositoryManager)iter.next();
			list.addAll( mgr.getUsernames() );
		}
		Collections.sort(list);
		return list;
	}

	/**
	 * Returns web application URL prefix plus servlet name.
	 * These makes up the first part of the URL for all 
	 * repositories, categories and blogs.
	 */
	public static String getRootUrl() {
		return instance.rootUrl;
	}

	/**
	 * Registers repository types specified in an xml file.
	 * 
	 * @param servletContext the BBServlet context
	 * @param instream input stream from an XML file with all the 
	 *                 repository setup information
	 * 
	 * @see se.bluefish.blueblog.servlet.BBServlet
	 */
	public static void init(String rootUrl, ServletContext servletContext, InputStream instream) throws BBException {
		if( instance != null ) {
			throw new RuntimeException("RepositoryFactory initialized twice!");
		}
		instance = new RepositoryFactory();
		instance.rootUrl = rootUrl;
		instance.servletContext = servletContext;
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		Document doc;
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			doc = builder.parse(instream);
		} catch (Exception e) {
			throw new BBException("Error reading the Repositories XML configuration file.", e);
		}
		NodeList repositoryManagerElements = doc.getElementsByTagName("repository-manager");
		for( int i=0; i < repositoryManagerElements.getLength(); ++i ) {
			Node node = repositoryManagerElements.item(i);
			if( node.getNodeType() == Node.ELEMENT_NODE ) {
				Element e = (Element)node;
				instance.parseRepositoryManager( e );
			}
		}

		NodeList formatterElements = doc.getElementsByTagName("blog-formatter");
		for( int i=0; i < formatterElements.getLength(); ++i ) {
			Node node = formatterElements.item(i);
			if( node.getNodeType() == Node.ELEMENT_NODE ) {
				Element e = (Element)node;
				instance.parseFormatter( e );
			}
		}

		NodeList repositoryElements = doc.getElementsByTagName("repository");
		for( int i=0; i < repositoryElements.getLength(); ++i ) {
			Node node = repositoryElements.item(i);
			if( node.getNodeType() == Node.ELEMENT_NODE ) {
				Element e = (Element)node;
				instance.parseRepository( e );
			}
		}
		instance.createRepositoryManagersInstances();
	}
	private RepositoryContext context = new RepositoryContext();
	private RepositoryManager defaultRepositoryManager = null;
	
	// rcfgMap = repository config map. RepositoryConfig.name -> RepositoryConfig 
	private List rcfgList = new ArrayList();
	
	private List repositoryManagers = new ArrayList();
	
	// rmcfgMap = repository manager config map. RepositoryManagerConfig.name -> RepositoryManagerConfig 
	private HashMap rmcfgMap = new HashMap();
	private String rootUrl;
	private ServletContext servletContext;

	private void createRepositoryManagersInstances() throws BBException {
		Iterator repositoryIter = rcfgList.iterator();
		while( repositoryIter.hasNext() ) {
			RepositoryConfig rc = (RepositoryConfig)repositoryIter.next();
			RepositoryManagerConfig rmc = (RepositoryManagerConfig)rmcfgMap.get(rc.manager);
			if( rmc == null ) {
				throw new BBException("Error in repositories.xml: manager \"" + rc.manager
					+ "\" in repository element has no corresponding repository-manager entry!");
			}
			RepositoryManager rm;
			try {
				Class rmClass = Class.forName(rmc.className);
				rm = (RepositoryManager) rmClass.newInstance();
			} catch (Exception e) {
				throw new BBException("Error creating RepositoryManager of type "+rmc.className, e);
			}
			// TODO: take care of default parameter values here
			rm.init(context, rc.parameters);
			if( defaultRepositoryManager == null ) {
				// TODO: which RM should be default should be specified in the repositories.xml file
				// for now, the first rm in the xml file is the default
				defaultRepositoryManager = rm;
			}
			repositoryManagers.add(rm);
		}
		
		// leave these to the garbage collector:
		rmcfgMap = null;
		rcfgList = null;
	}

	private void parseFormatter(Element element) throws BBException {
		String name = null;
		String className = null;
		NodeList nl = element.getElementsByTagName("name");
		if( nl.getLength() > 0 ) {
			Node nameNode = nl.item(0);
			if( nameNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
				name = nameNode.getFirstChild().getNodeValue();
			}
		}
		nl = element.getElementsByTagName("class");
		if( nl.getLength() > 0 ) {
			Node classNode = nl.item(0);
			if( classNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
				className = classNode.getFirstChild().getNodeValue();
			}
		} else {
			nl = element.getElementsByTagName("chain");
			if( nl.getLength() > 0 ) {
				Node chainNode = nl.item(0);
				if( name != null && chainNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
					String chainStr = chainNode.getFirstChild().getNodeValue();
					String[] chainParts = chainStr.split(",");
					List formatters = new ArrayList(chainParts.length);
					for( int i=0; i<chainParts.length; ++i ) {
						Formatter f = context.getFormatter( chainParts[i] );
						if( f != null ) {
							formatters.add( f );
						}
					}
					if( formatters.size() > 0 ) {
						ChainFormatter cf = new ChainFormatter( formatters );
						cf.setName( name );
						context.addFormatter(cf);
					}
				}
			}
		}
		if( name != null && className != null ) {
			try {
				Formatter f = (Formatter)Class.forName(className).newInstance();
				f.setName( name );
				context.addFormatter(f);
			} catch (Exception e) {
				throw new BBException("Error creating formatter with name "+name+
					" and class "+className, e);
			}
		}
	}

	private void parseRepository(Element element) {
		RepositoryConfig cfg = new RepositoryConfig();
		NodeList nl = element.getElementsByTagName("manager");
		if( nl.getLength() > 0 ) {
			Element managerNode = (Element)nl.item(0);
			if( managerNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
				cfg.manager = managerNode.getFirstChild().getNodeValue().trim();
			}
		}
		nl = element.getElementsByTagName("parameter");
		for( int i=0; i<nl.getLength(); ++i ) {
			Element paramNode = (Element)nl.item(i);
			NodeList pnl = paramNode.getElementsByTagName("name");
			String paramName = null;
			String paramValue = null;
			if( pnl.getLength() > 0 ) {
				Element nameNode = (Element)pnl.item(0);
				if( nameNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
					paramName = nameNode.getFirstChild().getNodeValue().trim();
				}
			}
			pnl = paramNode.getElementsByTagName("value");
			if( pnl.getLength() > 0 ) {
				Element valueNode = (Element)pnl.item(0);
				if( valueNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
					paramValue = valueNode.getFirstChild().getNodeValue().trim();
				}
			}
			if( paramName != null && paramValue != null ) {
				cfg.parameters.setProperty(paramName, paramValue);
			}
		}
		rcfgList.add(cfg);
	}
	
	private void parseRepositoryManager(Element element) {
		RepositoryManagerConfig cfg = new RepositoryManagerConfig();
		NodeList nl = element.getElementsByTagName("name");
		if( nl.getLength() > 0 ) {
			Element nameNode = (Element)nl.item(0);
			if( nameNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
				cfg.name = nameNode.getFirstChild().getNodeValue().trim();
			}
		}
		nl = element.getElementsByTagName("class");
		if( nl.getLength() > 0 ) {
			Element classNode = (Element) nl.item(0);
			if( classNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
				cfg.className = classNode.getFirstChild().getNodeValue().trim();
			}
		}
		nl = element.getElementsByTagName("description");
		if( nl.getLength() > 0 ) {
			Element descNode = (Element)nl.item(0);
			if( descNode.getFirstChild().getNodeType() == Node.TEXT_NODE ) {
				cfg.description = descNode.getFirstChild().getNodeValue().trim();
			}
		}
		rmcfgMap.put(cfg.name, cfg);
	}
}
