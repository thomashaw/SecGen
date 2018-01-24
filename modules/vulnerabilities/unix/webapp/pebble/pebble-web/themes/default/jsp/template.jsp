<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellpadding="8" cellspacing="0" class="header">
  <tr>
    <td valign="top" align="left">
      <div class="blogName">
        <a href="<c:out value="${blog.url}"/>"><c:out value="${blog.name}"/></a>
      </div>
      <div class="blogDescription">
        <c:out value="${blog.description}"/>
      </div>
      <br />
    </td>
    <td align="right" valign="top">
      <form method="get" action="search.action" accept-charset="<c:out value="${blog.characterEncoding}"/>">
        <input type="text" name="query" />&nbsp;<input type="submit" value="Search" />
      </form>
    </td>
  </tr>

  <tr>
    <td valign="middle" align="left" colspan="2">
      <br />
      <div class="small">
      Categories :
      <c:choose>
        <c:when test="${empty sessionScope.currentCategory}">
          All
        </c:when>
        <c:otherwise>
          <a href="changeCategory.action?category=all">All</a>
        </c:otherwise>
      </c:choose>

      <pebble:categories>
        |
        <%
          if (category.equals(session.getAttribute(Constants.CURRENT_CATEGORY_KEY))) {
        %>
          <c:out value="${category.name}" />
        <%
          } else {
        %>
        <a href="<c:out value="${category.permalink}" />"><c:out value="${category.name}" /></a>
        <%
          }
        %>
      </pebble:categories>
      </div>
    </td>
  </tr>
</table>

<table width="100%" cellspacing="0" cellpadding="8" class="bodyContent">
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

    <%-- start right nav --%>
    <td width="200" align="center" valign="top">
      <jsp:include page="rightnav.jsp"/>
    </td>
    <%-- end right nav --%>
  </tr>

  <tr>
    <td valign="bottom">
      <table width="100%" cellspacing="0" cellpadding="8" class="item">
        <tr class="itemBody" align="center">
          <td class="small">
            <a href="http://pebble.sourceforge.net"><img src="<pebble:theme/>/images/powered-by-pebble.gif" alt="Powered by Pebble" border="0" /></a>
          </td>
        </tr>
      </table>
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