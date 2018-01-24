<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<html:xhtml/>
<html:messages id="error">
  <fmt:message key="errors_header" />
      <li><c:out value="${error}"/></li>
  <fmt:message key="errors_footer" />
</html:messages>


	<html:form action="/ForumMessage?method=update" scope="request" method="POST" >
    <fieldset>	
    <legend><fmt:message key="label_forum_update_a_message" /></legend>			
		<div class="formBlock">
		<span class="leftFormField"><fmt:message key="label_forum_message" /></span>
		<span class="rightFormField"><html:textarea property="message" cols="37" rows="10"  /></span>
		</div>
    <html:hidden  property="id" value="${forumForm.map.id}"  />
    <html:hidden  property="parentId" value="${param.parentId}"  />
	<div class="formBlock">
			<span class="rightFormField" >
			    <html:submit><fmt:message key="label_reg_submit" /></html:submit>
				<html:reset><fmt:message key="label_reg_reset" /></html:reset>
			</span>
	     </div>
	</fieldset>
	</html:form>
