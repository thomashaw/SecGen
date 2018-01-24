<!--
  ** Template for redirection the root page to the start page
  ** @author Matthias L. Jugel
  ** @version $Id: index.jsp,v 1.7 2004/05/17 10:56:17 leo Exp $
  -->

<% response.sendRedirect(request.getContextPath() + "/"); return; %>