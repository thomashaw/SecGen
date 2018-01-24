<%@ include file="../header.jspf" %>

<%
  blog.setProperty(SimpleBlog.THEME_KEY, "default");
%>
<tagunit:assertEquals name="Get theme - default">

  <tagunit:expectedResult><%= request.getContextPath() %>/themes/default</tagunit:expectedResult>
  <tagunit:actualResult><pebble:theme/></tagunit:actualResult>

</tagunit:assertEquals>

<%
  blog.setProperty(SimpleBlog.THEME_KEY, "anotherTheme");
%>
<tagunit:assertEquals name="Get theme - customized">

  <tagunit:expectedResult><%= request.getContextPath() %>/themes/anotherTheme</tagunit:expectedResult>
  <tagunit:actualResult><pebble:theme/></tagunit:actualResult>

</tagunit:assertEquals>