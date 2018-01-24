<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Categories
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="aCategory" items="${categories}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="bodyContentEven">
            </c:when>
            <c:otherwise>
                <tr class="bodyContentOdd">
            </c:otherwise>
          </c:choose>
          <td><c:out value="${aCategory.name}"/></td>
          <td align="right">
            <a href="editCategory.secureaction?id=<c:out value="${aCategory.id}"/>" title="Edit this category">Edit</a>
            |
            <a href="javascript:confirmRemove('removeCategory.secureaction?id=<c:out value="${aCategory.id}"/>');" title="Remove this category">Remove</a>
          </td>
        </tr>
        </c:forEach>
      </table>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      To add a new category, enter the ID (a string containing no special characters or spaces) and the category name.
    </td>
  </tr>

  <form name="category" action="saveCategory.secureaction" method="POST" accept-charset="<c:out value="${blog.characterEncoding}" />">
    <tr class="itemBody">
      <td>
        <table width="100%">
          <tr>
            <td><b>ID</b></td>
            <td>
              <input type="text" name="id" size="40" value="<c:out value="${theCategory.id}"/>" />
            </td>
          </tr>
          <tr>
            <td><b>Name</b></td>
            <td>
              <input type="text" name="name" size="40" value="<c:out value="${theCategory.name}"/>" />
            </td>
          </tr>
          <tr>
            <td colspan="2" align="right">
              <input name="submit" type="submit" Value="Save Category" />
            </td>
          </tr>
        </table>
      </td>
    </tr>

</form>
</table>

<br />