<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Referer filters
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="filter" items="${filters}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="bodyContentEven">
            </c:when>
            <c:otherwise>
                <tr class="bodyContentOdd">
            </c:otherwise>
          </c:choose>
            <td>
              <c:out value="${filter.expression}" escapeXml="false"/>
            </td>
            <td align="right">
              <c:set var="removeUrl" value="removeRefererFilter.secureaction?id=${filter.id}" />
              <a href="javascript:confirmRemove('<c:out value="${removeUrl}"/>');" title="Remove this referer filter">Remove</a>
            </td>
          </tr>
        </c:forEach>
      </table>
    </td>
  </tr>

  <form name="refererFilter" action="saveRefererFilter.secureaction" method="POST">
    <tr class="itemBody">
      <td>
        To add a new filter, type a regular expression into the text field and click Add Filter.
        <br /><br />
        <b>Filter</b>
        <input type="text" name="expression" size="40" value="" />
        <input name="submit" type="submit" Value="Add Filter" />
      </td>
    </tr>
  </form>

</table>

<br />