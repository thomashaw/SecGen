package org.roller.business; 

import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test Roller Weblog Management.
 */
public class WeblogManagerTest extends RollerTestBase 
{    
    String dest_id = null;
    String c1_id = null;
    String c2_id = null;
    String c3_id = null;
    
    //------------------------------------------------------------------------
    public WeblogManagerTest(String name) 
    {
        super(name);
    }
    
    //------------------------------------------------------------------------
    public static void main(String args[]) 
    {
        junit.textui.TestRunner.run(WeblogManagerTest.class);
    }
    
    //------------------------------------------------------------------------
    public static Test suite() 
    {
        return new TestSuite(WeblogManagerTest.class);
    }

    //-----------------------------------------------------------------------
    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();
        setUpTestWeblogs();
        setUpCategoryTree();
    }
    
    /** Add a small category tree to the small test website (mWebsite). */
    public void setUpCategoryTree() throws Exception
    {
        getRoller().begin();
        
        WebsiteData wd = null;
        WeblogCategoryData root = null;
        WeblogManager wmgr = getRoller().getWeblogManager();

        wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
        root = wmgr.getRootWeblogCategory(wd);

        // create empty destination folder
        WeblogCategoryData dest = wmgr.createWeblogCategory();
        dest.setName("c0");
        dest.setParent(root);
        dest.setWebsite(wd);
        dest.save();       
        
        // create three level src category with entry in each category
        WeblogCategoryData c1 = wmgr.createWeblogCategory();
        c1.setName("c1");
        c1.setParent(root);
        c1.setWebsite(wd);
        c1.save();
        
        WeblogEntryData e1 = new WeblogEntryData(
            null, c1, wd, "title1", null, "text", "anchor", 
            new Timestamp(0), new Timestamp(0), Boolean.FALSE);
        e1.save();
        
        WeblogCategoryData c2 = wmgr.createWeblogCategory(); 
        c2.setName("c2");
        c2.setParent(c1);
        c2.setWebsite(wd);
        c2.save();
      
        WeblogEntryData e2 = new WeblogEntryData(
            null, c2, wd, "title2", null, "text", "anchor", 
            new Timestamp(0), new Timestamp(0), Boolean.FALSE);
        e2.save();
        
        WeblogCategoryData c3 = wmgr.createWeblogCategory(); 
        c3.setName("c3");
        c3.setParent(c2);
        c3.setWebsite(wd);
        c3.save();
        
        WeblogEntryData e3 = new WeblogEntryData(
            null, c3, wd, "title3", null, "text", "anchor", 
            new Timestamp(0), new Timestamp(0), Boolean.FALSE);
        e3.save();
        
        getRoller().commit(); 
        
        dest_id = dest.getId();
        c1_id = c1.getId();  
        c2_id = c2.getId();  
        c3_id = c3.getId();  
    }
    
    //-----------------------------------------------------------------------
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();
        tearDownTestWeblogs();
    }
    
