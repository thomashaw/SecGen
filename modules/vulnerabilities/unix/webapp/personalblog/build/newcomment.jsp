<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*"%>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/datetime" prefix="dt" %>

<div id="section">

<% Post post = (Post)request.getAttribute("post"); 
  
  Iterator i = post.getComments().iterator();

  while (i.hasNext()){
      Comment comment = (Comment)i.next();
      
%>
<%if (comment.getCreated()!=null){%>
<div class="commentTimeStamp"><dt:DateTime locale="<%=request.getLocale()%>" format="EEEE, MMMM dd, yyyy, hh:mm" value="<%=comment.getCreated()%>" /></div>
<%} else{%>
<div class="commentTimeStamp">No date/time registered</div>
<%} %>
<p></p>
<b><%=comment.getName()%></b> <bean:message key="comments.commented"/>:
<p></p>
<% ArrayList emots = (ArrayList)getServletContext().getAttribute("emoticons");
  Iterator xy = emots.iterator();
  while (xy.hasNext()){
      Category cat = (Category)xy.next();
      if(!(comment.getEmoticon()==null) && comment.getEmoticon().indexOf(cat.getValue()) >= 0){
%>
<img src="<%=cat.getImage()%>"/>
<% }
   }%>
<%=comment.getContent()%>
	  <p><hr>
<% } %>
<% String parentId = (String)request.getAttribute("postId"); %>

<html:form   action="comment.do?method=executeFinish" onsubmit="return changedRememberInfo(this)" method="post">
<html:hidden property="postId" value="<%=parentId%>" />


	<table align="center" cellpadding="5">
	<tr>
	<td align="right"><font color="red"><bean:message key="comments.yourname"/></font>: </td>
	<td align="left"><html:text property="name" size="40" maxlength="250" /></td>
	</tr>
	<tr>
	<td align="right"><bean:message key="comments.email"/>: </td>
	<td align="left"><html:text property="email" size="40" maxlength="250" /></td>
	</tr>
	<tr>
	<td align="right"><bean:message key="comments.url"/>: </td>
	<td align="left"><html:text property="url" size="40" maxlength="250" /></td>
	</tr>
	<tr>
	<td align="right"><font color="red"><bean:message key="comments.comment"/></font>: </td>
	<td align="left"><html:textarea property="content" rows="10" cols="40"  /></td>
	</tr>
	<tr>
	<td colspan="2" align="center">
	<html:submit>
		<bean:message key="comments.postcomment"/>
	</html:submit>
	</td>
	</tr>
	<tr>
	<td align="right"><bean:message key="comments.emoticon"/>: </td>
	<td colspan="2" align="left">
<% ArrayList emoticons = (ArrayList)getServletContext().getAttribute("emoticons");
  Iterator p = emoticons.iterator();
  while (p.hasNext()){
      Category cat = (Category)p.next();
%>
<input type="radio" name="emoticon" value="<%=cat.getValue()%>"><img src="<%=cat.getImage()%>"/>
<% } %>
	</td>
	</tr>
	</table>
<input type="checkbox" name="rememberCheck" /><bean:message key="comments.rememberinfo"/>?<br /><br />
<input type="button" onclick="forgetMe(this.form)" value="<bean:message key="comments.forgetinfo"/>" />
</html:form>

<script type="text/javascript" language="javascript">
<!--

var HOST = '';

// Copyright (c) 1996-1997 Athenia Associates.
// http://www.webreference.com/js/
// License is granted if and only if this entire
// copyright notice is included. By Tomer Shiran.

function setCookie (name, value, expires, path, domain, secure) {
    var curCookie = name + "=" + escape(value) + ((expires) ? "; expires=" + expires.toGMTString() : "") + ((path) ? "; path=" + path : "") + ((domain) ? "; domain=" + domain : "") + ((secure) ? "; secure" : "");
    document.cookie = curCookie;
}

function getCookie (name) {
    var prefix = name + '=';
    var c = document.cookie;
    var nullstring = '';
    var cookieStartIndex = c.indexOf(prefix);
    if (cookieStartIndex == -1)
        return nullstring;
    var cookieEndIndex = c.indexOf(";", cookieStartIndex + prefix.length);
    if (cookieEndIndex == -1)
        cookieEndIndex = c.length;
    return unescape(c.substring(cookieStartIndex + prefix.length, cookieEndIndex));
}

function deleteCookie (name, path, domain) {
    if (getCookie(name))
        document.cookie = name + "=" + ((path) ? "; path=" + path : "") + ((domain) ? "; domain=" + domain : "") + "; expires=Thu, 01-Jan-70 00:00:01 GMT";
}

function fixDate (date) {
    var base = new Date(0);
    var skew = base.getTime();
    if (skew > 0)
        date.setTime(date.getTime() - skew);
}

function rememberMe (f) {
    var now = new Date();
    fixDate(now);
    now.setTime(now.getTime() + 365 * 24 * 60 * 60 * 1000);
    setCookie('personalblog.auth', f.name.value, now, '', HOST, '');
    setCookie('personalblog.mail', f.email.value, now, '', HOST, '');
    setCookie('personalblog.home', f.url.value, now, '', HOST, '');
}

function forgetMe (f) {

    deleteCookie('personalblog.mail', '', HOST);
    deleteCookie('personalblog.home', '', HOST);
    deleteCookie('personalblog.auth', '', HOST);
    f.email.value = '';
    f.name.value = '';
    f.url.value = '';
}


document.commentForm.email.value = getCookie("personalblog.mail");
document.commentForm.name.value = getCookie("personalblog.auth");
document.commentForm.url.value = getCookie("personalblog.home");

function changedRememberInfo(f){
if (f.rememberCheck==undefined || f.rememberCheck.checked) rememberMe(f);
}
//-->
</script>






</div>
