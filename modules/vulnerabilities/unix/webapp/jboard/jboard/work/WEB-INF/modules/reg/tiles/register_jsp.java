package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;

public class register_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_xhtml;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_messages_id;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_fmt_message_key;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_out_value;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_form_scope_method_action;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_text_value_size_property;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_password_value_size_property;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_submit;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_reset;

  public register_jsp() {
    _jspx_tagPool_html_xhtml = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_messages_id = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_fmt_message_key = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_out_value = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_form_scope_method_action = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_text_value_size_property = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_password_value_size_property = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_submit = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_reset = new org.apache.jasper.runtime.TagHandlerPool();
  }

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspDestroy() {
    _jspx_tagPool_html_xhtml.release();
    _jspx_tagPool_html_messages_id.release();
    _jspx_tagPool_fmt_message_key.release();
    _jspx_tagPool_c_out_value.release();
    _jspx_tagPool_html_form_scope_method_action.release();
    _jspx_tagPool_html_text_value_size_property.release();
    _jspx_tagPool_html_password_value_size_property.release();
    _jspx_tagPool_html_submit.release();
    _jspx_tagPool_html_reset.release();
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
      out.write("\r\n");
      out.write("\r\n");
      if (_jspx_meth_html_xhtml_0(pageContext))
        return;
      out.write("\r\n");
      /* ----  html:messages ---- */
      org.apache.strutsel.taglib.html.ELMessagesTag _jspx_th_html_messages_0 = (org.apache.strutsel.taglib.html.ELMessagesTag) _jspx_tagPool_html_messages_id.get(org.apache.strutsel.taglib.html.ELMessagesTag.class);
      _jspx_th_html_messages_0.setPageContext(pageContext);
      _jspx_th_html_messages_0.setParent(null);
      _jspx_th_html_messages_0.setIdExpr("error");
      int _jspx_eval_html_messages_0 = _jspx_th_html_messages_0.doStartTag();
      if (_jspx_eval_html_messages_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
        java.lang.String error = null;
        if (_jspx_eval_html_messages_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
          javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
          out = _bc;
          _jspx_th_html_messages_0.setBodyContent(_bc);
          _jspx_th_html_messages_0.doInitBody();
        }
        error = (java.lang.String) pageContext.findAttribute("error");
        do {
          out.write("\r\n  ");
          if (_jspx_meth_fmt_message_0(_jspx_th_html_messages_0, pageContext))
            return;
          out.write("\r\n      ");
          out.write("<li>");
          if (_jspx_meth_c_out_0(_jspx_th_html_messages_0, pageContext))
            return;
          out.write("</li>\r\n  ");
          if (_jspx_meth_fmt_message_1(_jspx_th_html_messages_0, pageContext))
            return;
          out.write("\r\n");
          int evalDoAfterBody = _jspx_th_html_messages_0.doAfterBody();
          error = (java.lang.String) pageContext.findAttribute("error");
          if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
            break;
        } while (true);
        if (_jspx_eval_html_messages_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
          out = pageContext.popBody();
      }
      if (_jspx_th_html_messages_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
        return;
      _jspx_tagPool_html_messages_id.reuse(_jspx_th_html_messages_0);
      out.write(" \r\n");
      if (_jspx_meth_html_form_0(pageContext))
        return;
      out.write("\r\n\r\n");
    } catch (Throwable t) {
      out = _jspx_out;
      if (out != null && out.getBufferSize() != 0)
        out.clearBuffer();
      if (pageContext != null) pageContext.handlePageException(t);
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(pageContext);
    }
  }

  private boolean _jspx_meth_html_xhtml_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:xhtml ---- */
    org.apache.struts.taglib.html.XhtmlTag _jspx_th_html_xhtml_0 = (org.apache.struts.taglib.html.XhtmlTag) _jspx_tagPool_html_xhtml.get(org.apache.struts.taglib.html.XhtmlTag.class);
    _jspx_th_html_xhtml_0.setPageContext(pageContext);
    _jspx_th_html_xhtml_0.setParent(null);
    int _jspx_eval_html_xhtml_0 = _jspx_th_html_xhtml_0.doStartTag();
    if (_jspx_th_html_xhtml_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_xhtml.reuse(_jspx_th_html_xhtml_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_messages_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_0 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_0.setPageContext(pageContext);
    _jspx_th_fmt_message_0.setParent(_jspx_th_html_messages_0);
    _jspx_th_fmt_message_0.setKey("errors_header");
    int _jspx_eval_fmt_message_0 = _jspx_th_fmt_message_0.doStartTag();
    if (_jspx_th_fmt_message_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_0);
    return false;
  }

  private boolean _jspx_meth_c_out_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_messages_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:out ---- */
    org.apache.taglibs.standard.tag.el.core.OutTag _jspx_th_c_out_0 = (org.apache.taglibs.standard.tag.el.core.OutTag) _jspx_tagPool_c_out_value.get(org.apache.taglibs.standard.tag.el.core.OutTag.class);
    _jspx_th_c_out_0.setPageContext(pageContext);
    _jspx_th_c_out_0.setParent(_jspx_th_html_messages_0);
    _jspx_th_c_out_0.setValue("${error}");
    int _jspx_eval_c_out_0 = _jspx_th_c_out_0.doStartTag();
    if (_jspx_th_c_out_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_out_value.reuse(_jspx_th_c_out_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_1(javax.servlet.jsp.tagext.Tag _jspx_th_html_messages_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_1 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_1.setPageContext(pageContext);
    _jspx_th_fmt_message_1.setParent(_jspx_th_html_messages_0);
    _jspx_th_fmt_message_1.setKey("errors_footer");
    int _jspx_eval_fmt_message_1 = _jspx_th_fmt_message_1.doStartTag();
    if (_jspx_th_fmt_message_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_1);
    return false;
  }

  private boolean _jspx_meth_html_form_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:form ---- */
    org.apache.strutsel.taglib.html.ELFormTag _jspx_th_html_form_0 = (org.apache.strutsel.taglib.html.ELFormTag) _jspx_tagPool_html_form_scope_method_action.get(org.apache.strutsel.taglib.html.ELFormTag.class);
    _jspx_th_html_form_0.setPageContext(pageContext);
    _jspx_th_html_form_0.setParent(null);
    _jspx_th_html_form_0.setActionExpr("/RegisterProcess");
    _jspx_th_html_form_0.setScopeExpr("request");
    _jspx_th_html_form_0.setMethodExpr("POST");
    int _jspx_eval_html_form_0 = _jspx_th_html_form_0.doStartTag();
    if (_jspx_eval_html_form_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      do {
        out.write("\r\n\r\n");
        out.write("<fieldset>\r\n");
        out.write("<legend>");
        if (_jspx_meth_fmt_message_2(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</logon>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_3(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_4(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_password_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_5(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_password_1(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_6(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_1(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_7(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_2(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_8(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_3(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_9(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_4(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_10(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_5(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_11(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_6(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_12(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_7(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_13(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_8(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_14(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_9(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_15(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_10(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<label>\r\n\t");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_16(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_11(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n\t");
        out.write("</label>\r\n");
        out.write("</div>\r\n");
        out.write("<div class=\"formBlock\">\r\n\t");
        out.write("<span class=\"rightFormField\" >\r\n\t");
        if (_jspx_meth_html_submit_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("\r\n\t");
        if (_jspx_meth_html_reset_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("\r\n\t");
        out.write("</span>\r\n");
        out.write("</div>\r\n");
        out.write("</fieldset>\r\n\r\n");
        int evalDoAfterBody = _jspx_th_html_form_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
    }
    if (_jspx_th_html_form_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_form_scope_method_action.reuse(_jspx_th_html_form_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_2(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_2 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_2.setPageContext(pageContext);
    _jspx_th_fmt_message_2.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_2.setKey("label_reg_register");
    int _jspx_eval_fmt_message_2 = _jspx_th_fmt_message_2.doStartTag();
    if (_jspx_th_fmt_message_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_2);
    return false;
  }

  private boolean _jspx_meth_fmt_message_3(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_3 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_3.setPageContext(pageContext);
    _jspx_th_fmt_message_3.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_3.setKey("label_reg_userName");
    int _jspx_eval_fmt_message_3 = _jspx_th_fmt_message_3.doStartTag();
    if (_jspx_th_fmt_message_3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_3);
    return false;
  }

  private boolean _jspx_meth_html_text_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_0 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_0.setPageContext(pageContext);
    _jspx_th_html_text_0.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_0.setPropertyExpr("userName");
    _jspx_th_html_text_0.setSizeExpr("30");
    _jspx_th_html_text_0.setValueExpr("");
    int _jspx_eval_html_text_0 = _jspx_th_html_text_0.doStartTag();
    if (_jspx_th_html_text_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_4(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_4 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_4.setPageContext(pageContext);
    _jspx_th_fmt_message_4.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_4.setKey("label_reg_password");
    int _jspx_eval_fmt_message_4 = _jspx_th_fmt_message_4.doStartTag();
    if (_jspx_th_fmt_message_4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_4);
    return false;
  }

  private boolean _jspx_meth_html_password_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:password ---- */
    org.apache.strutsel.taglib.html.ELPasswordTag _jspx_th_html_password_0 = (org.apache.strutsel.taglib.html.ELPasswordTag) _jspx_tagPool_html_password_value_size_property.get(org.apache.strutsel.taglib.html.ELPasswordTag.class);
    _jspx_th_html_password_0.setPageContext(pageContext);
    _jspx_th_html_password_0.setParent(_jspx_th_html_form_0);
    _jspx_th_html_password_0.setPropertyExpr("password");
    _jspx_th_html_password_0.setSizeExpr("30");
    _jspx_th_html_password_0.setValueExpr("");
    int _jspx_eval_html_password_0 = _jspx_th_html_password_0.doStartTag();
    if (_jspx_th_html_password_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_password_value_size_property.reuse(_jspx_th_html_password_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_5(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_5 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_5.setPageContext(pageContext);
    _jspx_th_fmt_message_5.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_5.setKey("label_reg_confirm_password");
    int _jspx_eval_fmt_message_5 = _jspx_th_fmt_message_5.doStartTag();
    if (_jspx_th_fmt_message_5.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_5);
    return false;
  }

  private boolean _jspx_meth_html_password_1(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:password ---- */
    org.apache.strutsel.taglib.html.ELPasswordTag _jspx_th_html_password_1 = (org.apache.strutsel.taglib.html.ELPasswordTag) _jspx_tagPool_html_password_value_size_property.get(org.apache.strutsel.taglib.html.ELPasswordTag.class);
    _jspx_th_html_password_1.setPageContext(pageContext);
    _jspx_th_html_password_1.setParent(_jspx_th_html_form_0);
    _jspx_th_html_password_1.setPropertyExpr("confirmPassword");
    _jspx_th_html_password_1.setSizeExpr("30");
    _jspx_th_html_password_1.setValueExpr("");
    int _jspx_eval_html_password_1 = _jspx_th_html_password_1.doStartTag();
    if (_jspx_th_html_password_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_password_value_size_property.reuse(_jspx_th_html_password_1);
    return false;
  }

  private boolean _jspx_meth_fmt_message_6(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_6 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_6.setPageContext(pageContext);
    _jspx_th_fmt_message_6.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_6.setKey("label_reg_first_name");
    int _jspx_eval_fmt_message_6 = _jspx_th_fmt_message_6.doStartTag();
    if (_jspx_th_fmt_message_6.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_6);
    return false;
  }

  private boolean _jspx_meth_html_text_1(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_1 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_1.setPageContext(pageContext);
    _jspx_th_html_text_1.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_1.setPropertyExpr("firstName");
    _jspx_th_html_text_1.setSizeExpr("30");
    _jspx_th_html_text_1.setValueExpr("");
    int _jspx_eval_html_text_1 = _jspx_th_html_text_1.doStartTag();
    if (_jspx_th_html_text_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_1);
    return false;
  }

  private boolean _jspx_meth_fmt_message_7(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_7 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_7.setPageContext(pageContext);
    _jspx_th_fmt_message_7.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_7.setKey("label_reg_last_name");
    int _jspx_eval_fmt_message_7 = _jspx_th_fmt_message_7.doStartTag();
    if (_jspx_th_fmt_message_7.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_7);
    return false;
  }

  private boolean _jspx_meth_html_text_2(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_2 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_2.setPageContext(pageContext);
    _jspx_th_html_text_2.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_2.setPropertyExpr("lastName");
    _jspx_th_html_text_2.setSizeExpr("30");
    _jspx_th_html_text_2.setValueExpr("");
    int _jspx_eval_html_text_2 = _jspx_th_html_text_2.doStartTag();
    if (_jspx_th_html_text_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_2);
    return false;
  }

  private boolean _jspx_meth_fmt_message_8(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_8 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_8.setPageContext(pageContext);
    _jspx_th_fmt_message_8.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_8.setKey("label_reg_icq_id");
    int _jspx_eval_fmt_message_8 = _jspx_th_fmt_message_8.doStartTag();
    if (_jspx_th_fmt_message_8.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_8);
    return false;
  }

  private boolean _jspx_meth_html_text_3(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_3 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_3.setPageContext(pageContext);
    _jspx_th_html_text_3.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_3.setPropertyExpr("icq");
    _jspx_th_html_text_3.setSizeExpr("30");
    _jspx_th_html_text_3.setValueExpr("");
    int _jspx_eval_html_text_3 = _jspx_th_html_text_3.doStartTag();
    if (_jspx_th_html_text_3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_3);
    return false;
  }

  private boolean _jspx_meth_fmt_message_9(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_9 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_9.setPageContext(pageContext);
    _jspx_th_fmt_message_9.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_9.setKey("label_reg_aim_id");
    int _jspx_eval_fmt_message_9 = _jspx_th_fmt_message_9.doStartTag();
    if (_jspx_th_fmt_message_9.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_9);
    return false;
  }

  private boolean _jspx_meth_html_text_4(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_4 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_4.setPageContext(pageContext);
    _jspx_th_html_text_4.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_4.setPropertyExpr("aim");
    _jspx_th_html_text_4.setSizeExpr("30");
    _jspx_th_html_text_4.setValueExpr("");
    int _jspx_eval_html_text_4 = _jspx_th_html_text_4.doStartTag();
    if (_jspx_th_html_text_4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_4);
    return false;
  }

  private boolean _jspx_meth_fmt_message_10(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_10 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_10.setPageContext(pageContext);
    _jspx_th_fmt_message_10.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_10.setKey("label_reg_msn_messenger");
    int _jspx_eval_fmt_message_10 = _jspx_th_fmt_message_10.doStartTag();
    if (_jspx_th_fmt_message_10.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_10);
    return false;
  }

  private boolean _jspx_meth_html_text_5(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_5 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_5.setPageContext(pageContext);
    _jspx_th_html_text_5.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_5.setPropertyExpr("msn");
    _jspx_th_html_text_5.setSizeExpr("30");
    _jspx_th_html_text_5.setValueExpr("");
    int _jspx_eval_html_text_5 = _jspx_th_html_text_5.doStartTag();
    if (_jspx_th_html_text_5.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_5);
    return false;
  }

  private boolean _jspx_meth_fmt_message_11(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_11 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_11.setPageContext(pageContext);
    _jspx_th_fmt_message_11.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_11.setKey("label_reg_yahoo_messenger");
    int _jspx_eval_fmt_message_11 = _jspx_th_fmt_message_11.doStartTag();
    if (_jspx_th_fmt_message_11.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_11);
    return false;
  }

  private boolean _jspx_meth_html_text_6(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_6 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_6.setPageContext(pageContext);
    _jspx_th_html_text_6.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_6.setPropertyExpr("yahoo");
    _jspx_th_html_text_6.setSizeExpr("30");
    _jspx_th_html_text_6.setValueExpr("");
    int _jspx_eval_html_text_6 = _jspx_th_html_text_6.doStartTag();
    if (_jspx_th_html_text_6.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_6);
    return false;
  }

  private boolean _jspx_meth_fmt_message_12(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_12 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_12.setPageContext(pageContext);
    _jspx_th_fmt_message_12.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_12.setKey("label_reg_your_website");
    int _jspx_eval_fmt_message_12 = _jspx_th_fmt_message_12.doStartTag();
    if (_jspx_th_fmt_message_12.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_12);
    return false;
  }

  private boolean _jspx_meth_html_text_7(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_7 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_7.setPageContext(pageContext);
    _jspx_th_html_text_7.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_7.setPropertyExpr("webSite");
    _jspx_th_html_text_7.setSizeExpr("30");
    _jspx_th_html_text_7.setValueExpr("");
    int _jspx_eval_html_text_7 = _jspx_th_html_text_7.doStartTag();
    if (_jspx_th_html_text_7.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_7);
    return false;
  }

  private boolean _jspx_meth_fmt_message_13(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_13 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_13.setPageContext(pageContext);
    _jspx_th_fmt_message_13.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_13.setKey("label_reg_localisation");
    int _jspx_eval_fmt_message_13 = _jspx_th_fmt_message_13.doStartTag();
    if (_jspx_th_fmt_message_13.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_13);
    return false;
  }

  private boolean _jspx_meth_html_text_8(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_8 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_8.setPageContext(pageContext);
    _jspx_th_html_text_8.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_8.setPropertyExpr("localisation");
    _jspx_th_html_text_8.setSizeExpr("30");
    _jspx_th_html_text_8.setValueExpr("");
    int _jspx_eval_html_text_8 = _jspx_th_html_text_8.doStartTag();
    if (_jspx_th_html_text_8.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_8);
    return false;
  }

  private boolean _jspx_meth_fmt_message_14(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_14 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_14.setPageContext(pageContext);
    _jspx_th_fmt_message_14.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_14.setKey("label_reg_job");
    int _jspx_eval_fmt_message_14 = _jspx_th_fmt_message_14.doStartTag();
    if (_jspx_th_fmt_message_14.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_14);
    return false;
  }

  private boolean _jspx_meth_html_text_9(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_9 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_9.setPageContext(pageContext);
    _jspx_th_html_text_9.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_9.setPropertyExpr("job");
    _jspx_th_html_text_9.setSizeExpr("30");
    _jspx_th_html_text_9.setValueExpr("");
    int _jspx_eval_html_text_9 = _jspx_th_html_text_9.doStartTag();
    if (_jspx_th_html_text_9.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_9);
    return false;
  }

  private boolean _jspx_meth_fmt_message_15(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_15 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_15.setPageContext(pageContext);
    _jspx_th_fmt_message_15.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_15.setKey("label_reg_hobbies");
    int _jspx_eval_fmt_message_15 = _jspx_th_fmt_message_15.doStartTag();
    if (_jspx_th_fmt_message_15.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_15);
    return false;
  }

  private boolean _jspx_meth_html_text_10(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_10 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_10.setPageContext(pageContext);
    _jspx_th_html_text_10.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_10.setPropertyExpr("hobbies");
    _jspx_th_html_text_10.setSizeExpr("30");
    _jspx_th_html_text_10.setValueExpr("");
    int _jspx_eval_html_text_10 = _jspx_th_html_text_10.doStartTag();
    if (_jspx_th_html_text_10.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_10);
    return false;
  }

  private boolean _jspx_meth_fmt_message_16(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_16 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_16.setPageContext(pageContext);
    _jspx_th_fmt_message_16.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_16.setKey("label_reg_signature");
    int _jspx_eval_fmt_message_16 = _jspx_th_fmt_message_16.doStartTag();
    if (_jspx_th_fmt_message_16.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_16);
    return false;
  }

  private boolean _jspx_meth_html_text_11(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:text ---- */
    org.apache.strutsel.taglib.html.ELTextTag _jspx_th_html_text_11 = (org.apache.strutsel.taglib.html.ELTextTag) _jspx_tagPool_html_text_value_size_property.get(org.apache.strutsel.taglib.html.ELTextTag.class);
    _jspx_th_html_text_11.setPageContext(pageContext);
    _jspx_th_html_text_11.setParent(_jspx_th_html_form_0);
    _jspx_th_html_text_11.setPropertyExpr("signature");
    _jspx_th_html_text_11.setSizeExpr("30");
    _jspx_th_html_text_11.setValueExpr("");
    int _jspx_eval_html_text_11 = _jspx_th_html_text_11.doStartTag();
    if (_jspx_th_html_text_11.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_text_value_size_property.reuse(_jspx_th_html_text_11);
    return false;
  }

  private boolean _jspx_meth_html_submit_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:submit ---- */
    org.apache.strutsel.taglib.html.ELSubmitTag _jspx_th_html_submit_0 = (org.apache.strutsel.taglib.html.ELSubmitTag) _jspx_tagPool_html_submit.get(org.apache.strutsel.taglib.html.ELSubmitTag.class);
    _jspx_th_html_submit_0.setPageContext(pageContext);
    _jspx_th_html_submit_0.setParent(_jspx_th_html_form_0);
    int _jspx_eval_html_submit_0 = _jspx_th_html_submit_0.doStartTag();
    if (_jspx_eval_html_submit_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_submit_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        out = _bc;
        _jspx_th_html_submit_0.setBodyContent(_bc);
        _jspx_th_html_submit_0.doInitBody();
      }
      do {
        out.write("\r\n\t");
        if (_jspx_meth_fmt_message_17(_jspx_th_html_submit_0, pageContext))
          return true;
        out.write("\r\n\t");
        int evalDoAfterBody = _jspx_th_html_submit_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_submit_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
    }
    if (_jspx_th_html_submit_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_submit.reuse(_jspx_th_html_submit_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_17(javax.servlet.jsp.tagext.Tag _jspx_th_html_submit_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_17 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_17.setPageContext(pageContext);
    _jspx_th_fmt_message_17.setParent(_jspx_th_html_submit_0);
    _jspx_th_fmt_message_17.setKey("label_reg_submit");
    int _jspx_eval_fmt_message_17 = _jspx_th_fmt_message_17.doStartTag();
    if (_jspx_th_fmt_message_17.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_17);
    return false;
  }

  private boolean _jspx_meth_html_reset_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:reset ---- */
    org.apache.strutsel.taglib.html.ELResetTag _jspx_th_html_reset_0 = (org.apache.strutsel.taglib.html.ELResetTag) _jspx_tagPool_html_reset.get(org.apache.strutsel.taglib.html.ELResetTag.class);
    _jspx_th_html_reset_0.setPageContext(pageContext);
    _jspx_th_html_reset_0.setParent(_jspx_th_html_form_0);
    int _jspx_eval_html_reset_0 = _jspx_th_html_reset_0.doStartTag();
    if (_jspx_eval_html_reset_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_reset_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        out = _bc;
        _jspx_th_html_reset_0.setBodyContent(_bc);
        _jspx_th_html_reset_0.doInitBody();
      }
      do {
        out.write("\r\n\t");
        if (_jspx_meth_fmt_message_18(_jspx_th_html_reset_0, pageContext))
          return true;
        out.write("\r\n\t");
        int evalDoAfterBody = _jspx_th_html_reset_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_reset_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
    }
    if (_jspx_th_html_reset_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_reset.reuse(_jspx_th_html_reset_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_18(javax.servlet.jsp.tagext.Tag _jspx_th_html_reset_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_18 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_18.setPageContext(pageContext);
    _jspx_th_fmt_message_18.setParent(_jspx_th_html_reset_0);
    _jspx_th_fmt_message_18.setKey("label_reg_reset");
    int _jspx_eval_fmt_message_18 = _jspx_th_fmt_message_18.doStartTag();
    if (_jspx_th_fmt_message_18.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_18);
    return false;
  }
}
