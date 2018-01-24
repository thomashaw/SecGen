/*
 * Created on Aug 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.service;

import javax.mail.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SmtpAuthenticator extends Authenticator {
	private static Log log = LogFactory.getLog(Email.class);

    public PasswordAuthentication getPasswordAuthentication() {
        String username = null;
        String password = null;

        try {
            username = PersonalBlogService.getInstance().getPropertyManager()
                                          .getProperty(PersonalBlogService.WEBLOG_EMAIL);
            password = PersonalBlogService.getInstance().getPropertyManager()
                                          .getProperty(PersonalBlogService.WEBLOG_EMAIL);
        } catch (Exception e) {
            log.error("Unable to retrieve mail server aunthentication parms");
        }

        return new PasswordAuthentication(username, password);
    }
}
