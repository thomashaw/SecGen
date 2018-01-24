/*
 * Created on Jul 7, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;


/**
 * @author Matthew Payne
 *
 * Simple Email class plug in for Personal Blog
 *
 */
public class Email {
    private static Log log = LogFactory.getLog(Email.class);

    public static void sendMessage(String subject, String content,
        String toAddress, String mailHost) {
        log.debug("Sending email");

        Properties props = new Properties();

        try {
            props.put("mail.smtp.host",
                PersonalBlogService.getInstance().getPropertyManager()
                                   .getProperty(PersonalBlogService.EMAIL_HOST));
        } catch (Exception e) {
            log.error("Unable to get mail.host or weblog.pubEmail property");

            return;
        }

		Authenticator x = new SmtpAuthenticator();
        Session sendMailSession = Session.getInstance(props, x);
        sendMailSession.setDebug(true);
        MimeMessage newMessage = new MimeMessage(sendMailSession);

        try {
            newMessage.setFrom(new InternetAddress(PersonalBlogService.getInstance()
                                                                      .getPropertyManager()
                                                                      .getProperty(PersonalBlogService.WEBLOG_EMAIL)));

            InternetAddress to = new InternetAddress(toAddress);
            newMessage.addRecipient(Message.RecipientType.TO, to);

            Transport transport;
            transport = sendMailSession.getTransport(PersonalBlogService.getInstance()
                                                                        .getPropertyManager()
                                                                        .getProperty(PersonalBlogService.EMAIL_TRANSPORT));
            
            newMessage.setSubject(subject);
            newMessage.setText(content);
            Transport.send(newMessage);
        } catch (Exception e) {
            log.error("problem sending email",e);
        }
    }
}
