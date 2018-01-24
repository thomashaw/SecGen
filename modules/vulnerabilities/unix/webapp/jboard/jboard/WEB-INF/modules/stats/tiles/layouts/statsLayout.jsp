<%@taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts/html-el" prefix="html" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<head>
<title><tiles:getAsString name="title" /></title>
<html:base/>
<link rel="stylesheet" type="text/css" href="<html:rewrite href='/jboard/ressources/styles/common/global.css'/>" />
</head>
<body>


<h1>
<tiles:getAsString name="title" />
</h1>

<div>
<div>
<tiles:insert attribute="header" />
</div>
ceci est dans le module forum
<div>

<div>
<tiles:insert attribute="body" />
</div>


<div>
<tiles:insert attribute="footer" />
</div>


</body>
</html:html>
