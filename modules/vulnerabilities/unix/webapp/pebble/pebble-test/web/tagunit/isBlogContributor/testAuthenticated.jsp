<%@ page import="pebble.Constants,
                 pebble.AuthenticatedUser"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  user.setBlogContributor(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content rendered when authenticated as blog contributor">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isBlogContributor>Some hidden content</pebble:isBlogContributor>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>

<%
  user = new AuthenticatedUser();
  user.setBlogOwner(true);
  session.setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content not rendered when only authenticated as blog owner">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isBlogContributor>Some hidden content</pebble:isBlogContributor>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>