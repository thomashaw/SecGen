<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>

<div id="section">

<div class="date">
Recent Comments
</div></div>
<p></p>
<% ArrayList comments = (ArrayList)request.getAttribute("recentcomments");
  
  Iterator i = comments.iterator();
  while (i.hasNext()){
      Comment c = (Comment)i.next();
%>   
   
<div id="section">
<%if (c.getCreated()!=null){%>
<div class="commentTimeStamp"><dt:DateTime locale="<%=request.getLocale()%>" format="EEEE, MMMM dd, yyyy, hh:mm" value="<%=c.getCreated()%>" /></div>
<%} else{%>
<div class="commentTimeStamp">No date/time registered</div>
<%} %>
</div>
<p></p>
<div id="section">
<p>In response to <b><a href="index.do?post=<%=c.getPost().getId()%>"><%=c.getPost().getTitle()%></a></b>, <%=c.getName()%> wrote</p>

<p>

<%ArrayList emots = (ArrayList)getServletContext().getAttribute("emoticons");
  Iterator xy = emots.iterator();
  while (xy.hasNext()){
      Category cat = (Category)xy.next();
%>

<%     if(!(c.getEmoticon()==null) && c.getEmoticon().indexOf(cat.getValue()) >= 0){   %>
       <img src="<%=cat.getImage()%>"/>
<%     } %>

<%}%>
   
<%=c.getContent()%></p>

</div>
<p></p>
<% } %>




