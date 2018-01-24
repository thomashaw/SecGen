<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellpadding="8" cellspacing="0">
  <tr class="header">
    <td>
      TrackBacks for <a href="<c:out value="${blogEntry.permalink}"/>" target="_blank"><c:out value="${blogEntry.title}"/></a>
    </td>
    <td align="right">
      <input type="button" name="close" Value="Close" onClick="window.close()" />
    </td>
  </tr>

  <tr class="bodyContent">
    <td colspan="2">
      <%
        String trackbacksPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/trackbacks.jsp";
      %>
      <jsp:include page="<%= trackbacksPage %>" />
    </td>
  </tr>

<c:if test="${blogEntry.commentsEnabled}" >
<pebble:isBlogContributor>
  <form name="sendTrackBack" action="sendTrackBack.secureaction" method="POST" accept-charset="<c:out value="${blog.characterEncoding}" />">

  <tr class="bodyContent">
    <td colspan="2">
      <b>Send a TrackBack ping from this blog entry to another blog</b>
      <input type="hidden" name="entry" value="<c:out value="${blogEntry.id}"/>" />
    </td>
  </tr>

  <tr class="bodyContent">
    <td colspan="2">
      <table width="100%">
        <tr>
          <td valign="top"><b>TrackBack URL</b></td>
          <td><input type="text" name="url" size="50" /></td>
        </tr>

        <tr>
          <td valign="top"><b>Excerpt</b></td>
          <td>
            <textarea name="excerpt" rows="8" cols="40"><c:choose><c:when test="${not empty blogEntry.excerpt}"><c:out value="${blogEntry.excerpt}"/></c:when><c:otherwise><c:out value="${blogEntry.excerptFromBody}"/></c:otherwise></c:choose></textarea>
          </td>
        </tr>

        <tr>
          <td colspan="2" align="right">
            <input name="submit" type="submit" Value="Send TrackBack" />
          </td>
        </tr>
      </table>
    </td>
  </tr>

</form>
</pebble:isBlogContributor>
</c:if>

  <tr class="footer"><td colspan="2">&nbsp;</td></tr>

</table>

<%@ include file="/common/jsp/footer.jspf" %>