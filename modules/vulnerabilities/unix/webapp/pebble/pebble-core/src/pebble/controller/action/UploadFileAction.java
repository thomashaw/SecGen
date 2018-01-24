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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import pebble.Constants;
import pebble.PebbleProperties;
import pebble.blog.FileManager;
import pebble.blog.SimpleBlog;
import pebble.blog.FileMetaData;

/**
 * Superclass for actions that allow the user to upload a file.
 *
 * @author    Simon Brown
 */
public abstract class UploadFileAction extends AbstractFileAction {

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

    String type = getType();
    String path = "";
    String filename = "";

    FileManager fileManager = new FileManager(blog, type);

    try {
      boolean isMultipart = FileUpload.isMultipartContent(request);

      if (isMultipart) {
        DiskFileUpload upload = new DiskFileUpload();
        int sizeInBytes = PebbleProperties.getInstance().getFileUploadSize() * 1024; // convert to bytes
        upload.setSizeMax(sizeInBytes);
        upload.setSizeThreshold(sizeInBytes/4);
        upload.setRepositoryPath(System.getProperty("java.io.tmpdir"));

        List items;
        try {
          items = upload.parseRequest(request);
        } catch (FileUploadBase.SizeLimitExceededException e) {
          return "/common/jsp/template.jsp?content=/common/jsp/fileTooLarge.jsp";
        }

        // find the form fields first
        Iterator it = items.iterator();
        while (it.hasNext()) {
          FileItem item = (FileItem)it.next();
          if (item.isFormField() && item.getFieldName().equals("filename")) {
            filename = item.getString();
          } else if (item.isFormField() && item.getFieldName().equals("path")) {
            path = item.getString();
          }
        }

        // now the actual file
        it = items.iterator();
        while (it.hasNext()) {
          FileItem item = (FileItem)it.next();

          if (!item.isFormField() && item.getSize() > 0) {
            // if the filename hasn't been specified, use that from the file
            // being uploaded
            if (filename == null || filename.length() == 0) {
              filename = item.getName();
            }

            File destinationDirectory = fileManager.getFile(path);
            File file = new File(destinationDirectory, filename);
            if (!fileManager.isUnderneathRootDirectory(file)) {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              return null;
            }

            long itemSize = item.getSize()/1024;
            if (FileManager.hasEnoughSpace(blog, itemSize)) {
              writeFile(fileManager, path, filename, item);

              // if it's a theme file, also create a copy in blog.dir/theme
              if (type.equals(FileMetaData.THEME_FILE)) {
                fileManager = new FileManager(blog, FileMetaData.BLOG_DATA);
                writeFile(fileManager, "/theme" + path, filename, item);
              }
              break;
            } else {
              return "/common/jsp/template.jsp?content=/common/jsp/notEnoughSpace.jsp";
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ServletException(e);
    }

    return "/viewFiles.secureaction?type=" + type + "&path=" + path;
  }

  /**
   * Helper method to write a file.
   *
   * @param fileManager   a FileManager instance
   * @param path          the path where to save the file
   * @param filename      the filename
   * @param item          the uploaded item
   * @throws Exception    if something goes wrong writing the file
   */
  private void writeFile(FileManager fileManager, String path, String filename, FileItem item) throws Exception {
    File destinationDirectory = fileManager.getFile(path);
    destinationDirectory.mkdirs();

    File file = new File(destinationDirectory, filename);
    item.write(file);
  }

  /**
   * Gets the type of this upload (blog image, blog file or theme file).
   *
   * @return    a String representing the type
   * @see       pebble.blog.FileMetaData
   */
  protected abstract String getType();

  /**
   * Gets a list of all roles that are allowed to access this action.
   *
   * @return  an array of Strings representing role names
   */
  public abstract String[] getRoles(HttpServletRequest request);

}