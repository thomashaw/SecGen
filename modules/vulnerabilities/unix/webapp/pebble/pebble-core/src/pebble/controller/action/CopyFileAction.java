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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.FileManager;
import pebble.blog.SimpleBlog;
import pebble.blog.IllegalFileAccessException;
import pebble.blog.FileMetaData;

/**
 * Allows the user to copy (or rename/move) a file.
 *
 * @author Simon Brown
 */
public class CopyFileAction extends AbstractFileAction {

  /**
   * Peforms the processing associated with this action.
   *
   * @param request  The HttpServletRequest instance.
   * @param response The HttpServletResponse instance.
   * @return The name of the next view
   */
  public String process(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException {

    SimpleBlog blog = (SimpleBlog) request.getAttribute(Constants.BLOG_KEY);
    String name = request.getParameter("name");
    String type = request.getParameter("type");
    String path = request.getParameter("path");
    String newName = request.getParameter("newName");
    String submit = request.getParameter("submit");

    try {
      FileManager fileManager = new FileManager(blog, type);
      if (submit.equalsIgnoreCase("rename")) {
        fileManager.renameFile(path, name, newName);

        // if it's a theme file, also rename the copy in blog.dir/theme
        if (type.equals(FileMetaData.THEME_FILE)) {
          fileManager = new FileManager(blog, FileMetaData.BLOG_DATA);
          fileManager.renameFile("/theme" + path, name, newName);
        }
      } else {
        if (FileManager.hasEnoughSpace(blog, fileManager.getFileMetaData(path, name).getSizeInKB())) {
          fileManager.copyFile(path, name, newName);

          // if it's a theme file, also create a copy in blog.dir/theme
          if (type.equals(FileMetaData.THEME_FILE)) {
            fileManager = new FileManager(blog, FileMetaData.BLOG_DATA);
            fileManager.copyFile("/theme" + path, name, newName);
          }
        } else {
          return "/common/jsp/template.jsp?content=/common/jsp/notEnoughSpace.jsp";
        }
      }
    } catch (IllegalFileAccessException e) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return null;
    } catch (IOException ioe) {
      throw new ServletException(ioe);
    }

    return "/viewFiles.secureaction";
  }

}