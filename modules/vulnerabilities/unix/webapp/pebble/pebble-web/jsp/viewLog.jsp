<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td>
      Log for <c:out value="${logPeriod}"/>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <pre><c:out value="${log}"/></pre>
    </td>
  </tr>
  
</table>

<br />