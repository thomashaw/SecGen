<%@ include file="../header.jspf" %>

<tagunit:assertEquals name="Body content not rendered when user unauthenticated">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isUserAuthenticated>Some hidden content</pebble:isUserAuthenticated>
  </tagunit:actualResult>
</tagunit:assertEquals>