/*
 * Copyright (c) 2003-2004, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pebble.controller.action;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.AuthenticatedUser;
import pebble.Constants;
import pebble.decorator.BlogEntryDecoratorManager;
import pebble.decorator.BlogEntryDecoratorContext;
import pebble.blog.BlogEntry;
import pebble.blog.BlogException;
import pebble.blog.SimpleBlog;
import pebble.controller.SecureAction;
import pebble.controller.ValidationContext;
import pebble.mail.MailNotifier;
import pebble.mail.Notifier;
import pebble.util.StringUtils;
import pebble.webservice.UpdateNotificationPingsClient;

/**
 * Saves a blog entry.
 *
 * @author    Simon Brown
 */
public class SaveBlogEntryAction extends SecureAction {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(SaveBlogEntryAction.class);

  /** the value used if the blog entry is being previewed rather than added */
  private static final String PREVIEW = "Preview";
  private static final String SAVE_AS_DRAFT = "Save as Draft";
  private static final String SAVE_AS_TEMPLATE = "Save as Template";
  private static final String SAVE_AS_STATIC_PAGE = "Save as Static Page";
  private static final String POST_TO_BLOG = "Post to Blog";

  /**
   * Peforms the processing associated with this action.
   *
   * @param request  The HttpServletRequest instance.
   * @param response   The HttpServletResponse instance.
   * @return       The name of the next view
   */
  public String process(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException {

    String submitType = request.getParameter("submit");

    int type = Integer.parseInt(request.getParameter("type"));
    switch (type) {
      case BlogEntry.STATIC_PAGE :
        request.setAttribute(Constants.TITLE_KEY, "Edit static page");
        break;
      default :
        request.setAttribute(Constants.TITLE_KEY, "Edit blog entry");
        break;
    }

    if (submitType != null && submitType.equalsIgnoreCase(PREVIEW)) {
      return previewBlogEntry(request);
    } else if (submitType != null && submitType.equalsIgnoreCase(SAVE_AS_DRAFT)) {
      return saveBlogEntryAsDraft(request);
    } else if (submitType != null && submitType.equalsIgnoreCase(SAVE_AS_TEMPLATE)) {
      return saveBlogEntryAsTemplate(request);
    } else if (submitType != null && submitType.equalsIgnoreCase(SAVE_AS_STATIC_PAGE)) {
      return saveBlogEntryAsStaticPage(request);
    } else {
      return postBlogEntry(request);
    }
  }

  private String previewBlogEntry(HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    BlogEntry blogEntry = getBlogEntry(request);

    // we don't want to actually edit the original whilst previewing
    blogEntry = (BlogEntry)blogEntry.clone();
    populateBlogEntry(blogEntry, request);

    ValidationContext validationContext = new ValidationContext();
    blogEntry.validate(validationContext);
    request.setAttribute("validationContext", validationContext);
    request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
    BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
    decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
    decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
    request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
  }

  private String postBlogEntry(HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    BlogEntry blogEntry = getBlogEntry(request);

    populateBlogEntry(blogEntry, request);

    ValidationContext context = new ValidationContext();
    blogEntry.validate(context);

    request.setAttribute("validationContext", context);
    request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
    BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
    decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
    decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
    request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));

