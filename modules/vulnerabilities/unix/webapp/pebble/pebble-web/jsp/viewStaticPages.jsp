<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Static pages
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="blogEntry" items="${blogEntries}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="bodyContentEven">
            </c:when>
            <c:otherwise>
                <tr class="bodyContentOdd">
            </c:otherwise>
          </c:choose>
          <td><a href="<c:out value="${blogEntry.permalink}"/>"><c:out value="${blogEntry.title}"/></a></td>
          <td align="right">
            <a href="editStaticPage.secureaction?entry=<c:out value="${blogEntry.id}"/>" title="Edit this static page">Edit</a>
            |
            <a href="javascript:confirmRemove('removeStaticPage.secureaction?entry=<c:out value="${blogEntry.id}"/>');" title="Remove this static page">Remove</a>
          </td>
        </tr>
        </c:forEach>
      </table>
    </td>
  </tr>

</table>

<br />