package net.eyde.personalblog.service;

import net.eyde.personalblog.beans.BlogProperty;
import net.eyde.personalblog.beans.CalendarAction;
import net.eyde.personalblog.beans.Category;
import net.eyde.personalblog.beans.Comment;
import net.eyde.personalblog.beans.Post;
import net.eyde.personalblog.beans.Referrer;

import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author NEyde
 *
 *
 * When the user selects a date, they will get the previous 25 posts
 * from the date selected.
 *
 * When a user selects a specific post, they will see that post only.
 *
 * When a user selects a month, they will get all the posts for the month.
 */


public class PersonalBlogService {
    //Installation State
    public static final String INSTALLATION_STATE = "installation_state";
    public static final String STATE_UNDEFINED = "undefined";
    public static final String STATE_NO_HIBERNATE_FILE = "no_hibernate_file";
	public static final String STATE_DATABASE_OFF= "database_off";
    public static final String STATE_HIBERNATE_FILE_INVALID = "hibernate_file_invalid";
    public static final String STATE_TABLES_NOT_CREATED = "tables_not_created_yet";
    public static final String STATE_MISSING_PROPERTIES = "missing_properties";
	public static final String STATE_OK = "ok";    
    private static Log log = LogFactory.getLog(PersonalBlogService.class);
    private static PersonalBlogService service = null;

    //Property Name Constants
    public static final String WEBLOG_TITLE = "weblog.title";
    public static final String WEBLOG_DESCRIPTION = "weblog.description";
    public static final String WEBLOG_PICTURE = "weblog.ownerpicture";
    public static final String WEBLOG_OWNER_NICK_NAME = "weblog.ownernickname";
    public static final String WEBLOG_URL = "weblog.url";
    public static final String WEBLOG_OWNER = "weblog.owner";
    public static final String WEBLOG_EMAIL = "weblog.email";
    public static final String LINK_POST = "links.post";
    public static final String EMOTICON_VALUES = "emoticon.values";
    public static final String EMOTICON_IMAGES = "emoticon.images";
    public static final String LOGON_ID = "logon.id";
    public static final String LOGON_PASSWORD = "logon.password";
    public static final String EDITOR = "weblog.editor";
    public static final String EMAIL_HOST = "mail.smtp.host";
    public static final String EMAIL_TRANSPORT = "mail.transport";
	public static final String EMAIL_USERNAME = "mail.username";
	public static final String EMAIL_PASSWORD = "mail.password";
	public static final String CATEGORY_TITLES = "category.titles";
	public static final String CATEGORY_VALUES = "category.values";
	public static final String CATEGORY_IMAGES = "category.images";
    Configuration cfg;
    SessionFactory sf;

    int adjustHours;
    PropertyManager pm;
    CacheManager cache;

    // is really necessary when you are going to format it?
    Locale myLocale = Locale.US;
    String dburl;
    String dbuser;
    String dbpassword;
    SimpleDateFormat qf = new SimpleDateFormat("yyyy-MM-dd", myLocale);
    SimpleDateFormat monthNav = new SimpleDateFormat("yyyyMM", myLocale);


    /**
     * Constructor for PersonalBlogService.
     */
    protected PersonalBlogService(Properties conn)
        throws InitializationException {
        log.debug("initialization - constructor");

        try {
            cfg = new Configuration().addClass(Post.class)
                                     .addClass(Comment.class)
                                     .addClass(Referrer.class)
                                     .addClass(BlogProperty.class);

            if (conn != null) {
                cfg.setProperties(conn);
                pm = new PropertyManager(conn);
            } else {
                pm = new PropertyManager();
            }

            //TODO:is there a way to get the SessionFactory without cfg object
            //I want to take it out of here, for these 
            sf = cfg.buildSessionFactory();
        } catch (Exception e) {
            log.error("Error initializing PersonalBlog Service", e);

            throw new InitializationException(e);
        }
    }

    /**
     * Singleton  getInstance method
     */
    public static PersonalBlogService getInstance() throws ServiceException {
        if (service == null) {
            try {
                log.debug(
                    "Initializing PersonalBlog Service (WITHOUT CONNECTION PARMS)");
                service = new PersonalBlogService(null);
            } catch (ServiceException e) {
                log.error("Error getting instance of PersonalBlog Service", e);

                //TODO - Why can't exceptions thrown here be caught elsewhere
                throw e;
            }
        }

        return service;
    }

