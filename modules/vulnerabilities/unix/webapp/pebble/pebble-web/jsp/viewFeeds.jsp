<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>Feeds for <c:out value="${blog.name}"/></td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <tr>
          <td>
            Feed for all categories
          </td>
          <td align="right">
            <a href="<c:out value="rss.xml"/>" title="Get an RSS 2.0 feed for all categories"><img src="<pebble:theme/>/images/rss2.gif" alt="RSS 2.0" border="0" /></a>
            &nbsp;
            <a href="<c:out value="rssWithCommentsAndTrackBacks.xml"/>" title="Get an RSS 2.0 feed for all categories, including comments and TrackBacks"><img src="<pebble:theme/>/images/rss2-plus.gif" alt="RSS 2.0" border="0" /></a>
            <br />
            <a href="<c:out value="rdf.xml"/>" title="Get an RDF 1.0 feed for all categories"><img src="<pebble:theme/>/images/rdf.gif" alt="RDF 1.0" border="0" /></a>
            &nbsp;
            <a href="<c:out value="atom.xml"/>" title="Get an Atom 0.3 feed for all categories"><img src="<pebble:theme/>/images/atom03.gif" alt="Atom 0.3" border="0" /></a>
            <br />
          </td>
        </tr>

        <c:forEach var="category" items="${categories}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="itemBodyOdd">
            </c:when>
            <c:otherwise>
                <tr class="itemBodyEven">
            </c:otherwise>
          </c:choose>
            <td>
              Feed for <c:out value="${category.name}"/> category
            </td>
            <td align="right">
              <a href="<c:out value="rss.xml?category=${category.id}"/>" title="Get an RSS 2.0 feed for this category"><img src="<pebble:theme/>/images/rss2.gif" alt="RSS 2.0" border="0" /></a>
              &nbsp;
              <a href="<c:out value="rssWithCommentsAndTrackBacks.xml?category=${category.id}"/>" title="Get an RSS 2.0 feed for this category, including comments and TrackBacks"><img src="<pebble:theme/>/images/rss2-plus.gif" alt="RSS 2.0" border="0" /></a>
              <br />
              <a href="<c:out value="rdf.xml?category=${category.id}"/>" title="Get an RDF 1.0 feed for this category"><img src="<pebble:theme/>/images/rdf.gif" alt="RDF 1.0" border="0" /></a>
              &nbsp;
              <a href="<c:out value="atom.xml?category=${category.id}"/>" title="Get an Atom 0.3 feed for this category"><img src="<pebble:theme/>/images/atom03.gif" alt="Atom 0.3" border="0" /></a>
              <br />
            </td>
          </tr>
        </c:forEach>

      </table>
    </td>
  </tr>
</table>

<br />

<p>
<div align="center">
<a href="http://www.feedvalidator.org/check.cgi?url=<c:out value="${blog.url}rss.xml"/>"><img src="<%= request.getContextPath() %>/common/images/valid-rss.png" alt="[Valid RSS]" title="Validate my RSS feed" width="88" height="31" border="0" /></a>
&nbsp;
<a href="http://www.feedvalidator.org/check.cgi?url=<c:out value="${blog.url}atom.xml"/>"><img src="<%= request.getContextPath() %>/common/images/valid-atom.png" alt="[Valid Atom]" title="Validate my Atom feed" width="88" height="31" border="0" /></a>
</div>
</p>