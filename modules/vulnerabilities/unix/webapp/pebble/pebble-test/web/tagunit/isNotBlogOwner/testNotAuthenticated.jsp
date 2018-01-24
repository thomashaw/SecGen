<%@ include file="../header.jspf" %>

<tagunit:assertEquals name="Body content not rendered when user not logged in">
  <tagunit:expectedResult></tagunit:expectedResult>
  <tagunit:actualResult>
    <pebble:isNotBlogOwner>Some hidden content</pebble:isNotBlogOwner>
  </tagunit:actualResult>
</tagunit:assertEquals>