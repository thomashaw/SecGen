package pebble.blog.persistence.file;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import pebble.blog.Comment;
import pebble.blog.BlogEntry;
import pebble.blog.TrackBack;

public class BlogEntryHandler extends DefaultHandler {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(BlogEntryHandler.class);

  private static final int NOT_DEFINED = -1;
  private static final int TITLE = 0;
  private static final int EXCERPT = 1;
  private static final int BODY = 2;
  private static final int DATE = 3;
  private static final int AUTHOR = 4;
  private static final int ORIGINAL_PERMALINK = 5;
  private static final int STATIC_NAME = 6;
  private static final int CATEGORY = 7;
  private static final int COMMENTS_ENABLED = 8;
  private static final int TRACKBACKS_ENABLED = 9;
  private static final int EMAIL = 10;
  private static final int WEBSITE = 11;
  private static final int BLOG_NAME = 12;
  private static final int URL = 13;
  private static final int PARENT = 14;
  private static final int IP_ADDRESS = 15;

  private static final int IN_BLOG_ENTRY = 100;
  private static final int IN_COMMENT = 101;
  private static final int IN_TRACKBACK = 102;

  private BlogEntry blogEntry;
  private int groupStatus = IN_BLOG_ENTRY;
  private int elementStatus = NOT_DEFINED;
  private SimpleDateFormat oldDateTimeFormat;
  private SimpleDateFormat newDateTimeFormat;

  private StringBuffer elementContent;

  private String commentTitle;
  private String commentBody;
  private String commentAuthor;
  private String commentWebsite;
  private String commentIpAddress;
  private String commentEmail;
  private Date commentDate;
  private long commentParent = -1;

  private String trackBackTitle;
  private String trackBackExcerpt;
  private String trackBackBlogName;
  private String trackBackUrl;
  private String trackBackIpAddress;
  private Date trackBackDate;

  public BlogEntryHandler(BlogEntry blogEntry) {
    this.blogEntry = blogEntry;
    oldDateTimeFormat = new SimpleDateFormat(FileBlogEntryDAO.OLD_PERSISTENT_DATETIME_FORMAT);
    oldDateTimeFormat.setTimeZone(blogEntry.getRootBlog().getTimeZone());
    newDateTimeFormat = new SimpleDateFormat(FileBlogEntryDAO.NEW_PERSISTENT_DATETIME_FORMAT);
    newDateTimeFormat.setTimeZone(blogEntry.getRootBlog().getTimeZone());
  }

  public void startElement(String uri, String name, String qName, Attributes attributes) throws SAXException {
    //log.info("startElement : " + name);
    elementContent = new StringBuffer();
    if (name.equals("title")) {
      elementStatus = TITLE;
    } else if (name.equals("excerpt")) {
      elementStatus = EXCERPT;
    } else if (name.equals("body")) {
      elementStatus = BODY;
    } else if (name.equals("date")) {
      elementStatus = DATE;
    } else if (name.equals("author")) {
      elementStatus = AUTHOR;
    } else if (name.equals("originalPermalink")) {
      elementStatus = ORIGINAL_PERMALINK;
    } else if (name.equals("staticName")) {
      elementStatus = STATIC_NAME;
    } else if (name.equals("category")) {
      elementStatus = CATEGORY;
    } else if (name.equals("commentsEnabled")) {
      elementStatus = COMMENTS_ENABLED;
    } else if (name.equals("trackBacksEnabled")) {
      elementStatus = TRACKBACKS_ENABLED;
    } else if (name.equals("email")) {
      elementStatus = EMAIL;
    } else if (name.equals("website")) {
      elementStatus = WEBSITE;
    } else if (name.equals("ipAddress")) {
      elementStatus = IP_ADDRESS;
    } else if (name.equals("blogName")) {
      elementStatus = BLOG_NAME;
    } else if (name.equals("url")) {
      elementStatus = URL;
    } else if (name.equals("parent")) {
      elementStatus = PARENT;
    } else if (name.equals("comment")) {
      groupStatus = IN_COMMENT;
      elementStatus = NOT_DEFINED;
    } else if (name.equals("trackback")) {
      groupStatus = IN_TRACKBACK;
      elementStatus = NOT_DEFINED;
    } else {
      elementStatus = NOT_DEFINED;
    }
  }

