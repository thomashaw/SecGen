<%@taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts/html-el" prefix="html" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
    "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="fr" >
<head>
<title><tiles:getAsString name="title" /></title>
<html:base/>
<link rel="stylesheet" type="text/css" href="<html:rewrite href='/jboard/ressources/styles/global.css' />" />
<link rel="stylesheet" type="text/css" href="<html:rewrite href='/jboard/ressources/styles/forum.css' />" />
 <meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8"  />
</head>
<body>
	<div id="header" >
	  <tiles:insert attribute="header" />
	</div>
    <div id="content">
	   <tiles:insert attribute="body" />
	</div>
	<div id="footer">
	   <tiles:insert attribute="footer" />
    </div>
</body>
</html:html>
