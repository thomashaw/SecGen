<%@ page import="pebble.*,
                 java.util.HashSet,
                 java.util.Set,
                 pebble.blog.SimpleBlog"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<pebble:isPebbleAdmin>
<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td colspan="2">
      Pebble administration
    </td>
  </tr>

  <c:if test="${blogManager.multiUser}">
  <tr>
    <td valign="top"><b>Multi-user</b></td>
    <td>
      <a href="editPebbleProperties.secureaction" title="Edit Pebble properties">Pebble properties</a> |
      <a href="addBlog.secureaction" title="Add a new blog">Add blog</a>
    </td>
  </tr>
  </c:if>

  <tr class="itemMetadata">
    <td colspan="2">
      Uptime : <% request.setAttribute("uptime", PebbleProperties.getInstance().getUptime()); %><c:out value="${uptime.days}"/> days, <fmt:formatNumber value="${uptime.hours}" pattern="00"/>:<fmt:formatNumber value="${uptime.minutes}" pattern="00"/>:<fmt:formatNumber value="${uptime.seconds}" pattern="00"/> |
      JVM memory : Using <%= ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) %>Kb of <%= (Runtime.getRuntime().totalMemory() / 1024) %>Kb |
      Logged in as <c:out value="${sessionScope.authenticatedUser.name}"/>
    </td>
  </tr>
</table>

<br />
</pebble:isPebbleAdmin>