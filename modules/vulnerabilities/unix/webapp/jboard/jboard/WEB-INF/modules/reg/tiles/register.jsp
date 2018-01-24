<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<html:xhtml/>
<html:messages id="error">
  <fmt:message key="errors_header" />
      <li><c:out value="${error}"/></li>
  <fmt:message key="errors_footer" />
</html:messages> 
<html:form action="/RegisterProcess" scope="request" method="POST"  >

<fieldset>
<legend><fmt:message key="label_reg_register" /></logon>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_userName" /></span>
	<span class="rightFormField"><html:text property="userName" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_password" /></span>
	<span class="rightFormField"><html:password property="password" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_confirm_password" /></span>
	<span class="rightFormField"><html:password property="confirmPassword" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_first_name" /></span>
	<span class="rightFormField"><html:text property="firstName" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_last_name" /></span>
	<span class="rightFormField"><html:text property="lastName" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_icq_id" /></span>
	<span class="rightFormField"><html:text property="icq" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_aim_id" /></span>
	<span class="rightFormField"><html:text property="aim" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_msn_messenger" /></span>
	<span class="rightFormField"><html:text property="msn" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_yahoo_messenger" /></span>
	<span class="rightFormField"><html:text property="yahoo" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_your_website" /></span>
	<span class="rightFormField"><html:text property="webSite" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_localisation" /></span>
	<span class="rightFormField"><html:text property="localisation" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_job" /></span>
	<span class="rightFormField"><html:text property="job" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_hobbies" /></span>
	<span class="rightFormField"><html:text property="hobbies" size="30" value="" /></span>
	</label>
</div>
<div class="formBlock">
	<label>
	<span class="leftFormField"><fmt:message key="label_reg_signature" /></span>
	<span class="rightFormField"><html:text property="signature" size="30" value="" /></span>
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

