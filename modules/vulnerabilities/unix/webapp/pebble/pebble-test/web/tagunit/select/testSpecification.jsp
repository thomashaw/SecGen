<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>
<%@ taglib prefix="pebble" uri="http://www.simongbrown.com/pebble" %>

<tagunit:assertBodyContent name="empty" />
<tagunit:assertAttribute name="name" required="true" rtexprvalue="false" type="java.lang.String" />
<tagunit:assertAttribute name="label" required="false" rtexprvalue="false" type="java.lang.String" />
<tagunit:assertAttribute name="value" required="false" rtexprvalue="false" type="java.lang.String" />
<tagunit:assertAttribute name="items" required="true" rtexprvalue="true" type="java.lang.Object" />
<tagunit:assertAttribute name="selected" required="false" rtexprvalue="true" type="java.lang.Object" />
