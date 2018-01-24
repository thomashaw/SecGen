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
package pebble.radio;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import org.xml.sax.*;
import pebble.blog.*;

/**
 * Simple utility to convert posts exported from Radio Userland into the XML
 * format used by Pebble.
 *
 * @author    Simon Brown
 */
public class Converter {

  /**
   * Starts the converter.
   */
  public static void main(String[] args) throws Exception {
    File root = new File("c:\\sandbox\\blog\\sam\\radio");
    File sources[] = root.listFiles();
    SimpleBlog blog = new SimpleBlog("c:\\sandbox\\blog\\sam");
    for (int i = 0; i < sources.length; i++) {
      convert(blog, sources[i]);
    }

  }

  private static void convert(SimpleBlog blog, File source) throws Exception {
    System.out.println("Converting " + source.getName());
    // create a factory and builder - an abstraction for an XML parser
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setIgnoringComments(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new ErrorHandler() {
      public void warning(SAXParseException e) throws SAXException {
        System.out.println("Warning : " + e.getMessage());
        throw e;
      }

      public void error(SAXParseException e) throws SAXException {
        System.out.println("Error : " + e.getMessage());
        throw e;
      }

      public void fatalError(SAXParseException e) throws SAXException {
        System.out.println("Fatal : " + e.getMessage());
        throw e;
      }
    });

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
    String title = "No title";
    String body = "";
    Date date = null;
    Document doc = builder.parse(source);
    Node root = doc.getDocumentElement();
    NodeList nodes = root.getChildNodes();
    System.out.println(nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      System.out.println(n.getNodeName());

      if (n.getNodeName().equals("string")) {
        System.out.println(n.getAttributes().getNamedItem("name"));
        System.out.println(n.getAttributes().getNamedItem("name").getNodeValue());
        if (n.getAttributes().getNamedItem("name").getNodeValue().equals("title")) {
          title = n.getAttributes().getNamedItem("value").getNodeValue();
          System.out.println("Title : " + title);
        } else if (n.getAttributes().getNamedItem("name").getNodeValue().equals("text")) {
          body = n.getAttributes().getNamedItem("value").getNodeValue();
          System.out.println("Body : " + body);
        }
      }

      if (n.getNodeName().equals("date") && n.getAttributes().getNamedItem("name").getNodeValue().equals("when")) {
        date = sdf.parse((n.getAttributes().getNamedItem("value").getNodeValue()).substring(4));
        System.out.println("Date : " + date);
      }
    }

    DailyBlog daily = blog.getBlogForDay(date);
    BlogEntry entry = daily.createBlogEntry(title, body, date);
    daily.addEntry(entry);
    entry.store();
  }

}
