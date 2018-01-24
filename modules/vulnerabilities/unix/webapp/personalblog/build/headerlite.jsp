<%@ page language="java" import="net.eyde.personalblog.service.PersonalBlogService"%>

 
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>

<center><font color="#FF0000" size="6"><b>
<%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_TITLE) %>
</b></font></center><br />

<%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_DESCRIPTION) %>

