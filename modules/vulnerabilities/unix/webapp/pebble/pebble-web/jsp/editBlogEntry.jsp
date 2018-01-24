<%@ page import="pebble.blog.*,
                 pebble.Constants" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<%
  Blog blog = (Blog)request.getAttribute(Constants.BLOG_KEY);
%>

<script type="text/javascript">
  function addParagraph(textarea) {
    textarea.value = textarea.value + "<p>\n\n</p>\n\n";
  }

  function addBold(textarea) {
    textarea.value = textarea.value + "<b>" + prompt("Enter the text to make bold") + "</b>";
  }

  function addItalic(textarea) {
    textarea.value = textarea.value + "<i>" + prompt("Enter the text to make italic") + "</i>";
  }

  function addLink(textarea) {
    var url = prompt("Enter the URL", "http://");
    var link = prompt("Enter the text of the link");
    textarea.value = textarea.value + "<a href=\"" + url + "\">" + link + "</a>";
  }

  function addImage(textarea) {
    var url = prompt("Enter the URL for the image", "./images/");
    textarea.value = textarea.value + "<img src=\"" + url + "\" alt=\"\">";
  }

  function addEscapedText(textarea) {
    var text = prompt("Enter the text to be escaped", "");
    textarea.value = textarea.value + "<escape>" + text + "</escape>";
  }
</script>

<c:set var="originalBlogEntry" scope="request" value="${blogEntry}" />
<c:set var="blogEntry" scope="request" value="${previewBlogEntry}" />
<c:set var="displayMode" scope="request" value="preview" />
<%
  String blogEntryPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/blogEntry.jsp";
%>
<c:choose>
  <c:when test="${blogEntry.staticPage}">
  <%
    blogEntryPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/staticPage.jsp";
  %>
  </c:when>
  <c:otherwise>
  <%
    blogEntryPage = "/themes/" + ((SimpleBlog)blog).getTheme() + "/jsp/blogEntry.jsp";
  %>
  </c:otherwise>
</c:choose>
<jsp:include page="<%= blogEntryPage %>" />
<c:set var="blogEntry" scope="request" value="${originalBlogEntry}" />

