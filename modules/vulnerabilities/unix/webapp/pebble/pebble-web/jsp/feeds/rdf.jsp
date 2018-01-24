<%= "<?xml version=\"1.0\" encoding=\"" + ((pebble.blog.Blog)request.getAttribute(pebble.Constants.BLOG_KEY)).getCharacterEncoding() + "\"?>" %>

<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://purl.org/rss/1.0/">

  <channel rdf:about="<c:out value="${blog.url}"/>">
    <title><c:out value="${blog.name}"/><c:if test="${not empty category}"> (<c:out value="${category.name}"/> category)</c:if></title>
    <link><c:out value="${blog.url}"/></link>
    <description><c:out value="${blog.description}"/></description>
  </channel>

  <c:forEach var="blogEntry" items="${blogEntries}">
  <item rdf:about="<c:out value="${blogEntry.permalink}"/>">
    <title><c:out value="${blogEntry.title}"/></title>
    <link><c:out value="${blogEntry.permalink}"/></link>
    <c:choose>
      <c:when test="${empty blogEntry.excerpt}">
        <description>
          <c:out value="${blogEntry.body}" escapeXml="true"/>
        </description>
      </c:when>
      <c:otherwise>
        <description>
          <c:out value="${blogEntry.excerpt}" escapeXml="true" />
          &lt;p&gt;Read the full content &lt;a href="<c:out value="${blogEntry.permalink}"/>"&gt;here&lt;/a&gt;.&lt;/p&gt;
        </description>
      </c:otherwise>
    </c:choose>
  </item>
  </c:forEach>

</rdf:RDF>