//package lessons;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

//import org.easymock.MockControl;
//import com.mockobjects.servlet.MockHttpServletRequest;
//import com.mockobjects.servlet.MockHttpServletResponse;

import java.io.IOException;
import MyMockLib.MyHttpServletRequest;
import MyMockLib.MyHttpServletResponse;
//import lessons.SqlInjection;
//import WebSession;

// Imports for servlets
public class InvokeServlets {
    public static void main(String[] args) throws IOException {
	processServlets();
    }

    public static void processServlets() { 
        try {
            //MockHttpServletRequest request   = new MockHttpServletRequest();
	    //request.setupAddParameter("paramName", new String[] {"value1", "value2"} );
	    MyHttpServletRequest request   = new MyHttpServletRequest();
            MyHttpServletResponse response = new MyHttpServletResponse();

	    doServe(request, response);
	} catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doServe(HttpServletRequest request, HttpServletResponse response) throws IOException, javax.servlet.ServletException {
	HammerHead servlet = new HammerHead();

	servlet.service(request, response);

	WebSession s = new WebSession(servlet, null);

	SqlInjection inj = new SqlInjection();

	inj.start(s);
	inj.createContent(s);


	try {
	    String classNames[] = {
		"ChallengeScreen", "DatabaseXss", "BufferOverflow", "CommandInjection", "ParameterInjection", 
		"DatabaseXss", "DOS_Login", "Encoding", "ForcedBrowsing", "ForgetPassword", "HiddenFieldTampering", 
		"HttpBasics", "JavaScriptValidation", "PathBasedAccessControl", "ProductAdminScreen", "ReflectedXSS",
		"RefreshDBScreen", "RemoteAdminFlaw", "ReportCardScreen", "RoleBasedAccessControl", "SummaryReportCardScreen", 
		"ThreadSafetyProblem", "UncheckedEmail", "UserAdminScreen", "ViewDatabase", "WeakAuthenticationCookie"
	    };
	    int i = 10;
	    String className = "DatabaseXss";
	    if ( i > 3 ) {
		className = classNames[i-2];
	    }
	    AbstractLesson lesson = (AbstractLesson) Class.forName(className).newInstance();
	
	    lesson.start(s);
	    lesson.createContent(s);
	    
	} catch ( Exception e ) {

	}

    }
}
