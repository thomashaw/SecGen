/*
 * Created on Aug 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

import net.eyde.personalblog.beans.Post;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import org.dom4j.io.SAXReader;

import java.io.InputStream;

import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;


/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ImportExportService {
    static private Properties hibernate_properties;
    private static Log log = LogFactory.getLog(InitializationManager.class);
    Properties connProperties;
    String insert_statement = "insert into t_posts (post_id, reference, title, category, content, created, modified, version) values (?,?,?,?,?,?,?,?)";

    public boolean loadDbConnectParms() {
        log.debug("loading DB connection parameters from hibernate file");

        URL url = this.getClass().getResource("/hibernate.properties");
        hibernate_properties = new Properties();

        try {
            hibernate_properties.load(url.openStream());
        } catch (Exception e) {
            log.error("error loading hibernate properties", e);

            return false;
        }

        log.debug("DB connect properties loaded");

        return true;
    }

    public void importPostsFromXml() {
        log.debug("load posts from xml");

        PreparedStatement stmt = null;
        Connection conn = null;
        Calendar calendar = new GregorianCalendar();

        try {
            // Load the database driver
            Class.forName(hibernate_properties.getProperty(
                    "hibernate.connection.driver_class"));

            // Get a connection to the database
            conn = DriverManager.getConnection(hibernate_properties.getProperty(
                        "hibernate.connection.url") + "?user=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.username") + "&password=" +
                    hibernate_properties.getProperty(
                        "hibernate.connection.password"));

            stmt = conn.prepareStatement(insert_statement);
        } catch (SQLException e) {
            log.debug("error code:" + e.getErrorCode());
            log.error("import error", e);
        } catch (Exception e) {
            log.error("import error", e);
        }

        Document doc;
        Element root;

        try {
            SAXReader xmlReader = new SAXReader();
            InputStream is = this.getClass().getResourceAsStream("/backup.xml");
            doc = xmlReader.read(is);
            root = doc.getRootElement();

            log.debug("ROOT NAME: " + root.getName());

            for (Iterator i = root.elementIterator("t_posts"); i.hasNext();) {
                Element post = (Element) i.next();

                Post work = new Post();

                for (Iterator j = post.elementIterator(); j.hasNext();) {
                    Element field = (Element) j.next();

                    log.debug("NAME: " + field.getName());
                    log.debug("TEXT: " + field.getText());

                    if (field.getName().equals("title")) {
                        work.setTitle(field.getText());
                    } else if (field.getName().equals("category")) {
                        work.setCategory(field.getText());
                    } else if (field.getName().equals("content")) {
                        work.setContent(field.getText());
                    } else if (field.getName().equals("created")) {
                        calendar.set(Calendar.YEAR,
                            Integer.parseInt(field.getText().substring(0, 4)));
                        calendar.set(Calendar.MONTH,
                            Integer.parseInt(field.getText().substring(5, 7))-1);
                        calendar.set(Calendar.DATE,
                            Integer.parseInt(field.getText().substring(8, 10)));
                        work.setCreated(calendar.getTime());
                    } else if (field.getName().equals("modified")) {
						calendar.set(Calendar.YEAR,
							Integer.parseInt(field.getText().substring(0, 4)));
						calendar.set(Calendar.MONTH,
							Integer.parseInt(field.getText().substring(5, 7))-1);
						calendar.set(Calendar.DATE,
							Integer.parseInt(field.getText().substring(8, 10)));
						work.setModified(calendar.getTime());
                    } else if (field.getName().equals("version")) {
                        work.setVersion(Integer.parseInt(field.getText()));
                    } else if (field.getName().equals("post_id")) {
                        work.setId(field.getText());
                    } else if (field.getName().equals("reference")) {
                        work.setReference(field.getText());
                    } else {
                        log.debug("element not found");
                    }
                }

                try {
                    stmt.setString(1, work.getId());
                    stmt.setString(2, work.getReference());
                    stmt.setString(3, work.getTitle());
                    stmt.setString(4, work.getCategory());
                    stmt.setString(5, work.getContent());
                    stmt.setTimestamp(6,new Timestamp(work.getCreated().getTime()));
                    stmt.setTimestamp(7,new Timestamp(work.getModified().getTime()));
                    stmt.setInt(8, work.getVersion());

                    stmt.executeUpdate();
                } catch (SQLException e) {
                    log.debug("error code:" + e.getErrorCode());
                    log.error("import error", e);
                }
            }
        } catch (DocumentException de) {
            log.debug("document error", de);
        } catch (Exception e) {
            log.error("import error", e);
        }

        try {
            log.debug("Closing connections");
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            log.debug("error code:" + se.getErrorCode());
            log.debug("sql error", se);
        } catch (Exception e) {
            log.debug("general error", e);
        }

        log.debug("create tables- end");
    }
}
