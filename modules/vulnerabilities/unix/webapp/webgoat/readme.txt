WebGoat
Jeff Williams
jeff.williams@aspectsecurity.com
http://www.aspectsecurity.com
Version 0.9
11/12/2002
------------------------------------------------------

This program is a demonstration of common server-side
application flaws.  The exercises are intended to
be used by people to learn about application penetration
testing techniques.


WARNING 1: While running this program your machine will be 
extremely vulnerable to attack. You want to disconnect
from the Internet while using this program.

WARNING 2: This program is for educational purposes only. If you
attempt these techniques without authorization, you are very
likely to get caught.  If you are caught engaging in unauthorized
hacking, most companies will fire you. Claiming that you were
doing security research will not work as that is the first thing
that all hackers claim.



INSTALLATION:

1. install the Java Development Kit (JDK) from http://java.sun.com/j2se/1.4/
2. set JAVA_HOME=c:\j2sdk1.4  (or wherever you installed it)
3. install the Tomcat application server from http://jakarta.apache.org/
4. drop the WebGoat-*.war file into the "webapps" directory inside the tomcat 
   directory
5. rename WebGoat-*.war to WebGoat.war in the "webapps" directory
6. edit tomcat/web.xml and uncomment the invoker servlet mapping


BUILDING: (skip if you just want to run)

1. install ant from http://jakarta.apache.org
2. put tomcat/server/lib/catalina-ant.jar into your ant/lib directory
3. unpack the WebGoat src distribution
4. modify catalina.home property in build.xml to specify tomcat installation 
   directory
5. add '<user username="webgoat" password="webg0@t" 
        roles="admin,manager,standard,tomcat"/>'
   to the tomcat_home/conf/tomcat-users.xml file
6. type 'ant' to start the compile
7. type 'ant dist' to create a new WebGoat .war file in the dist directory
8. type 'ant install' to install the current build directory in tomcat. This
      does not install the application permanently -- you have to put the
      .war file in the webapps directory  for that.
9. type 'ant reload' to reinstall the current build directory in tomcat


RUNNING:

1. go to the tomcat/bin directory
2. click on startup.bat (set properties on the terminal window to 5000 lines)
3. start your browser and browse to...


http://localhost:8080/WebGoat/attack


