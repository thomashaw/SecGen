<%@ page language="java" %>
 
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>

<% String id = (String)request.getParameter("id"); %>

<div id="section">

<center>
<H3>Deleting Post</h3><p>
<a href="deletePost.do?method=executeFinish&postId=<%=id%>">Confirm Delete Post</a> | <a href="javascript:window.history.back()">Back</a>
</center>
 
</div>