<%
  if (pebble.blog.BlogManager.getInstance().isMultiUser()) {
%>
  <jsp:forward page="/common/jsp/template.jsp?content=/jsp/multiUserIndex.jsp" />
<%
  } else {
%>
  <jsp:forward page="/viewHomePage.action"/>
<%
  }
%>