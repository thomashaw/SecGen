<%@ page import="java.util.Enumeration"%>
<%@ page isErrorPage="true" %>

<%
  Enumeration e = request.getHeaderNames();
  while (e.hasMoreElements()) {
    String headerName = (String)e.nextElement();
    System.out.println(headerName + " = " + request.getHeader("headerName"));
  }
  System.out.println("Request URL = " + request.getRequestURL());
  System.out.println("Request URI = " + request.getRequestURI());
  System.out.println("Query string = " + request.getQueryString());
  System.out.println("Parameters :");
  java.util.Enumeration names = request.getParameterNames();
  while (names.hasMoreElements()) {
    String name = (String)names.nextElement();
    System.out.println(" " + name + " = " + request.getParameter(name));
  }

  if (exception != null) {
    System.out.println(pebble.util.ExceptionUtils.getStackTraceAsString(exception));
  }
%>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>
    Error
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      Sorry, but there has been a problem - if this problem persists, please <a href="http://www.simongbrown.com/jira/">raise an issue</a>.
      <!--
      <%= pebble.util.ExceptionUtils.getStackTraceAsString(exception) %>
      -->
    </td>
  </tr>

</table>