<%@ page language="java" import="java.util.*"%>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>

  
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>PersonalBlog Setup Wizard (step 3)</title>
<link rel="stylesheet" href="style.css" type="text/css" media="all" />
<style type="text/css" media="all">@import url("style.css");</style>
 
<div id="section">


<p><strong>PersonalBlog Setup Wizard (step 3)</strong></p>

<bean:message key="install.wizard.tablesnotcreated"/>:<br>

<% Vector v=(Vector) application.getAttribute("tables_not_created");
Enumeration enum=v.elements();
		 		while (enum.hasMoreElements()) {
                    String tabela = (String) enum.nextElement();
%> 

<%=tabela%><br>
 <%                    
                    
                }
%>

<a href="initialize.do?method=executeCreateTables"><h3><bean:message key="install.wizard.tablesnotcreated.submit"/></h3></a>


<p>&nbsp; </p></div>