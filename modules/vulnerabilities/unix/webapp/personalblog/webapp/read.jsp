<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>

<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>

<% int x=0;
String signedIn = (String)session.getValue("signedIn"); %>

<% List posts = (List)request.getAttribute("posts");
  Calendar lastDate = Calendar.getInstance();
  lastDate.set(Calendar.YEAR, -20);
  int count = 0;
  Iterator i = posts.iterator();
  while (i.hasNext() && count < 20){
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

<div id="section">
<div class="date"><dt:DateTime format="EEEE, MMMM dd, yyyy" locale="<%=request.getLocale()%>" value="<%=p.getCreated()%>" /></div>
</div><P>
      
<%    lastDate.setTime(p.getCreated());
      } %>
      

<div id="section">
<a name="<%=p.getId()%>"></a>
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
      
	  <a href="commentInit.do?method=executeStart&postId=<%=p.getId()%>">Comments [<%=p.getComments().size()%>]</a> <A href="index.do?post=<%=p.getId()%>">Permalink</A><P>
      

<% if(signedIn != null && signedIn.equals("true")){ %>
	<b><a href="editInit.do?method=executeStart&editId=<%=p.getId()%>">Edit this post</a></b> | <a href="deletePostInit.do?method=executeStart&id=<%=p.getId()%>">Delete</a>
<%    }%> 



</div><P></P> 
<% }%>

