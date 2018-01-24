package org.roller.business;import org.roller.RollerException;import org.roller.model.ConfigManager;import org.roller.pojos.RollerConfig;import org.roller.util.RollerConfigFile;import junit.framework.Test;import junit.framework.TestSuite;////////////////////////////////////////////////////////////////////////////////** * Test Roller Config Management. */public class ConfigManagerTest extends RollerTestBase{    String basedir = System.getProperty("basedir");	String path = "web/WEB-INF/roller-config.xml";	ConfigManager configMgr = null;	RollerConfig originalConfig;    //------------------------------------------------------------------------    public ConfigManagerTest()    {        super();    }    //------------------------------------------------------------------------    public ConfigManagerTest(String name)    {        super(name);    }    //------------------------------------------------------------------------    public static Test suite()    {        return new TestSuite(ConfigManagerTest.class);    }    //------------------------------------------------------------------------    public static void main(String args[])    {        junit.textui.TestRunner.run(ConfigManagerTest.class);    }    /**     * @see junit.framework.TestCase#setUp()     */    public void setUp() throws Exception    {        super.setUp();        configMgr = getRoller().getConfigManager();    }    public void tearDown() throws Exception    {        super.tearDown();	}	public void testRollerConfigFile()	{		path = new java.io.File(basedir,path).getAbsolutePath();		RollerConfigFile bean = RollerConfigFile.readConfig( path );		assertNotNull("RollerConfigFile should not be null", bean);		assertEquals("# editor pages = 5", 5, bean.getEditorPages().size());	}	public void testReadFromFile() throws RollerException	{/* This hoses my config every time. TODO:Fix
		// first delete any existing - hold value in originalConfig		deleteConfig();		// create new RollerConfig from file at path		path = new java.io.File(basedir,path).getAbsolutePath();		getRoller().begin();		RollerConfig config = configMgr.readFromFile(path);		configMgr.storeRollerConfig(config);		assertNotNull("Got RollerConfig back", config);		getRoller().commit();		// now fetch it back out		getRoller().begin();		RollerConfig config2 = configMgr.getRollerConfig();		assertEquals("Same RollerConfig", config, config2);		getRoller().commit();		// delete our new config		if (originalConfig != null)		{			RollerConfig oldConfig = originalConfig;			deleteConfig();			getRoller().begin();			configMgr.storeRollerConfig(oldConfig);			getRoller().commit();		}*/
	}/*	private RollerConfig createNewConfig()	{		RollerConfig config = new RollerConfig();        config.setAbsoluteURL ( "http://www.rollerweblogger.org/" );        config.setRssUseCache ( Boolean.TRUE );        config.setRssCacheTime ( new Integer(4000) );        config.setNewUserAllowed ( Boolean.TRUE );        config.setAdminUsers ( "dave,lance,matt" );        config.setUserThemes ( "/themes" );        config.setEditorPages ( "editor.jsp" );        config.setEnableAggregator ( Boolean.FALSE );        config.setUploadEnabled ( Boolean.TRUE );        config.setUploadMaxDirMB ( new BigDecimal("5.0") );        config.setUploadMaxFileMB ( new BigDecimal("0.5") );        config.setUploadAllow ( "gif,jpg" );        config.setUploadForbid ( "" );        config.setUploadDir ( null );        config.setUploadPath ( "/resources" );        config.setMemDebug ( Boolean.FALSE );        config.setAutoformatComments ( Boolean.FALSE );        config.setEscapeCommentHtml ( Boolean.FALSE );        config.setEmailComments ( Boolean.FALSE );        config.setEnableLinkback ( Boolean.FALSE );        config.setSiteName ( "RollerConfig Test" );        config.setSiteDescription ( "JUnit Test of RollerConfig Manager" );        config.setEmailAddress ( "lance@brainopolis.com" );        config.setIndexDir ( "/var/lucene-index" );        return config;	}	private void deleteConfig() throws RollerException	{		getRoller().begin();		originalConfig = configMgr.getRollerConfig();		if (originalConfig != null)		{			((ConfigManagerImpl)configMgr).removeRollerConfig(originalConfig.getId());		}		getRoller().commit();	}*/}