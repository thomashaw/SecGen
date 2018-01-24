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
    </td>
  </tr>

  <tr>
    <td valign="middle" align="left">
      <form method="get" action="changeCategory.action">
        <pebble:categoryFilter />&nbsp;<input type="submit" value="Change Category" />
      </form>
    </td>
    <td valign="middle" align="right">
      <form method="get" action="search.action" accept-charset="<c:out value="${blog.characterEncoding}"/>">
        <input type="text" name="query" />&nbsp;<input type="submit" value="Search" />
      </form>
    </td>
  </tr>
</table>

<br />

<table width="100%" cellspacing="0" cellpadding="0" border="0" class="bodyContent">
  <tr>
    <%-- start inclusion of blog content --%>
    <td valign="top" align="left" rowspan="2">
      <jsp:include page="/common/jsp/blogAdminPanel.jsp"/>
      <%
        String content = request.getParameter("content");
      %>
      <jsp:include page="<%= content %>"/>
    </td>
    <%-- end inclusion of blog content --%>

    <td rowspan="2">&nbsp;&nbsp;&nbsp;</td>

    <%-- start right nav --%>
    <td width="200" align="center" valign="top">
      <jsp:include page="rightnav.jsp"/>
    </td>
    <%-- end right nav --%>
  </tr>

  <tr>
    <td valign="bottom">
      <table width="100%" cellspacing="0" cellpadding="8" class="item">

        <tr class="itemHeader" align="center">
          <td>
            About this Blog
          </td>
        </tr>

        <tr class="itemBody" align="center">
          <td class="small">
            <a href="http://pebble.sourceforge.net"><img src="<pebble:theme/>/images/powered-by-pebble.gif" alt="Powered by Pebble" border="0" /></a>
          </td>
        </tr>
      </table>
      <br />
    </td>
  </tr>
</table>

<table width="100%" cellpadding="8" cellspacing="0" class="footer">
  <tr>
    <td align="left">
      Content &copy; <c:out value="${blog.author}" escapeXml="false" />
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