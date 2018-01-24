package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.apache.jasper.runtime.*;
import java.util.HashMap;

public class forumItemContent_jsp extends HttpJspBase {


  private static java.util.Vector _jspx_includes;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_xhtml;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_out_value;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_fmt_message_key;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_set_var_value_scope;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_html_link_name_forward;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_if_test;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_forEach_var_items;

  public forumItemContent_jsp() {
    _jspx_tagPool_html_xhtml = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_out_value = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_fmt_message_key = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_set_var_value_scope = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_html_link_name_forward = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_if_test = new org.apache.jasper.runtime.TagHandlerPool();
    _jspx_tagPool_c_forEach_var_items = new org.apache.jasper.runtime.TagHandlerPool();
  }

  public java.util.List getIncludes() {
    return _jspx_includes;
  }

  public void _jspDestroy() {
    _jspx_tagPool_html_xhtml.release();
    _jspx_tagPool_c_out_value.release();
    _jspx_tagPool_fmt_message_key.release();
    _jspx_tagPool_c_set_var_value_scope.release();
    _jspx_tagPool_html_link_name_forward.release();
    _jspx_tagPool_c_if_test.release();
    _jspx_tagPool_c_forEach_var_items.release();
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

      out.write("\t");
      out.write("\r\n");
      out.write("\r\n");
      out.write("\r\n");
      out.write("\r\n");
      if (_jspx_meth_html_xhtml_0(pageContext))
        return;
      out.write("\r\n");
      out.write("<div class=\"first\">\r\n\t");
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
      out.write("<div class=\"first\">\r\n\t\t  ");
      out.write("<span>");
      if (_jspx_meth_c_out_0(pageContext))
        return;
      out.write("</span>\r\n    ");
      out.write("</div>\r\n    ");
      out.write("<div class=\"first\">\r\n\t\t");
      out.write("<span>");
      if (_jspx_meth_fmt_message_0(pageContext))
        return;
      out.write(":&nbsp;");
      if (_jspx_meth_c_out_1(pageContext))
        return;
      out.write("</span>\r\n\t\t\r\n\t\t ");
      if (_jspx_meth_c_set_0(pageContext))
        return;
      out.write("\r\n\t\t ");
      java.lang.Long parentId = null;
      synchronized (pageContext) {
        parentId = (java.lang.Long) pageContext.getAttribute("parentId", PageContext.PAGE_SCOPE);
        if (parentId == null){
          try {
            parentId = (java.lang.Long) java.beans.Beans.instantiate(this.getClass().getClassLoader(), "java.lang.Long");
          } catch (ClassNotFoundException exc) {
            throw new InstantiationException(exc.getMessage());
          } catch (Exception exc) {
            throw new ServletException("Cannot create bean of class " + "java.lang.Long", exc);
          }
          pageContext.setAttribute("parentId", parentId, PageContext.PAGE_SCOPE);
        }
      }
      out.write("\r\n\t\t ");
 editParams.put("parentId", parentId);  
      out.write("\r\n\t\t");
      out.write("<span>");
      if (_jspx_meth_html_link_0(pageContext))
        return;
      out.write("</span>\r\n\r\n\t");
      out.write("</div>\r\n\t");
      out.write("<div class=\"first\">\r\n\r\n\t\t     ");
      if (_jspx_meth_c_if_0(pageContext))
        return;
      out.write("\r\n   \r\n\t\t");
 editParams.put("method","read");  
      out.write("\r\n\t\t\r\n\t\t");
      /* ----  c:forEach ---- */
      org.apache.taglibs.standard.tag.el.core.ForEachTag _jspx_th_c_forEach_0 = (org.apache.taglibs.standard.tag.el.core.ForEachTag) _jspx_tagPool_c_forEach_var_items.get(org.apache.taglibs.standard.tag.el.core.ForEachTag.class);
      _jspx_th_c_forEach_0.setPageContext(pageContext);
      _jspx_th_c_forEach_0.setParent(null);
      _jspx_th_c_forEach_0.setItems("${forumForm.map.forumItem.forumThreads}");
      _jspx_th_c_forEach_0.setVar("forumThread");
      int _jspx_push_body_count_c_forEach_0 = 0;
      try {
        int _jspx_eval_c_forEach_0 = _jspx_th_c_forEach_0.doStartTag();
        if (_jspx_eval_c_forEach_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
          do {
            out.write("\r\n\t\t\t");
            out.write("<div class=\"second\">\r\n\t\t      ");
 editParams.put("method","read");  
            out.write("\r\n\t\t\t ");
            if (_jspx_meth_c_set_1(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write(" \r\n\t\t\t ");
            java.lang.Long id = null;
            synchronized (pageContext) {
              id = (java.lang.Long) pageContext.getAttribute("id", PageContext.PAGE_SCOPE);
              if (id == null){
                throw new java.lang.InstantiationException("bean id not found within scope");
              }
            }
            out.write("\r\n\t\t\t ");
 editParams.put("id",id);  
            out.write("\r\n\r\n\t\t\t\t");
            out.write("<div class=\"third\">\r\n\t\t\t\t");
            out.write("<span>X");
            out.write("</span>");
            out.write("<span> ");
            if (_jspx_meth_html_link_1(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("</span>\r\n\t\t\t\t");
            out.write("<!-- update link -->\r\n\t\t   \t\t");
 editParams.put("method","beforeUpdate");  
            out.write("\r\n\t\t  \t\t");
            out.write("<span>");
            if (_jspx_meth_html_link_2(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("</span>\r\n\t\t  \t\t");
            out.write("<!-- end update link -->\r\n\t\t  \t\t");
            out.write("<!-- update link -->\r\n\t\t   \t\t");
 editParams.put("method","delete");  
            out.write("\r\n\t\t  \t\t");
            out.write("<span>");
            if (_jspx_meth_html_link_3(_jspx_th_c_forEach_0, pageContext, _jspx_push_body_count_c_forEach_0))
              return;
            out.write("</span>\r\n\t\t  \t\t");
            out.write("<!-- end update link -->\r\n\t\t  \t\t");
            out.write("</div>\r\n\t\t  \t");
            out.write("</div>\r\n\t\t");
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
      out.write("</div>\r\n");
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

  private boolean _jspx_meth_c_out_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:out ---- */
    org.apache.taglibs.standard.tag.el.core.OutTag _jspx_th_c_out_0 = (org.apache.taglibs.standard.tag.el.core.OutTag) _jspx_tagPool_c_out_value.get(org.apache.taglibs.standard.tag.el.core.OutTag.class);
    _jspx_th_c_out_0.setPageContext(pageContext);
    _jspx_th_c_out_0.setParent(null);
    _jspx_th_c_out_0.setValue("${forumForm.map.forumItem.title}");
    int _jspx_eval_c_out_0 = _jspx_th_c_out_0.doStartTag();
    if (_jspx_th_c_out_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_out_value.reuse(_jspx_th_c_out_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_0 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_0.setPageContext(pageContext);
    _jspx_th_fmt_message_0.setParent(null);
    _jspx_th_fmt_message_0.setKey("label_forum_description");
    int _jspx_eval_fmt_message_0 = _jspx_th_fmt_message_0.doStartTag();
    if (_jspx_th_fmt_message_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_0);
    return false;
  }

  private boolean _jspx_meth_c_out_1(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:out ---- */
    org.apache.taglibs.standard.tag.el.core.OutTag _jspx_th_c_out_1 = (org.apache.taglibs.standard.tag.el.core.OutTag) _jspx_tagPool_c_out_value.get(org.apache.taglibs.standard.tag.el.core.OutTag.class);
    _jspx_th_c_out_1.setPageContext(pageContext);
    _jspx_th_c_out_1.setParent(null);
    _jspx_th_c_out_1.setValue("${forumForm.map.forumItem.description}");
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
    _jspx_th_c_set_0.setValue("${forumForm.map.forumItem.id}");
    _jspx_th_c_set_0.setScope("page");
    int _jspx_eval_c_set_0 = _jspx_th_c_set_0.doStartTag();
    if (_jspx_th_c_set_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_set_var_value_scope.reuse(_jspx_th_c_set_0);
    return false;
  }

  private boolean _jspx_meth_html_link_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_0 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_name_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_0.setPageContext(pageContext);
    _jspx_th_html_link_0.setParent(null);
    _jspx_th_html_link_0.setForwardExpr("postAForumThread");
    _jspx_th_html_link_0.setNameExpr("editParams");
    int _jspx_eval_html_link_0 = _jspx_th_html_link_0.doStartTag();
    if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        out = _bc;
        _jspx_th_html_link_0.setBodyContent(_bc);
        _jspx_th_html_link_0.doInitBody();
      }
      do {
        if (_jspx_meth_fmt_message_1(_jspx_th_html_link_0, pageContext))
          return true;
        int evalDoAfterBody = _jspx_th_html_link_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_0 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
    }
    if (_jspx_th_html_link_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_name_forward.reuse(_jspx_th_html_link_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_1(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_1 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_1.setPageContext(pageContext);
    _jspx_th_fmt_message_1.setParent(_jspx_th_html_link_0);
    _jspx_th_fmt_message_1.setKey("label_forum_add_a_forum_thread");
    int _jspx_eval_fmt_message_1 = _jspx_th_fmt_message_1.doStartTag();
    if (_jspx_th_fmt_message_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_1);
    return false;
  }

  private boolean _jspx_meth_c_if_0(javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:if ---- */
    org.apache.taglibs.standard.tag.el.core.IfTag _jspx_th_c_if_0 = (org.apache.taglibs.standard.tag.el.core.IfTag) _jspx_tagPool_c_if_test.get(org.apache.taglibs.standard.tag.el.core.IfTag.class);
    _jspx_th_c_if_0.setPageContext(pageContext);
    _jspx_th_c_if_0.setParent(null);
    _jspx_th_c_if_0.setTest("${empty forumForm.map.forumItem.forumThreads}");
    int _jspx_eval_c_if_0 = _jspx_th_c_if_0.doStartTag();
    if (_jspx_eval_c_if_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      do {
        out.write("\r\n\t\t       ");
        out.write("<div class=\"empty\">\t\r\n\t\t         ");
        if (_jspx_meth_fmt_message_2(_jspx_th_c_if_0, pageContext))
          return true;
        out.write("    \r\n\t           ");
        out.write("</div>      \r\n\t\t     ");
        int evalDoAfterBody = _jspx_th_c_if_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
    }
    if (_jspx_th_c_if_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_if_test.reuse(_jspx_th_c_if_0);
    return false;
  }

  private boolean _jspx_meth_fmt_message_2(javax.servlet.jsp.tagext.Tag _jspx_th_c_if_0, javax.servlet.jsp.PageContext pageContext)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_2 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_2.setPageContext(pageContext);
    _jspx_th_fmt_message_2.setParent(_jspx_th_c_if_0);
    _jspx_th_fmt_message_2.setKey("label_forum_there_is_no_forum_threads");
    int _jspx_eval_fmt_message_2 = _jspx_th_fmt_message_2.doStartTag();
    if (_jspx_th_fmt_message_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_2);
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
    _jspx_th_c_set_1.setValue("${forumThread.id}");
    _jspx_th_c_set_1.setScope("page");
    int _jspx_eval_c_set_1 = _jspx_th_c_set_1.doStartTag();
    if (_jspx_th_c_set_1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_set_var_value_scope.reuse(_jspx_th_c_set_1);
    return false;
  }

  private boolean _jspx_meth_html_link_1(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_1 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_name_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_1.setPageContext(pageContext);
    _jspx_th_html_link_1.setParent(_jspx_th_c_forEach_0);
    _jspx_th_html_link_1.setForwardExpr("forumThread");
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
        if (_jspx_meth_c_out_2(_jspx_th_html_link_1, pageContext, _jspx_push_body_count_c_forEach_0))
          return true;
        out.write(" ");
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

  private boolean _jspx_meth_c_out_2(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_1, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  c:out ---- */
    org.apache.taglibs.standard.tag.el.core.OutTag _jspx_th_c_out_2 = (org.apache.taglibs.standard.tag.el.core.OutTag) _jspx_tagPool_c_out_value.get(org.apache.taglibs.standard.tag.el.core.OutTag.class);
    _jspx_th_c_out_2.setPageContext(pageContext);
    _jspx_th_c_out_2.setParent(_jspx_th_html_link_1);
    _jspx_th_c_out_2.setValue("${forumThread.title}");
    int _jspx_eval_c_out_2 = _jspx_th_c_out_2.doStartTag();
    if (_jspx_th_c_out_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_c_out_value.reuse(_jspx_th_c_out_2);
    return false;
  }

  private boolean _jspx_meth_html_link_2(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_2 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_name_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_2.setPageContext(pageContext);
    _jspx_th_html_link_2.setParent(_jspx_th_c_forEach_0);
    _jspx_th_html_link_2.setForwardExpr("forumThread");
    _jspx_th_html_link_2.setNameExpr("editParams");
    int _jspx_eval_html_link_2 = _jspx_th_html_link_2.doStartTag();
    if (_jspx_eval_html_link_2 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_2 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        _jspx_push_body_count_c_forEach_0++;
        out = _bc;
        _jspx_th_html_link_2.setBodyContent(_bc);
        _jspx_th_html_link_2.doInitBody();
      }
      do {
        if (_jspx_meth_fmt_message_3(_jspx_th_html_link_2, pageContext, _jspx_push_body_count_c_forEach_0))
          return true;
        int evalDoAfterBody = _jspx_th_html_link_2.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_2 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
        _jspx_push_body_count_c_forEach_0--;
    }
    if (_jspx_th_html_link_2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_name_forward.reuse(_jspx_th_html_link_2);
    return false;
  }

  private boolean _jspx_meth_fmt_message_3(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_2, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_3 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_3.setPageContext(pageContext);
    _jspx_th_fmt_message_3.setParent(_jspx_th_html_link_2);
    _jspx_th_fmt_message_3.setKey("label_forum_update");
    int _jspx_eval_fmt_message_3 = _jspx_th_fmt_message_3.doStartTag();
    if (_jspx_th_fmt_message_3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_3);
    return false;
  }

  private boolean _jspx_meth_html_link_3(javax.servlet.jsp.tagext.Tag _jspx_th_c_forEach_0, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  html:link ---- */
    org.apache.strutsel.taglib.html.ELLinkTag _jspx_th_html_link_3 = (org.apache.strutsel.taglib.html.ELLinkTag) _jspx_tagPool_html_link_name_forward.get(org.apache.strutsel.taglib.html.ELLinkTag.class);
    _jspx_th_html_link_3.setPageContext(pageContext);
    _jspx_th_html_link_3.setParent(_jspx_th_c_forEach_0);
    _jspx_th_html_link_3.setForwardExpr("forumThread");
    _jspx_th_html_link_3.setNameExpr("editParams");
    int _jspx_eval_html_link_3 = _jspx_th_html_link_3.doStartTag();
    if (_jspx_eval_html_link_3 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      if (_jspx_eval_html_link_3 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {
        javax.servlet.jsp.tagext.BodyContent _bc = pageContext.pushBody();
        _jspx_push_body_count_c_forEach_0++;
        out = _bc;
        _jspx_th_html_link_3.setBodyContent(_bc);
        _jspx_th_html_link_3.doInitBody();
      }
      do {
        if (_jspx_meth_fmt_message_4(_jspx_th_html_link_3, pageContext, _jspx_push_body_count_c_forEach_0))
          return true;
        int evalDoAfterBody = _jspx_th_html_link_3.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
      if (_jspx_eval_html_link_3 != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
        out = pageContext.popBody();
        _jspx_push_body_count_c_forEach_0--;
    }
    if (_jspx_th_html_link_3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_html_link_name_forward.reuse(_jspx_th_html_link_3);
    return false;
  }

  private boolean _jspx_meth_fmt_message_4(javax.servlet.jsp.tagext.Tag _jspx_th_html_link_3, javax.servlet.jsp.PageContext pageContext, int _jspx_push_body_count_c_forEach_0)
          throws Throwable {
    JspWriter out = pageContext.getOut();
    /* ----  fmt:message ---- */
    org.apache.taglibs.standard.tag.el.fmt.MessageTag _jspx_th_fmt_message_4 = (org.apache.taglibs.standard.tag.el.fmt.MessageTag) _jspx_tagPool_fmt_message_key.get(org.apache.taglibs.standard.tag.el.fmt.MessageTag.class);
    _jspx_th_fmt_message_4.setPageContext(pageContext);
    _jspx_th_fmt_message_4.setParent(_jspx_th_html_link_3);
    _jspx_th_fmt_message_4.setKey("label_forum_delete");
    int _jspx_eval_fmt_message_4 = _jspx_th_fmt_message_4.doStartTag();
    if (_jspx_th_fmt_message_4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
      return true;
    _jspx_tagPool_fmt_message_key.reuse(_jspx_th_fmt_message_4);
    return false;
  }
}
