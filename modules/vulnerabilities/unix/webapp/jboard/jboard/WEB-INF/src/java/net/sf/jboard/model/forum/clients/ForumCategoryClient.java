/*
jboard is a java bulletin board.
version $Name:  $
http://sourceforge.net/projects/jboard/
Copyright (C) 2003 Charles GAY
This file is part of jboard.
jboard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

jboard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jboard; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.sf.jboard.model.forum.clients;

import net.sf.jboard.fwk.exception.*;
import net.sf.jboard.model.forum.dao.ForumDAOFactory;
import net.sf.jboard.model.forum.dao.ForumDAOInterface;
import net.sf.jboard.model.forum.dto.ForumCategory;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.struts.fwk.actions.ConstantsMessages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * the ForumCategory client which interact with the DAO layer to act on the
 * DATA layer.
 * the visibility of this class is reduces to package.
 * the only public class is ForumFacadeClient.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.12 $
 */
  public class ForumCategoryClient extends ForumBaseClient implements ConstantsMessages {

    private static Log logger = LogFactory.getLog(ForumCategoryClient.class);
    private static ForumDAOInterface dao;


    /**
     * constructor.
     */
     protected ForumCategoryClient() {
        super();
        dao = ForumDAOFactory.getDAO();
    }

    /**
     * create a ForumCategory.
     *
     * @param cat
     *
     * @return boolean
     *
     * @throws ClientException
     */
    public ForumRoot createCategory(ForumCategory cat) throws ClientException {

        logger.debug("begin createCategory (ForumCategoryClient)");

        try {

            dao.createCategory(cat);
            return dao.readPanorama();
        } catch(DAOException daoex) {

            logger.error(" DAO problem"+daoex.getMessage());
            throw new ClientException(ERRORS_DAO);

        } catch(Exception ex) {

            logger.error(" DAO problem"+ ex.getMessage());
            throw new ClientException(ERRORS_DAO);
        }

       

       

    }

    /**
     * return a ForumCategory.
     *
     * @param catId
     *
     * @return ForumCategory
     *
     * @throws DAOException
     */
    public ForumCategory readCategory(Long catId) throws ClientException {

        ForumCategory cat;
		try {
			cat = ForumDAOFactory.getDAO().readCategory(catId);
		} catch (DAOException e) {
			logger.error(""+e.getMessage());
            throw new ClientException(e.getMessage());
		}

        return cat;

    }

    /**
     * update a ForumCategory.
     *
     * @param p_catId
     * @param p_cat
     *
     * @return boolean
     *
     * @throws DAOException
     */
    public ForumRoot updateCategory( ForumCategory cat) throws ClientException {

         try {
			dao.updateCategory(cat);
            return dao.readPanorama();
		} catch (DAOException e) {
			logger.error(""+e.getMessage());
            throw new ClientException(e.getMessage());
		}
         
    }

    /**
     * delete a ForumCategory.
     *
     * @param p_catId
     *
     * @return boolean
     *
     * @throws DAOException
     */
    public ForumRoot deleteCategory(Long p_catId) throws ClientException {

        try {
			dao.deleteCategory(p_catId);
            return dao.readPanorama();
            
		} catch (DAOException e) {
            logger.error(""+e.getMessage());
            throw new ClientException(e.getMessage());
		}

        

    }

}
