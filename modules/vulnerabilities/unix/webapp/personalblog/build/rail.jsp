<%@ page language="java" import="java.util.*,net.eyde.personalblog.beans.*,net.eyde.personalblog.service.PersonalBlogService"%>
 
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>


<div id="left">

<center><img src="<%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_PICTURE) %>" border="1"><p>
<a href="mailto:<%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_EMAIL) %>"><%=PersonalBlogService.getInstance().getPropertyManager().getProperty(PersonalBlogService.WEBLOG_EMAIL) %></a><br />
</center>
<center><a href="index.do"><h3>Home</h3></a></center>

<div align="center" class="calendar">

<script language="JavaScript">
<!-- Begin -->

var event_day = new Array(31);

<% String caldate = (String)request.getAttribute("currMonth");
   String year1 = caldate.substring(0, 4);
   String month1 = caldate.substring(5, 7);
%>

<% ArrayList calActions = (ArrayList)request.getAttribute("cal");
  Iterator ite = calActions.iterator();
  while (ite.hasNext()){
      CalendarAction calAct = (CalendarAction)ite.next();
%>
   event_day[<%=calAct.getDay()%>]="<%=calAct.getUrl()%>";
<% } %>


var day_of_week = new Array('Sun','Mon','Tue','Wed','Thu','Fri','Sat');
var month_of_year = new Array('January','February','March','April','May','June','July','August','September','October','November','December');

//  DECLARE AND INITIALIZE VARIABLES
var Calendar = new Date();

var year = "<%=request.getAttribute("currMonth")%>".substr(0,4);// Returns year
var real_year = Calendar.getYear();    // Returns month (0-11)

var month = "<%=request.getAttribute("currMonth")%>".substr(5,2)-1;
var curr_month = Calendar.getMonth();    // Returns month (0-11)


var today = "<%=request.getAttribute("currMonth")%>".substr(8,2);    // Returns day (1-31)
var real_day = Calendar.getDate();    // Returns day (1-31)

var weekday = Calendar.getDay();    // Returns day (1-31)

var DAYS_OF_WEEK = 7;    // "constant" for number of days in a week
var DAYS_OF_MONTH = 31;    // "constant" for number of days in a month
var cal;    // Used for printing

Calendar.setDate(1);    // Start the calendar day at '1'
Calendar.setMonth(month);    // Start the calendar month at now


var TR_start = '<TR>';
var TR_end = '</TR>';
var highlight_start = '<TD WIDTH="30"><TABLE CELLSPACING=0 BORDER=1 BGCOLOR=DEDEFF BORDERCOLOR=CCCCCC><TR><TD WIDTH=20><B><CENTER>';
var highlight_end   = '</CENTER></TD></TR></TABLE></B>';
var TD_start = '<TD WIDTH="30"><CENTER>';
var TD_end = '</CENTER></TD>';

cal =  '<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0 BORDERCOLOR=BBBBBB><TR><TD>';
cal += '<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=2>' + TR_start;
cal += '<TD COLSPAN="' + DAYS_OF_WEEK + '" BGCOLOR="#EFEFEF"><CENTER><B><a style="calendarhead" href="index.do;jsessionid=<%=session.getId()%>?month=<%=request.getAttribute("prevMonth")%>">&lt;&lt;</a>';

cal += '&nbsp;&nbsp;' + month_of_year[month]  + '&nbsp;&nbsp;' + year + '&nbsp;&nbsp;<a style="calendarhead" href="index.do;jsessionid=<%=session.getId()%>?month=<%=request.getAttribute("nextMonth")%>">&gt;&gt;</a></B>' + TD_end + TR_end;
cal += TR_start;

// LOOPS FOR EACH DAY OF WEEK
for(index=0; index < DAYS_OF_WEEK; index++)
{

cal += TD_start + day_of_week[index] + TD_end;
}

cal += TD_end + TR_end;
cal += TR_start;

// FILL IN BLANK GAPS UNTIL TODAY'S DAY
for(index=0; index < Calendar.getDay(); index++)
cal += TD_start + '&nbsp; ' + TD_end;

// LOOPS FOR EACH DAY IN CALENDAR
for(index=0; index < DAYS_OF_MONTH; index++)
{
if( Calendar.getDate() > index )
{
  // RETURNS THE NEXT DAY TO PRINT
  week_day =Calendar.getDay();

  // START NEW ROW FOR FIRST DAY OF WEEK
  if(week_day == 0)
  cal += TR_start;

  if(week_day != DAYS_OF_WEEK)
  {

  // SET VARIABLE INSIDE LOOP FOR INCREMENTING PURPOSES
  var day  = Calendar.getDate();
   if (event_day[day]!=undefined) 
		  var dayStr='<a href="'+event_day[day]+'">'+day+'</a>';  
  else
		  dayStr=day;  


  // HIGHLIGHT TODAY'S DATE
  if( real_day==Calendar.getDate() && month==curr_month && year==real_year)
  cal += highlight_start + dayStr + highlight_end + TD_end;

  // PRINTS DAY
  else
  cal += TD_start + dayStr + TD_end;
  }

  // END ROW FOR LAST DAY OF WEEK
  if(week_day == DAYS_OF_WEEK)
  cal += TR_end;
  }

  // INCREMENTS UNTIL END OF THE MONTH
  Calendar.setDate(Calendar.getDate()+1);

}// end for loop

cal += '</TD></TR></TABLE></TABLE>';

//  PRINT CALENDAR
document.write(cal);

//  End -->
</SCRIPT>

</div>

<div class="sidetitle">
<bean:message key="rail.recentcomments"/>
</div>


