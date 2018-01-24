<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<tagunit:assertBodyContent name="empty" />
<tagunit:assertAttribute name="name" required="true" rtexprvalue="false" type="java.lang.String" />