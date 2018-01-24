<%@ page language="java" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>

  
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>PersonalBlog Setup Wizard (step 2)</title>
<link rel="stylesheet" href="style.css" type="text/css" media="all" />
<style type="text/css" media="all">@import url("style.css");</style>
</head>
<body>
<div id="section">


<p><strong>PersonalBlog Setup Wizard (step 2)</strong></p>

<bean:message key="install.wizard.dboff"/>
<a href="initialize.do?method=executeTestDatabase"><h3><bean:message key="install.wizard.retry"/></h3></a>


<p>&nbsp; </p></div>
</body>
</html>