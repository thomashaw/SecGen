<%@ page import="pebble.blog.*,
                 java.util.Collection,
                 pebble.blog.BlogManager,
                 pebble.blog.CompositeBlog"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table cellspacing="0" cellpadding="8" class="item" width="100%">
  <tr class="itemHeader">
    <td align="left" valign="top">
      <c:out value="${blog.name}" />
    </td>
    <td align="right" valign="top">
      <form method="get" action="search.action">
        <input type="text" name="query" />&nbsp;<input type="submit" value="Search" />
      </form>
    </td>
  </tr>
</table>

<table cellspacing="0" cellpadding="8" width="100%">

<c:forEach var="simpleBlog" items="${blog.publicBlogs}" varStatus="status">
  <%
    // get the number of days worth of entries defined by the overall
    // composite blog, rather than using each simple blogs own value
    Blog blog = (Blog)request.getAttribute("blog");
    SimpleBlog simpleBlog = (SimpleBlog)pageContext.getAttribute("simpleBlog");
    Collection recentBlogEntries = simpleBlog.getRecentBlogEntries(blog.getRecentBlogEntriesOnHomePage());
    pageContext.setAttribute("recentBlogEntries", recentBlogEntries);
  %>
  <c:choose>
    <c:when test="${status.count % 2 == 0}">
      <tr class="itemBodyEven">
    </c:when>
    <c:otherwise>
        <tr class="itemBodyOdd">
    </c:otherwise>
  </c:choose>
    <td valign="top" width="25%" align="left">
      <a href="<c:out value="${simpleBlog.url}"/>" title="Go to this blog"><c:out value="${simpleBlog.name}"/></a>
      <div class="small">
        <c:out value="${simpleBlog.description}"/>
        <br />
        <br />
        <a href="<c:out value="${simpleBlog.url}viewFeeds.action"/>" title="See the XML feeds available for this blog"><img src="./common/images/xmlfeeds.gif" alt="XML Feeds" border="0" valign="middle" /></a> |
        <a href="<c:out value="${simpleBlog.url}viewReferers.action"/>" title="See referers for today"><c:out value="${simpleBlog.logger.log.totalLogEntries}"/> requests today</a>
      </div>
    </td>
    <td valign="top" width="75%" align="left">
      <table cellpadding="4" cellspacing="0" width="100%">
        <c:forEach var="recentBlogEntry" items="${recentBlogEntries}" >
        <tr>
          <td class="small" valign="top" width="66%">
            <a href="<c:out value="${recentBlogEntry.permalink}"/>" title="Go to this blog entry"><c:out value="${recentBlogEntry.title}"/></a>
          </td>
          <td class="small" align="right" valign="top">
            <c:choose><c:when test="${recentBlogEntry.trackBacksEnabled}"><a href="javascript:openWindow('<c:out value="${recentBlogEntry.trackBacksLink}"/>', 'viewTrackBacks<%= session.getId()%>', 640, 480);" title="See TrackBacks for this entry"><c:out value="${recentBlogEntry.numberOfTrackBacks}"/></a></c:when><c:otherwise>-</c:otherwise></c:choose>
          </td>
          <td class="small" align="right" valign="top">
            <c:choose><c:when test="${recentBlogEntry.commentsEnabled}"><a href="javascript:openWindow('<c:out value="${recentBlogEntry.commentsLink}"/>', 'viewComments<%= session.getId()%>', 640, 480);" title="See comments for this entry"><c:out value="${recentBlogEntry.numberOfComments}"/></a></c:when><c:otherwise>-</c:otherwise></c:choose>
          </td>
          <td class="small" valign="top">
            <fmt:formatDate value="${recentBlogEntry.date}" type="both" dateStyle="long" timeStyle="long" />
          </td>
          </tr>
        </c:forEach>
      </table>
    </td>
  </tr>
</c:forEach>

  <tr>
    <td align="center" colspan="2">
      <br />
      <a href="rss.xml" title="Get an RSS 2.0 feed"><img src="<%= request.getContextPath() %>/common/images/rss2.gif" alt="RSS 2.0" border="0" /></a>
      &nbsp;
      <a href="rssWithCommentsAndTrackBacks.xml" title="Get an RSS 2.0 feed, including comments and TrackBacks"><img src="<%= request.getContextPath() %>/common/images/rss2-plus.gif" alt="RSS 2.0" border="0" /></a>
      &nbsp;
      <a href="rdf.xml" title="Get an RDF 1.0 feed"><img src="<%= request.getContextPath() %>/common/images/rdf.gif" alt="RDF 1.0" border="0" /></a>
      &nbsp;
      <a href="atom.xml" title="Get an Atom 0.3 feed"><img src="<%= request.getContextPath() %>/common/images/atom03.gif" alt="Atom 0.3" border="0" /></a>
    </td>
  </tr>

</table>

