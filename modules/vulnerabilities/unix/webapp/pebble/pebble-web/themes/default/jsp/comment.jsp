<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="4" class="comment">

<tr>
  <td colspan="2" class="commentHeader">
    <a name="comment<c:out value="${comment.id}"/>" />
    <a href="<c:out value="${comment.permalink}" escapeXml="true"/>" title="Link to this comment"><c:out value="${comment.title}" escapeXml="true"/></a>
  </td>
</tr>

<tr>
  <td colspan="2" class="commentBody">
    <c:out value="${comment.body}" escapeXml="false"/>
  </td>
</tr>

<tr class="commentMetadata">
  <td align="left">
    Posted by
    <c:if test="${!empty comment.website}">
      <a href="<c:out value="${comment.website}" escapeXml="true"/>" target="_blank" title="Go to the author's website"><c:out value="${comment.author}" escapeXml="true"/></a>
    </c:if>
    <c:if test="${empty comment.website}">
      <c:out value="${comment.author}" escapeXml="true"/>
    </c:if>
    <pebble:isUserAuthenticated>
      (<c:out value="${comment.email}" escapeXml="true" default="-" />/<c:out value="${comment.ipAddress}" default="-" />)
    </pebble:isUserAuthenticated>
    on <fmt:formatDate value="${comment.date}" type="both" dateStyle="long" timeStyle="long"/>
  </td>
  <td align="right">
    <a href="<c:out value="${comment.permalink}"/>" title="Link to this comment">Permalink</a>
    <a href="javascript:openWindow('replyToComment.action?entry=<c:out value="${blogEntry.id}"/>&comment=<c:out value="${comment.id}"/>', 'viewComments<%= session.getId()%>', 640, 480);" title="Reply to this comment">Reply</a>
    <pebble:isBlogContributor>
      <a href="javascript:confirmRemove('removeComment.secureaction?entry=<c:out value="${blogEntry.id}"/>&comment=<c:out value="${comment.id}"/>');" title="Remove this comment">Remove</a>
    </pebble:isBlogContributor>
  </td>
</tr>

</table>