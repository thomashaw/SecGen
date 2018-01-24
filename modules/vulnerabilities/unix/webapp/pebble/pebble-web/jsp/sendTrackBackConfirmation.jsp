<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellpadding="8" cellspacing="0">
  <tr class="header">
    <td>
      TrackBack sent
    </td>
    <td align="right">
      <input type="button" name="close" Value="Close" onClick="window.close()" />
    </td>
  </tr>

  <tr class="bodyContent">
    <td>
      <c:if test="${not empty trackBackResponseCode}" >
        A TrackBack for your blog entry (<a href="<c:out value="${blogEntry.permalink}"/>" target="_blank"><c:out value="${blogEntry.title}" escapeXml="true"/></a>) has been sent. The response code was <c:out value="${trackBackResponseCode}" /> and the message was...
        <br />
        <pre><c:out value="${trackBackResponseMessage}" escapeXml="true" /></pre>
      </c:if>
    </td>
  </tr>

  <tr class="footer"><td colspan="2">&nbsp;</td></tr>

</table>

<%@ include file="/common/jsp/footer.jspf" %>