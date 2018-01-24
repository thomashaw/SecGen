<%@ page import="org.snipsnap.app.Application,
                 org.snipsnap.config.Configuration"%>
 <%--
  ** Template for redirection the root page to the start page
  ** @author Matthias L. Jugel
  ** @version $Id: index.jsp,v 1.7 2003/12/11 13:24:56 leo Exp $
  --%>

<%
  Configuration snipConfig = Application.get().getConfiguration();
  response.sendRedirect(snipConfig.getSnipUrl(snipConfig.getStartSnip()));
  return;
%>