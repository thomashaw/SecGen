<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<table width="100%" cellspacing="0" cellpadding="8">

  <c:choose>
    <c:when test="${blogEntry.commentsEnabled}" >

      <c:choose>
        <c:when test="${blogEntry.numberOfComments > 0}" >
          <c:forEach var="comment" items="${blogEntry.comments}" varStatus="status">
            <c:set var="comment" scope="request" value="${comment}"/>
            <c:choose>
              <c:when test="${status.count % 2 == 0}">
                <tr class="bodyContentEven">
              </c:when>
              <c:otherwise>
                <tr class="bodyContentOdd">
              </c:otherwise>
            </c:choose>
              <td>
                <table width="100%" cellspacing="0" cellpadding="0">
                <tr>
                <td>
                  <img src="<%= request.getContextPath() %>/common/images/spacer.gif" alt="" height="1" width="<c:out value="${comment.numberOfParents*16}" />" />
                </td>
                <td>
                  <jsp:include page="comment.jsp"/>
                </td>
                </tr>
                </table>
              </td>
            </tr>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <tr>
            <td class="body">
              No comments for this blog entry.
            </td>
          </tr>
        </c:otherwise>
      </c:choose>

    </c:when>
    <c:otherwise>
      <%--<tr>
        <td class="body">
          Not enabled for this blog entry.
        </td>
      </tr>--%>
    </c:otherwise>
  </c:choose>

</table>