    if (context.hasErrors())  {
      return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
    } else {
      try {
        switch (blogEntry.getType()) {
          case BlogEntry.NEW :
            blog.getBlogForToday().addEntry(blogEntry);
            blogEntry.store();
            break;
          case BlogEntry.TEMPLATE :
            blogEntry = blog.getBlogForToday().createBlogEntry();
            populateBlogEntry(blogEntry, request);
            blog.getBlogForToday().addEntry(blogEntry);
            blogEntry.store();
            break;
          case BlogEntry.DRAFT :
            BlogEntry draftBlogEntry = blogEntry;
            blogEntry = blog.getBlogForToday().createBlogEntry();
            populateBlogEntry(blogEntry, request);
            blog.getBlogForToday().addEntry(blogEntry);
            blogEntry.store();
            draftBlogEntry.remove();
            break;
          default :
            blogEntry.store();
            break;
        }

        // and should we send update notifications pings?
        if (blog.isUpdateNotificationPingsEnabled()) {
          UpdateNotificationPingsClient client = new UpdateNotificationPingsClient();
          client.sendUpdateNotificationPing(blog);
        }

        // notify via email of new entry?
        if (blog.isBlogEntryNotificationEnabled()) {
          Notifier notifier = new MailNotifier(blog);
          notifier.sendNotification(blogEntry);
        }

        decoratorContext = new BlogEntryDecoratorContext();
        decoratorContext.setView(BlogEntryDecoratorContext.SUMMARY_VIEW);
        decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
        request.setAttribute(Constants.BLOG_ENTRY_KEY, BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));
        return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/blogEntryPostedConfirmation.jsp";
      } catch (BlogException be) {
        log.error(be.getMessage(), be);
        be.printStackTrace();
        return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
      }
    }
  }

  private String saveBlogEntryAsDraft(HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    BlogEntry blogEntry = getBlogEntry(request);
    blogEntry = (BlogEntry)blogEntry.clone();
    blogEntry.setType(BlogEntry.DRAFT);
    populateBlogEntry(blogEntry, request);


    try {
      blogEntry.store();
    } catch (BlogException be) {
      log.error(be.getMessage(), be);
      be.printStackTrace();

      request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
      BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
      decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
      decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
      request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));
      return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
    }

    return "/viewDraftBlogEntries.secureaction";
  }

  private String saveBlogEntryAsTemplate(HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    BlogEntry blogEntry = getBlogEntry(request);
    blogEntry = (BlogEntry)blogEntry.clone();
    blogEntry.setType(BlogEntry.TEMPLATE);
    populateBlogEntry(blogEntry, request);

    try {
      blogEntry.store();
    } catch (BlogException be) {
      log.error(be.getMessage(), be);
      be.printStackTrace();

      request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
      BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
      decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
      decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
      request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));
      return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
    }

    return "/viewBlogEntryTemplates.secureaction";
  }

  private String saveBlogEntryAsStaticPage(HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    BlogEntry blogEntry = getBlogEntry(request);
    blogEntry.setType(BlogEntry.STATIC_PAGE);
    populateBlogEntry(blogEntry, request);

    ValidationContext context = new ValidationContext();
    blogEntry.validate(context);

    if (context.hasErrors())  {
      request.setAttribute("validationContext", context);
      request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
      BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
      decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
      decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
      request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));
      return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
    } else {
      BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
      decoratorContext.setView(BlogEntryDecoratorContext.SUMMARY_VIEW);
      decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
      request.setAttribute(Constants.BLOG_ENTRY_KEY, BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));
      try {
        blogEntry.store();
      } catch (BlogException be) {
        log.error(be.getMessage(), be);
        be.printStackTrace();

        request.setAttribute(Constants.BLOG_ENTRY_KEY, blogEntry);
        decoratorContext = new BlogEntryDecoratorContext();
        decoratorContext.setView(BlogEntryDecoratorContext.PREVIEW);
        decoratorContext.setMedia(BlogEntryDecoratorContext.HTML_PAGE);
        request.setAttribute("previewBlogEntry", BlogEntryDecoratorManager.applyDecorators(blogEntry, decoratorContext));
        return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogEntry.jsp";
      }

      return "/viewStaticPage.action?name=" + blogEntry.getStaticName();
    }
  }

  private BlogEntry getBlogEntry(HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    String id = request.getParameter("entry");
    int type = Integer.parseInt(request.getParameter("type"));

    switch (type) {
      case BlogEntry.PUBLISHED :
        return blog.getBlogEntry(id);
      case BlogEntry.TEMPLATE :
        return blog.getBlogEntryTemplate(id);
      case BlogEntry.DRAFT :
        return blog.getDraftBlogEntry(id);
      case BlogEntry.STATIC_PAGE :
        BlogEntry blogEntry = blog.getStaticPage(id);
        if (blogEntry == null) {
          blogEntry = blog.getBlogForToday().createStaticPage();
        }
        return blogEntry;
      default :
        // we're creating a new blog entry
        return blog.getBlogForToday().createBlogEntry();
    }
  }

  private void populateBlogEntry(BlogEntry blogEntry, HttpServletRequest request) {
    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    String title = request.getParameter("title");
    String body = StringUtils.filterNewlines(request.getParameter("body"));
    String excerpt = request.getParameter("excerpt");
    String originalPermalink = request.getParameter("originalPermalink");
    String staticName = request.getParameter("staticName");
    String commentsEnabled = request.getParameter("commentsEnabled");
    String trackBacksEnabled = request.getParameter("trackBacksEnabled");
    String category[] = request.getParameterValues("category");
    String author = ((AuthenticatedUser)request.getSession().getAttribute(Constants.AUTHENTICATED_USER)).getName();

    blogEntry.setTitle(title);
    blogEntry.setBody(body);
    blogEntry.setExcerpt(excerpt);
    blogEntry.removeAllCategories();
    if (category != null) {
      for (int i = 0; i < category.length; i++) {
        blogEntry.addCategory(blog.getBlogCategoryManager().getCategory(category[i]));
      }
    }

    blogEntry.setAuthor(author);
    blogEntry.setOriginalPermalink(originalPermalink);
    blogEntry.setStaticName(staticName);
    if (commentsEnabled != null && commentsEnabled.equalsIgnoreCase("true")) {
      blogEntry.setCommentsEnabled(true);
    } else {
      blogEntry.setCommentsEnabled(false);
    }
    if (trackBacksEnabled != null && trackBacksEnabled.equalsIgnoreCase("true")) {
      blogEntry.setTrackBacksEnabled(true);
    } else {
      blogEntry.setTrackBacksEnabled(false);
    }
  }

  /**
   * Gets a list of all roles that are allowed to access this action.
   *
   * @return  an array of Strings representing role names
   * @param request
   */
  public String[] getRoles(HttpServletRequest request) {
    return new String[]{Constants.BLOG_CONTRIBUTOR_ROLE};
  }

}