//    //-----------------------------------------------------------------------
//    
//    /** Add a small category tree to the small test website (mWebsite). */
//    public void testGetRecent() throws Exception
//    {
//        getRoller().begin();
//        
//        WebsiteData wd = null;
//        WeblogCategoryData root = null;
//        WeblogManager wmgr = getRoller().getWeblogManager();
//
//        wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
//        root = wmgr.getRootWeblogCategory(wd);
//
//        // create top level folders
//        WeblogCategoryData t1 = wmgr.createWeblogCategory();
//        t1.setName("toplevel1");
//        t1.setParent(root);
//        t1.setWebsite(wd);
//        t1.save();       
//        
//        WeblogCategoryData t2 = wmgr.createWeblogCategory();
//        t2.setName("toplevel2");
//        t2.setParent(root);
//        t2.setWebsite(wd);
//        t2.save();       
//        
//        WeblogCategoryData t3 = wmgr.createWeblogCategory();
//        t3.setName("toplevel3");
//        t3.setParent(root);
//        t3.setWebsite(wd);
//        t3.save(); 
//        
//        getRoller().commit(); 
//        
//        getRoller().begin(); 
//        
//        // get persistent instances
//        t1 = wmgr.retrieveWeblogCategory(t1.getId());
//        wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
//        
//        // Create four entries in 1st category
//        WeblogEntryData e1 = new WeblogEntryData(
//                null, t1, wd, "title1", null, "text1", "anchor", 
//                new Timestamp(0), new Timestamp(0), Boolean.FALSE);
//        e1.save();
//        
//        WeblogEntryData e2 = new WeblogEntryData(
//                null, t1, wd, "title2", null, "text2", "anchor", 
//                new Timestamp(0), new Timestamp(0), Boolean.FALSE);
//        e2.save();
//        
//        WeblogEntryData e3 = new WeblogEntryData(
//                null, t1, wd, "title3", null, "text3", "anchor", 
//                new Timestamp(0), new Timestamp(0), Boolean.FALSE);
//        e3.save();
//        
//        WeblogEntryData e4 = new WeblogEntryData(
//                null, t1, wd, "title4", null, "text4", "anchor", 
//                new Timestamp(0), new Timestamp(0), Boolean.FALSE);
//        e4.save();
//        
//        getRoller().commit(); 
//        
//        getRoller().begin();
//        
//        //List entries = wmgr.getRecentWeblogEntriesArray(
//            //wd.getUser().getUserName(), new Date(), "toplevel1", 15, false);
//        
//        List entries = wmgr.getWeblogEntries(
//                        wd,
//                        null,                       // startDate
//                        new Date(),                 // endDate
//                        "toplevel1",                 // catName
//                        WeblogManager.ALL,           // status
//                        new Integer(15));           // maxEntries
//
//        assertEquals(4, entries.size());
//        
//        getRoller().rollback();
//    }
//    
//    public void testGetWeblogCategoryRoot() throws Exception
//    {
//        getRoller().begin();
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        assertNotNull(wmgr.getRootWeblogCategory(mWebsite));
//        getRoller().rollback();
//    }    
//    
//    public void testGetWeblogCategories() throws Exception
//    {
//        getRoller().begin();
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        assertEquals(8, wmgr.getWeblogCategories(mWebsite).size());
//        getRoller().rollback();
//    }    
//    
//    public void testWeblogCategoryPaths() throws Exception
//    {
//        WebsiteData wd = null;
//        WeblogCategoryData root = null;
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        getRoller().begin();
//        
//        wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
//        root = wmgr.getRootWeblogCategory(wd);
//
//        WeblogCategoryData f1 = wmgr.createWeblogCategory();
//        f1.setName("f1");
//        f1.setParent(root);
//        f1.setWebsite(wd);
//        f1.save();
//        
//        WeblogCategoryData f2 = wmgr.createWeblogCategory(); 
//        f2.setName("f2");
//        f2.setParent(f1);
//        f2.setWebsite(wd);
//        f2.save();
//      
//        WeblogCategoryData f3 = wmgr.createWeblogCategory(); 
//        f3.setName("f3");
//        f3.setParent(f2);
//        f3.setWebsite(wd);
//        f3.save();
//        
//        getRoller().commit();
//        
//        getRoller().begin();
//        
//        // check count of descendents and ancestors
//        f1 = wmgr.retrieveWeblogCategory(f1.getId());        
//        assertEquals(2, f1.getAllDescendentAssocs().size());
//        assertEquals(1, f1.getAncestorAssocs().size());
//        
//        f2 = wmgr.retrieveWeblogCategory(f2.getId());        
//        assertEquals(1, f2.getAllDescendentAssocs().size());
//        assertEquals(2, f2.getAncestorAssocs().size());
//        
//        f3 = wmgr.retrieveWeblogCategory(f3.getId());        
//        assertEquals(0, f3.getAllDescendentAssocs().size());
//        assertEquals(3, f3.getAncestorAssocs().size());
//        
//        // test get by path
//        assertEquals("f1", 
//            wmgr.getWeblogCategoryByPath(wd, null, "f1").getName());
//        
//        assertEquals("f1", 
//            wmgr.getWeblogCategoryByPath(wd, null, "/f1").getName());
//        
//        assertEquals("f2", 
//            wmgr.getWeblogCategoryByPath(wd, null, "/f1/f2").getName());
//        
//        assertEquals("f3", 
//            wmgr.getWeblogCategoryByPath(wd, null, "/f1/f2/f3").getName());
//        
//        // test path creation
//        f3 = wmgr.getWeblogCategoryByPath(wd, null, "/f1/f2/f3");
//        String pathString = wmgr.getPath(f3);
//        String[] pathArray = Utilities.stringToStringArray(pathString,"/");
//        assertEquals("f1", pathArray[0]);
//        assertEquals("f2", pathArray[1]);
//        assertEquals("f3", pathArray[2]);
//        
//        getRoller().commit();
//    }    
//        
//    //-----------------------------------------------------------------------
//    
//    public void testGetWeblogEntryByAnchor() throws Exception
//    {
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        WeblogEntryData entry1 = (WeblogEntryData)mEntriesCreated.get(0);
//        
//        WebsiteData website = entry1.getWebsite();
//        
//        WeblogEntryData entry2 = wmgr.getWeblogEntryByAnchor(
//            website, entry1.getAnchor() );
//        
//        assertEquals( entry1.getId(), entry2.getId() );
//    }
//          
//    //-----------------------------------------------------------------------
//    public void testGetWeblogLastUpdateTime() throws Exception
//    {
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        WeblogEntryData entry1 = (WeblogEntryData)mEntriesCreated.get(0);
//        
//        WebsiteData website = entry1.getWebsite();
//        UserData user = website.getUser();        
//        Date updateTime = wmgr.getWeblogLastPublishTime(user.getUserName());
//        
//        long time1 = updateTime.getTime();
//        long time2 = entry1.getPubTime().getTime();
//        
//        assertEquals( time2, time1 );
//    }
//    
//    /** Count weblog entries in a weblogEntry map */
//    private int countEntries( Map entryMap ) 
//    {
//        int count = 0;
//        Iterator days = entryMap.values().iterator();
//        while (days.hasNext())
//        {
//            List dayEntries = (List) days.next();
//            Iterator entries = dayEntries.iterator();
//            while (entries.hasNext())
//            {
//                entries.next();
//                count++;
//            }
//        }
//        return count;
//    }
//          
//    public void testIsWeblogCategoryInUse() throws Exception
//    {
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        UserManager umgr = getRoller().getUserManager();
//                    
//        getRoller().begin();
//        
//        WebsiteData website = umgr.retrieveWebsite(mWebsite.getId());
//        WeblogEntryData entry = (WeblogEntryData)mEntriesCreated.get(2);
//        WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(website);
//        
//        WeblogCategoryData usedCat = 
//            wmgr.retrieveWeblogCategory(entry.getCategory().getId()); 
//                         
//        WeblogCategoryData unusedCat = wmgr.createWeblogCategory(
//            website, rootCat, "testy", "tasty", "testy.gif");
//        unusedCat.save();
//            
//        getRoller().commit();
//        
//        getRoller().begin();
//        usedCat = wmgr.retrieveWeblogCategory(usedCat.getId());
//        assertTrue(usedCat.isInUse());
//        
//        unusedCat = wmgr.retrieveWeblogCategory(unusedCat.getId());
//        assertFalse(unusedCat.isInUse());       
//        getRoller().commit();
//    }
//
//    public void testStoreComment() throws Exception
//    {
//        getRoller().begin();
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        // Get entry to which comment will be added
//        WeblogEntryData entry1 = (WeblogEntryData)mEntriesCreated.get(1);
//        
//        // Ensure that entry is a persistent instance
//        entry1 = getRoller().getWeblogManager().retrieveWeblogEntry(
//            entry1.getId());
//        
//        CommentData comment = new CommentData(
//            null, 
//            entry1, 
//            "TestCommentUser", 
//            "test@test.com", 
//            "", 
//            "This is a test", 
//            new Timestamp(new Date().getTime()), 
//            Boolean.FALSE);      // spam
//            
//        comment.save();
//        getRoller().commit();
//        
//        List comments = wmgr.getComments(entry1.getId());
//        assertTrue(comments.size() > mCommentCount);
//        
//        getRoller().begin();
//        wmgr.removeComment(comment.getId());
//        getRoller().commit();
//    }   
//    
//    public void testMoveCategoryContents() throws RollerException
//    {
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        WeblogCategoryData c1 = wmgr.retrieveWeblogCategory(c1_id);       
//        //WeblogCategoryData c2 = wmgr.retrieveWeblogCategory(c2_id);       
//        //WeblogCategoryData c3 = wmgr.retrieveWeblogCategory(c3_id);       
//        WeblogCategoryData dest = wmgr.retrieveWeblogCategory(dest_id);              
//        
//        getRoller().begin();
//               
//        // verify number of entries in each category
//        dest = wmgr.retrieveWeblogCategory(dest.getId());
//        c1 = wmgr.retrieveWeblogCategory(c1.getId());
//        assertEquals(0, dest.retrieveWeblogEntries(true).size());
//        assertEquals(0, dest.retrieveWeblogEntries(false).size());
//        assertEquals(1, c1.retrieveWeblogEntries(false).size());
//        assertEquals(3, c1.retrieveWeblogEntries(true).size());
//        
//        // move contents of source category c1 to destination catetory dest
//        c1.moveContents(dest);
//        
//        getRoller().commit(); 
//        
//        getRoller().begin();
//        
//        // after move, verify number of entries in each category
//        dest = wmgr.retrieveWeblogCategory(dest.getId());
//        c1 = wmgr.retrieveWeblogCategory(c1.getId());
//        assertEquals(3, dest.retrieveWeblogEntries(true).size());
//        assertEquals(3, dest.retrieveWeblogEntries(false).size());        
//        assertEquals(0, c1.retrieveWeblogEntries(true).size());
//        assertEquals(0, c1.retrieveWeblogEntries(false).size());
//        
//        getRoller().commit();      
//    }
//    
//    public void testMoveCategory() throws RollerException
//    {
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        WeblogCategoryData c1 = wmgr.retrieveWeblogCategory(c1_id);       
//        WeblogCategoryData c2 = wmgr.retrieveWeblogCategory(c2_id);       
//        WeblogCategoryData c3 = wmgr.retrieveWeblogCategory(c3_id);       
//        WeblogCategoryData dest = wmgr.retrieveWeblogCategory(dest_id);              
//        
//        getRoller().begin();
//               
//        // verify number of entries in each category
//        dest = wmgr.retrieveWeblogCategory(dest.getId());
//        c1 = wmgr.retrieveWeblogCategory(c1.getId());
//        assertEquals(0, dest.retrieveWeblogEntries(true).size());
//        assertEquals(0, dest.retrieveWeblogEntries(false).size());
//        assertEquals(1, c1.retrieveWeblogEntries(false).size());
//        assertEquals(3, c1.retrieveWeblogEntries(true).size());
//        
//        // move contents of source category c1 to destination catetory dest
//        c1.setParent(dest);
//        c1.save();
//        
//        getRoller().commit(); 
//        
//        getRoller().begin();
//        
//        // after move, verify number of entries in each category
//        dest = wmgr.retrieveWeblogCategory(dest.getId());
//        c1 = wmgr.retrieveWeblogCategory(c1_id);       
//        c2 = wmgr.retrieveWeblogCategory(c2_id);       
//        c3 = wmgr.retrieveWeblogCategory(c3_id);
//               
//        assertEquals(3, dest.retrieveWeblogEntries(true).size());
//        assertEquals(0, dest.retrieveWeblogEntries(false).size());
//                
//        assertEquals(dest, c1.getParent());
//        assertEquals(c1,   c2.getParent());
//        assertEquals(c2,   c3.getParent());
//
//        assertEquals(1, c1.retrieveWeblogEntries(false).size());
//        assertEquals(1, c2.retrieveWeblogEntries(false).size());
//        assertEquals(1, c3.retrieveWeblogEntries(false).size()); 
//        
//        List entries = c1.retrieveWeblogEntries(true);
//        assertEquals(3, entries.size());
//        
//        getRoller().commit();      
//    }
//    
//    public void testNextPrevPost() throws RollerException
//    {
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        
//        // category: root
//        WeblogEntryData entry0 = (WeblogEntryData)mEntriesCreated.get(0);
//        WeblogEntryData entry1 = (WeblogEntryData)mEntriesCreated.get(1);
//        WeblogEntryData entry2 = (WeblogEntryData)mEntriesCreated.get(2);
//        WeblogEntryData entry3 = (WeblogEntryData)mEntriesCreated.get(3);
//        WeblogEntryData entry4 = (WeblogEntryData)mEntriesCreated.get(4);
//        WeblogEntryData entry5 = (WeblogEntryData)mEntriesCreated.get(5);
//        WeblogEntryData entry6 = (WeblogEntryData)mEntriesCreated.get(6);
//        WeblogEntryData entry7 = (WeblogEntryData)mEntriesCreated.get(7);
//        
//        // next and prev only get published entries
//        
//        // entry0 is the first published entry
//        assertEquals(null, wmgr.getPreviousEntry(entry0, null));
//        
//        // next published entry is entry2
//        assertEquals(entry2, wmgr.getNextEntry(entry0, null));
//        
//        // prev to entry2 is entry 0
//        assertEquals(entry0, wmgr.getPreviousEntry(entry2, null));
//        
//        // constrain prev/next by category
//        
//        WeblogCategoryData cat = (WeblogCategoryData)mCategoriesCreated.get(1);
//        
//        assertEquals(null, wmgr.getPreviousEntry(entry5, "/root-cat1/root-cat1-cat0"));
//        assertEquals(entry7, wmgr.getNextEntry(entry5, "/root-cat1/root-cat1-cat0"));
//        assertEquals(entry5, wmgr.getPreviousEntry(entry7, "/root-cat1/root-cat1-cat0"));
//    }
//    
//    public void testGetComments() throws RollerException
//    {
//        getRoller().begin();
//        WeblogEntryData entry0 = (WeblogEntryData)mEntriesCreated.get(0);
//        WeblogManager wmgr = getRoller().getWeblogManager();
//        assertEquals(mCommentCount, wmgr.getComments(entry0.getId()).size());
//        getRoller().commit();
//    }
    
    public void testQueryWeblogEntries() throws RollerException
    {
        getRoller().begin();
        WeblogManager wmgr = getRoller().getWeblogManager();
        UserManager umgr = getRoller().getUserManager();
        
        WebsiteData website = (WebsiteData)mWebsites.get(0);
        website = umgr.retrieveWebsite(website.getId());

        List publishedEntries = wmgr.getWeblogEntries(
                        website,                 // userName
                        null,                    // startDate
                        new Date(),              // endDate
                        null,                    // catName
                        WeblogManager.PUB_ONLY,  // status
                        null);                   // maxEntries
        assertEquals(mExpectedPublishedEntryCount, publishedEntries.size());
                    
        List draftEntries = wmgr.getWeblogEntries(
                        website,                 // userName
                        null,                    // startDate
                        new Date(),              // endDate
                        null,                    // catName
                        WeblogManager.DRAFT_ONLY,  // status
                        null);                   // maxEntries
        assertEquals(mExpectedEntryCount-mExpectedPublishedEntryCount, draftEntries.size());
                                
        List allEntries = wmgr.getWeblogEntries(
                        website,                 // userName
                        null,                    // startDate
                        new Date(),              // endDate
                        null,                    // catName
                        WeblogManager.ALL,       // status
                        null);                   // maxEntries
        assertEquals(mExpectedEntryCount, allEntries.size());
                                            
        List allEntries2 = wmgr.getWeblogEntries(
                        website,                 // userName
                        null,                    // startDate
                        new Date(),              // endDate
                        null,                    // catName
                        null,                    // status
                        null);                   // maxEntries
        assertEquals(mExpectedEntryCount, allEntries2.size());
                                            
        getRoller().commit();
    }
    
    public void testGetRecentComments() throws Exception
    {
        getRoller().begin();
        WeblogManager wmgr = getRoller().getWeblogManager();
        UserManager umgr = getRoller().getUserManager();
        
        WebsiteData website = (WebsiteData)mWebsites.get(0);
        website = umgr.retrieveWebsite(website.getId());
        List comments = wmgr.getRecentComments(website);
        assertTrue(comments.size() > 1);
        // Comment 0 should be named 'name1' and Comment 1 'name0'
        /*
        System.out.println(((CommentData)comments.get(0)).getName());
        System.out.println(((CommentData)comments.get(1)).getName());
        */
        assertTrue("name1".compareTo("name0") >0);
        assertTrue(
            ((CommentData)comments.get(0)).getName().compareTo(
                ((CommentData)comments.get(1)).getName()) > 0);
       /*
        System.out.println(((CommentData)comments.get(0)).getPostTime());
        System.out.println(((CommentData)comments.get(1)).getPostTime());
        */
        assertTrue(
               ((CommentData)comments.get(0)).getPostTime().compareTo(
                   ((CommentData)comments.get(1)).getPostTime()) > 0);
    }
    
    public void testGetLastPublishTime() throws Exception
    {
        getRoller().begin();
        WeblogManager wmgr = getRoller().getWeblogManager();
        WebsiteData website = (WebsiteData)mWebsites.get(0);
        
        Date lastPub = wmgr.getWeblogLastPublishTime(
                           website.getUser().getUserName());
        //System.out.println(lastPub);
        assertTrue(lastPub.compareTo(new Date()) <= 0);
    }
}


