package se.bluefish.blueblog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

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
        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    se.bluefish.blueblog.servlet.BBServlet servlet = new se.bluefish.blueblog.servlet.BBServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpServletRequest request   = new MyHttpServletRequest();
            HttpServletResponse response = new MyHttpServletResponse();

	    se.bluefish.blueblog.servlet.ForwardingServlet servlet = new se.bluefish.blueblog.servlet.ForwardingServlet();

            servlet.service(request, response);
	    
	} catch (Exception e) {
            e.printStackTrace();
        }

    } 
    public static void processActions() { 
    } 
} 
