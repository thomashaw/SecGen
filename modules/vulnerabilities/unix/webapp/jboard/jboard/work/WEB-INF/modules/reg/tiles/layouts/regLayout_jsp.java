package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;

public class regLayout_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_tiles_getAsString_name;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_base;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_rewrite_href;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_tiles_insert_attribute;

  public regLayout_jsp() {
    _jspx_tagPool_tiles_getAsString_name = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_base = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_rewrite_href = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_tiles_insert_attribute = new org.apache.jasper.runtime.TagHandlerPool();
  }

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspDestroy() {
    _jspx_tagPool_tiles_getAsString_name.release();
    _jspx_tagPool_html_base.release();
    _jspx_tagPool_html_rewrite_href.release();
    _jspx_tagPool_tiles_insert_attribute.release();
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
      out.write("\r\n\r\n");
      out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\r\n    \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\r\n");
      out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\" >\r\n");
      out.write("<head>\r\n");
      out.write("<title>");
      if (_jspx_meth_tiles_getAsString_0(pageContext))
        return;
      out.write("</title>\r\n");
      if (_jspx_meth_html_base_0(pageContext))
        return;
      out.write("\r\n");
      out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
      if (_jspx_meth_html_rewrite_0(pageContext))
        return;
      out.write("\" />\r\n");
      out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
      if (_jspx_meth_html_rewrite_1(pageContext))
        return;
      out.write("\" />\r\n");
      out.write("<meta http-equiv=\"content-type\" name=\"encodage\" content=\"content=\"text/html\" scheme=\"ISO-8859-15\" /> \r\n");
      out.write("</head>\r\n");
      out.write("<body>\r\n\r\n\r\n");
      out.write("<div>\r\n\t");
      out.write("<div id=\"header\">\r\n\t\t");
      if (_jspx_meth_tiles_insert_0(pageContext))
        return;
      out.write("\r\n\t");
      out.write("</div>\r\n\r\n");
      out.write("<div id=\"content\">\r\n\t\t");
      if (_jspx_meth_tiles_insert_1(pageContext))
        return;
      out.write("\r\n\t");
      out.write("</div>\r\n\r\n\r\n\t");
      out.write("<div id=\"footer\">\r\n\t\t");
      if (_jspx_meth_tiles_insert_2(pageContext))
        return;
      out.write("\r\n\t");
      out.write("</div>\r\n");
      out.write("</div>\r\n\r\n");
      out.write("</body>\r\n");
      out.write("</html:html>\r\n");
    } catch (Throwable t) {
      out = _jspx_out;
      if (out != null && out.getBufferSize() != 0)
        out.clearBuffer();
      if (pageContext != null) pageContext.handlePageException(t);
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);
    }
  }

  private boolean _jspx_meth_tiles_getAsString_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  tiles:getAsString ---- */
    org.apache.struts.taglib.tiles.GetAttributeTag _jspx_th_tiles_getAsString_0 = (org.apache.struts.taglib.tiles.GetAttributeTag) _jspx_tagPool_tiles_getAsString_name.get(org.apache.struts.taglib.tiles.GetAttributeTag.class);
    _jspx_th_tiles_getAsString_0.setPageContext(pageContext);
    _jspx_th_tiles_getAsString_0.setParent(null);
    _jspx_th_tiles_getAsString_0.setName("title");
    int _jspx_eval_tiles_getAsString_0 = _jspx_th_tiles_getAsString_0.doStartTag();
    if (_jspx_th_tiles_getAsString_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_tiles_getAsString_name.reuse(_jspx_th_tiles_getAsString_0);
    return false;
  }

  private boolean _jspx_meth_html_base_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:base ---- */
    org.apache.strutsel.taglib.html.ELBaseTag _jspx_th_html_base_0 = (org.apache.strutsel.taglib.html.ELBaseTag) _jspx_tagPool_html_base.get(org.apache.strutsel.taglib.html.ELBaseTag.class);
    _jspx_th_html_base_0.setPageContext(pageContext);
    _jspx_th_html_base_0.setParent(null);
    int _jspx_eval_html_base_0 = _jspx_th_html_base_0.doStartTag();
    if (_jspx_th_html_base_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_base.reuse(_jspx_th_html_base_0);
    return false;
  }

  private boolean _jspx_meth_html_rewrite_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:rewrite ---- */
    org.apache.strutsel.taglib.html.ELRewriteTag _jspx_th_html_rewrite_0 = (org.apache.strutsel.taglib.html.ELRewriteTag) _jspx_tagPool_html_rewrite_href.get(org.apache.strutsel.taglib.html.ELRewriteTag.class);
    _jspx_th_html_rewrite_0.setPageContext(pageContext);
    _jspx_th_html_rewrite_0.setParent(null);
    _jspx_th_html_rewrite_0.setHrefExpr("/jboard/ressources/styles/global.css");
    int _jspx_eval_html_rewrite_0 = _jspx_th_html_rewrite_0.doStartTag();
    if (_jspx_th_html_rewrite_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_rewrite_href.reuse(_jspx_th_html_rewrite_0);
    return false;
  }

  private boolean _jspx_meth_html_rewrite_1(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:rewrite ---- */
    org.apache.strutsel.taglib.html.ELRewriteTag _jspx_th_html_rewrite_1 = (org.apache.strutsel.taglib.html.ELRewriteTag) _jspx_tagPool_html_rewrite_href.get(org.apache.strutsel.taglib.html.ELRewriteTag.class);
    _jspx_th_html_rewrite_1.setPageContext(pageContext);
    _jspx_th_html_rewrite_1.setParent(null);
    _jspx_th_html_rewrite_1.setHrefExpr("/jboard/ressources/styles/reg.css");
    int _jspx_eval_html_rewrite_1 = _jspx_th_html_rewrite_1.doStartTag();
    if (_jspx_th_html_rewrite_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_rewrite_href.reuse(_jspx_th_html_rewrite_1);
    return false;
  }

  private boolean _jspx_meth_tiles_insert_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  tiles:insert ---- */
    org.apache.struts.taglib.tiles.InsertTag _jspx_th_tiles_insert_0 = (org.apache.struts.taglib.tiles.InsertTag) _jspx_tagPool_tiles_insert_attribute.get(org.apache.struts.taglib.tiles.InsertTag.class);
    _jspx_th_tiles_insert_0.setPageContext(pageContext);
    _jspx_th_tiles_insert_0.setParent(null);
    _jspx_th_tiles_insert_0.setAttribute("header");
    int _jspx_eval_tiles_insert_0 = _jspx_th_tiles_insert_0.doStartTag();
    if (_jspx_th_tiles_insert_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_tiles_insert_attribute.reuse(_jspx_th_tiles_insert_0);
    return false;
  }

  private boolean _jspx_meth_tiles_insert_1(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  tiles:insert ---- */
    org.apache.struts.taglib.tiles.InsertTag _jspx_th_tiles_insert_1 = (org.apache.struts.taglib.tiles.InsertTag) _jspx_tagPool_tiles_insert_attribute.get(org.apache.struts.taglib.tiles.InsertTag.class);
    _jspx_th_tiles_insert_1.setPageContext(pageContext);
    _jspx_th_tiles_insert_1.setParent(null);
    _jspx_th_tiles_insert_1.setAttribute("body");
    int _jspx_eval_tiles_insert_1 = _jspx_th_tiles_insert_1.doStartTag();
    if (_jspx_th_tiles_insert_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_tiles_insert_attribute.reuse(_jspx_th_tiles_insert_1);
    return false;
  }

  private boolean _jspx_meth_tiles_insert_2(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  tiles:insert ---- */
    org.apache.struts.taglib.tiles.InsertTag _jspx_th_tiles_insert_2 = (org.apache.struts.taglib.tiles.InsertTag) _jspx_tagPool_tiles_insert_attribute.get(org.apache.struts.taglib.tiles.InsertTag.class);
    _jspx_th_tiles_insert_2.setPageContext(pageContext);
    _jspx_th_tiles_insert_2.setParent(null);
    _jspx_th_tiles_insert_2.setAttribute("footer");
    int _jspx_eval_tiles_insert_2 = _jspx_th_tiles_insert_2.doStartTag();
    if (_jspx_th_tiles_insert_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_tiles_insert_attribute.reuse(_jspx_th_tiles_insert_2);
    return false;
  }
}
