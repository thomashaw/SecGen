<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>
      Referers for <c:out value="${logPeriod}"/>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <tr>
          <td><b>Total<b></td>
          <td align="right"><b><c:out value="${totalReferers}"/></b></td>
        </tr>

        <c:forEach var="referer" items="${referers}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="itemBodyEven">
            </c:when>
            <c:otherwise>
                <tr class="itemBodyOdd">
            </c:otherwise>
          </c:choose>
            <td class="small">
              <a href="<c:out value="${referer.url}"/>" title="<c:out value="${referer.url}"/>"><c:out value="${referer.truncatedName}"/></a>
            </td>
            <td class="small" align="right">
              <pebble:isUserAuthenticated>
                (<a href="saveRefererFilter.secureaction?expression=<c:out value="${referer.url}"/>" title="Add this referer to your referer filters">Spam</a>)
              </pebble:isUserAuthenticated>
              <c:out value="${referer.count}"/>
            </td>
          </tr>
        </c:forEach>
      </table>
    </td>
  </tr>

</table>

<br />