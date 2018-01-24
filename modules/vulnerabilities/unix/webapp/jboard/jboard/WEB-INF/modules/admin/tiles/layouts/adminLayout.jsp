<!--
jboard is a java bulletin board.
version $Name$
http://sourceforge.net/projects/jboard/
Copyright (C) 2003 Charles GAY
This file is part of jboard.
jboard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

jboard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jboard; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 -->
<%@taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts/html-el" prefix="html" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
    "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="fr" >
<head>
<title><tiles:getAsString name="title" /></title>
<html:base/></base>
<link rel="stylesheet" type="text/css" href="<html:rewrite href='/jboard/ressources/styles/global.css'/>" />
<link rel="stylesheet" type="text/css" href="<html:rewrite href='/jboard/ressources/styles/admin.css'/>" />
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

<tiles:insert attribute="menu" ignore="true" />
</div>


<div>
<tiles:insert attribute="footer" />
</div>
</div>
</body>
</html>
