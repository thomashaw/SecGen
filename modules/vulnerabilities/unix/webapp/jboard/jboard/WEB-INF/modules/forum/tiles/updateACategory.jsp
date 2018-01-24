<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html:xhtml/>
<html:messages id="error">
  <fmt:message key="errors_header" />
      <li><c:out value="${error}"/></li>
  <fmt:message key="errors_footer" />
</html:messages>


	<html:form action="/ForumCategory?method=update&catId=${param.catId}" scope="request" method="POST" >
	<fieldset>
	<legend><fmt:message key="label_forum_update_a_category" /></legend>
		<div  class="formBlock">
		<label>
		<span class="leftFormField">
			<fmt:message key="label_forum_title" />
		</span>
		<span class="rightFormField">
			<html:text property="title" size="50"  />
		</span>
		</label>
		</div>
		
		<div  class="formBlock">
		     <label>
			<span class="leftFormField"><fmt:message key="label_forum_description" /></span>
			<span class="rightFormField"><html:textarea property="description" cols="37" rows="10"  /></span>
			</label>
		</div>

	<div class="formBlock">
		<span class="rightFormField" >
			<html:submit>
			<fmt:message key="label_reg_submit" />
			</html:submit>
			<html:reset>
			<fmt:message key="label_reg_reset" />
			</html:reset>
		</span>
    </div>
	</fieldset>
<html:hidden property="parentId" value="${forumForm.map.id}" />
	
	</html:form>
