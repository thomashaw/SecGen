/*
 * Copyright (c) 2003-2004, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pebble.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pebble.Constants;
import pebble.blog.SimpleBlog;
import pebble.blog.Blog;
import pebble.blog.BlogManager;

/**
 * An implementation of the front controller pattern, using the command
 * and controller strategy.
 *
 * @author    Simon Brown
 */
public class FrontController extends HttpServlet {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(FrontController.class);

  /** a reference to the factory used to create Action instances */
  private ActionFactory actionFactory;

  /** the extension used to refer to actions */
  private String actionExtension = ".action";

  /**
   * Initialises this instance.
   */
  public void init() {
    String actions = getServletConfig().getInitParameter("actions");
    this.actionExtension = getServletConfig().getInitParameter("actionExtension");
    this.actionFactory = new ActionFactory(actions);
  }

  /**
   * Processes the request - this is delegated to from doGet and doPost.
   *
   * @param request   the HttpServletRequest instance
   * @param response   the HttpServletResponse instance
   */
  protected void processRequest(HttpServletRequest request,
                                HttpServletResponse response)
      throws ServletException, IOException {

    Blog blog = (Blog)request.getAttribute(Constants.BLOG_KEY);
    if (blog == null) {
      // this only seems to happen when JSP containers don't call Servlet
      // filters when redirecting to a login page during authentication
      blog = BlogManager.getInstance().getBlog();
      request.setAttribute(Constants.BLOG_KEY, blog);
    }

    // find which action should be used
    String actionName = request.getRequestURI();
    if (actionName.indexOf("?") > -1) {
      // strip of the query string - some servers leave this on
      actionName = actionName.substring(0, actionName.indexOf("?"));
    }
    int index = actionName.lastIndexOf("/");
    actionName = actionName.substring(index + 1, (actionName.length() - actionExtension.length()));
    Action action;

    try {
      action = actionFactory.getAction(actionName);
    } catch (ActionNotFoundException anfe) {
      throw new ServletException(anfe);
    }

    boolean authorised = isAuthorised(request, action);
    if (!authorised) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    } else {
      // now process action, finding out which view to show the user next
      String nextView = action.process(request, response);

      String title = (String)request.getAttribute(Constants.TITLE_KEY);
      if (title == null || title.length() == 0) {
        title = blog.getName();
      } else {
        title = blog.getName() + " - " + title;
      }
      request.setAttribute(Constants.TITLE_KEY, title);

      // and finally redirect to appropriate view, remembering to prefix the path
      try {
        if (nextView != null) {
          RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextView);
          dispatcher.forward(request, response);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        e.printStackTrace();
        throw new ServletException(e);
      }
    }
  }

  private boolean isAuthorised(HttpServletRequest request, Action action) {
    if (action instanceof SecureAction) {
      SecureAction secureAction = (SecureAction)action;

      Blog blog = (Blog)request.getAttribute(Constants.BLOG_KEY);
      if (blog instanceof SimpleBlog) {
        return isUserInRole(request, secureAction) &&
            isUserAuthorisedForBlog(request, secureAction);
      } else {
        return isUserInRole(request, secureAction);
      }
    } else {
      return true;
    }
  }

  /**
   * Determines whether the current user in one of the roles specified
   * by the secure action.
   *
   * @param request   the HttpServletRequest
   * @param action    the SecureAction to check against
   * @return  true if the user is in one of the roles, false otherwise
   */
  private boolean isUserInRole(HttpServletRequest request, SecureAction action) {
    String roles[] = action.getRoles(request);
    for (int i = 0; i < roles.length; i++) {
      if (request.isUserInRole(roles[i])) {
        return true;
      }
    }
    return false;
  }

  private boolean isUserAuthorisedForBlog(HttpServletRequest request, SecureAction action) {
    SimpleBlog rootBlog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);
    String currentUser = request.getRemoteUser();
    String roles[] = action.getRoles(request);
    for (int i = 0; i < roles.length; i++) {
      if (rootBlog.isUserInRole(roles[i], currentUser)) {
        return true;
      }
    }
    return false;
  }

  /**
   * A default implementation of doGet that delegates to the processRequest method.
   *
   * @param req   the HttpServletRequest instance
   * @param res   the HttpServletResponse instance
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    processRequest(req, res);
  }

  /**
   * A default implementation of doPost that delegates to the processRequest method.
   *
   * @param req   the HttpServletRequest instance
   * @param res   the HttpServletResponse instance
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    processRequest(req, res);
  }

}