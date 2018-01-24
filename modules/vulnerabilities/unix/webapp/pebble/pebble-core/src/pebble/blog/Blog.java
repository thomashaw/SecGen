/*
 * Copyright (c) 2003-2004, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pebble.blog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Blog extends AbstractBlog {

  /** the name of the file containing blog properties */
  public static final String BLOG_PROPERTIES_FILE = "blog.properties";

  /** the log used by this class */
  private static Log log = LogFactory.getLog(Blog.class);

  /** the filesystem root of this blog */
  private String root;

  protected static final String FALSE = "false";
  protected static final String TRUE = "true";

  public static final String NAME_KEY = "name";
  public static final String AUTHOR_KEY = "author";
  public static final String DESCRIPTION_KEY = "description";
  public static final String IMAGE_KEY = "image";
  public static final String TIMEZONE_KEY = "timeZone";
  public static final String RECENT_BLOG_ENTRIES_ON_HOME_PAGE_KEY = "recentBlogEntriesOnHomePage";
  public static final String RECENT_COMMENTS_ON_HOME_PAGE_KEY = "recentCommentsOnHomePage";
  public static final String LANGUAGE_KEY = "language";
  public static final String COUNTRY_KEY = "country";
  public static final String CHARACTER_ENCODING_KEY = "characterEncoding";

  /** the properties for this blog */
  protected Properties properties;

  /** the url of this blog */
  protected String url = "";

  /**
   * Creates a new Blog instance, based at the specified location.
   *
   * @param root    an absolute path pointing to the root directory of the blog
   */
  public Blog(String root) {
    super(null);
    this.root = root;

    init();
  }

  protected void init() {
    loadProperties();
  }

  /**
   * Loads the properties for this blog, from the blog.properties file
   * in the root directory.
   */
  protected void loadProperties() {
    try {
      this.properties = new Properties(getDefaultProperties());

      File blogPropertiesFile = new File(getRoot(), BLOG_PROPERTIES_FILE);
      if (!blogPropertiesFile.exists()) {
        return;
      }

      FileInputStream fin = new FileInputStream(blogPropertiesFile);
      properties.load(fin);
      fin.close();
    } catch (IOException ioe) {
      log.error("A blog.properties file at " + getRoot() + " cannot be loaded", ioe);
    }
  }

  /**
   * Gets the default properties for a blog.
   *
   * @return    a Properties instance
   */
  protected abstract Properties getDefaultProperties();

  /**
   * Gets the filesystem root for this blog.
   *
   * @return  a String representing an absolute path
   */
  public String getRoot() {
    return root;
  }

  /**
   * Sets the filesystem root for this blog.
   *
   * @param root    a String representing the absolute path
   */
  protected void setRoot(String root) {
    this.root = root;
  }

  /**
   * Gets the properties associated with this blog.
   *
   * @return  a java.util.Properties object
   */
  public Properties getProperties() {
    return (Properties)properties.clone();
  }

  /**
   * Gets a named property for this blog.
   *
   * @param key     the property name/key
   */
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Sets a named property for this blog.
   *
   * @param key     the property name/key
   * @param value   the property value
   */
  public void setProperty(String key, String value) {
    if (key != null) {
      if (value != null) {
        properties.setProperty(key, value);
      } else {
        removeProperty(key);
      }
    }
  }

  /**
   * Removes a named property for this blog.
   *
   * @param key     the property name/key
   */
  public void removeProperty(String key) {
    properties.remove(key);
  }

  /**
   * Stores the properties associated with this blog.
   *
   * @throws BlogException    if the properties can't be stored
   */
  public void storeProperties() throws BlogException {
    try {
      FileOutputStream fout = new FileOutputStream(new File(getRoot(), BLOG_PROPERTIES_FILE));
      properties.store(fout, "Properties for " + getName());
      fout.flush();
      fout.close();
    } catch (IOException ioe) {
      log.error(ioe);
      throw new BlogException("Properties could not be saved : " + ioe.getMessage());
    }
  }

  /**
   * Gets the name of this blog.
   *
   * @return    the name
   */
  public String getName() {
    return properties.getProperty(NAME_KEY);
  }

  /**
   * Gets the author of this blog.
   *
   * @return    the author
   */
  public String getAuthor() {
    return properties.getProperty(AUTHOR_KEY);
  }

  /**
   * Gets the description of this blog.
   *
   * @return    the description
   */
  public String getDescription() {
    return properties.getProperty(DESCRIPTION_KEY);
  }

  /**
   * Gets the image for this blog.
   *
   * @return    a URL pointing to an image
   */
  public String getImage() {
    return properties.getProperty(IMAGE_KEY);
  }

  /**
   * Gets the URL where this blog is deployed.
   *
   * @return  a URL as a String
   */
  public String getUrl() {
    return BlogManager.getInstance().getBaseUrl() + this.url;
  }

  /**
   * Gets the domain name where this blog is deployed.
   *
   * @return  a domain name as a String
   */
  public String getDomainName() {
    // and set the domain name
    String url = getUrl();
    int index = url.indexOf("://");
    String domainName = url.substring(index+3);
    index = domainName.indexOf("/");
    domainName = domainName.substring(0, index);

    if (domainName.indexOf(":") > -1) {
      // the domain name still has a port number so remove it
      domainName = domainName.substring(0, domainName.indexOf(":"));
    }

    return domainName;
  }

