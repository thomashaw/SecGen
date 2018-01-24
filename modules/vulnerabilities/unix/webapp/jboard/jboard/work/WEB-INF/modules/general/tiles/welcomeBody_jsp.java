package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;

public class welcomeBody_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_errors;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_link_action;

  public welcomeBody_jsp() {
    _jspx_tagPool_html_errors = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_link_action = new org.apache.jasper.runtime.TagHandlerPool();
  }

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspDestroy() {
    _jspx_tagPool_html_errors.release();
    _jspx_tagPool_html_link_action.release();
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    JspFactory _jspxFactory = null;
    javax.servlet.jsp.PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;


    try {
      _jspxFactory = JspFactory.getDefaultFactory();
      response.setContentType("text/html;charset=ISO-8859-1");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("\r\n");
      if (_jspx_meth_html_errors_0(pageContext))
        return;
      out.write("\r\n");
      out.write("<div class=\"a\">welcome to the  general module ");
      out.write("</div>\r\n ");
      out.write("<div class=\"a\">\r\n\t ");
      if (_jspx_meth_html_link_0(pageContext))
        return;
      out.write("\r\n");
      out.write("</div>\t ");
    } catch (Throwable t) {
      out = _jspx_out;
      if (out != null && out.getBufferSize() != 0)
        out.clearBuffer();
      if (pageContext != null) pageContext.handlePageException(t);
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);
    }
  }

  private boolean _jspx_meth_html_errors_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:errors ---- */
    org.apache.strutsel.taglib.html.ELErrorsTag _jspx_th_html_errors_0 = (org.apache.strutsel.taglib.html.ELErrorsTag) _jspx_tagPool_html_errors.get(org.apache.strutsel.taglib.html.ELErrorsTag.class);
    _jspx_th_html_errors_0.setPageContext(pageContext);
    _jspx_th_html_errors_0.setParent(null);
    int _jspx_eval_html_errors_0 = _jspx_th_html_errors_0.doStartTag();
    if (_jspx_th_html_errors_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_errors.reuse(_jspx_th_html_errors_0);
    return false;
  }

  private boolean _jspx_meth_html_link_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_0 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_action.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_0.setPageContext(pageContext);
    _jspx_th_html_link_0.setParent(null);
    _jspx_th_html_link_0.setActionExpr("SwitchToModule?prefix=/forum&amp;page=/ForumPanorama.do");
    int _jspx_eval_html_link_0 = _jspx_th_html_link_0.doStartTag();
    if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        out = _bc;
        _jspx_th_html_link_0.setBodyContent(_bc);
        _jspx_th_html_link_0.doInitBody();
      }
      do {
        out.write("\r\n\t go to the bulletin board\r\n\t ");
        int evalDoAfterBody = _jspx_th_html_link_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
    }
    if (_jspx_th_html_link_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_action.reuse(_jspx_th_html_link_0);
    return false;
  }
}
