<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td colspan="2">
      View themes
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <form name="chooseTheme" action="viewTheme.secureaction" method="POST">
        <pebble:select name="theme" items='<%= (java.util.Collection)request.getAttribute("themes") %>' selected='<%= (String)request.getAttribute("theme") %>' />
        <input name="submit" type="submit" Value="Choose Theme" />
      </form>
    </td>

    <td>
      <c:if test="${not empty theme}">
        <form name="chooseFile" action="viewTheme.secureaction" method="POST">
          <input type="hidden" name="theme" value="<c:out value="${theme}" />" />
          <pebble:select name="file" items='<%= (java.util.Collection)request.getAttribute("files") %>' selected='<%= (String)request.getAttribute("file") %>' />
          <input name="submit" type="submit" Value="Choose File">
        </form>
      </c:if>
    </td>

  </tr>

<c:if test="${not empty file}">
  <tr class="itemBody">
    <td colspan="2">
      <pre><c:out value="${content}" escapeXml="true"/></pre>
    </td>
  </tr>
</c:if>

</table>

<br />