<table width="100%" cellpadding="8" cellspacing="0">
<form name="editBlogEntry" action="saveBlogEntry.secureaction" method="POST" accept-charset="<c:out value="${blog.characterEncoding}" />">
<input type="hidden" name="entry" value="<c:out value="${blogEntry.id}"/>" />
<input type="hidden" name="type" value="<c:out value="${blogEntry.type}"/>" />

  <c:if test="${not empty validationContext.errors}">
  <tr class="itemBody">
    <td class="validationError">
      <b><c:out value="${validationContext.numberOfErrors}" /> error(s)</b>
      <ul>
      <c:forEach var="error" items="${validationContext.errors}">
        <li><c:out value="${error.message}"/></li>
      </c:forEach>
      </ul>
    </td>
  </tr>
  </c:if>

  <tr class="itemBody">
    <td>
      <table>
        <c:if test="${blogEntry.type == 8}">
        <tr>
          <td valign="top"><b>Name</b></td>
          <td><c:out value="${blog.url}pages/"/><input type="text" name="staticName" size="20" value="<c:out value="${blogEntry.staticName}"/>">.html</td>
        </tr>
        </c:if>

        <tr>
          <td valign="top"><b>Title</b></td>
          <td><input type="text" name="title" size="60" value="<c:out value="${blogEntry.title}"/>"></td>
        </tr>

        <c:if test="${!blogEntry.staticPage}">
        <tr>
          <td valign="top"><b>Excerpt</b></td>
          <td><textarea name="excerpt" rows="10" cols="60"><c:out value="${blogEntry.excerpt}" escapeXml="true"/></textarea></td>
        </tr>
        </c:if>

        <tr>
          <td valign="top"><b>Body</b></td>
          <td><textarea name="body" rows="20" cols="60"><c:out value="${blogEntry.body}" escapeXml="true"/></textarea></td>
        </tr>

        <tr>
          <td valign="top">&nbsp;</td>
          <td align="center">
            <input type="button" value="Paragraph" onClick="addParagraph(this.form.body); return true" />
            <input type="button" value="Bold" onClick="addBold(this.form.body); return true" />
            <input type="button" value="Italic" onClick="addItalic(this.form.body); return true" />
            <input type="button" value="Link" onClick="addLink(this.form.body); return true" />
            <input type="button" value="Image" onClick="addImage(this.form.body); return true" />
            <input type="button" value="Escaped Text" onClick="addEscapedText(this.form.body); return true" />
          </td>
        </tr>

        <tr>
          <td valign="top"><b>Original permalink</b></td>
          <td><input type="text" name="originalPermalink" size="60" value="<c:out value="${blogEntry.originalPermalink}"/>"></td>
        </tr>

        <c:if test="${not blogEntry.staticPage}">
        <tr>
          <td valign="top"><b>Comments</b></td>
          <td>
            Enabled&nbsp;
            <input type="radio" name="commentsEnabled" value="true"
              <c:if test="${blogEntry.commentsEnabled}">
                checked="checked"
              </c:if>
            />
            Disabled&nbsp;<input type="radio" name="commentsEnabled" value="false"
              <c:if test="${not blogEntry.commentsEnabled}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>

        <tr>
          <td valign="top"><b>TrackBacks</b></td>
          <td>
            Enabled&nbsp;
            <input type="radio" name="trackBacksEnabled" value="true"
              <c:if test="${blogEntry.trackBacksEnabled}">
                checked="checked"
              </c:if>
            />
            Disabled&nbsp;<input type="radio" name="trackBacksEnabled" value="false"
              <c:if test="${not blogEntry.trackBacksEnabled}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>
        </c:if>

        <c:if test="${!empty blog.blogCategoryManager.categories}">
        <tr>
          <td valign="top"><b>Category</b></td>
          <td>
            <table width="100%" cellpadding="0" cellspacing="0">
              <tr>
              <c:forEach var="category" items="${blog.blogCategoryManager.categories}" varStatus="status">
                <c:if test="${status.count % 2 == 1}">
                <tr>
                </c:if>
                <%
                  Category category = (Category)pageContext.getAttribute("category");
                  BlogEntry blogEntry = (BlogEntry)request.getAttribute("blogEntry");
                %>
                <td>
                <input type="checkbox" name="category" value="<c:out value="${category.id}" />" <% if (blogEntry.inCategory(category)) { out.write("checked"); } %> />&nbsp;<c:out value="${category.name}" />
                </td>
                <c:if test="${status.count % 2 == 0}">
                </tr>
                </c:if>
              </c:forEach>
              <tr>
            </table>
            <br />
          </td>
        </tr>
        </c:if>

        <tr>
          <td colspan="2">
            <ul>
              <li>No HTML code should be used in the title, but the body may contain HTML code as required.</li>
              <li>Images that have been previously uploaded to your blog should be referenced by <code>./images/your-image-name.jpg</code> (this allows anybody reading your news feeds to see your images).</li>
              <li>The original permalink textfield should only be used when aggegrating content from another site.</li>
              <li>After previewing the entry, don't forget to post it!</li>
            </ul>
          </td>
        </tr>

        <tr>
          <td colspan="2" align="right">
            <input name="submit" type="submit" Value="Preview" />
            <c:choose>
            <c:when test="${blogEntry.staticPage}">
            <input name="submit" type="submit" Value="Save as Static Page" />
            </c:when>
            <c:otherwise>
            <input name="submit" type="submit" Value="Save as Template" />
            <input name="submit" type="submit" Value="Save as Draft" />
            <input name="submit" type="submit" Value="Post to Blog" />
            </c:otherwise>
            </c:choose>
          </td>
        </tr>
      </table>
    </td>
  </tr>

</form>
</table>