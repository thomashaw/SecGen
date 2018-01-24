package net.sf.jboard.security;


import javax.security.auth.callback.*;



/**
 * this class comes from the article called "all that jaas" posted on javaword.com in 2002.
 * <a href="http://www.javaworld.com/javaworld/jw-09-2002/jw-0913-jaas.html">
 * http://www.javaworld.com/javaworld/jw-09-2002/jw-0913-jaas.html</a>
 * <p>
 * PassiveCallbackHandler has constructor that takes
 * a username and password so its handle() method does
 * not have to prompt the user for input.
 * Useful for server-side applications.
 *
 * @author Paul Feuer and John Musser
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 */

public class PassiveCallbackHandler implements CallbackHandler {

  
    private String username;
    private char[] password;

    /**
     * <p>Creates a callback handler with the give username
     * and password.
     */
    public PassiveCallbackHandler(String user, String pass) {
        this.username = user;
        this.password = pass.toCharArray();
    }

    /**
     * Handles the specified set of Callbacks. Uses the
     * username and password that were supplied to our
     * constructor to popluate the Callbacks.
     *
     * This class supports NameCallback and PasswordCallback.
     *
     * @param   callbacks the callbacks to handle
     * @throws  IOException if an input or output error occurs.
     * @throws  UnsupportedCallbackException if the callback is not an
     * instance of NameCallback or PasswordCallback
     */
    public void handle(Callback[] callbacks)
        throws java.io.IOException, UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback)callbacks[i]).setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback)callbacks[i]).setPassword(password);
            } else {
                throw new UnsupportedCallbackException(
                            callbacks[i], "Callback class not supported");
            }
        }
    }

    /**
     * Clears out password state.
     */
    public void clearPassword() {
        if (password != null) {
            for (int i = 0; i < password.length; i++)
                password[i] = ' ';
            password = null;
        }
    }

}

