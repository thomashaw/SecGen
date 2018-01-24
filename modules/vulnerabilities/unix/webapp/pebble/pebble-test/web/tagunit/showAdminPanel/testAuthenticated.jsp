<%@ include file="../header.jspf" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content not rendered when not authenticated as blog owner, blog contributor or Pebble admin">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:showAdminPanel>Some hidden content</pebble:showAdminPanel>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>

<%
  user = new AuthenticatedUser();
  user.setBlogOwner(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when authenticated as blog owner">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:showAdminPanel>Some hidden content</pebble:showAdminPanel>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>

<%
  user = new AuthenticatedUser();
  user.setBlogContributor(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when authenticated as blog contributor">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:showAdminPanel>Some hidden content</pebble:showAdminPanel>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>

<%
  user = new AuthenticatedUser();
  user.setPebbleAdmin(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when authenticated as Pebble admin">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:showAdminPanel>Some hidden content</pebble:showAdminPanel>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>