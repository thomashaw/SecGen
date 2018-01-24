<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>

<div id="section">

<b>Editing: New Post</b>
<p>
<!-- taken out attribute type="net.eyde.personalblog.struts.form.NewPostForm" , it's now defined at struts-config-->
<html:form name="newPostForm" type="net.eyde.personalblog.struts.form.NewPostForm" action="postsave.do?method=executeFinish" method="post">
<b>Subject:</b><html:text property="title" size="70" /><p>
  <% String editorPage = (String)request.getAttribute("editorPage"); %>
    <jsp:include  page="<%= editorPage %>" flush="true"/>   
  <b>Category:</b>

<% ArrayList categories = (ArrayList)getServletContext().getAttribute("cats");
  Iterator i = categories.iterator();
  while (i.hasNext()){
      Category cat = (Category)i.next();
%>
<input type="checkbox" name="selectedCategories" value="<%=cat.getValue()%>"><%=cat.getName()%>
<% } %>
<BR> 
Email comments: <html:checkbox property="emailComments"/>
<html:submit onclick="javascript:postWeblogEntry(true)"  />
</html:form>
</div>
   
