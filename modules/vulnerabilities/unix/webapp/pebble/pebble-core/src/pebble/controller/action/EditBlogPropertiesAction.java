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
package pebble.controller.action;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.BlogException;
import pebble.blog.SimpleBlog;
import pebble.controller.SecureAction;

/**
 * Edits the properties associated with the current Blog.
 *
 * @author    Simon Brown
 */
public class EditBlogPropertiesAction extends SecureAction {

  /**
   * Peforms the processing associated with this action.
   *
   * @param request  The HttpServletRequest instance.
   * @param response   The HttpServletResponse instance.
   * @return       The name of the next view
   */
  public String process(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException {

    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);

    String submit = request.getParameter("submit");
    if (submit != null && submit.length() > 0) {
      Enumeration params = request.getParameterNames();
      while (params.hasMoreElements()) {
        String key = (String)params.nextElement();
        String value = request.getParameter(key);

        if (key.equals("submit")) {
          // this is the parameter representing the submit button - do nothing
        } else {
          // this is an existing parameter - save or remove it
          if (value == null || value.length() == 0) {
            blog.removeProperty(key);
          } else {
            blog.setProperty(key, value);
          }
        }
      }

      try {
        blog.storeProperties();
      } catch (BlogException e) {
        throw new ServletException(e);
      }
    }

    request.setAttribute("properties", blog.getProperties());

    Set resources = request.getSession().getServletContext().getResourcePaths("/themes/");
    List themes = new ArrayList();
    Iterator it = resources.iterator();
    String resource;
    while (it.hasNext()) {
      resource = (String)it.next();
      resource = resource.substring(8, resource.length()-1);
      if (!resource.equals("template")) {
        themes.add(resource);
      }
    }
    request.setAttribute("themes", themes);

    request.setAttribute(Constants.TITLE_KEY, "Blog properties");

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/editBlogProperties.jsp";
  }

  /**
   * Gets a list of all roles that are allowed to access this action.
   *
   * @return  an array of Strings representing role names
   * @param request
   */
  public String[] getRoles(HttpServletRequest request) {
    return new String[]{Constants.BLOG_OWNER_ROLE};
  }

}