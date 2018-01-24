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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.xmlrpc.XmlRpcServer;
import pebble.webservice.BloggerAPIHandler;
import pebble.webservice.MetaWeblogAPIHandler;

/**
 * Single entry point for all XML-RPC requests (e.g. Blogger API).
 *
 * @author    Simon Brown
 */
public class XmlRpcServlet extends HttpServlet {

  /**
   * Initialises this instance.
   */
  public void init() {
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

    try {
      XmlRpcServer xmlrpc = new XmlRpcServer();
      xmlrpc.addHandler("blogger", new BloggerAPIHandler());
      xmlrpc.addHandler("metaWeblog", new MetaWeblogAPIHandler());

      byte[] result = xmlrpc.execute(request.getInputStream());
      response.setContentType("text/xml; charset=UTF-8");
      response.setContentLength(result.length);
      OutputStream out = response.getOutputStream();
      out.write(result);
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException(e);
    }
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