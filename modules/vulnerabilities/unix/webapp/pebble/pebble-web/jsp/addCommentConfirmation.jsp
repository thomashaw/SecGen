<%@ include file="/common/jsp/header.jspf" %>

<script type="text/javascript">
  if (window.opener != null && window.opener != this) {
    window.opener.document.location.reload(true);
  }
</script>

<table width="100%" cellpadding="8" cellspacing="0">
  <tr class="header">
    <td>
      Comment added
    </td>
    <td align="right">
      <input type="button" name="close" Value="Close" onClick="window.close()" />
    </td>
  </tr>

  <tr class="body">
    <td colspan="2">
      <c:choose>
        <c:when test="${blog.commentNotificationEnabled}">
          Thank you - your comment for <a href="<c:out value="${blogEntry.permalink}"/>" target="_blank"><c:out value="${blogEntry.title}" escapeXml="true"/></a> has been posted and the blog owner has been notified.
        </c:when>
        <c:otherwise>
          Thank you - your comment for <a href="<c:out value="${blogEntry.permalink}"/>" target="_blank"><c:out value="${blogEntry.title}" escapeXml="true"/></a> has been posted.
        </c:otherwise>
      </c:choose>
    </td>
  </tr>

  <tr class="body">
    <td colspan="2">
      <%
        String commentsPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/comments.jsp";
      %>
      <jsp:include page="<%= commentsPage %>" />
    </td>
  </tr>

  <tr class="footer"><td colspan="2">&nbsp;</td></tr>

</table>

<%@ include file="/common/jsp/footer.jspf" %>