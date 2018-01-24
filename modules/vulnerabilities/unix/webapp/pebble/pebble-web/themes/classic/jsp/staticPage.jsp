<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8" class="blogEntry">

  <tr class="itemHeader">
    <td valign="top" colspan="2">
      <a name="a<c:out value="${blogEntry.id}"/>" />
      <a href="<c:out value="${blogEntry.permalink}"/>" title="Link to this entry"><c:out value="${blogEntry.title}"/></a>
    </td>
  </tr>

  <tr>
    <td colspan="2" class="blogEntryBody">
      <c:out value="${blogEntry.body}" escapeXml="false" />

      <c:if test="${blogEntry.aggregated}">
        <b class="small">This content has been aggregated from another source - please see the <a href="<c:out value="${blogEntry.permalink}"/>">original</a> for follow-ups and comments.</b>
      </c:if>
    </td>
  </tr>

  <tr class="blogEntryMetadata">
    <td align="left">
      Posted by <c:out value="${blogEntry.author}"/>
      on <fmt:formatDate value="${blogEntry.date}" type="both" dateStyle="long" timeStyle="long"/>
    </td>
    <td align="right">
      <c:if test="${displayMode != 'preview'}">
      <c:forEach var="category" items="${blogEntry.categories}">
        [<a href="changeCategory.action?category=<c:out value="${category.id}" />" title="Change category and only show entries in <c:out value="${category.name}"/>"><c:out value="${category.name}" /></a>]
      </c:forEach>
      <a href="<c:out value="${blogEntry.permalink}"/>" title="Link to this page">Permalink</a>
      <a href="<c:out value="${blogEntry.localPermalink}?printable=true"/>" title="See a printable version of this page">Printable</a>
      <pebble:isBlogContributor>
        <a href="javascript:openWindow('editStaticPage.secureaction?entry=<c:out value="${blogEntry.id}"/>', 'editStaticPage<%= session.getId()%>', 640, 640);" title="Edit this static page">Edit</a>
        <a href="javascript:confirmRemove('removeStaticPage.secureaction?entry=<c:out value="${blogEntry.id}"/>');" title="Remove this static page">Remove</a>
      </pebble:isBlogContributor>
      </c:if>
    </td>
  </tr>

</table>

<br />