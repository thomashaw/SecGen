/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */
package org.snipsnap.config;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.util.Properties;

/**
 * Interface template for global configuration options
 * @author Matthias L. Jugel
 * @version $Id: Globals.java.tmpl,v 1.2 2003/12/11 13:36:54 leo Exp $
 */
public interface Globals {
  public String getGlobal(String name);
  public void setGlobal(String name, String value);

  public void loadGlobals(InputStream stream) throws IOException;
  public void storeGlobals(OutputStream stream) throws IOException;

  // get all properties
  public Properties getGlobals();
  public String getGlobalDefault(String name);

  public void setWebInfDir(File file);
  public File getWebInfDir();

  public String getVersion();

  boolean isInstalled();

  
  // automatically created interface/constants stub from
  // /u/livshits/work/benchmarks/java/security_applications/CS/snipsnap/src/org/snipsnap/config/globals.conf
  // generated on 4/28/05 10:06 AM
  // constant/getter for 'app.cache'
  public final static String APP_CACHE = "app.cache";
  public String getCache();
  public String setCache(String value);
  // constant/getter for 'app.database'
  public final static String APP_DATABASE = "app.database";
  public String getDatabase();
  public String setDatabase(String value);
  // constant/getter for 'app.encoding'
  public final static String APP_ENCODING = "app.encoding";
  public String getEncoding();
  public String setEncoding(String value);
  // constant/getter for 'app.file.store'
  public final static String APP_FILE_STORE = "app.file.store";
  public String getFileStore();
  public String setFileStore(String value);
  // constant/getter for 'app.host'
  public final static String APP_HOST = "app.host";
  public String getHost();
  public String setHost(String value);
  // constant/getter for 'app.install.key'
  public final static String APP_INSTALL_KEY = "app.install.key";
  public String getInstallKey();
  public String setInstallKey(String value);
  // constant/getter for 'app.installed'
  public final static String APP_INSTALLED = "app.installed";
  public String getInstalled();
  public String setInstalled(String value);
  // constant/getter for 'app.jdbc.driver'
  public final static String APP_JDBC_DRIVER = "app.jdbc.driver";
  public String getJdbcDriver();
  public String setJdbcDriver(String value);
  // constant/getter for 'app.jdbc.password'
  public final static String APP_JDBC_PASSWORD = "app.jdbc.password";
  public String getJdbcPassword();
  public String setJdbcPassword(String value);
  // constant/getter for 'app.jdbc.url'
  public final static String APP_JDBC_URL = "app.jdbc.url";
  public String getJdbcUrl();
  public String setJdbcUrl(String value);
  // constant/getter for 'app.jdbc.user'
  public final static String APP_JDBC_USER = "app.jdbc.user";
  public String getJdbcUser();
  public String setJdbcUser(String value);
  // constant/getter for 'app.logger'
  public final static String APP_LOGGER = "app.logger";
  public String getLogger();
  public String setLogger(String value);
  // constant/getter for 'app.path'
  public final static String APP_PATH = "app.path";
  public String getPath();
  public String setPath(String value);
  // constant/getter for 'app.port'
  public final static String APP_PORT = "app.port";
  public String getPort();
  public String setPort(String value);
  // constant/getter for 'app.protocol'
  public final static String APP_PROTOCOL = "app.protocol";
  public String getProtocol();
  public String setProtocol(String value);
  // constant/getter for 'snipsnap.server.version'
  public final static String SNIPSNAP_SERVER_VERSION = "snipsnap.server.version";
  public String getSnipsnapServerVersion();
  public String setSnipsnapServerVersion(String value);

}
