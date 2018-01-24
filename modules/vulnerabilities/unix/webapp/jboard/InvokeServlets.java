import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorForm;

import MyMockLib.MyHttpServletRequest;
import MyMockLib.MyHttpServletResponse;

import java.io.IOException;
// Imports for servlets


class InvokeServlets {
    public static void main(String[] args) throws IOException {
	processServlets();
	processActions();
    }
    public static void processServlets() { 
    } 
    public static void processActions() { 
        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.forum.actions.ForumCategoryDispatchAction action = new net.sf.jboard.struts.forum.actions.ForumCategoryDispatchAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.forum.actions.ForumItemDispatchAction action = new net.sf.jboard.struts.forum.actions.ForumItemDispatchAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.forum.actions.ForumMessageDispatchAction action = new net.sf.jboard.struts.forum.actions.ForumMessageDispatchAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.forum.actions.ForumPanoramaAction action = new net.sf.jboard.struts.forum.actions.ForumPanoramaAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.forum.actions.ForumThreadDispatchAction action = new net.sf.jboard.struts.forum.actions.ForumThreadDispatchAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.fwk.actions.JboardBaseAction action = new net.sf.jboard.struts.fwk.actions.JboardBaseAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.reg.actions.LogoffAction action = new net.sf.jboard.struts.reg.actions.LogoffAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.reg.actions.LogonAction action = new net.sf.jboard.struts.reg.actions.LogonAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.reg.actions.RegisterAction action = new net.sf.jboard.struts.reg.actions.RegisterAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    net.sf.jboard.struts.stats.actions.StatsAction action = new net.sf.jboard.struts.stats.actions.StatsAction();

            //action.perform(null, null, request, response);
   	    action.execute ( new ActionMapping(), new ValidatorForm(), request, response );
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

    } 
} 
