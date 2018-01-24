<%@ include file="../header.jspf" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  user.setBlogOwner(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when authenticated as blog owner">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isBlogOwner>Some hidden content</pebble:isBlogOwner>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>

<%
  user = new AuthenticatedUser();
  user.setBlogContributor(true);
  session.setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content not rendered when only authenticated as blog contributor">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isBlogOwner>Some hidden content</pebble:isBlogOwner>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>