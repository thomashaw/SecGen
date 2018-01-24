<%= "<?xml version=\"1.0\" encoding=\"" + ((pebble.blog.Blog)request.getAttribute(pebble.Constants.BLOG_KEY)).getCharacterEncoding() + "\"?>" %>

<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<feed version="0.3" xmlns="http://purl.org/atom/ns#">
  <title><c:out value="${blog.name}"/><c:if test="${not empty category}"> (<c:out value="${category.name}"/> category)</c:if></title>
  <link rel="alternate" type="text/html"
   href="<c:out value="${blog.url}"/>"/>
  <modified><fmt:formatDate value="${blog.lastModified}" pattern="yyyy-MM-dd'T'HH:mm:ss'Z'" timeZone="UTC"/></modified>
  <author>
    <name><c:out value="${blog.author}"/></name>
  </author>
  <tagline><c:out value="${blog.description}"/></tagline>
  <copyright>Copyright <c:out value="${blog.author}"/></copyright>
  <generator>Pebble</generator>

  <c:forEach var="blogEntry" items="${blogEntries}">
  <entry>
    <title><c:out value="${blogEntry.title}"/></title>
    <link rel="alternate" type="text/html" href="<c:out value="${blogEntry.permalink}"/>"/>
    <author>
      <name><c:out value="${blogEntry.rootBlog.author}"/></name>
    </author>
    <id>tag:<c:out value="${blogEntry.rootBlog.domainName}"/>,<fmt:formatDate value="${blogEntry.date}" pattern="yyyy-MM-dd" timeZone="UTC"/>:<c:out value="${blogEntry.id}"/></id>
    <modified><fmt:formatDate value="${blogEntry.date}" pattern="yyyy-MM-dd'T'HH:mm:ss'Z'" timeZone="UTC"/></modified>
    <issued><fmt:formatDate value="${blogEntry.date}" pattern="yyyy-MM-dd'T'HH:mm:ss'Z'" timeZone="UTC"/></issued>
    <c:choose>
      <c:when test="${empty blogEntry.excerpt}">
        <content type="text/html" mode="escaped">
          <c:out value="${blogEntry.body}" escapeXml="true"/>
        </content>
      </c:when>
      <c:otherwise>
        <content type="text/html" mode="escaped">
          <c:out value="${blogEntry.excerpt}" escapeXml="true"/>
          &lt;p&gt;Read the full content &lt;a href="<c:out value="${blogEntry.permalink}"/>"&gt;here&lt;/a&gt;.&lt;/p&gt;
        </content>
      </c:otherwise>
    </c:choose>
  </entry>
  </c:forEach>
</feed>
