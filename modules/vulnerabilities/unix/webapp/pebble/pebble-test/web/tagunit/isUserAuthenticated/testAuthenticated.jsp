<%@ include file="../header.jspf" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when user authenticated">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isUserAuthenticated>Some hidden content</pebble:isUserAuthenticated>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>