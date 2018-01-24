<%@ include file="../header.jspf" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  user.setPebbleAdmin(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when authenticated as Pebble admin">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isPebbleAdmin>Some hidden content</pebble:isPebbleAdmin>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>

<%
  user = new AuthenticatedUser();
  user.setBlogOwner(true);
  user.setBlogContributor(true);
  session.setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content not rendered when only authenticated as blog contributor and blog owner">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isPebbleAdmin>Some hidden content</pebble:isPebbleAdmin>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>