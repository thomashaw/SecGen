<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      Edit file (<c:out value="${file.absolutePath}" />)
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <a href="viewFiles.secureaction?path=<c:out value="${file.path}&type=${type}"/>">Back to <c:out value="${file.path}"/></a>
    </td>
  </tr>

  <form name="editFile" action="saveFile.secureaction" method="POST">
    <tr class="itemBody">
      <td>
        <input type="hidden" name="name" value="<c:out value="${file.name}" />" />
        <input type="hidden" name="path" value="<c:out value="${file.path}" />" />
        <input type="hidden" name="type" value="<c:out value="${type}" />" />
        <textarea name="content" cols="80" rows="40"><c:out value="${content}" escapeXml="true"/></textarea>
      </td>
    </tr>

    <tr class="itemBody">
      <td align="right">
        <input name="submit" type="submit" Value="Save File">
      </td>
    </tr>
  </form>

</table>

<br />