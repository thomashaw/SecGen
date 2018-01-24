<%@ page import="pebble.*,
                 java.util.HashSet,
                 java.util.Set,
                 pebble.blog.SimpleBlog"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<pebble:showAdminPanel>
<table class="item" width="100%" cellspacing="0" cellpadding="8">

  <tr class="itemHeader">
    <td colspan="2">
      Blog administration
    </td>
  </tr>

  <pebble:isBlogContributor>
  <tr class="itemBody">
    <td valign="top"><b>Content</b></td>
    <td>
      <a href="addBlogEntry.secureaction" title="Add a new entry">New blog entry</a> |
      <a href="viewDraftBlogEntries.secureaction" title="Manage your draft blog entries">Drafts</a> |
      <a href="viewBlogEntryTemplates.secureaction" title="Manage your blog entry templates">Templates</a> |
      <a href="viewFiles.secureaction?type=blogFile" title="Manage files in blog">Files</a> |
      <a href="viewFiles.secureaction?type=blogImage" title="Manage images in blog">Images</a> |
      <a href="editCategories.secureaction" title="Edit the categories associated with your blog">Categories</a>
      <br />
      <a href="addStaticPage.secureaction" title="Add a new static page ">Add a new static page</a> |
      <a href="viewStaticPages.secureaction" title="Manage your static pages">Static pages</a>
    </td>
  </tr>
  </pebble:isBlogContributor>

  <pebble:isBlogOwner>
  <tr class="itemBody">
    <td valign="top"><b>Maintenance</b></td>
    <td>
      <a href="editBlogProperties.secureaction" title="Edit your blog properties">Blog properties</a> |
      <a href="editRefererFilters.secureaction" title="Edit referer filters">Referer filters</a> |
      <a href="buildSearchIndex.secureaction" title="Rebuild the search index">Rebuild search index</a> |
      <a href="reloadBlog.secureaction" title="Reload the blog">Reload</a>
      <br />
      <a href="viewTheme.secureaction" title="View the other themes that have been installed">View themes</a> |
      <a href="viewFiles.secureaction?type=themeFile" title="Manage files in theme">Theme (<c:out value="${blog.editableTheme.name}" />)</a>
    </td>
  </tr>
  <tr class="itemBody">
    <td valign="top"><b>Logs</b></td>
    <td>
      <a href="viewLogSummary.secureaction" title="View log summary for this month">Log summary</a> |
      <a href="viewLog.secureaction" title="View log for today">Log</a> |
      <a href="viewReferers.secureaction" title="View referers for today">Referers</a> |
      <a href="viewRequests.secureaction" title="View requests for today">Requests</a> |
      <a href="zipDirectory.secureaction?type=blogData&path=/logs" title="Export logs as ZIP file">ZIP file</a>
    </td>
  </tr>
  <tr class="itemBody">
    <td valign="top"><b>Export Blog</b></td>
    <td>
      <a href="exportBlog.secureaction?flavor=zip" title="Export blog as ZIP file">ZIP file</a> |
      <a href="exportBlog.secureaction?flavor=rss20" title="Export blog as RSS 2.0">RSS 2.0</a> |
      <a href="exportBlog.secureaction?flavor=rss20WithCommentsAndTrackBacks" title="Export blog as RSS 2.0 with comments and TrackBacks">RSS 2.0+</a> |
      <a href="exportBlog.secureaction?flavor=rdf" title="Export blog as RDF 1.0">RDF 1.0</a> |
      <a href="exportBlog.secureaction?flavor=atom" title="Export blog as Atom 0.3">Atom 0.3</a>
    </td>
  </tr>
  </pebble:isBlogOwner>

  <tr class="itemBody">
    <td valign="top"><b>Other</b></td>
    <td>
      <a href="http://pebble.sourceforge.net" title="Pebble home">Pebble home</a> |
      <a href="http://pebble.sourceforge.net/docs/" title="Pebble documentation">Documentation</a> |
      <a href="http://www.simongbrown.com/jira/" title="Issue tracker">Issue tracker</a> |
      <a href="<%= request.getContextPath() %>/changelog.txt" title="Changelog">Changelog</a>
    </td>
  </tr>

  <tr class="itemMetadata">
    <td colspan="2">
      Requests today : <a href="viewReferers.secureaction" title="See referers for today"><c:out value="${blog.logger.log.totalLogEntries}"/></a> |
      Uptime : <% request.setAttribute("uptime", PebbleProperties.getInstance().getUptime()); %><c:out value="${uptime.days}"/> days, <fmt:formatNumber value="${uptime.hours}" pattern="00"/>:<fmt:formatNumber value="${uptime.minutes}" pattern="00"/>:<fmt:formatNumber value="${uptime.seconds}" pattern="00"/> |
      JVM memory : Using <%= ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) %>Kb of <%= (Runtime.getRuntime().totalMemory() / 1024) %>Kb
      <br />
      Logged in as <c:out value="${sessionScope.authenticatedUser.name}"/> |
      Pebble <c:out value="${pebbleProperties.buildVersion}" /> (built <c:out value="${pebbleProperties.buildDate}" />)
    </td>
  </tr>
</table>

<br />
</pebble:showAdminPanel>