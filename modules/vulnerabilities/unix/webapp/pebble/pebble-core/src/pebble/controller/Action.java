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

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The superclass for all command/action components.
 *
 * @author    Simon Brown
 */
public abstract class Action {

  /** the log used by this class */
  private static Log log = LogFactory.getLog(Action.class);

  /**
   * Peforms the processing associated with this action.
   *
   * @param request     the HttpServletRequest instance
   * @param response    the HttpServletResponse instance
   * @return  the name of the next view
   */
  public abstract String process(HttpServletRequest request, HttpServletResponse response)
      throws ServletException;

  /**
   * A helper method to dump out the request parameters.
   *
   * @param request   the HttpServletRequest to output
   */
  protected final void dumpRequest(HttpServletRequest request) {
    log.debug("Dumping the request parameters");

    Enumeration enum = request.getParameterNames();
    while (enum.hasMoreElements()) {
      String key = (String)enum.nextElement();
      String[] paramValues = request.getParameterValues(key);
      for (int i = 0; i < paramValues.length; i++) {
        log.debug(" " + key + " : " + paramValues[i]);
      }
    }
  }

}