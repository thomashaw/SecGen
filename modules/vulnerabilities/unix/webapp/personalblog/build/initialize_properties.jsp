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
<bean:message key="install.wizard.properties.missing"/>:

<form name="form1" method="post" action="initialize.do?method=executeFinish">
  <p>&nbsp;</p>
  <table width="66%" border="0">
 
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.title"/></strong></td>
      <td><input name="title" type="text" id="title" value="your name here" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.description"/></strong></td>
      <td><input name="description" type="text" id="description" value="description" size="50"></td>
    </tr>    
    <tr> 

    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.owner.name"/></strong></td>
      <td><input name="owner" type="text" id="owner" value="your name here" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.owner.nickname"/></strong></td>
      <td><input name="nickname" type="text" id="nickname" value="your nick here" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.owner.picture"/></strong></td>
      <td><input name="picture" type="text" id="picture" value="your picture file here" size="50"></td>
    </tr>


    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.owner.email"/></strong></td>
      <td><input name="email" type="text" id="email" value="you@yoursite.com" size="50"></td>
    </tr>

        <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.email.host"/></strong></td>
      <td><input name="emailhost" type="text" id="emailhost" value="youremail.com" size="50"></td>
    </tr>
        <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.email.transport"/></strong></td>
      <td><input name="emailtransport" type="text" id="emailtransport" value="smtp" size="50"></td>
    </tr>
        <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.email.username"/></strong></td>
      <td><input name="emailusername" type="text" id="emailusername" value="emailuser" size="50"></td>
    </tr>

        <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.email.password"/></strong></td>
      <td><input name="emailpassword" type="text" id="emailpassword" value="emailpassword" size="50"></td>
    </tr>
    
        
    
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.url"/></strong></td>
      <td><input name="blogurl" type="text" id="blogurl" value="http://localhost:8080/" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.admin.username"/></strong></td>
      <td><input name="adminid" type="text" id="adminid" value="user"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.admin.password"/></strong></td>
      <td><input name="adminpassword" type="text" id="adminpassword" value="password"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.categories.titles"/></strong></td>
      <td><input name="categorytitles" type="text" id="categorytitles" value="Category1,Category2,Category3" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.categories.values"/></strong></td>
      <td><input name="categoryvalues" type="text" id="categoryvalues" value="A,B,C" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.categories.images"/></strong></td>
      <td><textarea name="categoryimages" cols="45" rows="2" id="categoryimages">images/image1.jpg,images/image2.jpg,images/image3.jpg</textarea></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.emoticons.values"/></strong></td>
      <td><input name="emoticonvalues" type="text" id="emoticonvalues" value="A,B,C,D,E,F" size="50"></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.emoticons.weblog.images"/></strong></td>
      <td><textarea name="emoticonimages" cols="45" rows="2" id="emoticonimages">images/thumbsup.gif,images/tongue.gif,images/frown.gif,images/knipoog.gif,images/nana.gif,images/ooh.gif</textarea></td>
    </tr>
    <tr> 
      <td><strong><bean:message key="install.wizard.properties.weblog.editor"/></strong></td>
      <td>   
      	<select name="posteditor">
      		<option value="editor-text.jsp"><bean:message key="install.wizard.properties.weblog.editor.text"/></option>
      		<option value="editor-ekit.jsp"><bean:message key="install.wizard.properties.weblog.editor.applet"/></option>
      		<option value="editor-IE.jsp"><bean:message key="install.wizard.properties.weblog.editor.js"/></option>      		
    	</select></td>
    </tr>
    
  </table>
  <p>
    <input type="submit" name="Submit" value="<bean:message key="install.wizard.properties.weblog.submit"/>">
  </p>
</form>
<p>&nbsp; </p></div>
</body>
</html>