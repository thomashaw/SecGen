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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pebble.Constants;
import pebble.blog.FileManager;
import pebble.blog.FileMetaData;
import pebble.blog.IllegalFileAccessException;
import pebble.blog.SimpleBlog;

/**
 * Allows the user to export a directory as a ZIP file.
 *
 * @author Simon Brown
 */
public class ZipDirectoryAction extends AbstractFileAction {

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
    String type = request.getParameter("type");
    String path = request.getParameter("path");

    // if there is no path, we're looking at the root
    if (path == null || path.length() == 0) {
      path = "/";
    }

    try {
      response.setContentType("application/zip");
      if (type.equals(FileMetaData.BLOG_DATA)) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String filename;
        if (path.equals("/logs")) {
          filename = blog.getId() + "-logs-" + sdf.format(blog.getCalendar().getTime()) + ".zip";
        } else {
          filename = blog.getId() + "-" + sdf.format(blog.getCalendar().getTime()) + ".zip";
        }
        response.setHeader("Content-Disposition", "filename=" + filename);
      } else {
        response.setHeader("Content-Disposition", "filename=export.zip");
      }
      FileManager fileManager = new FileManager(blog, type);
      List files = fileManager.getFiles(path, true);

      byte[] buf = new byte[1024];
      ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
      Iterator it = files.iterator();
      while (it.hasNext()) {
        FileMetaData file = (FileMetaData)it.next();
        if (file.isDirectory()) {
          continue;
        }

        FileInputStream in = new FileInputStream(fileManager.getFile(file.getAbsolutePath()));
        out.putNextEntry(new ZipEntry(file.getAbsolutePath().substring(1)));
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
      }

      out.close();
    } catch (IllegalFileAccessException e) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return null;
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new ServletException(ioe);
    }

    return null;
  }

}
