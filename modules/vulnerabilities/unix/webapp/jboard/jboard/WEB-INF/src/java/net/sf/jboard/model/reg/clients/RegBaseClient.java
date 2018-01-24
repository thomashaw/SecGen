package net.sf.jboard.model.reg.clients;

import java.util.Map;


import net.sf.jboard.model.reg.dao.RegDAOFactory;
import net.sf.jboard.model.reg.dao.RegDAOInterface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public class RegBaseClient {
    private static Log logger = LogFactory.getLog(RegBaseClient.class);

    /**
     * constructor.
     */
    public RegBaseClient() {
        super();
    
    }

    public  static void init(String daoImplementation,String dataSrcName,Map map){
       
          RegDAOFactory.initRegFactory( daoImplementation, dataSrcName, map);
          RegDAOInterface regDAO = RegDAOFactory.getDAO();
          
       
    }
        /**
         * Convenience method that returns the current {@link RegDAOInterface}
         * implementation.
         *
         * @return ForumDAOInterface
         */
        public  static RegDAOInterface getDAO() {

           
            return RegDAOFactory.getDAO();

        }

    }
