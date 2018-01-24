/*
 * Created on 23/06/2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.eyde.personalblog.global;

import java.util.HashMap;

/**
 * @author Emerson Cargnin
 * @author <a href="mailto:jorge@bcs.org.uk">Jorge Basto</a>
 * @modified   June 29, 2003
 */
public class GlobalData {
    private static HashMap ipLog = new HashMap(50);

    /**
     *
     */
    public GlobalData() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static HashMap getUsersMap()
    {return ipLog;}

    /**
     * The resource key identifier for the failure URI.
     */
    public static final String FAILURE_KEY =
           "failure";
    /**
     * The resource key identifier for the success URI.
     */
    public static final String SUCCESS_KEY =
           "success";
    /**
     * The resource key identifier for the cancel URI.
     */
    public static final String CANCEL_KEY =
           "cancel";
    /**
     * The resource key identifier for the submit request parameter.
     */
    public static final String SUBMIT_KEY =
           "submit";
    /**
     * The resource key identifier for the attribute under which the
     * error messages are kept.
     */
    public static final String ERRORS_KEY =
           "pblog.errors";

}
