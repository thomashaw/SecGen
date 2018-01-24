//package org.roller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import MyMockLib.MyHttpServletRequest;
import MyMockLib.MyHttpServletResponse;

import javax.servlet.jsp.JspException;

import java.io.IOException;
// Imports for servlets
/*
import org.roller.presentation.LoginServlet; 
import org.roller.presentation.atom.RollerAtomServlet; 
import org.roller.presentation.velocity.FlavorServlet; 
import org.roller.presentation.velocity.FoafServlet; 
import org.roller.presentation.velocity.LanguageServlet; 
import org.roller.presentation.velocity.PageServlet; 
import org.roller.presentation.velocity.PreviewServlet; 
import org.roller.presentation.velocity.SearchServlet; 
import org.roller.presentation.weblog.TrackbackServlet; 
import org.roller.presentation.xmlrpc.RollerXMLRPCServlet; 
*/

class InvokeServlets {
    public static void main(String[] args) throws IOException {
	processServlets();
	processTags();
    }

    public static void processServlets() { 
        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.LoginServlet servlet = new org.roller.presentation.LoginServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.atom.RollerAtomServlet servlet = new org.roller.presentation.atom.RollerAtomServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.velocity.FlavorServlet servlet = new org.roller.presentation.velocity.FlavorServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.velocity.FoafServlet servlet = new org.roller.presentation.velocity.FoafServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.velocity.LanguageServlet servlet = new org.roller.presentation.velocity.LanguageServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.velocity.PageServlet servlet = new org.roller.presentation.velocity.PageServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.velocity.PreviewServlet servlet = new org.roller.presentation.velocity.PreviewServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.velocity.SearchServlet servlet = new org.roller.presentation.velocity.SearchServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.weblog.TrackbackServlet servlet = new org.roller.presentation.weblog.TrackbackServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    org.roller.presentation.xmlrpc.RollerXMLRPCServlet servlet = new org.roller.presentation.xmlrpc.RollerXMLRPCServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

    } 

    public static void processTags() {
        try {
            org.roller.presentation.website.tags.AuthorizeUserTag tag = new org.roller.presentation.website.tags.AuthorizeUserTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.roller.presentation.website.tags.FileManagerTag tag = new org.roller.presentation.website.tags.FileManagerTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.roller.presentation.website.tags.FileUploadTag tag = new org.roller.presentation.website.tags.FileUploadTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.roller.presentation.weblog.tags.ApplyPluginsTag tag = new org.roller.presentation.weblog.tags.ApplyPluginsTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.roller.presentation.tags.DateTag tag = new org.roller.presentation.tags.DateTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.roller.presentation.tags.LinkParamTag tag = new org.roller.presentation.tags.LinkParamTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.roller.presentation.tags.StatusMessageTag tag = new org.roller.presentation.tags.StatusMessageTag();

            try {
                tag.doStartTag ();
                tag.doEndTag ();
            } catch (JspException e){}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
