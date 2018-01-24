<%@ page language="java" import="net.eyde.personalblog.service.PersonalBlogService"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
 
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_TITLE) %></title>
<link rel="stylesheet" href="style.css" type="text/css" />
</head>
<body bgcolor="#FFFFFF">
<tiles:insert attribute="header"/>
<tiles:insert attribute="body"/>
<tiles:insert attribute="footer"/>
</body>
</html>
