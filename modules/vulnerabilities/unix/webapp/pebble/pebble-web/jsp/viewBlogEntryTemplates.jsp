<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Blog entry templates
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="blogEntry" items="${blogEntries}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="itemBodyEven">
            </c:when>
            <c:otherwise>
                <tr class="itemBodyOdd">
            </c:otherwise>
          </c:choose>
          <td>
            <a href="editBlogEntryTemplate.secureaction?entry=<c:out value="${blogEntry.id}"/>" title="Edit/use this blog entry template">
              <c:choose>
                <c:when test="${!empty blogEntry.title}"><c:out value="${blogEntry.title}" /></c:when>
                <c:otherwise>No Title</c:otherwise>
              </c:choose>
            </a>
          </td>
          <td align="right">
            <a href="javascript:confirmRemove('removeBlogEntryTemplate.secureaction?entry=<c:out value="${blogEntry.id}"/>');" title="Remove this blog entry template">Remove</a>
          </td>
        </tr>
        </c:forEach>
      </table>
    </td>
  </tr>
</table>

<br />