<% ArrayList comments = (ArrayList)request.getAttribute("recentcomments");
  
  Iterator m = comments.iterator();
  int comcount = 0;
  while (m.hasNext()){
      comcount++;
      if(comcount > 5){
          break;
      }
      Comment c = (Comment)m.next();
%>
<B><%=c.getName()%></B> <bean:message key="rail.commentedto"/><a href="index.do?post=<%=c.getPost().getId()%>"><%=c.getPost().getTitle()%></a><%
        if(c.getContent().length() < 50) {
%>		
           <P><%= c.getContent() %></P><HR>
<%
        } else {
%>		
            <P><%= c.getContent().substring(0,50) %>...</P><hr>
<%
        }
    }
%>

<a href="lastcomments.do?method=executeRecent"><bean:message key="rail.morecomments"/></a>

<div class="sidetitle">
<bean:message key="rail.search"/>
</div>
<html:form action="search.do" method="post">
<html:text property="search" size="20" />
<html:submit value="Search"/>
</html:form>

<div class="sidetitle">
<bean:message key="rail.syndicate"/>/<bean:message key="rail.rss"/>
</div>

<table>
<% ArrayList categories = (ArrayList)getServletContext().getAttribute("cats");
  Iterator i = categories.iterator();
  while (i.hasNext()){
      Category cat = (Category)i.next();
%>
<tr><td><img src="<%=cat.getImage()%>"/></td><td><a href="index.do?cat=<%=cat.getValue()%>"><%=cat.getName()%></a></td><td><a href="rss.jsp?cat=<%=cat.getValue()%>"> <img src="images/rss.gif" border="0" align="left"></a></td></tr>
<% } %>
</table>

<div class="sidetitle">
Alternate Access
</div>

<a href="indexlite.do"><bean:message key="rail.pdafriendlyversion"/></a><br />
<a href=""><bean:message key="rail.avantgochannel"/></a><br />
</div>

<div id="right">
<div class="sidetitle">
	<bean:message key="rail.poweredby"/>
</div>
<a href="http://sourceforge.net/projects/personalblog"><img src="images/personalblog.gif" border="0"></a><br />
<a href="http://sourceforge.net/projects/personalblog"><bean:message key="rail.about"/></a> | <a href="http://sourceforge.net/project/showfiles.php?group_id=77079"><bean:message key="rail.download"/></a>

<div class="sidetitle">
<bean:message key="rail.favorites"/>
</div>
<% Post blogroll = (Post)request.getAttribute("links"); %>
<P><%= (blogroll!=null)?blogroll.getContent():"" %></P>


<div class="sidetitle">
<bean:message key="rail.referrerstoday"/>
</div>

<% 
  ArrayList referers = (ArrayList)getServletContext().getAttribute("refer");

	Iterator it = referers.iterator();
	int itcount = 0;
	while (it.hasNext()) {
		itcount++;
		if(itcount > 100){
			break;
		}
        Referrer ref = (Referrer) it.next();
		String link = ref.getReferrer();
		String linkText = "";
		if(link != null){
			if(link.length() < 20){
				linkText = link;
			} else {
				linkText = link.substring(0,20) + " ("+ref.getCounter()+")";
			}
		}
%>
<a href="<%=link%>"><%=linkText%></a></font><br />
<%}%>


<div class="sidetitle">
<bean:message key="admin.admin"/>
</div>

<% String signedIn = (String)session.getValue("signedIn"); 
if(signedIn == null || !signedIn.equals("true")){ %>
<a href="logonInit.do?method=executeStart"><bean:message key="admin.editthispage"/></a>
<% }else{%>
<a href="postInit.do?method=executeStart"><bean:message key="admin.newpost"/></a><br>

<a href="editInit.do?method=executeStart&editId=<%=blogroll.getId()%>"><bean:message key="admin.editlinks"/></a><br>
<a href="importInit.do?method=executeStart"><bean:message key="admin.import"/></a><br>
<a href="logout.do"><bean:message key="admin.signout"/></a><br>
<a href="propertyInit.do?method=executeStart">Edit Properties</a>

<% } %>

<div class="sidetitle">
<bean:message key="rail.usersaccess"/>
</div>



<table border=0>
<% 		int userscount = 0; 
		HashMap ipLog=(HashMap) request.getAttribute("iplog");
		Set set=ipLog.keySet();
		Iterator iter=set.iterator();
		while (iter.hasNext()) 
		{
			String user= (String) iter.next();
			int useraccesses=((Integer)ipLog.get(user)).intValue();
			userscount=userscount+useraccesses;
			if (user.length()>21) user=user.substring(0,21)+"<br>"+user.substring(21);

			if(signedIn != null && signedIn.equals("true"))
			{ %>
				<tr><td><%=user%></td><td>:<%=useraccesses%></td></tr>
<%			}
		} // while
%>
		<tr><td><b><bean:message key="rail.totalaccess"/>:<%=userscount%></b></td><td>
		</table>

<div class="sidetitle">
<bean:message key="rail.poweredby"/>
</div>
<a href="http://hibernate.sourceforge.net/" target="_blank"><img
    src="http://hibernate.bluemars.net/hib_images/hibernate_logo_a.png"
    border="0" alt="Powered By HIBERNATE"/></a>
<a href="http://jakarta.apache.org/struts/" target="_blank"><img
    src="http://jakarta.apache.org/struts/images/struts-power.gif"
    border="0" alt="Powered By STRUTS/TILES"/></a>
<a href="http://jakarta.apache.org/tomcat/" target="_blank"><img
    src="http://jakarta.apache.org/tomcat/images/tomcat-power.gif"
    border="0"  alt="Powered By TOMCAT"/></a>
    
</div>
    
