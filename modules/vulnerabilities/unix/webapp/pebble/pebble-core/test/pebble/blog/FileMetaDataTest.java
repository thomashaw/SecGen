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
package pebble.blog;

import java.util.Date;

import junit.framework.TestCase;

/**
 * Tests for the FileMetaData class.
 *
 * @author    Simon Brown
 */
public class FileMetaDataTest extends TestCase {

  private FileMetaData file;

  public void testConstructionOfRootFile() {
    file = new FileMetaData(null);
    assertEquals("/", file.getPath());
    assertEquals("", file.getName());

    file = new FileMetaData("");
    assertEquals("/", file.getPath());
    assertEquals("", file.getName());

    file = new FileMetaData("/");
    assertEquals("/", file.getPath());
    assertEquals("", file.getName());
  }

  public void testConstructionOfDirectory() {
    file = new FileMetaData("/directory");
    assertEquals("/", file.getPath());
    assertEquals("directory", file.getName());

    file = new FileMetaData("/directory/");
    assertEquals("/", file.getPath());
    assertEquals("directory", file.getName());
  }

  public void testConstructionOfSubDirectory() {
    file = new FileMetaData("/directory/subdirectory");
    assertEquals("/directory", file.getPath());
    assertEquals("subdirectory", file.getName());

    file = new FileMetaData("/directory/subdirectory/");
    assertEquals("/directory", file.getPath());
    assertEquals("subdirectory", file.getName());
  }

  public void testDirectoriesAreNotEditable() {
    file = new FileMetaData("/directory");
    assertFalse(file.isEditable());
  }

  public void testTextFilesAreEditable() {
    file = new FileMetaData("/somefile.txt");
    assertTrue(file.isEditable());
    file = new FileMetaData("/somefile.jsp");
    assertTrue(file.isEditable());
    file = new FileMetaData("/somefile.jspf");
    assertTrue(file.isEditable());
    file = new FileMetaData("/somefile.html");
    assertTrue(file.isEditable());
    file = new FileMetaData("/somefile.htm");
    assertTrue(file.isEditable());
    file = new FileMetaData("/somefile.css");
    assertTrue(file.isEditable());
    file = new FileMetaData("/somefile.xml");
    assertTrue(file.isEditable());
  }

  public void testBinaryFilesAreNotEditable() {
    file = new FileMetaData("/somefile.gif");
    assertFalse(file.isEditable());
    file = new FileMetaData("/somefile.jpg");
    assertFalse(file.isEditable());
    file = new FileMetaData("/somefile.png");
    assertFalse(file.isEditable());
    file = new FileMetaData("/somefile.zip");
    assertFalse(file.isEditable());
  }

  public void testAllDirectoriesAreNotEditable() {
    file = new FileMetaData("/somedirectory");
    file.setDirectory(true);
    assertFalse(file.isEditable());
  }

  public void testName() {
    file = new FileMetaData("/");
    file.setName("somename");
    assertEquals("somename", file.getName());
  }

  public void testLastModifiedDate() {
    Date date = new Date();
    file = new FileMetaData("/");
    file.setLastModified(date);
    assertEquals(date, file.getLastModified());
  }

  public void testDirectory() {
    file = new FileMetaData("/");
    file.setDirectory(true);
    assertTrue(file.isDirectory());

    file.setDirectory(false);
    assertFalse(file.isDirectory());
  }

  public void testAbsolutePath() {
    file = new FileMetaData("/somedirectory/somefile.gif");
    assertEquals("/somedirectory/somefile.gif", file.getAbsolutePath());
  }

  public void testUrl() {
    file = new FileMetaData("/somefile.txt");
    file.setType(FileMetaData.BLOG_IMAGE);
    assertEquals("./images/somefile.txt", file.getUrl());

    file.setType(FileMetaData.BLOG_FILE);
    assertEquals("./files/somefile.txt", file.getUrl());
  }

  public void testSize() {
    file = new FileMetaData("/");
    file.setSize(123456789);
    assertEquals(123456789, file.getSize());
    assertEquals(123456789/1024, file.getSizeInKB());
  }

}
