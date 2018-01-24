package org.roller.presentation.forms;

import org.roller.RollerException;
import java.util.Locale;

/**
 * Generated by XDoclet/ejbdoclet/strutsform. This class can be further processed with XDoclet/webdoclet/strutsconfigxml.
 *
 * @struts.form name="rollerConfigForm"
 */
public class RollerConfigForm
    extends    org.apache.struts.action.ActionForm
    implements java.io.Serializable
{
    protected java.lang.String id;
    protected java.lang.String absoluteURL;
    protected java.lang.Boolean rssUseCache;
    protected java.lang.Integer rssCacheTime;
    protected java.lang.Boolean newUserAllowed;
    protected java.lang.String adminUsers;
    protected java.lang.String userThemes;
    protected java.lang.String editorPages;
    protected java.lang.Boolean enableAggregator;
    protected java.lang.Boolean uploadEnabled;
    protected java.math.BigDecimal uploadMaxDirMB;
    protected java.math.BigDecimal uploadMaxFileMB;
    protected java.lang.String uploadAllow;
    protected java.lang.String uploadForbid;
    protected java.lang.String uploadDir;
    protected java.lang.String uploadPath;
    protected java.lang.Boolean memDebug;
    protected java.lang.Boolean autoformatComments;
    protected java.lang.Boolean escapeCommentHtml;
    protected java.lang.Boolean emailComments;
    protected java.lang.Boolean enableLinkback;
    protected java.lang.String siteDescription;
    protected java.lang.String siteName;
    protected java.lang.String emailAddress;
    protected java.lang.String indexDir;
    protected java.lang.Boolean encryptPasswords;
    protected java.lang.String algorithm;
    protected java.lang.String databaseVersion;
    protected java.util.List editorPagesList;

    /** Default empty constructor. */
    public RollerConfigForm() {}

    /** Constructor that takes the data object as argument. */
    public RollerConfigForm(org.roller.pojos.RollerConfig dataHolder, java.util.Locale locale) throws RollerException
    {
       copyFrom(dataHolder, locale);
    }

    public java.lang.String getId()
    {
        return this.id;
    }

   /** 
    * @struts.validator type="required" msgkey="errors.required"
    */
    public void setId( java.lang.String id )
    {
        this.id = id;
    }

    public java.lang.String getAbsoluteURL()
    {
        return this.absoluteURL;
    }

   /** 
    */
    public void setAbsoluteURL( java.lang.String absoluteURL )
    {
        this.absoluteURL = absoluteURL;
    }

    public java.lang.Boolean getRssUseCache()
    {
        return this.rssUseCache;
    }

   /** 
    */
    public void setRssUseCache( java.lang.Boolean rssUseCache )
    {
        this.rssUseCache = rssUseCache;
    }

    public java.lang.Integer getRssCacheTime()
    {
        return this.rssCacheTime;
    }

   /** 
    */
    public void setRssCacheTime( java.lang.Integer rssCacheTime )
    {
        this.rssCacheTime = rssCacheTime;
    }

    public java.lang.Boolean getNewUserAllowed()
    {
        return this.newUserAllowed;
    }

   /** 
    */
    public void setNewUserAllowed( java.lang.Boolean newUserAllowed )
    {
        this.newUserAllowed = newUserAllowed;
    }

    public java.lang.String getAdminUsers()
    {
        return this.adminUsers;
    }

   /** 
    */
    public void setAdminUsers( java.lang.String adminUsers )
    {
        this.adminUsers = adminUsers;
    }

    public java.lang.String getUserThemes()
    {
        return this.userThemes;
    }

   /** 
    */
    public void setUserThemes( java.lang.String userThemes )
    {
        this.userThemes = userThemes;
    }

    public java.lang.String getEditorPages()
    {
        return this.editorPages;
    }

   /** 
    */
    public void setEditorPages( java.lang.String editorPages )
    {
        this.editorPages = editorPages;
    }

    public java.lang.Boolean getEnableAggregator()
    {
        return this.enableAggregator;
    }

   /** 
    */
    public void setEnableAggregator( java.lang.Boolean enableAggregator )
    {
        this.enableAggregator = enableAggregator;
    }

    public java.lang.Boolean getUploadEnabled()
    {
        return this.uploadEnabled;
    }

   /** 
    */
    public void setUploadEnabled( java.lang.Boolean uploadEnabled )
    {
        this.uploadEnabled = uploadEnabled;
    }

    public java.math.BigDecimal getUploadMaxDirMB()
    {
        return this.uploadMaxDirMB;
    }

   /** 
    */
    public void setUploadMaxDirMB( java.math.BigDecimal uploadMaxDirMB )
    {
        this.uploadMaxDirMB = uploadMaxDirMB;
    }

    public java.math.BigDecimal getUploadMaxFileMB()
    {
        return this.uploadMaxFileMB;
    }

   /** 
    */
    public void setUploadMaxFileMB( java.math.BigDecimal uploadMaxFileMB )
    {
        this.uploadMaxFileMB = uploadMaxFileMB;
    }

    public java.lang.String getUploadAllow()
    {
        return this.uploadAllow;
    }

   /** 
    */
    public void setUploadAllow( java.lang.String uploadAllow )
    {
        this.uploadAllow = uploadAllow;
    }

    public java.lang.String getUploadForbid()
    {
        return this.uploadForbid;
    }

   /** 
    */
    public void setUploadForbid( java.lang.String uploadForbid )
    {
        this.uploadForbid = uploadForbid;
    }

    public java.lang.String getUploadDir()
    {
        return this.uploadDir;
    }

   /** 
    */
    public void setUploadDir( java.lang.String uploadDir )
    {
        this.uploadDir = uploadDir;
    }

    public java.lang.String getUploadPath()
    {
        return this.uploadPath;
    }

   /** 
    */
    public void setUploadPath( java.lang.String uploadPath )
    {
        this.uploadPath = uploadPath;
    }

    public java.lang.Boolean getMemDebug()
    {
        return this.memDebug;
    }

   /** 
    */
    public void setMemDebug( java.lang.Boolean memDebug )
    {
        this.memDebug = memDebug;
    }

    public java.lang.Boolean getAutoformatComments()
    {
        return this.autoformatComments;
    }

   /** 
    */
    public void setAutoformatComments( java.lang.Boolean autoformatComments )
    {
        this.autoformatComments = autoformatComments;
    }

    public java.lang.Boolean getEscapeCommentHtml()
    {
        return this.escapeCommentHtml;
    }

   /** 
    */
    public void setEscapeCommentHtml( java.lang.Boolean escapeCommentHtml )
    {
        this.escapeCommentHtml = escapeCommentHtml;
    }

    public java.lang.Boolean getEmailComments()
    {
        return this.emailComments;
    }

   /** 
    */
    public void setEmailComments( java.lang.Boolean emailComments )
    {
        this.emailComments = emailComments;
    }

    public java.lang.Boolean getEnableLinkback()
    {
        return this.enableLinkback;
    }

   /** 
    */
    public void setEnableLinkback( java.lang.Boolean enableLinkback )
    {
        this.enableLinkback = enableLinkback;
    }

    public java.lang.String getSiteDescription()
    {
        return this.siteDescription;
    }

   /** 
    */
    public void setSiteDescription( java.lang.String siteDescription )
    {
        this.siteDescription = siteDescription;
    }

    public java.lang.String getSiteName()
    {
        return this.siteName;
    }

   /** 
    */
    public void setSiteName( java.lang.String siteName )
    {
        this.siteName = siteName;
    }

    public java.lang.String getEmailAddress()
    {
        return this.emailAddress;
    }

   /** 
    */
    public void setEmailAddress( java.lang.String emailAddress )
    {
        this.emailAddress = emailAddress;
    }

    public java.lang.String getIndexDir()
    {
        return this.indexDir;
    }

   /** 
    */
    public void setIndexDir( java.lang.String indexDir )
    {
        this.indexDir = indexDir;
    }

    public java.lang.Boolean getEncryptPasswords()
    {
        return this.encryptPasswords;
    }

   /** 
    */
    public void setEncryptPasswords( java.lang.Boolean encryptPasswords )
    {
        this.encryptPasswords = encryptPasswords;
    }

    public java.lang.String getAlgorithm()
    {
        return this.algorithm;
    }

   /** 
    */
    public void setAlgorithm( java.lang.String algorithm )
    {
        this.algorithm = algorithm;
    }

    public java.lang.String getDatabaseVersion()
    {
        return this.databaseVersion;
    }

   /** 
    */
    public void setDatabaseVersion( java.lang.String databaseVersion )
    {
        this.databaseVersion = databaseVersion;
    }

    public java.util.List getEditorPagesList()
    {
        return this.editorPagesList;
    }

   /** 
    */
    public void setEditorPagesList( java.util.List editorPagesList )
    {
        this.editorPagesList = editorPagesList;
    }

    /**
     * Copy values from this form bean to the specified data object.
     * Only copies primitive types (Boolean, boolean, String, Integer, int, Timestamp, Date)
     */
    public void copyTo(org.roller.pojos.RollerConfig dataHolder, Locale locale) throws RollerException
    {

        dataHolder.setId(this.id);

        dataHolder.setAbsoluteURL(this.absoluteURL);

        dataHolder.setRssUseCache(this.rssUseCache);

        dataHolder.setRssCacheTime(this.rssCacheTime);

        dataHolder.setNewUserAllowed(this.newUserAllowed);

        dataHolder.setAdminUsers(this.adminUsers);

        dataHolder.setUserThemes(this.userThemes);

        dataHolder.setEditorPages(this.editorPages);

        dataHolder.setEnableAggregator(this.enableAggregator);

        dataHolder.setUploadEnabled(this.uploadEnabled);

        dataHolder.setUploadAllow(this.uploadAllow);

        dataHolder.setUploadForbid(this.uploadForbid);

        dataHolder.setUploadDir(this.uploadDir);

        dataHolder.setUploadPath(this.uploadPath);

        dataHolder.setMemDebug(this.memDebug);

        dataHolder.setAutoformatComments(this.autoformatComments);

        dataHolder.setEscapeCommentHtml(this.escapeCommentHtml);

        dataHolder.setEmailComments(this.emailComments);

        dataHolder.setEnableLinkback(this.enableLinkback);

        dataHolder.setSiteDescription(this.siteDescription);

        dataHolder.setSiteName(this.siteName);

        dataHolder.setEmailAddress(this.emailAddress);

        dataHolder.setIndexDir(this.indexDir);

        dataHolder.setEncryptPasswords(this.encryptPasswords);

        dataHolder.setAlgorithm(this.algorithm);

        dataHolder.setDatabaseVersion(this.databaseVersion);

    }

    /**
     * Copy values from specified data object to this form bean.
     * Includes all types.
     */
    public void copyFrom(org.roller.pojos.RollerConfig dataHolder, Locale locale) throws RollerException
    {

        this.id = dataHolder.getId();

        this.absoluteURL = dataHolder.getAbsoluteURL();

        this.rssUseCache = dataHolder.getRssUseCache();

        this.rssCacheTime = dataHolder.getRssCacheTime();

        this.newUserAllowed = dataHolder.getNewUserAllowed();

        this.adminUsers = dataHolder.getAdminUsers();

        this.userThemes = dataHolder.getUserThemes();

        this.editorPages = dataHolder.getEditorPages();

        this.enableAggregator = dataHolder.getEnableAggregator();

        this.uploadEnabled = dataHolder.getUploadEnabled();

        this.uploadAllow = dataHolder.getUploadAllow();

        this.uploadForbid = dataHolder.getUploadForbid();

        this.uploadDir = dataHolder.getUploadDir();

        this.uploadPath = dataHolder.getUploadPath();

        this.memDebug = dataHolder.getMemDebug();

        this.autoformatComments = dataHolder.getAutoformatComments();

        this.escapeCommentHtml = dataHolder.getEscapeCommentHtml();

        this.emailComments = dataHolder.getEmailComments();

        this.enableLinkback = dataHolder.getEnableLinkback();

        this.siteDescription = dataHolder.getSiteDescription();

        this.siteName = dataHolder.getSiteName();

        this.emailAddress = dataHolder.getEmailAddress();

        this.indexDir = dataHolder.getIndexDir();

        this.encryptPasswords = dataHolder.getEncryptPasswords();

        this.algorithm = dataHolder.getAlgorithm();

        this.databaseVersion = dataHolder.getDatabaseVersion();

    }

    public void doReset(
    	org.apache.struts.action.ActionMapping mapping, 
    	javax.servlet.ServletRequest request)
    {

        this.id = null;

        this.absoluteURL = null;

        this.rssUseCache = null;

        this.rssCacheTime = null;

        this.newUserAllowed = null;

        this.adminUsers = null;

        this.userThemes = null;

        this.editorPages = null;

        this.enableAggregator = null;

        this.uploadEnabled = null;

        this.uploadAllow = null;

        this.uploadForbid = null;

        this.uploadDir = null;

        this.uploadPath = null;

        this.memDebug = null;

        this.autoformatComments = null;

        this.escapeCommentHtml = null;

        this.emailComments = null;

        this.enableLinkback = null;

        this.siteDescription = null;

        this.siteName = null;

        this.emailAddress = null;

        this.indexDir = null;

        this.encryptPasswords = null;

        this.algorithm = null;

        this.databaseVersion = null;

    }
    public void reset(
    	org.apache.struts.action.ActionMapping mapping, 
    	javax.servlet.ServletRequest request)
    {
        doReset(mapping, request);
    }
    public void reset(
    	org.apache.struts.action.ActionMapping mapping, 
    	javax.servlet.http.HttpServletRequest request)
    {
        doReset(mapping, request);
    }
}
