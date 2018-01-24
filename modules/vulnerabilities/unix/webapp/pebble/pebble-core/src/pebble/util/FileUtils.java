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
package pebble.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * A collection of utility methods for manipulating files.
 *
 * @author    Simon Brown
 */
public final class FileUtils {

  /**
   * Determines whether a given file is underneath a given root.
   *
   * @param root    the root directory
   * @param file    the file to test
   * @return    true if the file is underneath the root,
   *            false otherwise or if this can not be determined because
   *            of security constraints in place
   */
  public static boolean underneathRoot(File root, File file) {
    try {
      // first of all, find the root directory for this type of file
      root = root.getCanonicalFile();
      file = file.getCanonicalFile();
      while (file != null) {
        if (file.equals(root)) {
          return true;
        } else {
          file = file.getParentFile();
        }
      }
    } catch (IOException ioe) {
      return false;
    }

    return false;
  }

  /**
   * Deletes a directory, including all files and sub-directories.
   *
   * @param directory   a File instance representing the directory to delete
   */
  public static void deleteDirectory(File directory) {
    File files[] = directory.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          deleteDirectory(files[i]);
        } else {
          files[i].delete();
        }
      }
    }

    directory.delete();
  }

  /**
   * Copies a file.
   *
   * @param source        the source File
   * @param destination   the destination File
   */
  public static void copyFile(File source, File destination) throws IOException {
    FileChannel srcChannel = new FileInputStream(source).getChannel();
    FileChannel dstChannel = new FileOutputStream(destination).getChannel();
    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    srcChannel.close();
    dstChannel.close();
  }

}