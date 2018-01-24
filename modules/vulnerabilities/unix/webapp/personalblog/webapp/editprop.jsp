<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>

<div id="section">
Edit Properties

<TABLE >
<html:form action="property.do?method=executeFinish">
   <logic:iterate name="propertyForm" property="blogProperties" id="foo"
                  indexId="ctr">
<TR WIDTH="100%">           
           <TD WIDTH="20%"><bean:write name="propertyForm" property='<%= "nameIndexed[" + ctr + "]" %>'/></TD>
           <TD WIDTH="80%"><html:text  name="propertyForm" property='<%= "valueIndexed[" + ctr + "]" %>' /><BR>
               <bean:write name="propertyForm" property='<%= "descriptionIndexed[" + ctr + "]" %>'/></TD>
</TR>           
   </logic:iterate>
</TABLE>         
   <html:submit property="submitValue">Save Changes</html:submit>
</html:form>
</div>

