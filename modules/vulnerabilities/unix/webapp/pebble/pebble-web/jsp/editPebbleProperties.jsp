<%@ page import="java.util.*,
                 pebble.blog.Blog,
                 pebble.Constants"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.simongbrown.com/pebble" prefix="pebble" %>

<%
  Blog blog = (Blog)request.getAttribute(Constants.BLOG_KEY);
%>

<table width="100%" cellpadding="8" cellspacing="0" class="item">
<form name="editPebbleProperties" action="editPebbleProperties.secureaction" method="POST" accept-charset="<c:out value="${blog.characterEncoding}" />">

  <tr class="itemHeader">
    <td>
      Pebble Properties
    </td>
  </tr>

  <tr class="itemBody">
    <td>
      <table>
        <tr>
          <td colspan="2">
            <b>General properties</b>
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
            Author
          </td>
          <td>
            <input type="text" name="author" size="40" value="<c:out value="${blog.author}"/>">
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
          <td colspan="2">
            <br />
            <b>Internationalization and localization</b>
          </td>
        </tr>

        <tr>
          <td>
            Country
          </td>
          <td>
            <pebble:select name="country" items="<%= Locale.getISOCountries() %>" selected="<%= blog.getCountry() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Language
          </td>
          <td>
            <pebble:select name="language" items="<%= Locale.getISOLanguages() %>" selected="<%= blog.getLanguage() %>" />
          </td>
        </tr>

        <tr>
          <td>
            Time zone
          </td>
          <td>
            <%
              List timeZones = Arrays.asList(TimeZone.getAvailableIDs());
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