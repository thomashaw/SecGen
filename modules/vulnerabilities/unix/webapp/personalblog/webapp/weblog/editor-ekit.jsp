<%@ page language="java" import="net.eyde.personalblog.beans.Post"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%>
<script type="text/javascript">
<!--
function postWeblogEntry(publish)
{
 if (document.editPostForm) {  
    document.editPostForm.content.value = document.Ekit.getDocumentText();
    }  else {
    document.newPostForm.content.value = document.Ekit.getDocumentText();
 
}
}
-->
</script>

<html:hidden property="content" />

<%-- Use the Ekit applet --%>


<APPLET CODE="com.hexidec.ekit.EkitApplet.class" 
    ARCHIVE="ekitapplet.jar" NAME="Ekit" WIDTH="90%" HEIGHT="350">
<PARAM NAME="codebase" VALUE="weblog">
<PARAM NAME="code" VALUE="com.hexidec.ekit.EkitApplet.class">
<PARAM NAME="archive" VALUE="ekitapplet.jar">
<PARAM NAME="type" VALUE="application/x-java-applet;version=1.3">
<PARAM NAME="scriptable" VALUE="true">

<!-- Load text into Ekit applet by using the form bean -->
<PARAM NAME="DOCUMENT" 
    VALUE="<HTML><HEAD></HEAD><BODY><bean:write 
        name="editpost" property="content" /></BODY></HTML>">

<PARAM NAME="STYLESHEET" VALUE="ekit.css">
<PARAM NAME="LANGCODE" VALUE="en">
<PARAM NAME="LANGCOUNTRY" VALUE="US">
<PARAM NAME="TOOLBAR" VALUE="true">
<PARAM NAME="SOURCEVIEW" VALUE="false">
<PARAM NAME="EXCLUSIVE" VALUE="true">
<PARAM NAME="MENUICONS" VALUE="true">
</APPLET>
<br />


