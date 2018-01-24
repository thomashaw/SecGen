<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8">

  <tr class="itemBody" align="center">
    <td>
      <div class="small">
        <a href="rss.xml" title="RSS 2.0 feed for this blog">RSS</a> |
        <a href="rdf.xml" title="RDF 1.0 feed for this blog">RDF</a> |
        <a href="atom.xml" title="Atom 0.3 feed for this blog">Atom</a> |
        <a href="viewFeeds.action" title="See the XML feeds available for this blog">All</a>
      </div>
    </td>
  </tr>

  <tr class="itemBody" align="center">
    <td>
      <div class="calendar"><pebble:calendar/></div>
      <div class="small">
        <a href="viewReferers.action" title="See the refers for today">Referers</a>
      </div>
    </td>
  </tr>

  <tr class="itemBody" align="center">
    <td>
      <b>Recent Blog Entries</b>
      <br />
      <c:forEach var="recentBlogEntry" items="${blog.recentBlogEntries}" >
        <div class="small">
          <a href="<c:out value="${recentBlogEntry.permalink}"/>" title="Link to this entry"><c:out value="${recentBlogEntry.title}"/></a>
        </div>
      </c:forEach>
    </td>
  </tr>

  <c:if test="${blog.recentCommentsOnHomePage > 0}">
  <%--
    -- to show separate lists of the recent comments and TrackBacks,
    -- uncomment this fragment
    --
  <tr class="itemBody" align="center">
    <td>
      <b>Recent Comments</b>
      <br />
      <c:forEach var="comment" items="${blog.recentComments}" varStatus="status" >
        <div class="small">
          <a href="<c:out value="${comment.permalink}"/>" title="Link to this comment"><c:out value="${comment.title}"/></a>
          <br />
          <c:out value="${comment.truncatedContent}" escapeXml="true" />
          <c:if test="${status.count < blog.recentCommentsOnHomePage}">
          <br /><br />
          </c:if>
        </div>
      </c:forEach>
    </td>
  </tr>

  <tr class="itemBody" align="center">
    <td>
      <b>Recent TrackBacks</b>
      <br />
      <c:forEach var="trackBack" items="${blog.recentTrackBacks}" varStatus="status" >
        <div class="small">
          <a href="<c:out value="${trackBack.permalink}"/>" title="Link to this TrackBack"><c:out value="${trackBack.title}"/></a>
          <br />
          <c:out value="${trackBack.truncatedContent}" escapeXml="true" />
          <c:if test="${status.count < blog.recentCommentsOnHomePage}">
          <br /><br />
          </c:if>
        </div>
      </c:forEach>
    </td>
  </tr>
  --%>

  <tr class="itemBody" align="center">
    <td>
      <b>Recent Responses</b>
      <br />
      <c:forEach var="aResponse" items="${blog.recentResponses}" varStatus="status" >
        <div class="small">
          <a href="<c:out value="${aResponse.permalink}"/>" title="Link to this response"><c:out value="${aResponse.title}"/></a>
          <br />
          <c:out value="${aResponse.truncatedContent}" escapeXml="true" />
          <c:if test="${status.count < blog.recentCommentsOnHomePage}">
          <br /><br />
          </c:if>
        </div>
      </c:forEach>
    </td>
  </tr>
  </c:if>

  <tr class="itemBody" align="center">
    <td>
      <b>Other Blogs</b>
      <br />
      <c:if test="${blogManager.multiUser}">
        <c:forEach var="blog" items="${blogManager.blog.publicBlogs}">
          <div class="small"><a href="<c:out value="${blog.url}"/>"><c:out value="${blog.name}"/></a></div>
        </c:forEach>
      </c:if>
      <div class="small"><a href="http://www.simongbrown.com/blog/">Simon Brown</a></div>
    </td>
  </tr>

</table>