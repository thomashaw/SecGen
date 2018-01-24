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
package pebble.mail;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.blog.Comment;
import pebble.blog.BlogEntry;
import pebble.blog.SimpleBlog;
import pebble.blog.TrackBack;
import pebble.decorator.BlogEntryDecoratorManager;
import pebble.decorator.BlogEntryDecoratorContext;

/**
 * An implementation of the Notifier interface that sends HTML based e-mail.
 *
 * @author    Simon Brown
 */
public class MailNotifier implements Notifier {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(MailNotifier.class);

  /** the notifying blog */
  private SimpleBlog notifyingBlog;

  /**
   * Creates a new instance, using the properties from the specified Blog.
   *
   * @param blog    a SimpleBlog instance
   */
  public MailNotifier(SimpleBlog blog) {
    this.notifyingBlog = blog;
  }

  /**
   * Sends a notification of a new entry via e-mail.
   *
   * @param entry   the BlogEntry that has just been added
   */
  public void sendNotification(BlogEntry entry) {
    // first of all decorate the blog entry, as if it was being rendered
    // via a HTML page or XML feed
    BlogEntryDecoratorContext decoratorContext = new BlogEntryDecoratorContext();
    decoratorContext.setView(BlogEntryDecoratorContext.SUMMARY_VIEW);
    decoratorContext.setMedia(BlogEntryDecoratorContext.EMAIL);
    entry = BlogEntryDecoratorManager.applyDecorators(entry, decoratorContext);

    SimpleBlog blog = entry.getRootBlog();

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
    sdf.setTimeZone(blog.getTimeZone());

    String subject = "Blog entry added entitled \"" + entry.getTitle() + "\"";
    String author = entry.getAuthor();

    String message = entry.getBody();
    message += "\n\n<br><br>";
    message += "Posted by " + author + " on " + sdf.format(entry.getDate());
    message += "\n\n<br><br>";
    message += "Click <a href=\"" + entry.getPermalink() + "\">here</a> to view the blog entry and comment upon it.";

    Thread t = new SendMailThread(blog.getEmailAddresses(), new HashSet(), subject, message);
    t.start();
  }

  /**
   * Sends a notification of a new comment via e-mail.
   *
   * @param comment   the Comment that has just been added
   */
  public void sendNotification(Comment comment) {
    SimpleBlog blog = comment.getBlogEntry().getRootBlog();

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
    sdf.setTimeZone(blog.getTimeZone());

    String subject = "Comment added for \"" + comment.getBlogEntry().getTitle() + "\"";
    String author = comment.getAuthor();
    if (comment.getWebsite() != null) {
      author = "<a href=\"" + comment.getWebsite() + "\">" + author + "</a>";
    }

    String message = comment.getBody();
    message += "\n\n<br><br>";
    message += "Posted by " + author + " on " + sdf.format(comment.getDate());
    message += "\n\n<br><br>";
    message += "Click <a href=\"" + comment.getBlogEntry().getCommentsLink() + "\">here</a> to view all comments, add your own reply or to remove your e-mail address from further notifications when new comments are added to this blog entry.";

    Collection to = new HashSet();
    to.addAll(blog.getEmailAddresses());
    Collection bcc = new HashSet();
    Iterator it = comment.getBlogEntry().getComments().iterator();
    Comment blogComment;
    while (it.hasNext()) {
      blogComment = (Comment)it.next();
      if (blogComment.getEmail() != null && blogComment.getEmail().length() > 0) {
        bcc.add(blogComment.getEmail());
      }
    }

    Thread t = new SendMailThread(to, bcc, subject, message);
    t.start();
  }

  /**
   * Sends a notification of a new comment via e-mail.
   *
   * @param trackBack   the TrackBack that has just been added
   */
  public void sendNotification(TrackBack trackBack) {
    SimpleBlog blog = trackBack.getBlogEntry().getRootBlog();
    String subject = "TrackBack received for \"" + trackBack.getBlogEntry().getTitle() + "\"";
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
    sdf.setTimeZone(blog.getTimeZone());

    String message = "<a href=\"" + trackBack.getUrl() + "\">" + trackBack.getTitle() + "</a>";
    message += "\n<br>";
    message += trackBack.getExcerpt();
    message += "\n\n<br><br>";
    message += "Posted by " + trackBack.getBlogName() + " on " + sdf.format(trackBack.getDate());
    message += "\n\n<br><br>";
    message += "Click <a href=\"" + trackBack.getBlogEntry().getTrackBacksLink() + "\">here</a> to view all TrackBacks.";

    Thread t = new SendMailThread(blog.getEmailAddresses(), new HashSet(), subject, message);
    t.start();
  }

  /**
   * A thread allowing the e-mail to be sent asynchronously, so the requesting
   * thread (and therefore the user) isn't held up.
   */
  class SendMailThread extends Thread {

    /** the e-mail addresses of the recipients in the TO field */
    private Collection to;

    /** the e-mail addresses of the recipients in the BCC field */
    private Collection bcc;

    /** the subject of the e-mail */
    private String subject;

    /** the body of the e-mail */
    private String message;

    /** the property used by JavaMail to specify the SMTP host */
    private static final String JAVAMAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";

    /**
     * Creates a new thread to send a new e-mail.
     *
     * @param to     the e-mail addresses of the recipients in the TO field
     * @param bcc     the e-mail addresses of the recipients in the BCC field
     * @param subject       the subject of the e-mail
     * @param message       the body of the e-mail
     */
    public SendMailThread(Collection to, Collection bcc, String subject, String message) {
      this.to = to;
      this.bcc = bcc;
      this.subject = subject;
      this.message = message;
    }

    /**
     * Performs the processing associated with this thread.
     */
    public void run() {
      try {
        // setup the e-mail session for this bean
        Session session = createSession(notifyingBlog.getSmtpHost());

        // create a message and try to send it
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(notifyingBlog.getFirstEmailAddress(), notifyingBlog.getName()));
        Collection internetAddresses = new HashSet();
        Iterator it = to.iterator();
        while (it.hasNext()) {
          internetAddresses.add(new InternetAddress(it.next().toString()));
        }
        msg.addRecipients(Message.RecipientType.TO, (InternetAddress[])internetAddresses.toArray(new InternetAddress[]{}));

        internetAddresses = new HashSet();
        it = bcc.iterator();
        while (it.hasNext()) {
          internetAddresses.add(new InternetAddress(it.next().toString()));
        }
        msg.addRecipients(Message.RecipientType.BCC, (InternetAddress[])internetAddresses.toArray(new InternetAddress[]{}));

        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setContent(message, "text/html");

        log.debug("From : " + notifyingBlog.getName() + " (" + notifyingBlog.getFirstEmailAddress() + ")");
        log.debug("Subject : " + subject);
        log.debug("Message : " + message);

        Transport.send(msg);
      } catch (Exception e) {
        log.warn("Notification e-mail could not be sent", e);
      }
    }

    /**
     * Creates a reference to a JavaMail Session.
     *
     * @param ref   the name of the SMT host or a JNDI name
     * @return  a Session instance
     * @throws Exception    if something goes wronf creating a session
     */
    private Session createSession(String ref) throws Exception {
      if (ref.startsWith("java:comp/env")) {
        // this is a JNDI based mail session
        Context ctx = new InitialContext();
        return (Session)ctx.lookup(ref);
      } else {
        // this is a simple SMTP hostname based session
        Properties props = new Properties();
        props.put(JAVAMAIL_SMTP_HOST_PROPERTY, ref);
        return Session.getDefaultInstance(props, null);
      }
    }

  }

}
