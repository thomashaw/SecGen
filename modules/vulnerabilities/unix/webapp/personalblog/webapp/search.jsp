<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>

<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>

<div id="section">

<H3>Search Results:</H3>
<ul>
<% ArrayList posts = (ArrayList)request.getAttribute("posts");

  Iterator i = posts.iterator();
  while (i.hasNext()){
      Post p = (Post)i.next();

%>
<li> <a href="index.do?post=<%=p.getId()%>"><dt:DateTime format="EEEE, MMMM dd, yyyy" value="<%=p.getCreated()%>" /> - <%=p.getTitle()%></a>
<% } %>
</ul>

</div>
