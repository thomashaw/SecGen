<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>
    File too large
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      The size of the file you are uploading exceeds the allowed size of <fmt:formatNumber value="${pebbleProperties.fileUploadSize}" type="number" />&nbsp;KB.
    </td>
  </tr>

</table>