  public void endElement(String uri, String name, String qName) throws SAXException {
    //log.info("endElement : " + name);
    if (groupStatus == IN_BLOG_ENTRY) {
      switch (elementStatus) {
        case TITLE :
          blogEntry.setTitle(elementContent.toString());
          break;
        case EXCERPT :
          blogEntry.setExcerpt(elementContent.toString());
          break;
        case BODY :
          blogEntry.setBody(elementContent.toString());
          break;
        case DATE :
          try {
            blogEntry.setDate(newDateTimeFormat.parse(elementContent.toString()));
          } catch (ParseException pe) {
            try {
              blogEntry.setDate(oldDateTimeFormat.parse(elementContent.toString()));
            } catch (ParseException pe2) {
              log.error("Couldn't parse date of " + elementContent.toString());
            }
          }
          break;
        case AUTHOR :
          blogEntry.setAuthor(elementContent.toString());
          break;
        case ORIGINAL_PERMALINK :
          blogEntry.setOriginalPermalink(elementContent.toString());
          break;
        case STATIC_NAME :
          blogEntry.setStaticName(elementContent.toString());
          break;
        case CATEGORY :
          blogEntry.addCategory(blogEntry.getRootBlog().getBlogCategoryManager().getCategory(elementContent.toString()));
          break;
        case COMMENTS_ENABLED :
          blogEntry.setCommentsEnabled(Boolean.valueOf(elementContent.toString()).booleanValue());
          break;
        case TRACKBACKS_ENABLED :
          blogEntry.setTrackBacksEnabled(Boolean.valueOf(elementContent.toString()).booleanValue());
          break;
      }
    } else if (groupStatus == IN_COMMENT && name.equals("comment")) {
      Comment comment = blogEntry.createComment(commentTitle, commentBody, commentAuthor, commentEmail, commentWebsite, commentIpAddress, commentDate);
      if (commentParent != -1) {
        comment.setParent(blogEntry.getComment(commentParent));
      }
      blogEntry.addComment(comment);
      groupStatus = IN_BLOG_ENTRY;

      // and blank all the comment variables
      commentTitle = null;
      commentBody = null;
      commentAuthor = null;
      commentWebsite = null;
      commentIpAddress = null;
      commentEmail = null;
      commentDate = null;
      commentParent = -1;
    } else if (groupStatus == IN_COMMENT) {
      switch (elementStatus) {
        case TITLE :
          commentTitle = elementContent.toString();
          break;
        case BODY :
          commentBody = elementContent.toString();
          break;
        case DATE :
          try {
            commentDate = newDateTimeFormat.parse(elementContent.toString());
          } catch (ParseException pe) {
            try {
              commentDate = oldDateTimeFormat.parse(elementContent.toString());
            } catch (ParseException pe2) {
              log.error("Couldn't parse date of " + elementContent.toString());
            }
          }
          break;
        case AUTHOR :
          commentAuthor = elementContent.toString();
          break;
        case EMAIL :
          commentEmail = elementContent.toString();
          break;
        case WEBSITE :
          commentWebsite = elementContent.toString();
          break;
        case IP_ADDRESS :
          commentIpAddress = elementContent.toString();
          break;
        case PARENT :
          commentParent = Long.parseLong(elementContent.toString());
          break;
      }
    } else if (groupStatus == IN_TRACKBACK && name.equals("trackback")) {
      TrackBack trackBack = blogEntry.createTrackBack(trackBackTitle, trackBackExcerpt, trackBackUrl, trackBackBlogName, trackBackIpAddress, trackBackDate);
      blogEntry.addTrackBack(trackBack);
      groupStatus = IN_BLOG_ENTRY;

      // and blank all the TrackBack variables
      trackBackTitle = null;
      trackBackExcerpt = null;
      trackBackBlogName = null;
      trackBackUrl = null;
      trackBackIpAddress = null;
      trackBackDate = null;
    } else if (groupStatus == IN_TRACKBACK) {
      switch (elementStatus) {
        case TITLE :
          trackBackTitle = elementContent.toString();
          break;
        case EXCERPT :
          trackBackExcerpt = elementContent.toString();
          break;
        case DATE :
          try {
            trackBackDate = newDateTimeFormat.parse(elementContent.toString());
          } catch (ParseException pe) {
            try {
              trackBackDate = oldDateTimeFormat.parse(elementContent.toString());
            } catch (ParseException pe2) {
              log.error("Couldn't parse date of " + elementContent.toString());
            }
          }
          break;
        case BLOG_NAME :
          trackBackBlogName = elementContent.toString();
          break;
        case URL :
          trackBackUrl = elementContent.toString();
          break;
        case IP_ADDRESS :
          trackBackIpAddress = elementContent.toString();
          break;
      }
    }

    elementStatus = NOT_DEFINED;
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    elementContent.append(new String(ch, start, length));
    //log.info("characters : " + s);
  }

  public void warning(SAXParseException e) throws SAXException {
    log.warn(e);
  }

  public void error(SAXParseException e) throws SAXException {
    log.error(e);
  }

  public void fatalError(SAXParseException e) throws SAXException {
    log.fatal(e);
  }

}