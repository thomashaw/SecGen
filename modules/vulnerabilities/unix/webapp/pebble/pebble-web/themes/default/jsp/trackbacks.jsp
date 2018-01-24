<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8">

  <c:choose>
    <c:when test="${blogEntry.trackBacksEnabled}" >

      <c:choose>
        <c:when test="${blogEntry.numberOfTrackBacks != 0}" >
          <c:forEach var="trackback" items="${blogEntry.trackBacks}" varStatus="status">
            <c:set var="trackback" scope="request" value="${trackback}"/>
            <c:choose>
              <c:when test="${status.count % 2 == 0}">
                <tr class="bodyContentEven">
              </c:when>
              <c:otherwise>
                <tr class="bodyContentOdd">
              </c:otherwise>
            </c:choose>
              <td colspan="2">
              <jsp:include page="trackback.jsp"/>
              </td>
            </tr>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <tr>
            <td colspan="2" class="body">
              No TrackBacks for this blog entry.
            </td>
          </tr>
        </c:otherwise>
      </c:choose>

    </c:when>
    <c:otherwise>
      <tr>
        <td colspan="2" class="body">
          TrackBacks not enabled for this blog entry.
        </td>
      </tr>
    </c:otherwise>
  </c:choose>

  <c:if test="${blogEntry.trackBacksEnabled}" >
    <tr>
      <td colspan="2" align="center">
        The TrackBack URL for this blog entry is
        <br />
        <c:out value="${blog.url}" />addTrackBack.action?entry=<c:out value="${blogEntry.id}" />
      </td>
    </tr>
  </c:if>

</table>