<%@ page language="java" import="net.eyde.personalblog.service.PersonalBlogService"%>
 
<%@ taglib  uri="/tags/struts-bean" prefix="bean" %>


<div id="top">


<table><tr><td align="left">
<h1><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_TITLE) %></h1>
<h3><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_DESCRIPTION) %></h3>
<h6>Version : #version# - #builddate#</h6>
</td><td align="right"> 
<% String signedIn = (String)session.getValue("signedIn"); 
if("true".equals(signedIn)){ %><div align="right">
Welcome Administrator!!!!</div>
<% }%>
</td>
</tr>
</table>
</div>


