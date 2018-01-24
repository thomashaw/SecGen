<%@ page import="pebble.AuthenticatedUser,
                 pebble.Constants"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<tagunit:assertEquals name="Body content not rendered when user not logged in">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isPebbleAdmin>Some hidden content</pebble:isPebbleAdmin>
  </tagunit:actualResult>
</tagunit:assertEquals>