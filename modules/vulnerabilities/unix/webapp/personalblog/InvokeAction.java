import net.eyde.personalblog.struts.action.*;
    
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import MyMockLib.MyHttpServletRequest;
import MyMockLib.MyHttpServletResponse;

public class InvokeAction {
    public static void main(String[] args){
	// ReadAction
	{
	    BlogGeneralAction r = new ReadAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}

	// BlogFowardAction
	{
	    BlogGeneralAction r = new BlogFowardAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}

	// CommentAction
	{
	    BlogGeneralAction r = new CommentAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}

	// DeletePostAction
	{
	    BlogGeneralAction r = new DeletePostAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}

	//  EditPropertiesAction
	{
	    BlogGeneralAction r = new EditPropertiesAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}

	//  EditSaveAction
	{
	    BlogGeneralAction r = new EditSaveAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}


	//  ImportAction
	{
	    BlogGeneralAction r = new ImportAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}

	//  LogonAction
	{
	    BlogGeneralAction r = new LogonAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}


	//  LogoutAction
	{
	    BlogGeneralAction r = new LogoutAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}	

	//  PostSaveAction
	{
	    BlogGeneralAction r = new PostSaveAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}	


	//  SearchAction
	{
	    BlogGeneralAction r = new SearchAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();
		
		r.executeSub ( new ActionMapping(), new ValidatorForm(), request, response );
	    } catch (java.lang.Exception e){}
	}	

	//  InitializeAction
	{
	    InitializeAction r = new InitializeAction();
	    
	    try {
		HttpServletRequest request   = new MyHttpServletRequest();
		HttpServletResponse response = new MyHttpServletResponse();

		r.executeStart ( new ActionMapping(), new ValidatorForm(), request, response );		
		r.executeTestDatabase ( new ActionMapping(), new ValidatorForm(), request, response );
		r.executeCreateTables ( new ActionMapping(), new ValidatorForm(), request, response );
		r.executeFinish ( new ActionMapping(), new ValidatorForm(), request, response );		
	    } catch (java.lang.Exception e){}
	}	
    }
}
