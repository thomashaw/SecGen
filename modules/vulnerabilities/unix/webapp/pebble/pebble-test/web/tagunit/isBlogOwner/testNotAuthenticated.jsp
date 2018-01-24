<%@ page import="pebble.AuthenticatedUser,
                 pebble.Constants"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<tagunit:assertEquals name="Body content not rendered when user not logged in">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isBlogOwner>Some hidden content</pebble:isBlogOwner>
  </tagunit:actualResult>
</tagunit:assertEquals>