<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<tagunit:assertEquals name="Build dropdown" ignoreWhitespace="false">

  <tagunit:expectedResult><select name="category"><option value="all">All Categories</option></select></tagunit:expectedResult>
  <tagunit:actualResult><pebble:categoryFilter/></tagunit:actualResult>

</tagunit:assertEquals>