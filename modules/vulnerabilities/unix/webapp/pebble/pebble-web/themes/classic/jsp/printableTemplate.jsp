<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellpadding="8" cellspacing="0" class="header">
  <tr>
    <td valign="top" align="left" colspan="2">
      <div class="blogName">
        <a href="<c:out value="${blog.url}"/>"><c:out value="${blog.name}"/></a>
      </div>
      <div class="blogDescription">
        <c:out value="${blog.description}"/>
      </div>
      <br />
    </td>
  </tr>
</table>

<br />

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <%-- start inclusion of blog content --%>
    <td valign="top" align="left" rowspan="2">
      <%
        String content = request.getParameter("content");
      %>
      <jsp:include page="<%= content %>"/>
    </td>
    <%-- end inclusion of blog content --%>
  </tr>
</table>

<table width="100%" cellpadding="8" cellspacing="0" class="footer">
  <tr>
    <td align="left">
      Content &copy; <c:out value="${blog.author}"/>
    </td>
    <td align="right">
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

<%@ include file="/common/jsp/footer.jspf" %>