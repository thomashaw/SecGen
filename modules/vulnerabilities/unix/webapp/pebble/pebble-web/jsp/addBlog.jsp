<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Add a new blog
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="aBlog" items="${blogs}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="itemBodyEven">
            </c:when>
            <c:otherwise>
                <tr class="itemBodyOdd">
            </c:otherwise>
          </c:choose>
          <td><a href="<c:out value="${aBlog.url}"/>" target="_blank"><%= request.getContextPath() %>/<c:out value="${aBlog.id}"/></a></td>
          <td align="right"></td>
        </tr>
        </c:forEach>
      </table>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      To add a new blog, enter the string that will be used to identify this blog underneath this web application.
    </td>
  </tr>

  <form name="addBlog" action="addBlog.secureaction" method="POST">

  <tr class="bodyContent">
    <td>
      <b><%= request.getContextPath() %>/</b>
      <input name="id" type="text" value="" />
      <input type="submit" value="Add Blog" />
    </td>
  </tr>

  </form>

</table>