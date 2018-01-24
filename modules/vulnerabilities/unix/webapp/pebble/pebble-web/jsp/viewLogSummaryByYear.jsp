<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>
      Log summary for <fmt:formatDate value="${logSummary.date}" pattern="yyyy" />
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <tr>
          <td><b>Total for <fmt:formatDate value="${logSummary.date}" pattern="yyyy" /></b></td>
          <td align="right">&nbsp;</td>
          <td align="right"><b><c:out value="${logSummary.totalRequests}"/></b></td>
        </tr>

        <c:forEach var="logSummaryForMonth" items="${logSummary.logSummaries}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="itemBodyOdd">
            </c:when>
            <c:otherwise>
                <tr class="itemBodyEven">
            </c:otherwise>
          </c:choose>
            <td>
              <fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="MMMM yyyy"/>
            </td>
            <td align="right">
              <a href="viewLogSummary.secureaction?year=<fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="yyyy"/>&month=<fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="MM"/>" title="See log summary for month">Log Summary</a> |
              <a href="viewReferers.secureaction?year=<fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="yyyy"/>&month=<fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="MM"/>" title="See referers for month">Referers</a> |
              <a href="viewRequests.secureaction?year=<fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="yyyy"/>&month=<fmt:formatDate value="${logSummaryForMonth.date}" type="date" pattern="MM"/>" title="See requests for month">Requests</a>
            </td>
            <td align="right"><c:out value="${logSummaryForMonth.totalRequests}"/></td>
            </td>
          </tr>
        </c:forEach>

      </table>
    </td>
  </tr>

</table>

<br />