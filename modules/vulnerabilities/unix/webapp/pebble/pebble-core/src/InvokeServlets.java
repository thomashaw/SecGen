import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.jsp.JspException;

//import org.apache.struts.action.ActionMapping;
//import org.apache.struts.validator.ValidatorForm;

import MyMockLib.MyHttpServletRequest;
import MyMockLib.MyHttpServletResponse;

import java.io.IOException;
// Imports for servlets


class InvokeServlets {
    public static void main(String[] args) throws IOException {
	processServlets();
	processActions();
	processTags();
    }

    public static void processServlets() { 
        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.FrontController servlet = new pebble.controller.FrontController();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.XmlRpcServlet servlet = new pebble.controller.XmlRpcServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

    } 
    public static void processActions() { 
        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.AddBlogAction action = new pebble.controller.action.AddBlogAction();

            //action.perform(null, null, request, response);
            action.process(request, response);

	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.AddBlogEntryAction action = new pebble.controller.action.AddBlogEntryAction();

            //action.perform(null, null, request, response);
            action.process(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.AddCommentAction action = new pebble.controller.action.AddCommentAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.AddStaticPageAction action = new pebble.controller.action.AddStaticPageAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.AddTrackBackAction action = new pebble.controller.action.AddTrackBackAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.BuildSearchIndexAction action = new pebble.controller.action.BuildSearchIndexAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ChangeCategoryAction action = new pebble.controller.action.ChangeCategoryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.CopyFileAction action = new pebble.controller.action.CopyFileAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.CreateDirectoryAction action = new pebble.controller.action.CreateDirectoryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.DeleteFileAction action = new pebble.controller.action.DeleteFileAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditBlogEntryAction action = new pebble.controller.action.EditBlogEntryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditBlogEntryTemplateAction action = new pebble.controller.action.EditBlogEntryTemplateAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditBlogPropertiesAction action = new pebble.controller.action.EditBlogPropertiesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditCategoriesAction action = new pebble.controller.action.EditCategoriesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditCategoryAction action = new pebble.controller.action.EditCategoryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditDraftBlogEntryAction action = new pebble.controller.action.EditDraftBlogEntryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditFileAction action = new pebble.controller.action.EditFileAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditPebblePropertiesAction action = new pebble.controller.action.EditPebblePropertiesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditRefererFiltersAction action = new pebble.controller.action.EditRefererFiltersAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.EditStaticPageAction action = new pebble.controller.action.EditStaticPageAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ErrorAction action = new pebble.controller.action.ErrorAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ExportBlogAction action = new pebble.controller.action.ExportBlogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.FeedAction action = new pebble.controller.action.FeedAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.FileAction action = new pebble.controller.action.FileAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.FourZeroFourAction action = new pebble.controller.action.FourZeroFourAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.FourZeroOneAction action = new pebble.controller.action.FourZeroOneAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.FourZeroThreeAction action = new pebble.controller.action.FourZeroThreeAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.LoginAction action = new pebble.controller.action.LoginAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.LoginPageAction action = new pebble.controller.action.LoginPageAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.LogoutAction action = new pebble.controller.action.LogoutAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ReloadBlogAction action = new pebble.controller.action.ReloadBlogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveBlogEntryAction action = new pebble.controller.action.RemoveBlogEntryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveBlogEntryTemplateAction action = new pebble.controller.action.RemoveBlogEntryTemplateAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveCategoryAction action = new pebble.controller.action.RemoveCategoryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveCommentAction action = new pebble.controller.action.RemoveCommentAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveDraftBlogEntryAction action = new pebble.controller.action.RemoveDraftBlogEntryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveEmailAddressAction action = new pebble.controller.action.RemoveEmailAddressAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveRefererFilterAction action = new pebble.controller.action.RemoveRefererFilterAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveStaticPageAction action = new pebble.controller.action.RemoveStaticPageAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.RemoveTrackBackAction action = new pebble.controller.action.RemoveTrackBackAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ReplyToCommentAction action = new pebble.controller.action.ReplyToCommentAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.SaveBlogEntryAction action = new pebble.controller.action.SaveBlogEntryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.SaveCategoryAction action = new pebble.controller.action.SaveCategoryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.SaveFileAction action = new pebble.controller.action.SaveFileAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.SaveRefererFilterAction action = new pebble.controller.action.SaveRefererFilterAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.SearchAction action = new pebble.controller.action.SearchAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.SendTrackBackAction action = new pebble.controller.action.SendTrackBackAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.UploadFileToBlogAction action = new pebble.controller.action.UploadFileToBlogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.UploadFileToThemeAction action = new pebble.controller.action.UploadFileToThemeAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.UploadImageToBlogAction action = new pebble.controller.action.UploadImageToBlogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewBlogEntryAction action = new pebble.controller.action.ViewBlogEntryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewBlogEntryTemplatesAction action = new pebble.controller.action.ViewBlogEntryTemplatesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewCommentsAction action = new pebble.controller.action.ViewCommentsAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewDailyBlogAction action = new pebble.controller.action.ViewDailyBlogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewDraftBlogEntriesAction action = new pebble.controller.action.ViewDraftBlogEntriesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewFeedsAction action = new pebble.controller.action.ViewFeedsAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewFilesAction action = new pebble.controller.action.ViewFilesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewHomePageAction action = new pebble.controller.action.ViewHomePageAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewLogAction action = new pebble.controller.action.ViewLogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewLogSummaryAction action = new pebble.controller.action.ViewLogSummaryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewMonthlyBlogAction action = new pebble.controller.action.ViewMonthlyBlogAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewReferersAction action = new pebble.controller.action.ViewReferersAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewReferersForTodayAction action = new pebble.controller.action.ViewReferersForTodayAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewRequestsAction action = new pebble.controller.action.ViewRequestsAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewStaticPageAction action = new pebble.controller.action.ViewStaticPageAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewStaticPagesAction action = new pebble.controller.action.ViewStaticPagesAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewThemeAction action = new pebble.controller.action.ViewThemeAction();
            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ViewTrackBacksAction action = new pebble.controller.action.ViewTrackBacksAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    pebble.controller.action.ZipDirectoryAction action = new pebble.controller.action.ZipDirectoryAction();

            action.process(request, response);
            //action.perform(null, null, request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

    } 


    public static void processTags() {
        try {
            pebble.tagext.BlogCategoriesTag tag = new pebble.tagext.BlogCategoriesTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.CalendarTag tag = new pebble.tagext.CalendarTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.CategoryFilterTag tag = new pebble.tagext.CategoryFilterTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.EncodeUrlTag tag = new pebble.tagext.EncodeUrlTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.GetThemeTag tag = new pebble.tagext.GetThemeTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsBlogContributorTag tag = new pebble.tagext.IsBlogContributorTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsBlogOwnerTag tag = new pebble.tagext.IsBlogOwnerTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsNotBlogContributorTag tag = new pebble.tagext.IsNotBlogContributorTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsNotBlogOwnerTag tag = new pebble.tagext.IsNotBlogOwnerTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsPebbleAdminTag tag = new pebble.tagext.IsPebbleAdminTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsUserAuthenticatedTag tag = new pebble.tagext.IsUserAuthenticatedTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.IsUserUnauthenticatedTag tag = new pebble.tagext.IsUserUnauthenticatedTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.SelectTag tag = new pebble.tagext.SelectTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pebble.tagext.ShowAdminPanelTag tag = new pebble.tagext.ShowAdminPanelTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

} 
