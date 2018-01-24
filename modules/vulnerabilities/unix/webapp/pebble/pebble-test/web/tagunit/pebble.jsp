<%@ page import="pebble.blog.persistence.DAOFactory,
                 pebble.blog.persistence.mock.MockDAOFactory,
                 pebble.blog.*,
                 pebble.Constants"%>
<%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>

<%--
  this code sets up the (mock) Pebble environment so that it
  can be tested easily
--%>

<%
  DAOFactory.setConfiguredFactory(new MockDAOFactory());
  Blog blog = new SimpleBlog("");
  BlogManager.getInstance().setBlog(blog);
  request.setAttribute(Constants.BLOG_KEY, blog);
%>

<tagunit:setProperty name="ignoreWarnings" value="false"/>

<tagunit:testTagLibrary uri="/tagunit">
  <tagunit:tagLibraryDescriptor jar="pebble-test.jar" name="pebble.tld"/>
</tagunit:testTagLibrary>