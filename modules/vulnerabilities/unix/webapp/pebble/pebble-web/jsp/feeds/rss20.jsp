<%= "<?xml version=\"1.0\" encoding=\"" + ((pebble.blog.Blog)request.getAttribute(pebble.Constants.BLOG_KEY)).getCharacterEncoding() + "\"?>" %>

<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<rss version="2.0">
<channel>
  <title><c:out value="${blog.name}"/><c:if test="${not empty category}"> (<c:out value="${category.name}"/> category)</c:if></title>
  <link><c:out value="${blog.url}"/></link>
  <description><c:out value="${blog.description}"/></description>
  <language><c:out value="${blog.language}"/></language>
  <copyright>Copyright <c:out value="${blog.author}"/></copyright>
  <lastBuildDate><fmt:formatDate value="${blog.lastModified}" pattern="EEE, dd MMM yyyy HH:mm:ss z" timeZone="GMT"/></lastBuildDate>
  <generator>Pebble</generator>
  <docs>http://backend.userland.com/rss</docs>
  <image>
    <url><c:out value="${blog.image}"/></url>
    <title><c:out value="${blog.name}"/><c:if test="${not empty category}"> (<c:out value="${category.name}"/> category)</c:if></title>
    <link><c:out value="${blog.url}"/></link>
  </image>
  <c:forEach var="blogEntry" items="${blogEntries}">
  <item>
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
    <c:forEach var="category" items="${blogEntry.categories}">
    <category><c:out value="${category.name}"/></category>
    </c:forEach>
    <comments><c:out value="${blogEntry.commentsLink}"/></comments>
    <guid isPermaLink="true"><c:out value="${blogEntry.permalink}"/></guid>
    <pubDate><fmt:formatDate value="${blogEntry.date}" pattern="EEE, dd MMM yyyy HH:mm:ss z" timeZone="GMT"/></pubDate>
  </item>
  </c:forEach>
  </channel>
</rss>