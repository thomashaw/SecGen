<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
  <tr class="itemHeader">
    <td>
      <c:choose>
        <c:when test="${type == 'blogImage'}">Images</c:when>
        <c:when test="${type == 'blogFile'}">Files</c:when>
        <c:when test="${type == 'themeFile'}">Files for <c:out value="${blog.editableTheme.name}" /> theme</c:when>
        <c:when test="${type == 'blogData'}">Blog data files</c:when>
      </c:choose>
      (path is <c:out value="${directory.absolutePath}" />)
    </td>
  </tr>

  <c:if test='${not empty directory.name}'>
  <tr class="itemBody">
    <td>
      <a href="<c:out value="viewFiles.secureaction?path=${directory.path}&type=${type}"/>">Back to parent</a>
    </td>
  </tr>
  </c:if>

  <c:choose>
  <c:when test="${not empty files}">
  <tr class="itemBody">
    <td>
      <table width="100%" cellspacing="0" cellpadding="4">
        <c:forEach var="aFile" items="${files}" varStatus="status">
          <c:choose>
            <c:when test="${status.count % 2 == 0}">
              <tr class="bodyContentEven">
            </c:when>
            <c:otherwise>
                <tr class="bodyContentOdd">
            </c:otherwise>
          </c:choose>
            <td align="center">
              <c:choose>
                <c:when test="${aFile.directory}">
                  <a href="<c:out value="viewFiles.secureaction?path=${aFile.absolutePath}&type=${type}"/>">[+]</a>
                </c:when>
                <c:otherwise>&nbsp;&nbsp;&nbsp;</c:otherwise>
              </c:choose>
            </td>
            <td>
              <a href="<c:out value="viewFiles.secureaction?path=${aFile.path}&file=${aFile.name}&type=${type}"/>#viewFile" title="Rename or copy this file"><c:out value="${aFile.name}"/></a>
            </td>
            <td align="right">
              <fmt:formatNumber value="${aFile.sizeInKB}" type="number" />&nbsp;KB
            </td>
            <td align="right">
              <fmt:formatDate value="${aFile.lastModified}" type="both" dateStyle="short" timeStyle="short" />
            </td>
            <td align="right">
              <c:if test="${not empty aFile.url}">
                <a href="<c:out value="${aFile.url}"/>" title="View/download this file">View</a> |
              </c:if>
              <c:if test="${aFile.editable}">
              <a href="<c:out value="editFile.secureaction?name=${aFile.name}&path=${aFile.path}&type=${type}"/>" title="Edit the content of this file">Edit</a>
              |
              </c:if>
              <a href="javascript:confirmRemove('<c:out value="deleteFile.secureaction?name=${aFile.name}&path=${aFile.path}&type=${type}"/>');" title="Delete this file">Delete</a>
            </td>
          </tr>
        </c:forEach>
      </table>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      Using <fmt:formatNumber value="${root.sizeInKB}" type="number" />&nbsp;KB here, <fmt:formatNumber value="${currentUsage}" type="number" />&nbsp;KB in total
      <c:if test="${pebbleProperties.fileUploadQuota > -1}">
      <br />
      Quota is <fmt:formatNumber value="${pebbleProperties.fileUploadQuota}" type="number" />&nbsp;KB between images, files and your theme
      </c:if>
      <br />
      Export this directory as a <a href="<c:out value="zipDirectory.secureaction?path=${directory.absolutePath}&type=${type}"/>">ZIP file</a>
    </td>
  </tr>
  </c:when>
  <c:otherwise>
  <tr class="itemBody">
    <td>
        There are no files in this directory.
    </td>
  </tr>
  </c:otherwise>
  </c:choose>

  <c:if test="${not empty file}">
  <form name="copyFile" action="copyFile.secureaction" method="POST">
  <tr class="itemBody">
    <td>
      <b>Rename/copy file</b>
    </td>
  </tr>
  <tr class="itemBody">
    <td>
      <a name="viewFile" />
      Name
      <input type="hidden" name="type" value="<c:out value="${type}" />" />
      <input type="hidden" name="path" value="<c:out value="${file.path}" />" />
      <input type="hidden" name="name" value="<c:out value="${file.name}" />" />
      <input type="text" name="newName" size="40" value="<c:out value="${file.name}"/>" />
      <input name="submit" type="submit" Value="Rename" />
      <c:if test="${not file.directory}">
      <input name="submit" type="submit" Value="Copy" />
      </c:if>
    </td>
  </tr>
  </form>
  </c:if>

<form name="createDirectory" action="createDirectory.secureaction" method="POST">
  <tr class="itemBody">
    <td>
      <b>Create directory</b>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      Name
      <input type="hidden" name="type" value="<c:out value="${type}" />" />
      <input type="hidden" name="path" value="<c:out value="${directory.absolutePath}" />" />
      <input type="text" name="name" size="40" value="" />
      <input name="submit" type="submit" Value="Create Directory" />
    </td>
  </tr>
</form>

<form name="uploadFile" enctype="multipart/form-data" action="<c:out value="${uploadAction}" />" method="POST">
  <tr class="itemBody">
    <td>
      <b>Upload file</b>
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      Local Name
      <input type="hidden" name="path" value="<c:out value="${directory.absolutePath}" />" />
      <input name="file" type="file" onChange="populateFilename(this,document.uploadFile.filename)" />
      <br />
      Remote Name
      <input name="filename" type="text" value="" />
      <br />
      Files must be less than <fmt:formatNumber value="${pebbleProperties.fileUploadSize}" type="number" />&nbsp;KB
      <input type="submit" value="Upload File" />
    </td>
  </tr>

</form>

</table>

<br />