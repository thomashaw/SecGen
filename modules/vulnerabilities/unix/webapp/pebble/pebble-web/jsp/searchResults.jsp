<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Search results for "<c:out value="${searchResults.query}"/>" (<c:out value="${searchResults.numberOfHits}"/> found, ordered by rank)
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="hit" items="${searchResults.hits}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="itemBodyEven">
            </c:when>
            <c:otherwise>
                <tr class="itemBodyOdd">
            </c:otherwise>
          </c:choose>
          <td>
            <a href="<c:out value="${hit.permalink}"/>" title="Go to this entry - score is <c:out value="${hit.score}"/>"><c:out value="${hit.title}"/></a>
          </td>
          <td align="right">
            <fmt:formatDate value="${hit.date}" type="date" dateStyle="long"/>
          </td>
        </tr>
        </c:forEach>
      </table>
    </td>
  </tr>
</table>

<br />