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

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.FileManager;
import pebble.blog.FileMetaData;
import pebble.blog.SimpleBlog;
import pebble.blog.IllegalFileAccessException;

/**
 * Allows the user to view the files in a specific location - blog images,
 * blog files and theme files.
 *
 * @author    Simon Brown
 */
public class ViewFilesAction extends AbstractFileAction {

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
    String type = request.getParameter("type");
    String path = request.getParameter("path");
    String fileName = request.getParameter("file");

    // if there is no path, we're looking at the root
    if (path == null || path.length() == 0) {
      path= "/";
    }

    FileManager fileManager = new FileManager(blog, type);
    FileMetaData directory = fileManager.getFileMetaData(path);

    try {
      List files = fileManager.getFiles(path);
      request.setAttribute("files", files);
    } catch (IllegalFileAccessException e) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return null;
    }

    String uploadAction = null;
    if (type.equals(FileMetaData.BLOG_IMAGE)) {
      uploadAction = "uploadImageToBlog.secureaction";
      request.setAttribute(Constants.TITLE_KEY, "Images in blog");
    } else if (type.equals(FileMetaData.THEME_FILE)) {
      uploadAction = "uploadFileToTheme.secureaction";
      request.setAttribute(Constants.TITLE_KEY, "Files in theme");
    } else {
      uploadAction = "uploadFileToBlog.secureaction";
      request.setAttribute(Constants.TITLE_KEY, "Files in blog");
    }

    // does the user want details about a specific file?
    if (fileName != null && fileName.length() > 0) {
      request.setAttribute("file", fileManager.getFileMetaData(path, fileName));
    }

    request.setAttribute("type", type);
    request.setAttribute("directory", directory);
    request.setAttribute("uploadAction", uploadAction);
    request.setAttribute("root", fileManager.getFileMetaData("/"));
    request.setAttribute("currentUsage", new Long(FileManager.getCurrentUsage(blog)));

    return "/themes/" + blog.getTheme() + "/jsp/template.jsp?content=/jsp/viewFiles.jsp";
  }

}