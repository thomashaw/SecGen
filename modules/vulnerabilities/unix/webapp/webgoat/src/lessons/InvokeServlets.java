//package lessons;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import MyMockLib.MyHttpServletRequest;
import MyMockLib.MyHttpServletResponse;
//import lessons.*;
//import WebSession;

// Imports for servlets
public class InvokeServlets {
    public static void main(String[] args) throws IOException {
        processServlets();
    }

    public static void processServlets() {
        try {
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
            
	    {
                AbstractLesson lesson = new ChallengeScreen();

                lesson.start(s);
                lesson.createContent(s);
            }
            {
                AbstractLesson lesson = new DatabaseXss();

                lesson.start(s);
                lesson.createContent(s);
            }

	    /*
            {
                AbstractLesson lesson = new BufferOverflow();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new CommandInjection();

                lesson.start(s);
                lesson.createContent(s);
	    }*/

            {
                AbstractLesson lesson = new ParameterInjection();

                lesson.start(s);
                lesson.createContent(s);
            }

	    /*
            {
                AbstractLesson lesson = new DOS_Login();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new Encoding();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new ForcedBrowsing();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new ForgetPassword();

                lesson.start(s);
                lesson.createContent(s);
            }*/

            {
                AbstractLesson lesson = new HiddenFieldTampering();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new HttpBasics();

                lesson.start(s);
                lesson.createContent(s);
            }

	    /*
            {
                AbstractLesson lesson = new JavaScriptValidation();

                lesson.start(s);
                lesson.createContent(s);
	    }

            {
                AbstractLesson lesson = new PathBasedAccessControl();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new ProductAdminScreen();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new ReflectedXSS();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new RefreshDBScreen();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new RemoteAdminFlaw();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new ReportCardScreen();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new RoleBasedAccessControl();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new SummaryReportCardScreen();

                lesson.start(s);
                lesson.createContent(s);
            }*/

            {
                AbstractLesson lesson = new ThreadSafetyProblem();

                lesson.start(s);
                lesson.createContent(s);
            }


            {
                AbstractLesson lesson = new UncheckedEmail();

                lesson.start(s);
                lesson.createContent(s);
            }

	    /*
            {
                AbstractLesson lesson = new UserAdminScreen();

                lesson.start(s);
                lesson.createContent(s);
	    }*/

            {
                AbstractLesson lesson = new ViewDatabase();

                lesson.start(s);
                lesson.createContent(s);
            }

            {
                AbstractLesson lesson = new WeakAuthenticationCookie();

                lesson.start(s);
                lesson.createContent(s);
            }
        } catch ( Exception e ) {

        }

    }
}


