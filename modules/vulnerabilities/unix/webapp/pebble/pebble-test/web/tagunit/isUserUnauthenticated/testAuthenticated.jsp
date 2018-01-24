<%@ page import="pebble.Constants,
                 pebble.AuthenticatedUser"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content not rendered when user authenticated">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isUserUnauthenticated>Some hidden content</pebble:isUserUnauthenticated>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>