/**
   * Sets the URL where this blog is deployed.
   *
   * @param newUrl   the URL as a String
   */
  public void setUrl(String newUrl) {
    this.url = newUrl;
  }

  /**
   * Gets the ID of the time zone for the blog.
   *
   * @return    a String (Europe/London by default)
   */
  public String getTimeZoneId() {
    return properties.getProperty(TIMEZONE_KEY);
  }

  /**
   * Gets the TimeZone instance representing the timezone for the blog.
   *
   * @return    a TimeZone instance
   */
  public TimeZone getTimeZone() {
    return TimeZone.getTimeZone(getTimeZoneId());
  }

  /**
   * Gets a Calendar instance representing the current moment in time,
   * with the timezone set to be the same as that specified for this blog.
   *
   * @return    a Calendar instance
   */
  public Calendar getCalendar() {
    return Calendar.getInstance(getTimeZone());
  }

  /**
   * Gets the number of recent blog entries that are displayed on the home page.
   *
   * @return  an int (3 by default)
   */
  public int getRecentBlogEntriesOnHomePage() {
    return Integer.parseInt(properties.getProperty(RECENT_BLOG_ENTRIES_ON_HOME_PAGE_KEY));
  }

  /**
   * Gets the number of recent comments that are displayed on the home page.
   *
   * @return  an int (0 by default)
   */
  public int getRecentCommentsOnHomePage() {
    return Integer.parseInt(properties.getProperty(RECENT_COMMENTS_ON_HOME_PAGE_KEY));
  }

  /**
   * Gets the character encoding in use by this blog.
   *
   * @return  the character encoding as an IANA registered character set code
   */
  public String getCharacterEncoding() {
    return properties.getProperty(CHARACTER_ENCODING_KEY);
  }

  /**
   * Gets string instance representing the language for the blog.
   *
   * @return    a two-letter ISO language code (en by default)
   */
  public String getLanguage() {
    return properties.getProperty(LANGUAGE_KEY);
  }

  /**
   * Gets string instance representing the country for the blog.
   *
   * @return    a two-letter ISO country code (GB by default)
   */
  public String getCountry() {
    return properties.getProperty(COUNTRY_KEY);
  }

  /**
   * Gets the Locale instance for the blog.
   *
   * @return  a Locale instance based upon the language and country.
   */
  public Locale getLocale() {
    return new Locale(getLanguage(), getCountry());
  }

  /**
   * Gets the location where the blog images are stored.
   *
   * @return    an absolute, local path on the filing system
   */
  public String getImagesDirectory() {
    return getRoot() + File.separator + "images";
  }

  /**
   * Gets the location where the blog search indexes are stored.
   *
   * @return    an absolute, local path on the filing system
   */
  public String getIndexDirectory() {
    return getRoot() + File.separator + "index";
  }

  /**
   * Gets the location where the blog logs are stored.
   *
   * @return    an absolute, local path on the filing system
   */
  public String getLogsDirectory() {
    return getRoot() + File.separator + "logs";
  }

  /**
   * Gets the most recent blog entries for a given category, the number
   * of which is specified.
   *
   * @param   category          a Category instance, or null
   * @param   numberOfEntries   the number of entries to get
   * @return  a List containing the most recent blog entries
   */
  public abstract List getRecentBlogEntries(Category category, int numberOfEntries);

  /**
   * Gets the most recent blog entries, the number of which is specified.
   *
   * @param   numberOfEntries   the number of entries to get
   * @return  a List containing the most recent blog entries
   */
  public List getRecentBlogEntries(int numberOfEntries) {
    return getRecentBlogEntries(null, numberOfEntries);
  }

  /**
   * Gets the most recent blog entries, the number of which is taken from
   * the recentBlogEntriesOnHomePage property.
   *
   * @return  a List containing the most recent blog entries
   */
  public List getRecentBlogEntries() {
    return this.getRecentBlogEntries(getRecentBlogEntriesOnHomePage());
  }

  /**
   * Gets the most recent blog entries, the number of which is taken from
   * the recentBlogEntriesOnHomePage property.
   *
   * @return  a List containing the most recent blog entries
   */
  public List getRecentBlogEntries(Category category) {
    return getRecentBlogEntries(category, getRecentBlogEntriesOnHomePage());
  }

  /**
   * Gets the date that this blog was last updated.
   *
   * @return  a Date instance representing the time of the most recent entry
   */
  public abstract Date getLastModified();

  /**
   * Logs this request for blog.
   *
   * @param request   the HttpServletRequest instance for this request
   */
  public abstract void log(HttpServletRequest request);

  /**
   * Gets a string representation of this object.
   *
   * @return  a String
   */
  public String toString() {
    return getName();
  }

}