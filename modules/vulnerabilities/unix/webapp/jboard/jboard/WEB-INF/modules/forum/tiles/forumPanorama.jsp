<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<%@page import="java.util.HashMap" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html:xhtml/>
<%-- declaration of an HashMap called editparams --%>
<jsp:useBean id="editParams" scope="page" class="java.util.HashMap"/>

<html:messages id="error">
  <fmt:message key="errors_header" />
      <li><c:out value="${error}"/></li>
  <fmt:message key="errors_footer" />
</html:messages>

<div class="first">
     <span>forum&nbsp;</span>
     
     <span><html:link forward="postACategory"  ><fmt:message key="label_forum_add_a_category" /></html:link></span>
     
     <%-- we catch in the request context a collection named forumCategories in the forumRoot object --%>
     <%-- which is composed of category objects --%> 
         <c:if test="${empty forumForm.map.forumRoot.forumCategories}">
         <br />
         <fmt:message key="label_forum_there_is_no_categories" />      
         	 <br />
			 <br />
			 <br />
			 <br />
         </c:if>
	<c:forEach  items="${forumForm.map.forumRoot.forumCategories}" var="category" >

	   	<%-- Categories Output --%>
		   <div class="second">
			   <% editParams.put("method","read");  %>
			   <div class="title">
				 <span>++</span>
				 <label>title:&nbsp;</label><span><c:out value="${category.title}" /></span>
				 <c:set var="parentId" value="${category.id}" scope="page"  />
				   <jsp:useBean id="parentId" type="java.lang.Long" />
				   <%-- add a param and his value to the HashMap  --%>
				   <% editParams.put("parentId", parentId);  %>
			
				   <%-- update link --%>
				   <% editParams.put("method","beforeUpdate");  %>
			
				   <span><html:link forward="forumCategory" name="editParams"   ><fmt:message key="label_forum_update" /></html:link></span>
				   <%-- end update link --%>
			
				   <%-- delete link  --%>  
				   <% editParams.put("method","delete");  %>
			
				   <span><html:link forward="forumCategory" name="editParams" ><fmt:message key="label_forum_delete" /></html:link></span>
				   <% editParams.remove("method");  %>
				   <%-- end delete link --%>
				   <span><html:link forward="postAForumItem" name="editParams"  ><fmt:message key="label_forum_add_a_forum_item" /></html:link></span>
				   
		       
					<span><fmt:message key="label_forum_description" />:&nbsp;<c:out value="${category.description}" /></span>
			  </div>
	          
		  	  			<%-- forumItems Output --%>
							 <% editParams.put("method","read");  %>
							   
							   <c:forEach items="${category.forumItems}" var="ForumItem" >
									<% editParams.put("method","read");  %>
								   <%-- put in a page variable(fitId) the object field id --%>
								   <c:set var="id" value="${ForumItem.id}" scope="page"  />
								   <jsp:useBean id="id" type="java.lang.Long" />
								   <% editParams.put("id", id);  %>
								   <div class="third">
										<span>X<html:link forward="forumItem" name="editParams"  ><c:out value="${ForumItem.title}" /></html:link></span>
										<%-- update link --%>
										<% editParams.put("method","beforeUpdate");  %>
										<span><html:link forward="forumItem" name="editParams"   ><fmt:message key="label_forum_update" /></html:link></span>
										<%-- end update link --%>
										<%-- delete link  --%>  
										<% editParams.put("method","delete");  %>
										<span><html:link forward="forumItem" name="editParams" ><fmt:message key="label_forum_delete" /></html:link></span>
										<% editParams.remove("method");  %>
										<%-- end delete link --%>
								   </div>
							  </c:forEach>
							 <% editParams.remove("id");  %>
								 <c:if test="${empty category.forumItems}">
									   <div class="empty">
										  <span><fmt:message key="label_forum_there_is_no_forum_items" /></span>
									   </div>
								 </c:if>
							<% editParams.remove("id");  %>
	         
		  </div>
	</c:forEach>
</div>