<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="4" class="trackback">

<tr>
  <td colspan="2" class="trackbackHeader">
    <a name="trackback<c:out value="${trackback.id}"/>" />
    <a href="<c:out value="${trackback.url}" escapeXml="true"/>" target="_blank" title="Go to the linked entry"><c:out value="${trackback.title}" escapeXml="true"/></a>
  </td>
</tr>

<tr>
  <td colspan="2" class="trackbackBody">
    <br />
    <c:out value="${trackback.excerpt}" escapeXml="false"/>
  </td>
</tr>

<tr class="trackbackMetadata">
  <td align="left">
    Posted by <c:out value="${trackback.blogName}" escapeXml="false"/>
    <pebble:isUserAuthenticated>
    (<c:out value="${trackback.ipAddress}" />)
    </pebble:isUserAuthenticated>
    on <fmt:formatDate value="${trackback.date}" type="both" dateStyle="long" timeStyle="long"/>
  </td>
  <td align="right">
    <pebble:isBlogContributor>
      <a href="<c:out value="${trackback.permalink}"/>" title="Link to this TrackBack">Permalink</a>
      <a href="javascript:confirmRemove('removeTrackBack.secureaction?entry=<c:out value="${blogEntry.id}"/>&trackback=<c:out value="${trackback.id}"/>');" title="Remove this TrackBack">Remove</a>
    </pebble:isBlogContributor>
  </td>
</tr>

</table>