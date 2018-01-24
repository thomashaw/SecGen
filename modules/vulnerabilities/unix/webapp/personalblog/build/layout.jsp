<%@ page language="java" import="net.eyde.personalblog.service.PersonalBlogService"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
  
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html:html xhtml="true" locale="true">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_TITLE)%></title>
<link rel="stylesheet" href="style.css" type="text/css" media="all" />
<style type="text/css" media="all">@import url("style.css");</style>
<link rel="alternate" type="application/rss+xml" title="RSS" href="rss.jsp" />
<script language="Javascript">
<!--
	function openWindow(url) {
		window.open(url, "subject", "scrollbars=yes, width=550, height=450");
	}
//-->
</script>
 
</head>
<body>
<tiles:insert attribute="header"/>
<tiles:insert attribute="rail"/>
<div id="middle">
<html:errors/></b>
<tiles:insert attribute="body"/>
</div>	
<tiles:insert attribute="footer"/>
</body>
</html:html>