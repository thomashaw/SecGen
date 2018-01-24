<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<c:choose>
  <c:when test="${empty blogEntries}">
    <br />There are no blog entries.
  </c:when>

  <c:otherwise>
    <c:forEach var="blogEntry" items="${blogEntries}">
      <c:set var="blogEntry" value="${blogEntry}" scope="request"/>
      <jsp:include page="blogEntry.jsp"/>
    </c:forEach>
  </c:otherwise>
</c:choose>