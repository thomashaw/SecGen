

<%@ page language="java" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>



<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>PersonalBlog Setup Wizard (step 3)</title>
<link rel="stylesheet" href="style.css" type="text/css" media="all" />
<style type="text/css" media="all">@import url("style.css");</style>
</head>
<body>

 
<div id="section">


<p><strong>PersonalBlog Setup Wizard (step 2)</strong></p>
One or more of configurations properties are missing, please update the fields below :
<a href="initialize.do?method=executeTestDatabase"><h3>retry</h3></a>

<form name="form1" method="post" action="initialize.do?method=executeCreateHibernateFile">
  <p>&nbsp;</p>
  <table width="66%" border="0">
 
    <tr> 
      <td width="32%"><strong>Database Driver</strong></td>
      <td width="68%"><input name="dbdriver" type="text" id="dbdriver" value="org.gjt.mm.mysql.Driver" size="50"></td>
    </tr>
    <tr> 
      <td><strong>Database URL</strong></td>
      <td><input name="dburl" type="text" id="dburl" value="jdbc:mysql://localhost:3306/personalblog" size="50"></td>
    </tr>
    <tr> 
      <td><strong>Database User Id</strong></td>
      <td><input name="dbuser" type="text" id="dbuser" value="user"></td>
    </tr>
    <tr> 
      <td><strong>Database Password</strong></td>
      <td><input name="dbpassword" type="text" id="dbpassword" value="password"></td>
    </tr>
    
  </table>
  <p>
    <input type="submit" name="Submit" value="Initialize">
  </p>
</form>
<p>&nbsp; </p></div>
</body>
</html>