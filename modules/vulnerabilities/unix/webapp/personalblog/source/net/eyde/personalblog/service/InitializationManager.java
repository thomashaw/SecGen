/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.Element;

import org.dom4j.io.SAXReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;


/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class InitializationManager {
    static private Properties hibernate_properties;
    private static Log log = LogFactory.getLog(InitializationManager.class);
    Properties connProperties;

    /**
     *
     */
    public InitializationManager() {
        //get hibernate properties from hibernate.properties file
    }

    /**
     *
     */
    public InitializationManager(Properties connectionProperties) {
        connProperties = connectionProperties;
    }

    //    public void createHibernateConfigFile(String inDriver, String inUrl,
    //        String inUser, String inPassword, String inPath) {
    public void createHibernateConfigFile(String inPath) {
        log.debug("initializationPersonalBlog - start");

        try {
            File hibernate = new File(inPath);
            log.debug("writing to hibernate.properties -start");
            log.debug("filepath: " + inPath);

            BufferedWriter writer = new BufferedWriter(new FileWriter(hibernate));
            writer.write("hibernate.connection.driver_class=" +
                connProperties.getProperty("hibernate.connection.driver_class"));
            writer.newLine();
            writer.write("hibernate.connection.url=" +
                connProperties.getProperty("hibernate.connection.url"));
            writer.newLine();
            writer.write("hibernate.connection.username=" +
                connProperties.getProperty("hibernate.connection.username"));
            writer.newLine();
            writer.write("hibernate.connection.password=" +
                connProperties.getProperty("hibernate.connection.password"));
            writer.close();

            log.debug("writing to hibernate.properties - stop");
        } catch (Exception e) {
            log.error("Error while writing hibernate properties");
            log.error(e.toString());
        }

        log.debug("initializationPersonalBlog - end");
    }

    // Here I am using the Hibernate properties to create a direct JDBC connection
    public void createTables(String fileName) {
        log.debug("createTables - start");

        try {
            // Load the database driver
            Class.forName(hibernate_properties.getProperty(
                    "hibernate.connection.driver_class"));

            // Get a connection to the database
            Connection conn = DriverManager.getConnection(hibernate_properties.getProperty(
                        "hibernate.connection.url") + "?user=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.username") + "&password=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.password"));

            Statement stmt = conn.createStatement();

            Document doc;
            Element root;

            SAXReader xmlReader = new SAXReader();
            InputStream is = this.getClass().getResourceAsStream(fileName);
            doc = xmlReader.read(is);
            root = doc.getRootElement();

            Iterator elementIterator = root.elementIterator();

            while (elementIterator.hasNext()) {
                Element element = (Element) elementIterator.next();

                log.debug("NAME: " + element.getName());
                log.debug("TEXT: " + element.getText());

                try {
                    stmt.executeUpdate(element.getText());
                } catch (SQLException e) {
                    log.debug("error code:" + e.getErrorCode());
                    log.error("The table already exists", e);
                }
            }

            log.debug("Closing connections");

            stmt.close();
            conn.close();
        } catch (Exception e) {
            log.error("Error while creating tables", e);
        }

        log.debug("create tables- end");
    }

    /**
      * @return true if hibernate file is in the classpath
      */
    public boolean hibernateFileExists() {
        log.debug("Checking for existence of hibernate files");

        URL url = this.getClass().getResource("/hibernate.properties");
        hibernate_properties = new Properties();

        try {
            hibernate_properties.load(url.openStream());
        } catch (Exception e) {
            log.error("error while checking for hibernate file", e);

            return false;
        }

        log.debug("hibernate file exists");

        return true;
    }

    /**
       * @return
       */
    public boolean dataBaseOn() {
        boolean result = true;

        try {
            //no need to test if file exist, just will get here if file is there
            // Load the database driver
            Class.forName(hibernate_properties.getProperty(
                    "hibernate.connection.driver_class"));

            // Get a connection to the database
            Connection conn = DriverManager.getConnection(hibernate_properties.getProperty(
                        "hibernate.connection.url") + "?user=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.username") + "&password=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.password"));

            log.debug("Closing connections");

            conn.close();
        } catch (Exception e) {
            log.debug("error while testing if database is on", e);
            result = false;
        }

        return result;
    }

    /**
             * @return
             */
    public boolean tablesCreated(Vector v) {
        boolean result = true;

        try {
            //no need to test if file exist, just will get here if file is there
            // Load the database driver
            Class.forName(hibernate_properties.getProperty(
                    "hibernate.connection.driver_class"));

            // Get a connection to the database
            Connection conn = DriverManager.getConnection(hibernate_properties.getProperty(
                        "hibernate.connection.url") + "?user=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.username") + "&password=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.password"));

            Statement stmt = conn.createStatement();

            try {
                stmt.executeQuery("select * from t_comments");
            } catch (SQLException e) {
                v.add("t_comments");
                log.debug("didn't find t_comments table");
            }

            try {
                stmt.executeQuery("select * from t_posts");
            } catch (SQLException e1) {
                v.add("t_posts");
                log.debug("didn't find t_posts table");
            }

            try {
                stmt.executeQuery("select * from t_referrers");
            } catch (SQLException e2) {
                v.add("t_referrers");
                log.debug("didn't find t_referrers");
            }

            try {
                stmt.executeQuery("select * from t_properties");
            } catch (SQLException e3) {
                v.add("t_properties");
                log.debug("didn't find t_properties");
            }

            log.debug("Closing connections");

            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (SQLException e) {
            log.error(e);
        }

        if (v.size() > 0) {
            result = false;
        }

        return result;
    }

    /**
     * @return
     */
    public boolean propertiesMissing() {
        log.debug("Checking for missing properties");

        boolean result = true;

        try {
            //no need to test if file exist, just will get here if file is there
            // Load the database driver
            Class.forName(hibernate_properties.getProperty(
                    "hibernate.connection.driver_class"));

            // Get a connection to the database
            Connection conn = DriverManager.getConnection(hibernate_properties.getProperty(
                        "hibernate.connection.url") + "?user=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.username") + "&password=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.password"));

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from t_properties");
            Properties p = new Properties();

            while (rs.next()) {
                p.put(rs.getString("name"), rs.getString("value"));
            }

            log.debug("Closing connections");

            stmt.close();
            conn.close();

            //TODO:change for PersonalBLogService constants
            result = false;
            result = (!p.containsKey("weblog.title")) ? true : result;
            result = (!p.containsKey("weblog.description")) ? true : result;
            result = (!p.containsKey("weblog.ownerpicture")) ? true : result;
            result = (!p.containsKey("weblog.ownernickname")) ? true : result;
            result = (!p.containsKey("weblog.url")) ? true : result;
            result = (!p.containsKey("weblog.email")) ? true : result;
            result = (!p.containsKey("links.post")) ? true : result;
            result = (!p.containsKey("emoticon.values")) ? true : result;
            result = (!p.containsKey("emoticon.images")) ? true : result;
            result = (!p.containsKey("logon.id")) ? true : result;
            result = (!p.containsKey("logon.password")) ? true : result;
            result = (!p.containsKey("category.titles")) ? true : result;
            result = (!p.containsKey("category.values")) ? true : result;
            result = (!p.containsKey("category.images")) ? true : result;
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (SQLException e) {
            log.error(e);
        }

        return result;
    }
}
