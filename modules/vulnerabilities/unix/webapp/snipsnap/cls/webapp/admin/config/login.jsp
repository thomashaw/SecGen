 <%--
  ** login dialog
  ** @author Matthias L. Jugel
  ** @version $Id: login.jsp,v 1.3 2004/05/17 10:56:17 leo Exp $
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="i18n.setup" scope="page" />

<table>
  <tr>
    <td><fmt:message key="config.login.text"/></td>
    <td>
      <fmt:message key="config.login"/><br/>
      <input type="password" name="key" value=""/>
    </td>
  </tr>
</table>