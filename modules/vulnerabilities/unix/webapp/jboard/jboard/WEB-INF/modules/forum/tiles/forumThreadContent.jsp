<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<%@page import="java.util.HashMap" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html:xhtml/>
<div class="first">
	<html:messages id="error">
	  <fmt:message key="errors_header" />
		  <li><c:out value="${error}"/></li>
	  <fmt:message key="errors_footer" />
	</html:messages>
	<!-- declaration of an HashMap called editparams -->
	<jsp:useBean id="editParams" scope="page" class="java.util.HashMap"/>
	
	<span><fmt:message key="label_forum_title" />&nbsp;<c:out value="${forumForm.map.forumThread.title}"  /></span>
	
	<c:set  var="parentId" value="${forumForm.map.forumThread.id}"  scope="page" />
	<jsp:useBean id="parentId" type="java.lang.Long" />
	<c:forEach  items="${forumForm.map.forumThread.forumMessages}" var="message" >
		<c:set  var="id" value="${message.id}"  scope="page" />
		
		<jsp:useBean id="id" type="java.lang.Long" />
		
		 <% editParams.put("id", id);  %>
		 <% editParams.put("parentId", parentId);  %>
		<% editParams.put("method","delete");  %>
			<div class="third">
				 <span><c:out value="${message.message}" /></span>
				 <span><html:link forward="forumMessage"   name="editParams"  ><fmt:message key="label_forum_delete" /></html:link></span>
				 <% editParams.put("method","read");  %>
				 <html:link forward="forumMessage"   name="editParams"  ><fmt:message key="label_forum_update" /></html:link></span>
			</div>
	</c:forEach>
	<html:link forward="postAMessage" paramId="id"  paramName="parentId"  >
		<fmt:message key="label_forum_add_a_message" />
	</html:link>

</div>