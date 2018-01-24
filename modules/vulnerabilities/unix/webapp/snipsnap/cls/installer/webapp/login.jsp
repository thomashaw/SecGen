<%--
  ** Server Bootstrap Installer login page
  ** @author Matthias L. Jugel
  ** @version $Id: login.jsp,v 1.10 2004/05/17 10:56:17 leo Exp $
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<table>
<tr><td>
  <fmt:message key="install.server.password"/> <input type="password" name="password">
</td></tr>
</table>