    public static PersonalBlogService getInstance(Properties conn)
        throws ServiceException {
        if (service == null) {
            try {
                log.debug(
                    "Initializing PersonalBlog Service (WITH CONNECTION PARMS)");
                service = new PersonalBlogService(conn);
            } catch (Exception e) {
                log.error("Error getting instance of PersonalBlog Service", e);

                //TODO - Why can't exceptions thrown here be caught elsewhere
            }
        }

        return service;
    }

    /*
     * This method will return the most recent posts for today's date.  This method
     * will return a maximum of 25 total posts or three days worth of posts.
     *
     */
    public PropertyManager getPropertyManager() {
        return pm;
    }

    public List getPosts() throws ServiceException {
		Session session = null;
        List posts = null;

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		String startdate = qf.format(cal.getTime());

        try {
            session = sf.openSession();
		} catch (Exception e) {
			log.error("error opening session", e);
			throw new ServiceException(e);
		}

		try {
            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.created > '" + startdate +
                    "' order by post.created desc");
        } catch (Exception e) {
            log.error("error reading posts", e);
            throw new ServiceException(e);
        }

		try {
			session.close();
		} catch (Exception e) {
			log.error("error closing session", e);
			throw new ServiceException(e);
		}

        return posts;
    }

    public List getPostsByDate(String dateParm) throws ServiceException {
		Session session = null;
        List posts = null;
        String start;
        String end;
        Calendar cal = Calendar.getInstance();

        if (!dateParm.equals("")) {
            cal.set(Calendar.YEAR, Integer.parseInt(dateParm.substring(0, 4)));
            cal.set(Calendar.MONTH,
                Integer.parseInt(dateParm.substring(5, 7)) - 1);
            cal.set(Calendar.DATE, Integer.parseInt(dateParm.substring(8, 10)));
        }

        //add one day to compensate for time.
        cal.add(Calendar.DATE, 1);
        end = qf.format(cal.getTime());
        cal.set(Calendar.DATE, 1);
        start = qf.format(cal.getTime());

        try {
            session = sf.openSession();

            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post" +
                    " where post.created >= '" + start +
                    "' and post.created <= '" + end +
                    "' order by post.created desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading posts | dateParam:" + dateParm, e);
            throw new ServiceException(e);
        }

        return posts;
    }

    public List getPostsByMonth(String monthParm) throws ServiceException {
		Session session = null;
        List posts = null;
        String start;
        String end;
        Calendar cal = Calendar.getInstance();

        if (!monthParm.equals("")) {
            cal.set(Calendar.YEAR, Integer.parseInt(monthParm.substring(0, 4)));
            cal.set(Calendar.MONTH,
                Integer.parseInt(monthParm.substring(5, 7)) - 1);
            cal.set(Calendar.DATE, 1);
        } else {
            cal.set(Calendar.DATE, 1);
        }

        start = qf.format(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, -1);
        end = qf.format(cal.getTime());

        try {
            session = sf.openSession();

            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.created >= '" + start +
                    "' and post.created <= '" + end +
                    "' order by post.created desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading posts |dateParam:" + monthParm, e);
            throw new ServiceException(e);
        }

        return posts;
    }

    public List getPostsByCategory(String cat) throws ServiceException {
		Session session = null;
        List posts = null;

        try {
            session = sf.openSession();

            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.category like '%" + cat +
                    "%' order by post.created desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading posts | cat:" + cat, e);
            throw new ServiceException(e);
        }

        return posts;
    }

    public List getPostsBySearch(String searchTxt) throws ServiceException {
		Session session = null;
        List posts = null;

        searchTxt = searchTxt.replace('\'', ' ');
        searchTxt = searchTxt.replace(';', ' ');

        try {
            session = sf.openSession();

            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.content like '%" + searchTxt + "%' " +
                    "   or post.title   like '%" + searchTxt + "%' " +
                    "order by post.created desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading posts | searchTxt:" + searchTxt, e);
            throw new ServiceException(e);
        }

        return posts;
    }

    public void createPost(Post inPost) throws ServiceException {
		Session session = null;
        try {
            session = sf.openSession();
            inPost.setCreated(new Date());
            inPost.setModified(inPost.getCreated());
            session.save(inPost);
            session.flush();
            session.close();
        } catch (Exception e) {
            log.error("Error while creating post | postId:" + inPost.getId(), e);
            throw new ServiceException(e);
        }

        return;
    }

    public void updatePost(String postId, String title, String content,
        String cat) throws ServiceException {
			Session session = null;
        try {
            session = sf.openSession();

            Post post = (Post) session.load(Post.class, postId);
            post.setTitle(title);
            post.setContent(content);
            post.setCategory(cat);
            post.setModified(new Date());

            session.update(post);

            //session.update(inPost, inPost.getId());
            session.flush();
            session.close();
        } catch (Exception e) {
            log.error("Error while updating post | postId:" + postId, e);
            throw new ServiceException(e);
        }

        return;
    }

    public void updatePost(Post inPost) throws ServiceException {
		Session session = null;
        try {
            session = sf.openSession();
            inPost.setModified(new Date());
            session.update(inPost, inPost.getId());
            session.flush();
            session.close();
        } catch (Exception e) {
            log.error("Error while updating post | postId:" + inPost.getId(), e);
            throw new ServiceException(e);
        }

        return;
    }

    public void deletePost(String postId) throws ServiceException {
		Session session = null;
        try {
            session = sf.openSession();

            session.delete(session.load(Post.class, postId));

            session.flush();
            session.close();
        } catch (Exception e) {
            log.error("Error while deleting post| postId:" + postId, e);
            throw new ServiceException(e);
        }

        return;
    }

    public Post getPost(String postId) throws ServiceException {
		Session session = null;
        Post post = null;

        try {
            session = sf.openSession();

            post = (Post) session.load(Post.class, postId);

            session.close();
        } catch (Exception e) {
            log.error("Error while reading post id:" + postId, e);
            throw new ServiceException("Error while reading post id:" + postId,
                e);
        }

        return post;
    }

    public List getOldPost(String oldReference) throws ServiceException {
		Session session = null;
        List posts = null;

        try {
            session = sf.openSession();

            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.reference like '" + oldReference + "%' " +
                    "order by post.created desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading posts | ref:" + oldReference, e);
            throw new ServiceException(e);
        }

        return posts;
    }

    public void createComment(Comment newComment, String postId)
        throws ServiceException {
			Session session = null;
        try {
            session = sf.openSession();

            Post currentPost = (Post) session.load(Post.class, postId);
            newComment.setCreated(new Date());
            currentPost.addComment(newComment);
            session.update(currentPost, currentPost.getId());

            //session.save(newComment);
            session.flush();
            session.close();
        } catch (Exception e) {
            log.error("Error while creating comment | postId:" + postId, e);
            throw new ServiceException(e);
        }

        return;
    }

    public void createComment(Comment newComment, Post post)
        throws ServiceException {
			Session session = null;
        try {
            session = sf.openSession();

            newComment.setCreated(new Date());
            post.addComment(newComment);
            session.update(post, post.getId());

            //session.save(newComment);
            session.flush();
            session.close();
        } catch (Exception e) {
            log.error("Error while creating comment | postId:" + post.getId(), e);
            throw new ServiceException(e);
        }

        return;
    }

    public List getRecentComments() throws ServiceException {
		Session session = null;
        List comments = null;

        try {
            session = sf.openSession();

            //does it get's all comments from database to show just the recents ones?
            comments = session.find(
                    "from comment in class net.eyde.personalblog.beans.Comment " +
                    "order by comment.created desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading comment", e);
            throw new ServiceException(e);
        }

        log.debug("qty comments returned:" + comments.size());

        return comments;
    }

    public Post getLinks() throws ServiceException {
		Session session = null;
        Post post = null;
        String link = null;

        try {
            session = sf.openSession();
            link = getPropertyManager().getProperty(PersonalBlogService.LINK_POST);
            post = (Post) session.load(Post.class, link);

            session.close();
        } catch (ObjectNotFoundException e) {
            log.info("couldn't find object:" + link);
            post = initializeLinks();
        } catch (Exception e) {
            log.error("Error while reading links", e);
            throw new ServiceException(e);
        }

        return post;
    }

    public Post initializeLinks() throws ServiceException {
		Session session = null;
        log.debug("Creating links post");

        Post post = new Post();
        post.setContent("Blogroll");
		post.setEmailComments(Boolean.FALSE);
        post.setTitle("Blogroll");
        this.createPost(post);

        try {
            List posts = null;

            session = sf.openSession();
            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.title = 'Blogroll'");

            //TODO: Why not using id==0 above? or maybe using a contant BLOGROLL_POST=0
            Iterator postsit = posts.iterator();

            if (postsit.hasNext()) {
                post = (Post) postsit.next();
            }

            session.close();
        } catch (ObjectNotFoundException e) {
            log.info("couldn't find link object");
        } catch (Exception e) {
            log.error("Error while reading links", e);
            throw new ServiceException(e);
        }

        pm.updateProperty("links.post", post.getId());

        return post;
    }

    public List getReferrers() throws ServiceException {
		Session session = null;
        List referrers = null;
        Calendar cal = Calendar.getInstance();

        try {
            session = sf.openSession();

            referrers = session.find(
                    "from referrer in class net.eyde.personalblog.beans.Referrer" +
                    " where referrer.date > '" + qf.format(cal.getTime()) +
                    "' " + "order by referrer.counter desc");

            session.close();
        } catch (Exception e) {
            log.error("Error while reading Referrers", e);
            throw new ServiceException(e);
        }

        return referrers;
    }
      public List getCategories() {
		  log.info("Retrieving Categories");
		  
          ArrayList categories = new ArrayList();
		StringTokenizer catTitles = null;
		StringTokenizer catValues = null;
		StringTokenizer catImages = null;

		try {

          catTitles = new StringTokenizer(getPropertyManager().getProperty(PersonalBlogService.CATEGORY_TITLES), ",");
          catValues = new StringTokenizer(getPropertyManager().getProperty(PersonalBlogService.CATEGORY_VALUES), ",");
          catImages = new StringTokenizer(getPropertyManager().getProperty(PersonalBlogService.CATEGORY_IMAGES), ",");
		} catch (Exception e) {
			log.error("Error while parsing Categories ", e);
		}

          while (catTitles.hasMoreTokens()) {
              Category item = new Category();
              item.setName(catTitles.nextToken());
              item.setValue(catValues.nextToken().charAt(0));
              item.setImage(catImages.nextToken());
			  log.info("Category '"+ item.getName()+"' parsed.");

              categories.add(item);
          }

          return categories;
      }
    public List getEmoticons() {
		log.info("Retrieving Emoticons");

        ArrayList emoticons = new ArrayList();
		
		StringTokenizer emotValues = null;
		StringTokenizer emotImages = null;
		
		try {
        emotValues = new StringTokenizer(getPropertyManager().getProperty(PersonalBlogService.EMOTICON_VALUES), ",");
        emotImages = new StringTokenizer(getPropertyManager().getProperty(PersonalBlogService.EMOTICON_IMAGES), ",");
		} catch (Exception e) {
			log.error("Error while parsing Emoticons", e);
		}

        while (emotValues.hasMoreTokens()) {
            Category item = new Category();
            item.setValue(emotValues.nextToken().charAt(0));
            item.setImage(emotImages.nextToken());

            emoticons.add(item);
        }

        return emoticons;
    }

    /**
     *  Description of the Method
     *
     *@param  val  Description of the Parameter
     *@return      Description of the Return Value
     */
    private String cleanNull(String val) {
        if ((val == null) || val.trim().equals("")) {
            return "";
        } else {
            return val;
        }
    }

    /**
     *  Description of the Method
     *
     *@param  date  Description of the Parameter
     *@return       Description of the Return Value
     */
    private java.util.Date adjustDate(java.util.Date date) {
        Calendar adjust = new GregorianCalendar();
        adjust.setTime(date);
        adjust.add(Calendar.HOUR, adjustHours);
        date = adjust.getTime();

        return date;
    }

    public void checkReferer(String refUrl) throws ServiceException {
		Session session = null;
        log.debug("referer URL:" + refUrl);

        List referrerList;
        Referrer ref = null;

        try {
            if (!(refUrl == null) &&
                    !refUrl.startsWith(PersonalBlogService.getInstance()
                                                              .getPropertyManager()
                                                              .getProperty(PersonalBlogService.WEBLOG_URL)) &&
                    !refUrl.startsWith("http://127.0.0.1") &&
                    !refUrl.startsWith("http://localhost") &&
                    (refUrl.indexOf(":5335") < 0)) { //what's this???

                Calendar cal = Calendar.getInstance();
                session = sf.openSession();

                Date today = new Date();
                referrerList = session.find(
                        "from referrer in class net.eyde.personalblog.beans.Referrer " +
                        "where referrer.referrer = '" + refUrl + "' " +
                        "and referrer.date > '" + qf.format(cal.getTime()) +
                        "' ");

                Iterator referrers = referrerList.iterator();

                if (referrers.hasNext()) {
                    ref = (Referrer) referrers.next();
                }

                if (ref == null) {
                    ref = new Referrer();
                    ref.setDate(today);
                    ref.setCreated(new Date());
                    ref.setReferrer(refUrl);
                    ref.setCounter(1);
                    session.save(ref);
                } else {
                    ref.setCounter(ref.getCounter() + 1);
                    session.update(ref, ref.getId());
                }

                session.flush();
                session.close();

                return;
            }
        } catch (Exception e) {
            log.error("refUrl:" + refUrl, e);
            throw new ServiceException(e);
        }
    }

    public String getCurrMonth(String referenceDate) {
        return calcMonth(referenceDate, 0);
    }

    public String calcMonth(String referenceDate, int offset) {
        Calendar cal = Calendar.getInstance();

        if (!referenceDate.equals("")) {
            cal.set(Calendar.YEAR,
                Integer.parseInt(referenceDate.substring(0, 4)));
            cal.set(Calendar.MONTH,
                Integer.parseInt(referenceDate.substring(5, 7)) - 1);
            cal.set(Calendar.DATE, 1);
        } else {
            cal.set(Calendar.DATE, 1);
        }

        cal.add(Calendar.MONTH, offset);

        return qf.format(cal.getTime());
    }

    public String getPrevMonth(String referenceDate) {
        return calcMonth(referenceDate, -1);
    }

    public String getNextMonth(String referenceDate) {
        return calcMonth(referenceDate, 1);
    }

    public List getCalendarActions(String dateParm) throws ServiceException {
		Session session = null;
        ArrayList actions = new ArrayList();
        List posts = null;

        String start;
        String end;
        Calendar cal = Calendar.getInstance();

        if (!dateParm.equals("")) {
            cal.set(Calendar.YEAR, Integer.parseInt(dateParm.substring(0, 4)));
            cal.set(Calendar.MONTH,
                Integer.parseInt(dateParm.substring(5, 7)) - 1);
            cal.set(Calendar.DATE, 1);
        }

        cal.set(Calendar.DAY_OF_MONTH, 1);
        start = qf.format(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        end = qf.format(cal.getTime());

        try {
            session = sf.openSession();

            posts = session.find(
                    "from post in class net.eyde.personalblog.beans.Post " +
                    "where post.created >= '" + start +
                    "' and post.created <= '" + end + "'");

            session.close();
        } catch (Exception e) {
            log.error("dataParam="+dateParm,e);
            throw new ServiceException("dateParam" + dateParm, e);
        }

        Iterator it = posts.iterator();

        while (it.hasNext()) {
            Post post = (Post) it.next();
            CalendarAction calAction = new CalendarAction();

            calAction.setDay(post.getCreated().getDate());
            calAction.setUrl("index.do?caldate=" +
                qf.format(post.getCreated()));

            if (!actions.contains(calAction)) {
                actions.add(calAction);
            }
        }

        return actions;
    }

    private String stringDate(Calendar calendar) {
        String s = String.valueOf(calendar.get(1));

        return s + twoDigits(calendar.get(2) + 1) + twoDigits(calendar.get(5));
    }

    private String twoDigits(int i) {
        String s = String.valueOf(i);

        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }

    public boolean checkLogIn(String inUser, String inPassword)
        {
        log.debug("Checking Login: " + inUser + "/" + inPassword);

        try {
            if ((inUser != null) && (inPassword != null) &&
                    inUser.equals(getPropertyManager().getProperty(PersonalBlogService.LOGON_ID)) &&
                    inPassword.equals(getPropertyManager().getProperty(PersonalBlogService.LOGON_PASSWORD))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
        	log.info(inPassword, e);
            return false;
        }
    }

    public void importOldData() throws ServiceException {
		Session session = null;
        SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd", myLocale);
        SimpleDateFormat timef = new SimpleDateFormat("HHmmss", myLocale);
        String sql;

        sql = "select * from personalblog where parentId = 0" +
            " order by created desc";

        try {
            // Load the database driver
            Class.forName("org.gjt.mm.mysql.Driver");

            // Get a connection to the database
            Connection conn = DriverManager.getConnection(dburl + "?user=" +
                    dbuser + "&password=" + dbpassword);

            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                session = sf.openSession();

                Post xyz = new Post();
                Timestamp created = rs.getTimestamp("created");
                xyz.setContent(rs.getString("content"));
                xyz.setCategory(rs.getString("category"));
                xyz.setCreated(rs.getDate("created"));
                xyz.setModified(rs.getDate("modified"));
                xyz.setTitle(rs.getString("title"));
                xyz.setReference(datef.format(created) + "#" +
                    timef.format(created));

                session.save(xyz);
                session.flush();
                session.close();
            }
        } catch (Exception e) {
            log.error("Error while importing data", e);
            throw new ServiceException(e);
        }
    }
}
