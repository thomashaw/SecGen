package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;

public class index_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_logic_redirect_forward;

  public index_jsp() {
    _jspx_tagPool_logic_redirect_forward = new org.apache.jasper.runtime.TagHandlerPool();
  }

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspDestroy() {
    _jspx_tagPool_logic_redirect_forward.release();
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
      if (_jspx_meth_logic_redirect_0(pageContext))
        return;
      out.write("\r\n\r\n");
      out.write("\r\n");
    } catch (Throwable t) {
      out = _jspx_out;
      if (out != null && out.getBufferSize() != 0)
        out.clearBuffer();
      if (pageContext != null) pageContext.handlePageException(t);
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);
    }
  }

  private boolean _jspx_meth_logic_redirect_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  logic:redirect ---- */
    org.apache.strutsel.taglib.logic.ELRedirectTag _jspx_th_logic_redirect_0 = (org.apache.strutsel.taglib.logic.ELRedirectTag) _jspx_tagPool_logic_redirect_forward.get(org.apache.strutsel.taglib.logic.ELRedirectTag.class);
    _jspx_th_logic_redirect_0.setPageContext(pageContext);
    _jspx_th_logic_redirect_0.setParent(null);
    _jspx_th_logic_redirect_0.setForwardExpr("welcome");
    int _jspx_eval_logic_redirect_0 = _jspx_th_logic_redirect_0.doStartTag();
    if (_jspx_th_logic_redirect_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_logic_redirect_forward.reuse(_jspx_th_logic_redirect_0);
    return false;
  }
}
