<%@ include file="/common/jsp/header.jspf" %>

<table width="100%" cellpadding="8" cellspacing="0">
  <tr class="header">
    <td>
      Comments for <a href="<c:out value="${blogEntry.permalink}"/>" target="_blank"><c:out value="${blogEntry.title}"/></a>
    </td>
    <td align="right">
      <input type="button" name="close" Value="Close" onClick="window.close()" />
    </td>
  </tr>

<%
  String commentsPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/comments.jsp";
  String commentPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/comment.jsp";
%>
  <tr class="bodyContent">
    <td colspan="2">
      <jsp:include page="<%= commentsPage %>" />
    </td>
  </tr>

<c:if test="${blogEntry.commentsEnabled}" >

<form name="addComment" action="addComment.action" method="POST" accept-charset="<c:out value="${blog.characterEncoding}" />">

  <tr class="bodyContent">
    <td colspan="2">
      <b>Add a comment<b>
      <input type="hidden" name="entry" value="<c:out value="${blogEntry.id}"/>">
      <input type="hidden" name="parent" value="<c:out value="${previewComment.parent.id}"/>">
    </td>
  </tr>

  <c:if test="${not empty validationContext.errors}">
  <tr class="bodyContent">
    <td colspan="2" class="validationError">
      <b><c:out value="${validationContext.numberOfErrors}" /> error(s)</b>
      <ul>
      <c:forEach var="error" items="${validationContext.errors}">
        <li><c:out value="${error.message}"/></li>
      </c:forEach>
      </ul>
    </td>
  </tr>
  </c:if>

  <tr class="bodyContent">
    <td colspan="2">
      <c:if test="${!empty previewComment}">
        <c:set var="comment" scope="request" value="${previewComment}"/>
      <table width="100%">
        <tr>
          <td align="center" valign="top"><img src="<%= request.getContextPath() %>/common/images/preview.gif" alt="Preview" align="left"/></td>
          <td valign="top"><jsp:include page="<%= commentPage %>" /></td>
        </tr>
      </table>
      </c:if>
    </td>
  </tr>

  <tr class="bodyContent">
    <td colspan="2">
      <table width="100%">
        <tr>
          <td valign="top"><b>Title</b></td>
          <td><input type="text" name="title" size="40" value="<c:out value="${previewComment.title}"/>"/></td>
        </tr>

        <tr>
          <td valign="top"><b>Body</b></td>
          <td><textarea name="body" rows="8" cols="40"><c:out value="${previewComment.body}"/></textarea></td>
        </tr>

        <tr>
          <td valign="top"><b>Name</b></td>
          <td><input type="text" name="author" size="40" value="<c:out value="${previewComment.author}"/>"></td>
        </tr>

        <tr>
          <td valign="top"><b>E-mail address</b></td>
          <td>
            <input type="text" name="email" size="40" value="<c:out value="${previewComment.email}"/>">
          </td>
        </tr>

        <tr>
          <td valign="top"><b>Website</b></td>
          <td><input type="text" name="website" size="40" value="<c:out value="${previewComment.website}"/>"></td>
        </tr>

        <tr>
          <td valign="top"><b>Remember me</b></td>
          <td>
            Yes&nbsp;
            <input type="radio" name="rememberMe" value="true"
              <c:if test="${rememberMe}">
                checked="checked"
              </c:if>
            />
            No&nbsp;<input type="radio" name="rememberMe" value="false"
              <c:if test="${not rememberMe}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>

        <tr>
          <td colspan="2">
            <ul>
              <li>Allowed HTML/XHTML tags are : b, i, br, p, pre, a href=""</li>
              <li><b>E-mail addresses are not publicly displayed</b> - please leave your e-mail address if you would like to be notified when new comments are added to this blog entry.</li>
              <li>After previewing the comment, don't forget to add it!
            </ul>
          </td>
        </tr>

        <tr>
          <td colspan="2" align="right">
            <input name="submit" type="submit" Value="Preview" />
            <input name="submit" type="submit" Value="Add Comment" />
          </td>
        </tr>
      </table>
    </td>
  </tr>
</form>

<form name="removeEmailAddress" action="removeEmailAddress.action" method="POST">
  <tr class="bodyContent">
    <td colspan="2">
      <b>Remove e-mail address</b>
      <input type="hidden" name="entry" value="<c:out value="${blogEntry.id}"/>">
    </td>
  </tr>

  <tr class="bodyContent">
    <td colspan="2">
      If you would like to stop being notified when new comments are added to this blog entry, please
      enter your e-mail address below and click Remove.
    </td>
  </tr>

  <tr class="bodyContent">
    <td colspan="2">
      <b>E-mail address</b>
      <input type="text" name="email" size="40" />
      <input name="submit" type="submit" Value="Remove" />
    </td>
  </tr>
</form>

  <tr class="footer"><td colspan="2">&nbsp;</td></tr>

</table>
</c:if>

<%@ include file="/common/jsp/footer.jspf" %>