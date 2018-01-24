<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>
    Not enough free space
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      You do not have enough free space - please free some space by removing unused files or asking your system administrator to increase your quota from <fmt:formatNumber value="${pebbleProperties.fileUploadQuota}" type="number" />&nbsp;KB.
    </td>
  </tr>

</table>