<%@ page language="java" import="net.eyde.personalblog.beans.Post"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>

<!--
function postWeblogEntry(publish)
{
 if (document.editPostForm) {  
    document.editPostForm.submit();
    }  else {
    document.newPostForm.submit(); 
}
}
-->
<%Post editpost=(Post)request.getAttribute("editpost");out.println(editpost);%>
<html:textarea property="content" value="<%=editpost.getContent()%>" rows="22" cols="60" /><p>

