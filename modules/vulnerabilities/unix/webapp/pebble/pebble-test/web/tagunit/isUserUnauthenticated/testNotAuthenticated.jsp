<%@ page import="pebble.AuthenticatedUser,
                 pebble.Constants"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<tagunit:assertEquals name="Body content rendered when user not logged in">
  <tagunit:expectedResult>Some hidden content</tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isUserUnauthenticated>Some hidden content</pebble:isUserUnauthenticated>
  </tagunit:actualResult>
</tagunit:assertEquals>