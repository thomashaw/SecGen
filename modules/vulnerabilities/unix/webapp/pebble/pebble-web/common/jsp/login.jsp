<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<table width="100%" cellspacing="0" cellpadding="8" class="item">

  <tr class="itemHeader">
    <td align="center">
    </td>
  </tr>

  <tr class="itemBody">
    <td align="center">
      <c:if test="${param.error eq true}" >
        An incorrect username/password was entered - please try again or click <a href="index.jsp">here</a> to go back to the blog.
        <br /><br />
      </c:if>

      <form name="loginForm" method="post" action="j_security_check">
      <input type="hidden" name="j_uri" value="<c:out value="${blog.url}"/>" />
      <table>
        <tr>
          <td align="left"><b>Username</b></td>
          <td align="left"><input type="text" name="j_username" /></td>
        </tr>
        <tr>
          <td align="left"><b>Password</b></td>
          <td align="left"><input type="password" name="j_password" /></td>
        </tr>
        <tr>
          <td colspan="2" align="right">
            <input type="submit" value="Login" />
          </td>
        </tr>
      </table>
      </form>
    </td>
  </tr>

</table>

<script type="text/javascript">
  document.loginForm.j_username.focus()
</script>