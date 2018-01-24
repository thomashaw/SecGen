<%
  request.setAttribute("pebble.useCommonTheme", "true");
%>
<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellspacing="0" cellpadding="8" class="header">
  <tr>
    <td colspan="2" align="center">
      <a href="http://pebble.sourceforge.net"><img src="<%= request.getContextPath() %>/common/images/pebble.jpg" alt="Pebble" border="0"/></a>
      <br />
      Blogging tools written in Java
    </td>
  </tr>
</table>

<table width="100%" cellspacing="0" cellpadding="8" class="bodyContent">
  <tr>
    <td align="center" colspan="2">
      <jsp:include page="/common/jsp/pebbleAdminPanel.jsp"/>
       <%
         String content = request.getParameter("content");
       %>
       <jsp:include page="<%= content %>"/>
    </td>
  </tr>
</table>

<table width="100%" cellpadding="8" cellspacing="0" class="footer">
  <tr>
    <td align="center">
      Powered by <a href="http://pebble.sourceforge.net"><b>Pebble <c:out value="${pebbleProperties.version}"/></b></a>
      [
        <pebble:isUserUnauthenticated>
          <a href="login.secureaction">Login</a>
        </pebble:isUserUnauthenticated>
        <pebble:isUserAuthenticated>
          <a href="logout.action">Logout</a>
        </pebble:isUserAuthenticated>
      ]
    </td>
  </tr>
</table>

