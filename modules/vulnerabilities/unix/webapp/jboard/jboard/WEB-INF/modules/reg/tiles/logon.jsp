<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<html:xhtml/>
<html:messages id="error">
  <fmt:message key="errors_header" />
      <li><c:out value="${error}"/></li>
  <fmt:message key="errors_footer" />
</html:messages> 

<c:out value="${param.authenticationFailed}" />
<html:form action="/LogonProcess" scope="request" method="POST"  >

<fieldset>
<legend><fmt:message key="label_reg_logon" /></legend>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_login" /></span>
	<span class="rightFormField"><html:text property="login" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_password" /></span>
	<span class="rightFormField"><html:password property="password" size="30" value="" /></span>
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

</html:form>