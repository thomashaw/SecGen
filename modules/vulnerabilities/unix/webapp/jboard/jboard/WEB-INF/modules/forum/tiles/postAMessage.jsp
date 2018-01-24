<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html:xhtml/>
<html:messages id="error">
  <fmt:message key="errors_header" />
      <li><c:out value="${error}"/></li>
  <fmt:message key="errors_footer" />
</html:messages>

<html:form action="/ForumMessage?method=create" scope="request" method="POST" >
<fieldset>
<legend><fmt:message key="label_forum_add_a_message" /></legend>

<div class="formBlock">
<label>
  <span class="leftFormField"><fmt:message key="label_forum_reply" /></span>
  <span class="rightFormField"><html:textarea property="message" cols="37" rows="10" value="" /></span>
</label>
</div>

<html:hidden property="parentId" value="${forumForm.map.id}" />
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
</html:form>
