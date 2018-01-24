<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader" align="center">
    <td>
      <a href="./"><b>Home</b></a>
    </td>
  </tr>

  <tr class="itemBody" align="center">
    <td>
      <table class="calendar"><tr><td><pebble:calendar/></td></tr></table>
      <br />
      <a href="viewFeeds.action" title="See the XML feeds available for this blog"><img border="0" src="<pebble:theme/>/images/xmlfeeds.gif" alt="XML Feeds" /></a>
      <br />
      <a href="viewReferers.action" title="See the referers for today"><img src="<pebble:theme/>/images/blog-referers.gif" border="0" alt="Blog Statistics" /></a>
      <br />
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
  </c:if>

  <c:if test="${blog.recentCommentsOnHomePage > 0}">
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
  </c:if>

  <tr class="itemBody" align="center">
    <td>
      <b>Links</b>
      <br />
      <div class="small"><a href="http://pebble.sourceforge.net">Pebble</a></div>
    </td>
  </tr>

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
      <br />
    </td>
  </tr>

</table>

<br />