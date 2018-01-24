<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://snipsnap.com/snipsnap" prefix="s" %>

<%-- [<a href="http://www.google.com/search?q=<c:out value='${snip.nameEncoded}'/>">google</a>]
[<a href="http://www.daypop.com/search?q=<c:out value='${snip.nameEncoded}'/>">daypop</a>] --%>
<c:if test="${snip.version > 1}">
  [<a href="exec/diff?name=<c:out value='${snip.nameEncoded}'/>&amp;oldVersion=<c:out value="${snip.version-1}"/>&amp;newVersion=<c:out value="${snip.version}"/>"><fmt:message key="menu.diff"/></a>]
  [<a href="exec/history?name=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.history"/></a>]
</c:if>
<s:check roles="Admin">[<a href="exec/remove?name=<c:out value='${snip.nameEncoded}'/>" onClick="return confirm('<fmt:message key="dialog.deleteSnipSure"/>');"><fmt:message key="menu.delete"/></a>]</s:check>
<s:check roles="Admin,Editor" permission="Edit" snip="${snip}">
 <s:check roles="Admin">
   <s:checkObject roles="Admin" permission="Edit" snip="${snip}" invert="true">
    [<a href="exec/lock?name=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.lock"/></a>]
   </s:checkObject>
   <s:checkObject roles="Admin" permission="Edit" snip="${snip}">
    [<a href="exec/lock?name=<c:out value='${snip.nameEncoded}'/>&amp;unlock=true"><fmt:message key="menu.unlock"/></a>]
   </s:checkObject>
 </s:check>
 <s:check roles="Admin" invert="true">
   <s:checkObject roles="Editor" permission="Edit" snip="${snip}" invert="true">
    [<a href="exec/lock?name=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.lock"/></a>]
   </s:checkObject>
   <s:checkObject roles="Editor" permission="Edit" snip="${snip}">
    [<a href="exec/lock?name=<c:out value='${snip.nameEncoded}'/>&amp;unlock=true"><fmt:message key="menu.unlock"/></a>]
   </s:checkObject>
 </s:check>
</s:check>
<s:check roles="Authenticated" permission="Edit" snip="${snip}">[<a href="raw/<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.view"/></a>]</s:check>
<s:check roles="Authenticated" permission="Edit" snip="${snip}">[<a href="exec/edit?name=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.edit"/></a>]</s:check>
<s:check roles="Authenticated" permission="Edit" snip="${snip}">[<a href="exec/new?parent=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.new"/></a>]</s:check>
<s:check roles="Editor">[<a href="exec/copy?snip=<c:out value='${snip.nameEncoded}'/>"><fmt:message key="menu.copy"/></a>]</s:check>
<s:check roles="Authenticated" permission="Edit" snip="${snip}" invert="true"><span class="inactive">[<fmt:message key="menu.edit"/>]</span></s:check>
[<a href="rdf/<c:out value='${snip.nameEncoded}'/>">rdf</a>]
<%-- keep extra --%>
<s:check roles="Editor"><div class="permissions"><c:out value="${snip.permissions}"/></div></s:check>
