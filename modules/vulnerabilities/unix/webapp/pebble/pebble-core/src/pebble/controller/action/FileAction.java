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

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.util.FileUtils;
import pebble.blog.*;
import pebble.controller.Action;

/**
 * Gets a file/image from a blog.
 *
 * @author    Simon Brown
 */
public class FileAction extends Action {

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

    Object o = request.getAttribute(Constants.BLOG_KEY);
    if (o instanceof CompositeBlog) {
      try {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
      } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    }

    SimpleBlog blog = (SimpleBlog)request.getAttribute(Constants.BLOG_KEY);

    String name = request.getParameter("name");
    String type = request.getParameter("type");
    if (name == null || name.length() == 0 || name.equals("/")) {
      // send back a "forbidden" because somebody is trying to look in the
      // directory rather than requesting a specific image
      // - we might do file browsing in the future :-)
      try {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return null;
      } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    }

    FileManager fileManager = new FileManager(blog, type);
    File root = fileManager.getRootDirectory();
    File file = fileManager.getFile(name);

    if (!file.exists()) {
      // file doesn't exist so send back a 404
      try {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
      } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    }

    if (!FileUtils.underneathRoot(root, file) || file.isDirectory()) {
      // somebody is trying to access a file outside of the
      // "files" or "images" directories
      // or they are trying to browse a directory
      try {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return null;
      } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    } else {
      // first of all set the appropriate content type
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      response.setContentType(fileNameMap.getContentTypeFor(name));

      // now set the content length
      int length = (int)file.length();
      response.setContentLength(length);

      // and finally the last modified date
      SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
      Date lastModified = new Date(file.lastModified());
      Calendar expires = blog.getCalendar();
      expires.add(Calendar.MONTH, 1);
      response.setHeader("Last-Modified", sdf.format(lastModified));
      response.setHeader("Expires", sdf.format(expires.getTime()));
      try {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        int i = in.read();
        while (i != -1) {
          out.write(i);
          i = in.read();
        }
        in.close();
        out.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
        throw new ServletException(ioe);
      }
    }

    return null;
  }

}