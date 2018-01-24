
package net.eyde.personalblog.struts.action;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * <p>An <strong>Action</strong> that forwards to the context-relative
 * URI specified by the <code>parameter</code> property of our associated
 * <code>ActionMapping</code>.  This can be used to integrate Struts with
 * other business logic components that are implemented as servlets (or JSP
 * pages), but still take advantage of the Struts controller servlet's
 * functionality (such as processing of form beans).</p>
 *
 * <p>To configure the use of this Action in your
 * <code>struts-config.xml</code> file, create an entry like this:</p>
 *
 * <code>
 *   &lt;action path="/saveSubscription"
 *           type="org.apache.struts.actions.ForwardAction"
 *           name="subscriptionForm"
 *          scope="request"
 *          input="/subscription.jsp"
 *      parameter="/path/to/processing/servlet"/&gt;
 * </code>
 *
 * <p>which will forward control to the context-relative URI specified by the
 * <code>parameter</code> attribute.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2003/08/22 03:48:16 $
 */

public class BlogFowardAction extends BlogGeneralAction {

  


	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has
	 * already been completed.
	 *
	 * @param mapping The ActionMapping used to select this instance
	 * @param form The optional ActionForm bean for this request (if any)
	 * @param request The HTTP request we are processing
	 * @param response The HTTP response we are creating
	 *
	 * @exception Exception if an error occurs
	 */
	public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response)
	throws Exception {
		//runs the default working 
		super.execute(mapping, form, request, response);
		// Create a RequestDispatcher the corresponding resource
		String path = mapping.getParameter();

		if (path == null) {
			response.sendError
				(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				 messages.getMessage("forward.path"));
			return (null);
		}

		// Let the controller handle the request
		ActionForward retVal = new ActionForward(path);
		retVal.setContextRelative(true);

		return retVal;
	}


}

