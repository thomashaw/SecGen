package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;

public class postAForumItem_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_xhtml;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_messages_id;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_fmt_message_key;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_javascript_formName;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_form_scope_onsubmit_method_action;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_text_value_size_property;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_textarea_value_rows_property_cols;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_hidden_value_property;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_submit;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_reset;

  public postAForumItem_jsp() {
    _jspx_tagPool_html_xhtml = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_messages_id = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_fmt_message_key = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_javascript_formName = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_form_scope_onsubmit_method_action = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_text_value_size_property = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_textarea_value_rows_property_cols = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_hidden_value_property = new org.apache.jasper.runtime.TagHandlerPool();
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
    _jspx_tagPool_html_javascript_formName.release();
    _jspx_tagPool_html_form_scope_onsubmit_method_action.release();
    _jspx_tagPool_html_text_value_size_property.release();
    _jspx_tagPool_html_textarea_value_rows_property_cols.release();
    _jspx_tagPool_html_hidden_value_property.release();
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
          out.write("<c:out value=\"${error}\"/>");
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
      out.write("\r\n");
      if (_jspx_meth_html_javascript_0(pageContext))
        return;
      out.write("\r\n\r\n");
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

  private boolean _jspx_meth_html_javascript_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:javascript ---- */
    org.apache.strutsel.taglib.html.ELJavascriptValidatorTag _jspx_th_html_javascript_0 = (org.apache.strutsel.taglib.html.ELJavascriptValidatorTag) _jspx_tagPool_html_javascript_formName.get(org.apache.strutsel.taglib.html.ELJavascriptValidatorTag.class);
    _jspx_th_html_javascript_0.setPageContext(pageContext);
    _jspx_th_html_javascript_0.setParent(null);
    _jspx_th_html_javascript_0.setFormNameExpr("forumForm");
    int _jspx_eval_html_javascript_0 = _jspx_th_html_javascript_0.doStartTag();
    if (_jspx_th_html_javascript_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_javascript_formName.reuse(_jspx_th_html_javascript_0);
    return false;
  }

  private boolean _jspx_meth_html_form_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:form ---- */
    org.apache.strutsel.taglib.html.ELFormTag _jspx_th_html_form_0 = (org.apache.strutsel.taglib.html.ELFormTag) _jspx_tagPool_html_form_scope_onsubmit_method_action.get(org.apache.strutsel.taglib.html.ELFormTag.class);
    _jspx_th_html_form_0.setPageContext(pageContext);
    _jspx_th_html_form_0.setParent(null);
    _jspx_th_html_form_0.setActionExpr("/ForumItem?method=create");
    _jspx_th_html_form_0.setScopeExpr("request");
    _jspx_th_html_form_0.setMethodExpr("POST");
    _jspx_th_html_form_0.setOnsubmitExpr("return validateForumForm(this);");
    int _jspx_eval_html_form_0 = _jspx_th_html_form_0.doStartTag();
    if (_jspx_eval_html_form_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      do {
        out.write("\r\n");
        out.write("<fieldset>\r\n");
        out.write("<legend>");
        if (_jspx_meth_fmt_message_2(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</legend>\r\n\r\n");
        out.write("<div class=\"formBlock\"> \r\n  ");
        out.write("<label>\r\n      ");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_3(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n      ");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_text_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n  ");
        out.write("</label>\r\n");
        out.write("</div>\r\n\r\n");
        out.write("<div class=\"formBlock\">\r\n  ");
        out.write("<label>\r\n      ");
        out.write("<span class=\"leftFormField\">");
        if (_jspx_meth_fmt_message_4(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n      ");
        out.write("<span class=\"rightFormField\">");
        if (_jspx_meth_html_textarea_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("</span>\r\n  ");
        out.write("</label>\r\n");
        out.write("</div>\r\n\r\n");
        if (_jspx_meth_html_hidden_0(_jspx_th_html_form_0, pageContext))
          return true;
        out.write("\r\n");
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
        out.write("</fieldset>\r\n");
        int evalDoAfterBody = _jspx_th_html_form_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
    }
    if (_jspx_th_html_form_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_form_scope_onsubmit_method_action.reuse(_jspx_th_html_form_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_2(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_2 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_2.setPageContext(pageContext);
    _jspx_th_fmt_message_2.setParent(_jspx_th_html_form_0);
    _jspx_th_fmt_message_2.setKey("label_forum_add_a_forum_item");
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
    _jspx_th_fmt_message_3.setKey("label_forum_title");
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
    _jspx_th_html_text_0.setPropertyExpr("title");
    _jspx_th_html_text_0.setSizeExpr("50");
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
    _jspx_th_fmt_message_4.setKey("label_forum_description");
    int _jspx_eval_fmt_message_4 = _jspx_th_fmt_message_4.doStartTag();
    if (_jspx_th_fmt_message_4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_4);
    return false;
  }

  private boolean _jspx_meth_html_textarea_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:textarea ---- */
    org.apache.strutsel.taglib.html.ELTextareaTag _jspx_th_html_textarea_0 = (org.apache.strutsel.taglib.html.ELTextareaTag) _jspx_tagPool_html_textarea_value_rows_property_cols.get(org.apache.strutsel.taglib.html.ELTextareaTag.class);
    _jspx_th_html_textarea_0.setPageContext(pageContext);
    _jspx_th_html_textarea_0.setParent(_jspx_th_html_form_0);
    _jspx_th_html_textarea_0.setPropertyExpr("description");
    _jspx_th_html_textarea_0.setColsExpr("37");
    _jspx_th_html_textarea_0.setRowsExpr("10");
    _jspx_th_html_textarea_0.setValueExpr("");
    int _jspx_eval_html_textarea_0 = _jspx_th_html_textarea_0.doStartTag();
    if (_jspx_th_html_textarea_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_textarea_value_rows_property_cols.reuse(_jspx_th_html_textarea_0);
    return false;
  }

  private boolean _jspx_meth_html_hidden_0(javax.servlet.jsp.tagext.Tag _jspx_th_html_form_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:hidden ---- */
    org.apache.strutsel.taglib.html.ELHiddenTag _jspx_th_html_hidden_0 = (org.apache.strutsel.taglib.html.ELHiddenTag) _jspx_tagPool_html_hidden_value_property.get(org.apache.strutsel.taglib.html.ELHiddenTag.class);
    _jspx_th_html_hidden_0.setPageContext(pageContext);
    _jspx_th_html_hidden_0.setParent(_jspx_th_html_form_0);
    _jspx_th_html_hidden_0.setPropertyExpr("parentId");
    _jspx_th_html_hidden_0.setValueExpr("${forumForm.map.parentId}");
    int _jspx_eval_html_hidden_0 = _jspx_th_html_hidden_0.doStartTag();
    if (_jspx_th_html_hidden_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_hidden_value_property.reuse(_jspx_th_html_hidden_0);
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
        if (_jspx_meth_fmt_message_5(_jspx_th_html_submit_0, pageContext))
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

  private boolean _jspx_meth_fmt_message_5(javax.servlet.jsp.tagext.Tag _jspx_th_html_submit_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_5 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_5.setPageContext(pageContext);
    _jspx_th_fmt_message_5.setParent(_jspx_th_html_submit_0);
    _jspx_th_fmt_message_5.setKey("label_reg_submit");
    int _jspx_eval_fmt_message_5 = _jspx_th_fmt_message_5.doStartTag();
    if (_jspx_th_fmt_message_5.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_5);
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
        if (_jspx_meth_fmt_message_6(_jspx_th_html_reset_0, pageContext))
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

  private boolean _jspx_meth_fmt_message_6(javax.servlet.jsp.tagext.Tag _jspx_th_html_reset_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_6 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_6.setPageContext(pageContext);
    _jspx_th_fmt_message_6.setParent(_jspx_th_html_reset_0);
    _jspx_th_fmt_message_6.setKey("label_reg_reset");
    int _jspx_eval_fmt_message_6 = _jspx_th_fmt_message_6.doStartTag();
    if (_jspx_th_fmt_message_6.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_6);
    return false;
  }
}
