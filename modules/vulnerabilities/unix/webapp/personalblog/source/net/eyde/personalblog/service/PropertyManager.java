/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

import net.eyde.personalblog.beans.BlogProperty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;

import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

/**
 * @author NEyde
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public class PropertyManager {

    private static Log log = LogFactory.getLog(PropertyManager.class);

    Configuration cfg;
    SessionFactory sf;
    Session session;
    //TODO Re-enable cache for Property Manager
    //CacheManager cache;
    //private int defaultLiveTime = 30;

	/*public PropertyManager(Properties conn, CacheManager _cache) {

		try {
			cfg = new Configuration().addClass(BlogProperty.class);
			if(conn != null){
				 cfg.setProperties(conn);
			}
			
			sf = cfg.buildSessionFactory();
			cache = _cache;

		} catch (Exception e) {
			log.error("Error while reading hibernate props", e);
			System.out.println("unable to connect to hibernate data source.");
		}
	}
*/

    public PropertyManager() {

        try {
            cfg = new Configuration().addClass(BlogProperty.class);
            sf = cfg.buildSessionFactory();

        } catch (Exception e) {
            log.error("Error while reading hibernate props", e);
            System.out.println("unable to connect to hibernate data source.");
        }

    }

	public PropertyManager(Properties connectionProps) {

		try {
			cfg = new Configuration().addClass(BlogProperty.class)
			                         .setProperties(connectionProps);
			sf = cfg.buildSessionFactory();

		} catch (Exception e) {
			log.error("Error while reading hibernate props", e);
			System.out.println("unable to connect to hibernate data source.");
		}

	}

    public void addProperty(String name, String value) throws Exception {

        try {

            session = sf.openSession();
            BlogProperty prop = new BlogProperty();
            prop.setName(name);
            prop.setValue(value);
            session.save(prop);
            session.flush();
            session.close();


        } catch (Exception e) {
            log.error("Error while adding property | name:" + name, e);
            throw e;
        }

        return;


    }

    public void updateProperty(String name, String value) {

        try {

            session = sf.openSession();
            BlogProperty prop = new BlogProperty();
            prop.setName(name);
            prop.setValue(value);
            session.update(prop, prop.getName());
            session.flush();
            session.close();

            //cache.invalidate(name); // make sure fresh copy is pulled next time
        } catch (Exception e) {
            log.error("Error while updating property | name:" + name, e);
        }

        return;


    }

    public String getProperty(String name) throws Exception {
        String propValue = this.getProperty(name, false);
        return propValue;

    }

    public String getProperty(String name, boolean freshCopy) throws Exception {
        BlogProperty prop = null;

/*
        if (freshCopy) {
            cache.invalidate(name);
        } else {
            prop = (BlogProperty) cache.getCache(name);
        }
*/
        if (prop == null) {

            try {

                session = sf.openSession();
                prop = (BlogProperty) session.load(BlogProperty.class, name);
                //cache.putCache(prop, name, defaultLiveTime);
                session.close();

            } catch (Exception e) {
                log.error("Error while retrieving  property id:" + name, e);
                throw e;
            }
        }

        return prop.getValue();

    }

//This will load the properties the first time
public void load(Properties applicationProps) {

	log.info("loading properties");

	try {
		session = sf.openSession();
			
		Enumeration props = applicationProps.keys();

		 while(props.hasMoreElements())
		 {
			BlogProperty prop = new BlogProperty();
			prop.setName((String)props.nextElement());
			prop.setValue((String)applicationProps.getProperty(prop.getName()));
			session.save(prop);
					
			log.info("Property " + prop.getName() + " ("+prop.getValue()+")"+" saved.");
		 }
		session.flush();
		session.close();
			
	} catch (Exception e) {
		log.error("error while loading properties", e);
	}
}

//This will update the PersonalBlog properties with new values
	public void updateProperties(List blogProperties) {

		log.info("updating properties");
		
		try {
			session = sf.openSession();
			
			Iterator iteratorProps = blogProperties.iterator();

			while (iteratorProps.hasNext()) {
				BlogProperty prop = (BlogProperty) iteratorProps.next();
				session.update(prop, prop.getName());
				log.info("Property " + prop.getName() + " ("+prop.getValue()+")"+" updated.");
			}

			session.flush();
			session.close();
			
		} catch (Exception e) {
			log.error("error while updating properties", e);
		}
	}

/*
     public void populate() throws HibernateException  {

    
            session = sf.openSession();
            List properties = session.find("from post in class net.eyde.personalblog.beans.BlogProperty ");
            session.close();
            BlogProperty prop;
            Iterator itr = properties.iterator();

            while (itr.hasNext()) {
                prop = (BlogProperty) itr.next();
                //cache.putCache(prop, prop.getName(), defaultLiveTime);
            }
    }
*/


    public List getProperties() {
    	
    	log.debug("retrieving all personalblog properties");

        List properties = null;
        try {
            session = sf.openSession();

            properties = session.find(
                    "from post in class net.eyde.personalblog.beans.BlogProperty ");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading properties", e);
        }

		log.debug("total retrieved properties: "+properties.size());

        return properties;

    }
}
