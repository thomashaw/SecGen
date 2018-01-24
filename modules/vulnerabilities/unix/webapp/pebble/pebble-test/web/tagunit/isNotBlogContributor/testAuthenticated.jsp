<%@ page import="pebble.Constants,
                 pebble.AuthenticatedUser"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<%
  AuthenticatedUser user = new AuthenticatedUser();
  user.setBlogContributor(true);
  request.getSession().setAttribute(Constants.AUTHENTICATED_USER, user);
%>
<tagunit:assertEquals name="Body content not rendered when authenticated as blog contributor">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isNotBlogContributor>Some hidden content</pebble:isNotBlogContributor>
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
<tagunit:assertEquals name="Body content rendered when only authenticated as blog owner">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isNotBlogContributor>Some hidden content</pebble:isNotBlogContributor>
  </tagunit:actualResult>
</tagunit:assertEquals>
<%
  session.removeAttribute(Constants.AUTHENTICATED_USER);
%>