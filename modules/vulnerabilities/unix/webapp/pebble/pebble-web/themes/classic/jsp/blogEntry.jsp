<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<c:if test="${displayMode == 'detail'}">
<table width="100%" cellspacing="0" cellpadding="8">
  <tr>
    <td align="center" class="small">
    <c:if test="${not empty blogEntry.previousBlogEntry}">
      << <a href="<c:out value="${blogEntry.previousBlogEntry.localPermalink}" />"><c:out value="${blogEntry.previousBlogEntry.title}" /></a> |
    </c:if>
    <a href="<c:out value="${blog.url}" />">Home</a>
    <c:if test="${not empty blogEntry.nextBlogEntry}">
      | <a href="<c:out value="${blogEntry.nextBlogEntry.localPermalink}" />"><c:out value="${blogEntry.nextBlogEntry.title}" /></a> >>
    </c:if>
    </td>
  </tr>
</table>
</c:if>

<table width="100%" cellspacing="0" cellpadding="8" class="blogEntry">

  <tr class="blogEntryHeader">
    <td valign="top" colspan="2">
      <a name="a<c:out value="${blogEntry.id}"/>" />
      <a href="<c:out value="${blogEntry.permalink}"/>" title="Link to this entry"><c:out value="${blogEntry.title}"/></a>
    </td>
  </tr>

  <tr>
    <td colspan="2" class="blogEntryBody">
      <c:choose>
        <c:when test="${displayMode == 'detail'}">
          <c:out value="${blogEntry.body}" escapeXml="false" />
          <c:if test="${blogEntry.aggregated}">
            <p><b class="small">This content has been aggregated from another source - please see the <a href="<c:out value="${blogEntry.permalink}"/>">original</a> for follow-ups and comments.</b></p>
          </c:if>
        </c:when>
        <c:when test="${displayMode == 'preview'}">
          <b>Excerpt</b><c:out value="${blogEntry.excerpt}" escapeXml="false" />
          <br /><br />
          <b>Body</b><c:out value="${blogEntry.body}" escapeXml="false" />
        </c:when>
        <c:otherwise>
          <c:choose>
            <c:when test="${empty blogEntry.excerpt}">
              <c:out value="${blogEntry.body}" escapeXml="false" />
              <c:if test="${blogEntry.aggregated}">
                <p><b class="small">This content has been aggregated from another source - please see the <a href="<c:out value="${blogEntry.permalink}"/>">original</a> for follow-ups and comments.</b></p>
              </c:if>
            </c:when>
            <c:otherwise>
              <c:out value="${blogEntry.excerpt}" escapeXml="false" />
              <p><b class="small">Read the full content <a href="<c:out value="${blogEntry.permalink}"/>">here</a>.</b></p>
            </c:otherwise>
          </c:choose>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>

  <tr class="blogEntryMetadata">
    <td align="left">
      Posted by <c:out value="${blogEntry.author}"/>
      on <a href="<c:out value="${blogEntry.dailyBlog.permalink}"/>" title="See all entries for this day"><fmt:formatDate value="${blogEntry.date}" type="both" dateStyle="long" timeStyle="long"/></a>
    </td>
    <td align="right">
      <c:forEach var="category" items="${blogEntry.categories}">
        [<a href="changeCategory.action?category=<c:out value="${category.id}" />" title="Change category and only show entries in <c:out value="${category.name}"/>"><c:out value="${category.name}" /></a>]
      </c:forEach>
      <a href="<c:out value="${blogEntry.permalink}"/>" title="Link to this entry">Permalink</a>
      <a href="<c:out value="${blogEntry.localPermalink}?printable=true"/>" title="See a printable version of this entry">Printable</a>
      <c:if test="${blogEntry.trackBacksEnabled}">
        <a href="javascript:openWindow('<c:out value="${blogEntry.trackBacksLink}"/>', 'viewTrackBacks<%= session.getId()%>', 640, 480);" title="See TrackBacks for this entry">TrackBacks[<c:out value="${blogEntry.numberOfTrackBacks}"/>]</a>
      </c:if>
      <c:if test="${blogEntry.commentsEnabled}">
        <a href="javascript:openWindow('<c:out value="${blogEntry.commentsLink}"/>', 'viewComments<%= session.getId()%>', 640, 480);" title="See comments for this entry">Comments[<c:out value="${blogEntry.numberOfComments}"/>]</a>
      </c:if>

      <c:if test="${displayMode != 'preview'}">
      <pebble:isBlogContributor>
        <a href="editBlogEntry.secureaction?entry=<c:out value="${blogEntry.id}"/>" title="Edit this entry">Edit</a>
        <a href="javascript:confirmRemove('removeBlogEntry.secureaction?entry=<c:out value="${blogEntry.id}"/>');" title="Remove this entry">Remove</a>
      </pebble:isBlogContributor>
      </c:if>
    </td>
  </tr>


<c:if test="${displayMode == 'detail'}">
  <tr>
    <td colspan="2" class="blogEntryBody">
      <jsp:include page="comments.jsp"/>
    </td>
  </tr>
  <c:if test="${blogEntry.commentsEnabled}">
  <tr>
    <td colspan="2" class="blogEntryBody">
      <div align="center"><a href="javascript:openWindow('<c:out value="${blogEntry.commentsLink}"/>', 'viewComments<%= session.getId()%>', 640, 480);" title="Add a comment on this entry">Add a comment</a></div>
    </td>
  </tr>
  </c:if>
  <tr>
    <td colspan="2" class="blogEntryBody">
      <jsp:include page="trackbacks.jsp"/>
    </td>
  </tr>
</c:if>

</table>

<br />
