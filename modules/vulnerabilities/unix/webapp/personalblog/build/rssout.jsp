<%response.setContentType("text/xml");%><?xml version="1.0" encoding="ISO-8859-1"?>
<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*,net.eyde.personalblog.service.PersonalBlogService"%>

<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%!
    String cleanXML(String data)
    {
	    StringBuffer retval = new StringBuffer("");

    	int length=data.length();
    	char c;
    	for (int i=0;i<length;i++)
    	{
    	    switch(c=data.charAt(i))
    	    {
    	    case '&':
        		retval.append("&amp;");
        		break;
    	    case '<':
        		retval.append("&lt;");
        		break;
    	    case '>':
        		retval.append("&gt;");
        		break;
    	    case '\'':
        		retval.append("&apos;");
        		break;
    	    case '\"':
        		retval.append("&quot;");
        		break;
    	    default:
    		    retval.append(c);
    	    }

    	}

// RSS Specs say that the tag should only have a max of 500 chars.
        String output="";
                output = retval.toString();
        return output;


    }
%>
<rss version="0.92">
<channel>
<title><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_TITLE) %></title>
<link><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_URL) %></link>
<description><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_DESCRIPTION) %></description>
<language>en-gb</language>

<% ArrayList posts = (ArrayList)request.getAttribute("posts");
  String lastDate = "";
  int count = 0;
  Iterator i = posts.iterator();
  while (i.hasNext() && count < 5){
      count++;
      Post p = (Post)i.next();
%>
<item>
<title><%=p.getTitle()%></title>
<link><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_URL) %>index.do?post=<%=p.getId()%></link>
<description><%=cleanXML(p.getContent())%></description>
</item>
<% } %>

</channel>
</rss>
