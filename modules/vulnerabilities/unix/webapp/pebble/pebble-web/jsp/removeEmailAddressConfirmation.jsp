<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellpadding="8" cellspacing="0">
  <tr class="header">
    <td>
      E-mail address removed
    </td>
    <td align="right">
      <input type="button" name="close" Value="Close" onClick="window.close()" />
    </td>
  </tr>

  <tr class="bodyContent">
    <td colspan="2">
      Thank you, your e-mail address (<c:out value="${param.email}" />) has been removed and you will not receive
      notifications when new comments are added to this blog entry.
    </td>
  </tr>

  <tr class="footer"><td colspan="2">&nbsp;</td></tr>

</table>

<%@ include file="/common/jsp/footer.jspf" %>