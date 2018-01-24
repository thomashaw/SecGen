<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<%@taglib uri="/tags/jstl/c" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<div>
	<c:forEach  items="${sessionScope.subjectID.principals}" var="principal" >
		<c:if test="${principal.type == 1}">
			<c:set var="userName" scope="page" value="${principal.name}" />
		</c:if>
	</c:forEach>
	
	<c:if test="${userName == 'guest'}">
		<html:link forward="logon">
		<fmt:message key="label_common_logon" />
		</html:link>
		
		<html:link forward="register">
		<fmt:message key="label_reg_register" />
		</html:link>
	</c:if>
	<c:if test="${userName != 'guest'}">
		<html:link forward="logoff">
		<fmt:message key="label_reg_logoff" />
		</html:link>
	</c:if>
	<c:out value="${userName}" />

</div>