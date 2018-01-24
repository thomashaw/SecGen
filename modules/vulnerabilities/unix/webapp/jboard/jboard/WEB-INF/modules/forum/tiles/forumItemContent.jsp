	<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<%@page import="java.util.HashMap" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html:xhtml/>
<div class="first">
	<jsp:useBean id="editParams" scope="page" class="java.util.HashMap"/>
	
	<div class="first">
		  <span><c:out value="${forumForm.map.forumItem.title}"  /></span>
    </div>
    <div class="first">
		<span><fmt:message key="label_forum_description" />:&nbsp;<c:out value="${forumForm.map.forumItem.description}" /></span>
		
		 <c:set var="parentId" value="${forumForm.map.forumItem.id}" scope="page"  />
		 <jsp:useBean id="parentId" class="java.lang.Long" />
		 <% editParams.put("parentId", parentId);  %>
		<span><html:link forward="postAForumThread" name="editParams"  ><fmt:message key="label_forum_add_a_forum_thread" /></html:link></span>

	</div>
	<div class="first">

		     <c:if test="${empty forumForm.map.forumItem.forumThreads}">
		       <div class="empty">	
		         <fmt:message key="label_forum_there_is_no_forum_threads" />    
	           </div>      
		     </c:if>
   
		<% editParams.put("method","read");  %>
		
		<c:forEach  items="${forumForm.map.forumItem.forumThreads}" var="forumThread" >
			<div class="second">
		      <% editParams.put("method","read");  %>
			 <c:set var="id" value="${forumThread.id}" scope="page"  /> 
			 <jsp:useBean id="id" type="java.lang.Long" />
			 <% editParams.put("id",id);  %>

				<div class="third">
				<span>X</span><span> <html:link forward="forumThread" name="editParams"  ><c:out value="${forumThread.title}" /> </html:link></span>
				<!-- update link -->
		   		<% editParams.put("method","beforeUpdate");  %>
		  		<span><html:link forward="forumThread" name="editParams"   ><fmt:message key="label_forum_update" /></html:link></span>
		  		<!-- end update link -->
		  		<!-- update link -->
		   		<% editParams.put("method","delete");  %>
		  		<span><html:link forward="forumThread" name="editParams"   ><fmt:message key="label_forum_delete" /></html:link></span>
		  		<!-- end update link -->
		  		</div>
		  	</div>
		</c:forEach>
	</div>
</div>