 <%@ taglib uri="http://www.tagunit.org/tagunit/core" prefix="tagunit" %>

<%--
  Tests for TagUnit tag libraries
  -------------------------------
  This page contains the tests required to automatically test the basics of
  the tag libraries that are a part of the TagUnit framework
  
--%>

<tagunit:testTagLibrary uri="/test/tagunit">
  <tagunit:tagLibraryDescriptor jar="tagunit.jar" name="tagunit-core.tld"/>
</tagunit:testTagLibrary>

<tagunit:testTagLibrary uri="/test/tagunit">
  <tagunit:tagLibraryDescriptor jar="tagunit.jar" name="tagunit-display.tld"/>
</tagunit:testTagLibrary>