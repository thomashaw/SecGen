<%@ page import="pebble.blog.SimpleBlog,
                 pebble.blog.Blog,
                 pebble.Constants"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<%
  Blog blog = (Blog)request.getAttribute(Constants.BLOG_KEY);
%>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      <a href="<c:out value="${blogEntry.permalink}"/>"><c:out value="${blogEntry.title}" escapeXml="true"/></a> has been posted
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <c:choose>
        <c:when test="${blog.updateNotificationPingsEnabled}">
          The following websites have been pinged via XML-RPC
          <ul>
          <c:forEach var="website" items="${blog.updateNotificationPingsAsCollection}">
            <li><c:out value="${website}"/></li>
          </c:forEach>
          </ul>
        </c:when>
        <c:otherwise>
          XML-RPC notifications are disabled so no websites have been pinged.
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
</table>

<br />

<%
  String blogEntryPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/blogEntry.jsp";
%>
<jsp:include page="<%= blogEntryPage %>" />