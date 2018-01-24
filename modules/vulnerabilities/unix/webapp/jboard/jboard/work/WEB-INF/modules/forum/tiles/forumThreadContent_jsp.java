package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;
import java.util.HashMap;

public class forumThreadContent_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_xhtml;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_messages_id;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_fmt_message_key;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_out_value;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_set_var_value_scope;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_forEach_var_items;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_link_name_forward;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_link_paramName_paramId_forward;

  public forumThreadContent_jsp() {
    _jspx_tagPool_html_xhtml = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_messages_id = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_fmt_message_key = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_out_value = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_set_var_value_scope = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_forEach_var_items = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_link_name_forward = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_link_paramName_paramId_forward = new org.apache.jasper.runtime.TagHandlerPool();
  }

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspDestroy() {
    _jspx_tagPool_html_xhtml.release();
    _jspx_tagPool_html_messages_id.release();
    _jspx_tagPool_fmt_message_key.release();
    _jspx_tagPool_c_out_value.release();
    _jspx_tagPool_c_set_var_value_scope.release();
    _jspx_tagPool_c_forEach_var_items.release();
    _jspx_tagPool_html_link_name_forward.release();
    _jspx_tagPool_html_link_paramName_paramId_forward.release();
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
      out.write("\r\n");
      if (_jspx_meth_html_xhtml_0(pageContext))
        return;
      out.write("\r\n");
      out.write("<div class=\"first\">\r\n\t");
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
          out.write("\r\n\t  ");
          if (_jspx_meth_fmt_message_0(_jspx_th_html_messages_0, pageContext))
            return;
          out.write("\r\n\t\t  ");
          out.write("<li>");
          if (_jspx_meth_c_out_0(_jspx_th_html_messages_0, pageContext))
            return;
          out.write("</li>\r\n\t  ");
          if (_jspx_meth_fmt_message_1(_jspx_th_html_messages_0, pageContext))
            return;
          out.write("\r\n\t");
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
      out.write("\r\n\t");
      out.write("<!-- declaration of an HashMap called editparams -->\r\n\t");
      java.util.HashMap editParams = null;
      synchronized (pageContext) {
        editParams = (java.util.HashMap) pageContext.getAttribute("editParams", PageContext.PAGE_SCOPE);
        if (editParams == null){
          try {
            editParams = (java.util.HashMap) java.beans.Beans.instantiate(this.getClass().getClassLoader(), "java.util.HashMap");
          } catch (ClassNotFoundException exc) {
            throw new InstantiationException(exc.getMessage());
          } catch (Exception exc) {
            throw new ServletException("Cannot create bean of class " + "java.util.HashMap", exc);
          }
          pageContext.setAttribute("editParams", editParams, PageContext.PAGE_SCOPE);
        }
      }
      out.write("\r\n\t\r\n\t");
      out.write("<span>");
      if (_jspx_meth_fmt_message_2(pageContext))
        return;
      out.write("&nbsp;");
      if (_jspx_meth_c_out_1(pageContext))
        return;
      out.write("</span>\r\n\t\r\n\t");
      if (_jspx_meth_c_set_0(pageContext))
        return;
      out.write("\r\n\t");
      java.lang.Long parentId = null;
      synchronized (pageContext) {
        parentId = (java.lang.Long) pageContext.getAttribute("parentId", PageContext.PAGE_SCOPE);
        if (parentId == null){
          throw new java.lang.InstantiationException("bean parentId not found within scope");
        }
      }
      out.write("\r\n\t");
      /* ----  c:forEach ---- */
      org.apache.taglibs.standard.tag.el.core.ForEachTag _jspx_th_c_forEach_0 = (org.apache.taglibs.standard.tag.el.core.ForEachTag) _jspx_tagPool_c_forEach_var_items.get(org.apache.taglibs.standard.tag.el.core.ForEachTag.class);
      _jspx_th_c_forEach_0.setPageContext(pageContext);
      _jspx_th_c_forEach_0.setParent(null);
      _jspx_th_c_forEach_0.setItems("${forumForm.map.forumThread.forumMessages}");
      _jspx_th_c_forEach_0.setVar("message");
      int _jspx_push_body_count_c_forEach_0 = 0;
      try {
        int _jspx_eval_c_forEach_0 = _jspx_th_c_forEach_0.doStartTag();
        if (_jspx_eval_c_forEach_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
          do {
            out.write("\r\n\t\t");
            if (_jspx_meth_c_set_1(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("\r\n\t\t\r\n\t\t");
            java.lang.Long id = null;
            synchronized (pageContext) {
              id = (java.lang.Long) pageContext.getAttribute("id", PageContext.PAGE_SCOPE);
              if (id == null){
                throw new java.lang.InstantiationException("bean id not found within scope");
              }
            }
            out.write("\r\n\t\t\r\n\t\t ");
 editParams.put("id", id);  
            out.write("\r\n\t\t ");
 editParams.put("parentId", parentId);  
            out.write("\r\n\t\t");
 editParams.put("method","delete");  
            out.write("\r\n\t\t\t");
            out.write("<div class=\"third\">\r\n\t\t\t\t ");
            out.write("<span>");
            if (_jspx_meth_c_out_2(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("</span>\r\n\t\t\t\t ");
            out.write("<span>");
            if (_jspx_meth_html_link_0(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("</span>\r\n\t\t\t\t ");
 editParams.put("method","read");  
            out.write("\r\n\t\t\t\t ");
            if (_jspx_meth_html_link_1(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("</span>\r\n\t\t\t");
            out.write("</div>\r\n\t");
            int evalDoAfterBody = _jspx_th_c_forEach_0.doAfterBody();
            if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
              break;
          } while (true);
        }
        if (_jspx_th_c_forEach_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
          return;
      } catch (Throwable _jspx_exception) {
        while (_jspx_push_body_count_c_forEach_0-- > 0)
          out = pageContext.popBody();
        _jspx_th_c_forEach_0.doCatch(_jspx_exception);
      } finally {
        _jspx_th_c_forEach_0.doFinally();
        _jspx_tagPool_c_forEach_var_items.reuse(_jspx_th_c_forEach_0);
      }
      out.write("\r\n\t");
      if (_jspx_meth_html_link_2(pageContext))
        return;
      out.write("\r\n\r\n");
      out.write("</div>");
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

  private boolean _jspx_meth_fmt_message_2(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_2 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_2.setPageContext(pageContext);
    _jspx_th_fmt_message_2.setParent(null);
    _jspx_th_fmt_message_2.setKey("label_forum_title");
    int _jspx_eval_fmt_message_2 = _jspx_th_fmt_message_2.doStartTag();
    if (_jspx_th_fmt_message_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_2);
    return false;
  }

  private boolean _jspx_meth_c_out_1(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:out ---- */
    org.apache.taglibs.standard.tag.el.core.OutTag _jspx_th_c_out_1 = (org.apache.taglibs.standard.tag.el.core.OutTag) _jspx_tagPool_c_out_value.get(org.apache.taglibs.standard.tag.el.core.OutTag.class);
    _jspx_th_c_out_1.setPageContext(pageContext);
    _jspx_th_c_out_1.setParent(null);
    _jspx_th_c_out_1.setValue("${forumForm.map.forumThread.title}");
    int _jspx_eval_c_out_1 = _jspx_th_c_out_1.doStartTag();
    if (_jspx_th_c_out_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_out_value.reuse(_jspx_th_c_out_1);
    return false;
  }

  private boolean _jspx_meth_c_set_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:set ---- */
    org.apache.taglibs.standard.tag.el.core.SetTag _jspx_th_c_set_0 = (org.apache.taglibs.standard.tag.el.core.SetTag) _jspx_tagPool_c_set_var_value_scope.get(org.apache.taglibs.standard.tag.el.core.SetTag.class);
    _jspx_th_c_set_0.setPageContext(pageContext);
    _jspx_th_c_set_0.setParent(null);
    _jspx_th_c_set_0.setVar("parentId");
    _jspx_th_c_set_0.setValue("${forumForm.map.forumThread.id}");
    _jspx_th_c_set_0.setScope("page");
    int _jspx_eval_c_set_0 = _jspx_th_c_set_0.doStartTag();
    if (_jspx_th_c_set_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_set_var_value_scope.reuse(_jspx_th_c_set_0);
    return false;
  }

  private boolean _jspx_meth_c_set_1(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:set ---- */
    org.apache.taglibs.standard.tag.el.core.SetTag _jspx_th_c_set_1 = (org.apache.taglibs.standard.tag.el.core.SetTag) _jspx_tagPool_c_set_var_value_scope.get(org.apache.taglibs.standard.tag.el.core.SetTag.class);
    _jspx_th_c_set_1.setPageContext(pageContext);
    _jspx_th_c_set_1.setParent(_jspx_th_c_forEach_0);
    _jspx_th_c_set_1.setVar("id");
    _jspx_th_c_set_1.setValue("${message.id}");
    _jspx_th_c_set_1.setScope("page");
    int _jspx_eval_c_set_1 = _jspx_th_c_set_1.doStartTag();
    if (_jspx_th_c_set_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_set_var_value_scope.reuse(_jspx_th_c_set_1);
    return false;
  }

  private boolean _jspx_meth_c_out_2(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:out ---- */
    org.apache.taglibs.standard.tag.el.core.OutTag _jspx_th_c_out_2 = (org.apache.taglibs.standard.tag.el.core.OutTag) _jspx_tagPool_c_out_value.get(org.apache.taglibs.standard.tag.el.core.OutTag.class);
    _jspx_th_c_out_2.setPageContext(pageContext);
    _jspx_th_c_out_2.setParent(_jspx_th_c_forEach_0);
    _jspx_th_c_out_2.setValue("${message.message}");
    int _jspx_eval_c_out_2 = _jspx_th_c_out_2.doStartTag();
    if (_jspx_th_c_out_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_out_value.reuse(_jspx_th_c_out_2);
    return false;
  }

  private boolean _jspx_meth_html_link_0(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_0 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_name_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_0.setPageContext(pageContext);
    _jspx_th_html_link_0.setParent(_jspx_th_c_forEach_0);
    _jspx_th_html_link_0.setForwardExpr("forumMessage");
    _jspx_th_html_link_0.setNameExpr("editParams");
    int _jspx_eval_html_link_0 = _jspx_th_html_link_0.doStartTag();
    if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        _jspx_push_body_count_c_forEach_0++;
        out = _bc;
        _jspx_th_html_link_0.setBodyContent(_bc);
        _jspx_th_html_link_0.doInitBody();
      }
      do {
        if (_jspx_meth_fmt_message_3(_jspx_th_html_link_0, pageContext, _jspx_push_body_count_c_forEach_0))
          return true;
        int evalDoAfterBody = _jspx_th_html_link_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
        _jspx_push_body_count_c_forEach_0--;
    }
    if (_jspx_th_html_link_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_name_forward.reuse(_jspx_th_html_link_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_3(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_3 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_3.setPageContext(pageContext);
    _jspx_th_fmt_message_3.setParent(_jspx_th_html_link_0);
    _jspx_th_fmt_message_3.setKey("label_forum_delete");
    int _jspx_eval_fmt_message_3 = _jspx_th_fmt_message_3.doStartTag();
    if (_jspx_th_fmt_message_3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_3);
    return false;
  }

  private boolean _jspx_meth_html_link_1(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_1 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_name_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_1.setPageContext(pageContext);
    _jspx_th_html_link_1.setParent(_jspx_th_c_forEach_0);
    _jspx_th_html_link_1.setForwardExpr("forumMessage");
    _jspx_th_html_link_1.setNameExpr("editParams");
    int _jspx_eval_html_link_1 = _jspx_th_html_link_1.doStartTag();
    if (_jspx_eval_html_link_1 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_1 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        _jspx_push_body_count_c_forEach_0++;
        out = _bc;
        _jspx_th_html_link_1.setBodyContent(_bc);
        _jspx_th_html_link_1.doInitBody();
      }
      do {
        if (_jspx_meth_fmt_message_4(_jspx_th_html_link_1, pageContext, _jspx_push_body_count_c_forEach_0))
          return true;
        int evalDoAfterBody = _jspx_th_html_link_1.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_1 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
        _jspx_push_body_count_c_forEach_0--;
    }
    if (_jspx_th_html_link_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_name_forward.reuse(_jspx_th_html_link_1);
    return false;
  }

  private boolean _jspx_meth_fmt_message_4(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_1, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_4 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_4.setPageContext(pageContext);
    _jspx_th_fmt_message_4.setParent(_jspx_th_html_link_1);
    _jspx_th_fmt_message_4.setKey("label_forum_update");
    int _jspx_eval_fmt_message_4 = _jspx_th_fmt_message_4.doStartTag();
    if (_jspx_th_fmt_message_4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_4);
    return false;
  }

  private boolean _jspx_meth_html_link_2(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_2 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_paramName_paramId_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_2.setPageContext(pageContext);
    _jspx_th_html_link_2.setParent(null);
    _jspx_th_html_link_2.setForwardExpr("postAMessage");
    _jspx_th_html_link_2.setParamIdExpr("id");
    _jspx_th_html_link_2.setParamNameExpr("parentId");
    int _jspx_eval_html_link_2 = _jspx_th_html_link_2.doStartTag();
    if (_jspx_eval_html_link_2 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_2 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        out = _bc;
        _jspx_th_html_link_2.setBodyContent(_bc);
        _jspx_th_html_link_2.doInitBody();
      }
      do {
        out.write("\r\n\t\t");
        if (_jspx_meth_fmt_message_5(_jspx_th_html_link_2, pageContext))
          return true;
        out.write("\r\n\t");
        int evalDoAfterBody = _jspx_th_html_link_2.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_2 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
    }
    if (_jspx_th_html_link_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_paramName_paramId_forward.reuse(_jspx_th_html_link_2);
    return false;
  }

  private boolean _jspx_meth_fmt_message_5(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_2, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_5 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_5.setPageContext(pageContext);
    _jspx_th_fmt_message_5.setParent(_jspx_th_html_link_2);
    _jspx_th_fmt_message_5.setKey("label_forum_add_a_message");
    int _jspx_eval_fmt_message_5 = _jspx_th_fmt_message_5.doStartTag();
    if (_jspx_th_fmt_message_5.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_5);
    return false;
  }
}
