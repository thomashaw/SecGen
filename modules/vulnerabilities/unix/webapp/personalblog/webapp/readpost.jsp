<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>

<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>

<% int x=0;
Iterator xy;
String signedIn = (String)session.getValue("signedIn");
ArrayList categories = (ArrayList)getServletContext().getAttribute("cats");
%>

<div id="section">
<H3>Selected Post</H3>
</div><P>


<% Post select = (Post)request.getAttribute("post");%>
<div id="section">
<div class="date"><dt:DateTime locale="<%=request.getLocale()%>" format="EEEE, MMMM dd, yyyy" value="<%=select.getCreated()%>" /></div>
</div>
<P>

<div id="section">
<a name="<%=select.getId()%>"></a>
<h3><%=select.getTitle()%>


<% xy = categories.iterator();
   while (xy.hasNext()){
        Category cat = (Category)xy.next();
        if(!(select.getCategory()==null) && select.getCategory().indexOf(cat.getValue()) >= 0){
%>
        <img src="<%=cat.getImage()%>"/>

<%      }
   } %></h3>
   
<p><%=select.getContent()%></p>
      
<a href="commentInit.do?method=executeStart&postId=<%=select.getId()%>">Comments [<%=select.getComments().size()%>]</a>
      

<% if(signedIn != null && signedIn.equals("true")){ %>
	<b><a href="editInit.do?method=executeStart&editId=<%=select.getId()%>">Edit this post</a></b> | <a href="deletePostInit.do?method=executeStart&id=<%=select.getId()%>">Delete</a>
<%    }%> 

</div>
<P>





<div id="section">
<H3>Recent Posts</H3>
</div><P>


<% List posts = (List)request.getAttribute("posts");
  Calendar lastDate = Calendar.getInstance();
  lastDate.set(Calendar.YEAR, -20);

  Iterator i = posts.iterator();
  while (i.hasNext()){
      Post p = (Post)i.next(); 
      Calendar postDate = Calendar.getInstance();
      postDate.setTime(p.getCreated());
      
      if(!((lastDate.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)) &&
         (lastDate.get(Calendar.DATE)  == postDate.get(Calendar.DATE))  &&
         (lastDate.get(Calendar.YEAR)  == postDate.get(Calendar.YEAR)))  ){ %>
      
<div id="section">
<div class="date"><dt:DateTime format="EEEE, MMMM dd, yyyy" locale="<%=request.getLocale()%>" value="<%=p.getCreated()%>" /></div>
</div><P>
      
<%    lastDate.setTime(p.getCreated());
      } %>
      

<div id="section">
<a name="<%=p.getId()%>"></a>
<h3><%=p.getTitle()%>


<% 
  xy = categories.iterator();
  while (xy.hasNext()){
      Category cat = (Category)xy.next();
      if(!(p.getCategory()==null) && p.getCategory().indexOf(cat.getValue()) >= 0){
%>
<img src="<%=cat.getImage()%>"/>

<% }} %>  </h3>
	  <p><%=p.getContent()%></p>
      
	  <a href="commentInit.do?method=executeStart&postId=<%=p.getId()%>">Comments [<%=p.getComments().size()%>]</a>
      

<% if(signedIn != null && signedIn.equals("true")){ %>
	<b><a href="editInit.do?method=executeStart&editId=<%=p.getId()%>">Edit this post</a></b> | <a href="deletePostInit.do?method=executeStart&id=<%=p.getId()%>">Delete</a>
<%    }%> 



</div><P></P> 
<% }%>

