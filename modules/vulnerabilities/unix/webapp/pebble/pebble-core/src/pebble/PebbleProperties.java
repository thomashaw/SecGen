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
package pebble;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.util.RelativeDate;

/**
 * A singleton representing configurable properties for Pebble.
 *
 * @author    Simon Brown
 */
public class PebbleProperties {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(PebbleProperties.class);

  /** the singleton instance */
  private static final PebbleProperties instance = new PebbleProperties();

  private static final String BUILD_VERSION_KEY = "build.version";
  private static final String BUILD_DATE_KEY = "build.date";
  private static final String BLOG_DIR_KEY = "blog.dir";
  private static final String BLOG_URL_KEY = "blog.url";
  private static final String BLOG_MULTIUSER_KEY = "blog.multiUser";
  public static final String FILE_UPLOAD_SIZE_KEY = "blog.fileUploadSize";
  public static final String FILE_UPLOAD_QUOTA_KEY = "blog.fileUploadQuota";
  public static final String DAO_FACTORY_KEY = "pebble.daoFactory";

  /** the Properties object that backs this instance */
  private Properties properties;

  /** the time that Pebble was started */
  private Date startTime;

  /**
   * Default, no args constructor made private for the singleton pattern.
   */
  private PebbleProperties() {
    try {
      Properties defaults = new Properties();
      defaults.setProperty(BLOG_DIR_KEY, "${user.home}/blog");
      defaults.setProperty(BLOG_MULTIUSER_KEY, "false");
      defaults.setProperty(FILE_UPLOAD_SIZE_KEY, "2048");
      defaults.setProperty(FILE_UPLOAD_QUOTA_KEY, "-1");
      defaults.setProperty(DAO_FACTORY_KEY, "pebble.blog.persistence.file.FileDAOFactory");

      this.properties = new Properties(defaults);
      properties.load(getClass().getClassLoader().getResourceAsStream("pebble.properties"));

      // and copy build properties into the main properties
      Properties buildProperties = new Properties();
      InputStream in = getClass().getClassLoader().getResourceAsStream("pebble-build.properties");
      if (in != null) {
        buildProperties.load(in);
        properties.setProperty(BUILD_VERSION_KEY, buildProperties.getProperty(BUILD_VERSION_KEY));
        properties.setProperty(BUILD_DATE_KEY, buildProperties.getProperty(BUILD_DATE_KEY));
        in.close();
      }

      Enumeration e = properties.propertyNames();
      while (e.hasMoreElements()) {
        String name = (String)e.nextElement();
        log.info(" " + name + " = " + properties.getProperty(name));
      }

      // and note when Pebble was started
      this.startTime = new Date();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      e.printStackTrace();
    }
  }

  /**
   * Gets the singleton instance.
   *
   * @return    a PebbleProperties instance
   */
  public static PebbleProperties getInstance() {
    return instance;
  }

  /**
   * Gets the version of Pebble being used.
   *
   * @return    the version number as a String
   */
  public String getVersion() {
    return properties.getProperty(BUILD_VERSION_KEY);
  }

  /**
   * Gets the version of Pebble being used.
   *
   * @return    the version number as a String
   */
  public String getBuildVersion() {
    return getVersion();
  }

  /**
   * Gets the date that Pebble was built.
   *
   * @return    the date as a String
   */
  public String getBuildDate() {
    return properties.getProperty(BUILD_DATE_KEY);
  }

  /**
   * Gets the value of blog.dir.
   *
   * @return    a String
   */
  public String getBlogDir() {
    return properties.getProperty(BLOG_DIR_KEY);
  }

  /**
   * Gets the value of blog.url.
   *
   * @return    a String
   */
  public String getBlogUrl() {
    return properties.getProperty(BLOG_URL_KEY);
  }

  /**
   * Gets the value of blog.multiUser.
   *
   * @return    a String
   */
  public boolean getBlogMultiUser() {
    return properties.getProperty(BLOG_MULTIUSER_KEY).equalsIgnoreCase("true");
  }

  /**
   * Gets the maximum size of uploads in KB.
   *
   * @return  an int (2048 by default)
   */
  public int getFileUploadSize() {
    return Integer.parseInt(properties.getProperty(FILE_UPLOAD_SIZE_KEY));
  }

  /**
   * Gets the size of the quote for file uploads in KB.
   *
   * @return  an int (10240 by default)
   */
  public int getFileUploadQuota() {
    return Integer.parseInt(properties.getProperty(FILE_UPLOAD_QUOTA_KEY));
  }

  /**
   * Gets the name of the DAOFactory implementation.
   *
   * @return  a fully qualified class name
   */
  public String getDAOFactory() {
    return properties.getProperty(DAO_FACTORY_KEY);
  }

  /**
   * Gets the amount of time that Pebble has been running for.
   *
   * @return  a number of milliseconds
   */
  public RelativeDate getUptime() {
    return new RelativeDate(new Date().getTime() - startTime.getTime());
  }

}