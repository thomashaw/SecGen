package net.sf.jboard.model.forum.clients;

import net.sf.jboard.fwk.exception.ClientException;
import net.sf.jboard.model.forum.dto.ForumCategory;
import net.sf.jboard.model.forum.dto.ForumItem;
import net.sf.jboard.model.forum.dto.ForumMessage;
import net.sf.jboard.model.forum.dto.ForumRoot;
import net.sf.jboard.model.forum.dto.ForumThread;

/**
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 *
 */
public interface ForumFacadeInterface {

    /**
     * @param cat
     */
    public ForumRoot createCategory(ForumCategory cat)throws ClientException;

    /**
     * @param l
     * @return
     */
    public ForumCategory readCategory(Long l)throws ClientException;

    /**
     * @param cat
     */
    public ForumRoot updateCategory(ForumCategory cat)throws ClientException;

    /**
     * @param fit
     */
    public ForumRoot createForumItem(ForumItem fit)throws ClientException;

    /**
     * @param l
     * @return
     */
    public ForumItem readForumItem(Long l)throws ClientException;

    /**
     * @param l
     */
    public ForumItem deleteForumThread(Long forumThreadId,Long forumItemId)throws ClientException;

    /**
     * @param ft
     */
    public ForumItem updateForumThread(ForumThread ft)throws ClientException;

    /**
     * @param l
     * @return
     */
    public ForumThread readForumThread(Long l)throws ClientException;

    /**
     * @param ft
     * @param msg
     * @return
     */
    public ForumItem createForumThread(ForumThread ft, ForumMessage msg)throws ClientException;

    /**
     * @param msg
     */
    public ForumThread updateMessage(ForumMessage msg)throws ClientException;

    /**
     * @param l
     * @return
     */
    public ForumMessage readMessage(Long l)throws ClientException;

    /**
     * @param msg
     */
    public ForumThread createMessage(ForumMessage msg)throws ClientException;

    /**
     * @param l
     */
    public ForumRoot deleteForumItem(Long l)throws ClientException;

    /**
     * @param l
     */
    public ForumThread deleteMessage(Long msgId,Long forumThreadId)throws ClientException;

    /**
     * @return
     */
    public ForumRoot getPanorama()throws ClientException;

    /**
     * @param l
     */
    public ForumRoot deleteCategory(Long l)throws ClientException;

    /**
     * @param fit
     */
    public ForumRoot updateForumItem(ForumItem fit)throws ClientException;

}
