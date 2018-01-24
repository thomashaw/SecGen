package net.eyde.personalblog.struts.action;

import net.eyde.personalblog.service.PersonalBlogService;
import net.eyde.personalblog.service.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *  Description of the Class
 *
 *@author     NEyde
 *@created    September 17, 2002
 */
public final class ReadAction extends BlogGeneralAction {
	private static Log log = LogFactory.getLog(ReadAction.class);
	// ----------------------------------- Public Methods

	/**
	 *  Process the specified HTTP request, and create the corresponding HTTP
	 *  response (or forward to another web component that will create it). Return
	 *  an ActionForward instance describing where and how control should be
	 *  forwarded, or null if the response has already been completed.
	 *
	 *@param  mapping               The ActionMapping used to select this instance
	 *@param  request               The HTTP request we are processing
	 *@param  response              The HTTP response we are creating
	 *@param  form                  Description of the Parameter
	 *@return                       Description of the Return Value
	 *@exception  IOException       if an input/output error occurs
	 *@exception  ServletException  if a servlet exception occurs
	 */
    public ActionForward executeSub(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        ActionErrors errors = new ActionErrors();
        String forward = "readposts";

        // Get request parameters
        String reqDate = cleanNull(request.getParameter("caldate"));
        String reqCategory = cleanNull(request.getParameter("cat"));
        String reqMonth = cleanNull(request.getParameter("month"));
        String reqPost = cleanNull(request.getParameter("post"));
        String reqOldPost = cleanNull(request.getParameter("date"));

        // Get instance of PersonalBlog Service
        PersonalBlogService pblog = PersonalBlogService.getInstance();
  
        // Set Request Parameters
        // Depending on the parameters, call the appropriate method
        try {
            if (reqPost.length() > 1) {
                request.setAttribute("post", pblog.getPost(reqPost));
                request.setAttribute("posts", pblog.getPosts());
                forward = "readpost";
            } else if (reqOldPost.length() > 1) {
                request.setAttribute("posts", pblog.getOldPost(reqOldPost));
                forward = "readposts";
            } else if ((reqDate.equals("") || (reqDate == null)) &&
                    (reqCategory.equals("") || (reqCategory == null)) &&
                    (reqMonth.equals("") || (reqMonth == null))) {
                request.setAttribute("posts", pblog.getPosts());
            } else if (!reqMonth.equals("")) {
                request.setAttribute("posts", pblog.getPostsByMonth(reqMonth));
            } else if (!reqCategory.equals("")) {
                request.setAttribute("posts",
                  	pblog.getPostsByCategory(reqCategory));
	
            } else {
                request.setAttribute("posts", pblog.getPostsByDate(reqDate));
            }

            // Get Date Urls that will be used for setting calendar links
            if ((reqMonth.equals("") || (reqMonth == null)) &&
                    !reqDate.equals("")) {
                reqMonth = reqDate;
            }

            request.setAttribute("cal", pblog.getCalendarActions(reqMonth));
            request.setAttribute("currMonth", pblog.getCurrMonth(reqMonth));
            request.setAttribute("prevMonth", pblog.getPrevMonth(reqMonth));
            request.setAttribute("nextMonth", pblog.getNextMonth(reqMonth));
        } catch (ServiceException e) {
            ActionMessages messages = new ActionMessages();
            ActionMessage message = new ActionMessage(
                    "exception.postdoesnotexist");
            messages.add(ActionMessages.GLOBAL_MESSAGE, message);

            errors.add(messages); 
            e.printStackTrace();
        }

        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        return (mapping.findForward(forward));
    }
}
