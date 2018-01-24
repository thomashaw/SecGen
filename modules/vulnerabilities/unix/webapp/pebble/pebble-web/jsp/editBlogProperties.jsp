<%@ page import="java.util.*, pebble.*, pebble.blog.*"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<%
  Blog blog = (Blog)request.getAttribute(Constants.BLOG_KEY);
%>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
<form name="editBlogProperties" action="editBlogProperties.secureaction" method="POST" accept-charset="<c:out value="${blog.characterEncoding}" />">

  <tr class="itemHeader">
    <td>
      Blog properties
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table>
        <tr>
          <td>
            <b>General blog properties</b>
          </td>
        </tr>

        <tr>
          <td>
            Name
          </td>
          <td>
            <input type="text" name="name" size="40" value="<c:out value="${blog.name}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Description
          </td>
          <td>
            <input type="text" name="description" size="40" value="<c:out value="${blog.description}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Image
          </td>
          <td>
            <input type="text" name="image" size="40" value="<c:out value="${blog.image}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Author
          </td>
          <td>
            <input type="text" name="author" size="40" value="<c:out value="${blog.author}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Theme
          </td>
          <td>
            <pebble:select name="theme" items='<%= (Collection)request.getAttribute("themes") %>' selected="<%= ((SimpleBlog)blog).getTheme() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Recent blog entries
          </td>
          <td>
            <%
              List numbers = new ArrayList();
              for (int i = 0; i <= 20; i++) {
                numbers.add(new Integer(i));
              }
            %>
            <pebble:select name="recentBlogEntriesOnHomePage" items='<%= numbers %>' selected="<%= ""+blog.getRecentBlogEntriesOnHomePage() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Recent comments/TrackBacks
          </td>
          <td>
            <pebble:select name="recentCommentsOnHomePage" items='<%= numbers %>' selected="<%= ""+blog.getRecentCommentsOnHomePage() %>" /> (requires a <a href="reloadBlog.secureaction">blog reload</a>)
          </td>
        </tr>

        <tr>
          <td>
            <br />
            <b>Internationalization and localization</b>
          </td>
        </tr>

        <tr>
          <td>
            Country
          </td>
          <td>
            <pebble:select name="country" items="<%= java.util.Locale.getISOCountries() %>" selected="<%= blog.getCountry() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Language
          </td>
          <td>
            <pebble:select name="language" items="<%= java.util.Locale.getISOLanguages() %>" selected="<%= blog.getLanguage() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Time zone
          </td>
          <td>
            <%
              List timeZones = Arrays.asList(java.util.TimeZone.getAvailableIDs());
              Collections.sort(timeZones);
            %>
            <pebble:select name="timeZone" items="<%= timeZones %>" selected="<%= blog.getTimeZoneId() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Character encoding
          </td>
          <td>
            <input type="text" name="characterEncoding" size="10" value="<c:out value="${blog.characterEncoding}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Lucene Analyzer
          </td>
          <td>
            <input type="text" name="luceneAnalyzer" size="40" value="<c:out value="${blog.luceneAnalyzer}"/>">
          </td>
        </tr>

        <tr>
          <td>
            <br />
            <b>Security</b>
          </td>
        </tr>

        <tr>
          <td>
            Blog owners
          </td>
          <td>
            <input type="text" name="blogOwners" size="40" value="<c:out value="${blog.blogOwners}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Blog contributors
          </td>
          <td>
            <input type="text" name="blogContributors" size="40" value="<c:out value="${blog.blogContributors}"/>">
          </td>
        </tr>

        <tr>
          <td>
            <br />
            <b>E-mail notifications</b>
          </td>
        </tr>

        <tr>
          <td>
            Comments and TrackBacks
          </td>
          <td>
            Enabled&nbsp;
            <input type="radio" name="commentNotification" value="true"
              <c:if test="${blog.commentNotificationEnabled}">
                checked="checked"
              </c:if>
            />
            Disabled&nbsp;<input type="radio" name="commentNotification" value="false"
              <c:if test="${not blog.commentNotificationEnabled}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>

        <tr>
          <td>
            Blog Entries
          </td>
          <td>
            Enabled&nbsp;
            <input type="radio" name="blogEntryNotification" value="true"
              <c:if test="${blog.blogEntryNotificationEnabled}">
                checked="checked"
              </c:if>
            />
            Disabled&nbsp;<input type="radio" name="blogEntryNotification" value="false"
              <c:if test="${not blog.blogEntryNotificationEnabled}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>

        <tr>
          <td>
            E-mail address
          </td>
          <td>
            <input type="text" name="email" size="40" value="<c:out value="${blog.email}"/>">
          </td>
        </tr>

        <tr>
          <td>
            SMTP (host or JNDI name)
          </td>
          <td>
            <input type="text" name="smtpHost" size="40" value="<c:out value="${blog.smtpHost}"/>">
          </td>
        </tr>

        <tr>
          <td>
            <br />
            <b>XML-RPC update notification pings</b>
          </td>
        </tr>

        <tr>
          <td>
            Update notification pings
          </td>
          <td>
            Enabled&nbsp;
            <input type="radio" name="updateNotificationPingsEnabled" value="true"
              <c:if test="${blog.updateNotificationPingsEnabled}">
                checked="checked"
              </c:if>
            />
            Disabled&nbsp;
            <input type="radio" name="updateNotificationPingsEnabled" value="false"
              <c:if test="${not blog.updateNotificationPingsEnabled}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>

        <tr>
          <td valign="top">
            Websites to ping
          </td>
          <td>
            <textarea name="updateNotificationPings" rows="8" cols="40"><c:out value="${blog.updateNotificationPings}"/></textarea>
          </td>
        </tr>

        <tr>
          <td>
            <br />
            <b>Blogger and MetaWeblog APIs</b>
          </td>
        </tr>

        <tr>
          <td>
            Posting to blog via XML-RPC
          </td>
          <td>
            Enabled&nbsp;
            <input type="radio" name="xmlRpcEnabled" value="true"
              <c:if test="${blog.xmlRpcEnabled}">
                checked="checked"
              </c:if>
            />
            Disabled&nbsp;<input type="radio" name="xmlRpcEnabled" value="false"
              <c:if test="${not blog.xmlRpcEnabled}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>

        <tr>
          <td>
            Username
          </td>
          <td>
            <input type="text" name="xmlRpcUsername" size="40" value="<c:out value="${blog.xmlRpcUsername}"/>">
          </td>
        </tr>

        <tr>
          <td>
            Password
          </td>
          <td>
            <input type="password" name="xmlRpcPassword" size="40" value="<c:out value="${blog.xmlRpcPassword}"/>">
          </td>
        </tr>

        <tr>
          <td>
            XML-RPC URL
          </td>
          <td>
            <c:out value="${blogManager.baseUrl}xmlrpc"/>
          </td>
        </tr>

        <tr>
          <td>
            XML-RPC handler
          </td>
          <td>
            <code>blogger</code> or <code>metaWeblog</code>
          </td>
        </tr>

        <tr>
          <td>
            Blog ID
          </td>
          <td>
            <c:out value="${blog.id}"/>
          </td>
        </tr>

        <tr>
          <td>
            <br />
            <b>Plugins</b>
          </td>
        </tr>

        <tr>
          <td valign="top">
            Blog entry decorators
          </td>
          <td>
            <textarea name="blogEntryDecorators" rows="8" cols="40"><c:out value="${blog.blogEntryDecorators}"/></textarea>
            <br />
            (requires a <a href="reloadBlog.secureaction">blog reload</a> or a webapp restart if new classes have been added to the classpath)
          </td>
        </tr>

        <c:if test="${blogManager.multiUser}">
        <tr>
          <td>
            <br />
            <b>Multi-user properties</b>
          </td>
        </tr>

        <tr>
          <td>
            Public/Private blog
          </td>
          <td>
            &nbsp;
            Public&nbsp;<input type="radio" name="private" value="false"
              <c:if test="${blog.public}">
                checked="checked"
              </c:if>
            />
            Private<input type="radio" name="private" value="true"
              <c:if test="${blog.private}">
                checked="checked"
              </c:if>
            />
          </td>
        </tr>
        </c:if>
      </table>
    </td>
  </tr>

  <tr class="itemBody">
    <td align="right">
      <input name="submit" type="submit" Value="Save Properties">
    </td>
  </tr>

</form>
</table>

<br />