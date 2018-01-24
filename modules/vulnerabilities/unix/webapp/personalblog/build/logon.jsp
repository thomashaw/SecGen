<%@ page language="java" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
 
<div id="section">

<center>
<b><font color="red"><%--=errorMsg--%></font></b><p>
<b>      <bean:message key="logon.welcome"/>:</b>
<p>
<html:form action="logon.do?method=executeFinish" method="post">

<table>
  <tr>
     <td><bean:message key="logon.username"/>: </td>
     <td><html:text property="userName" size="30"/></td>
  </tr>
  <tr>
     <td><bean:message key="logon.password"/>:</td>
     <td><html:password property="password" size="30"/></td>
  </tr>
  <tr>
     <td><html:submit value="Login"/></td>
  </tr>
</table>
</html:form>
</center>

</div>