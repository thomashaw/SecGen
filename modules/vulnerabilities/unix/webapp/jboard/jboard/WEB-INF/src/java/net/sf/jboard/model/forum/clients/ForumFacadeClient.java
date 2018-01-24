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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.model.forum.dto.ForumCategory;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.model.forum.dto.ForumThread;
import net.sf.jboard.model.forum.dto.ForumMessage;

/**
 * <strong>unique</strong> EntryPoint of the Forum Business part.
 * it regroups all the business Forum uses case in one location to reduce couplage
 * between the presentation and the business tiers.
 * it is the implementation of the <strong>facade</strong> design pattern.
 * it is the only <strong>public</strong>class in this package to reduce couplage 
 * with the presentation tiers.
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.6 $
 * 
 */
public class ForumFacadeClient implements ForumFacadeInterface{
    
    
    private static Log logger = LogFactory.getLog(ForumFacadeClient.class);
    
    /**
     * constructor.
     * this constructor is <i>protected</i>, because it can be called only by her factory.
     *
     */
    protected ForumFacadeClient(){
        
    }
    /**
     * create a ForumCategory.
     * @param cat
     * @throws ClientException
     */
    public ForumRoot getPanorama() throws ClientException{
        ForumRootClient fpc = null;
        fpc = new ForumRootClient();
        
			return fpc.getPanorama();
	}
    
    
    
    /**
     * create a ForumCategory.
     * @param cat
     * @throws ClientException
     */
    public ForumRoot createCategory(ForumCategory cat) throws ClientException{
        ForumCategoryClient fcc = null;
        fcc = new ForumCategoryClient();
        return fcc.createCategory(cat);
        
    }
    /**
     * delete a ForumCategory.
     * @param catId
     * @throws DAOException
     */
    public ForumRoot deleteCategory(Long catId) throws ClientException{
        ForumCategoryClient fcc = null;
        fcc = new ForumCategoryClient();
       
		return fcc.deleteCategory(catId);
		
        
    }
    /**
     * read a ForumCategory.
     * @param catId
     * @throws DAOException
     */
    public ForumCategory readCategory(Long catId) throws ClientException{
        ForumCategoryClient fcc = null;
        fcc = new ForumCategoryClient();
         return fcc.readCategory(catId);
        
    }
    /**
     * update a ForumCategory.
     * @param cat
     * @throws DAOException
     */
    public ForumRoot updateCategory(ForumCategory cat) throws ClientException{
        ForumCategoryClient fcc = null;
        fcc = new ForumCategoryClient();
        return fcc.updateCategory(cat);
        
    }
    /**
     * create a ForumItem.
     * @param fit
     * @throws ClientException
     */
    public ForumRoot createForumItem(ForumItem fit) throws ClientException{
        ForumItemClient fic = null;
        fic = new ForumItemClient();
        return fic.createForumItem(fit);
        
    }
    /**
     * delete a ForumItem.
     * @param fitId
     * @throws ClientException
     */
    public ForumRoot deleteForumItem(Long fitId) throws ClientException{
        ForumItemClient fic = null;
        fic = new ForumItemClient();
        return fic.deleteForumItem(fitId);
        
    }
    /**
     * read a ForumItem.
     * @param fitId
     * @throws ClientException
     */
    public ForumItem readForumItem(Long fitId) throws ClientException{
        ForumItemClient fic = null;
        fic = new ForumItemClient();
        return fic.readForumItem(fitId);
        
    }
    /**
     * update a ForumItem.
     * @param fit
     * @throws ClientException
     */
    public ForumRoot updateForumItem(ForumItem fit) throws ClientException{
        ForumItemClient fic = null;
        fic = new ForumItemClient();
        return fic.updateForumItem(fit);
        
    }
    /**
     * create a ForumThread.
     * @param ft
     * @param msg
     * @throws ClientException
     */
    public ForumItem createForumThread(ForumThread ft,ForumMessage msg) throws ClientException{
        ForumThreadClient ftc = null;
        ftc = new ForumThreadClient();
        return ftc.createForumThread(ft,msg);
        
    }
    /**
     * delete a ForumThread.
     * @param ftId
     * @throws DAOException
     */
    public ForumItem deleteForumThread(Long ftId,Long fitId) throws ClientException{
        ForumThreadClient ftc = null;
        ftc = new ForumThreadClient();
       
	    	return ftc.deleteForumThread(ftId,fitId);
		
    }
    /**
     * read a ForumThread.
     * @param ftId
     * @throws DAOException
     */
    public ForumThread readForumThread(Long ftId) throws ClientException{
        ForumThreadClient ftc = null;
        ftc = new ForumThreadClient();
        return ftc.readForumThread(ftId);
        
    }
    /**
     * update a ForumThread.
     * @param ft
     * @throws DAOException
     */
    public ForumItem updateForumThread(ForumThread ft) throws ClientException{
        ForumThreadClient ftc = null;
        ftc = new ForumThreadClient();
        
	    return ftc.updateForumThread(ft);
		
    } 
    /**
     * create a ForumMessage.
     * @param msg
     * @throws ClientException
     */
    public ForumThread createMessage(ForumMessage msg) throws ClientException{
        ForumMessageClient fmc = null;
        ForumThread ft;
        fmc = new ForumMessageClient();
        ft = fmc.createMessage(msg);
        return ft;
    } 
    /**
     * delete a ForumMessage.
     * @param msgId
     * @throws DAOException
     */
    public ForumThread deleteMessage(Long msgId,Long ftId) throws ClientException{
        ForumMessageClient fmc = null;
        fmc = new ForumMessageClient();
        
		return fmc.deleteMessage(msgId,ftId);
		
        
    } 
    /**
     * read a ForumMessage.
     * @param msgId
     * @throws DAOException
     */
    public ForumMessage readMessage(Long msgId) throws ClientException{
        ForumMessageClient fmc = null;
        fmc = new ForumMessageClient();
        return fmc.readMessage(msgId);
        
    }  
    /**
     * update a ForumMessage.
     * @param msg
     * @throws DAOException
     */
    public ForumThread updateMessage(ForumMessage msg) throws ClientException{
        ForumMessageClient fmc = null;
        fmc = new ForumMessageClient();
        return fmc.updateMessage(msg);
        
    } 
}
