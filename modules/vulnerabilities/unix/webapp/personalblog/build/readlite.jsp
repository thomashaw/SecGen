<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*,net.eyde.personalblog.service.PersonalBlogService"%>

<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>
<BR>
<a href="mailto:<%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_EMAIL) %>"><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_EMAIL) %></a>

<% List posts = (List)request.getAttribute("posts");
  Calendar lastDate = Calendar.getInstance();
  lastDate.set(Calendar.YEAR, -20);
  int count = 0;
  Iterator i = posts.iterator();
  while (i.hasNext() && count < 8){
      count++;
      Post p = (Post)i.next(); 
      Calendar postDate = Calendar.getInstance();
      postDate.setTime(p.getCreated());
      
      if(!((lastDate.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)) &&
         (lastDate.get(Calendar.DATE)  == postDate.get(Calendar.DATE))  &&
         (lastDate.get(Calendar.YEAR)  == postDate.get(Calendar.YEAR)))  ){ %>

<% if (p.getReference() != null) { %>
<a NAME="<%=p.getReference().substring(9,15)%>"></A>      
<% } %>

<h2><dt:DateTime format="EEEE, MMMM dd, yyyy" value="<%=p.getCreated()%>" /></h2>
      
<%    lastDate.setTime(p.getCreated());
      } %>
      
<h3><%=p.getTitle()%>


<% ArrayList categories = (ArrayList)getServletContext().getAttribute("cats");
  Iterator xy = categories.iterator();
  while (xy.hasNext()){
      Category cat = (Category)xy.next();
      if(!(p.getCategory()==null) && p.getCategory().indexOf(cat.getValue()) >= 0){
%>
<img src="<%=cat.getImage()%>"/>

<% }} %>  </h3>
	  <p><%=p.getContent()%></p>
      
	  <SMALL>Comments [<%=p.getComments().size()%>]</SMALL>
      <HR>
<% }%>

       
<h2><bean:message key="rail.referrerstoday"/></H2>

<% 
  ArrayList referers = (ArrayList)getServletContext().getAttribute("refer");

	Iterator it = referers.iterator();
	int itcount = 0;
	while (it.hasNext()) {
		itcount++;
		if(itcount > 100){
			break;
		}
        Referrer ref = (Referrer) it.next();
		String link = ref.getReferrer();
		String linkText = "";
		if(link != null){
			if(link.length() < 20){
				linkText = link;
			} else {
				linkText = link.substring(0,20) + " ("+ref.getCounter()+")";
			}
		}
%>
<a href="<%=link%>"><%=linkText%></a><br />
<%}%>
       
