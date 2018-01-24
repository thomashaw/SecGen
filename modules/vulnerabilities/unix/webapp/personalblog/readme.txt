Welcome to PersonalBlog README. Here are the general steps required to get personalblog running:



1. Create an empty database at MySql (hsqldb is no longer supported, as I loose all data using it :)

2. Change the config/resources/hibernate.properties to reflect the database you just created

3. 	Make sure you have CATALINA_HOME set, log4j uses it to discover the log dir

4. 	To use deploy by ant, you have to make sure you Ant classpath has catalina-ant.jar.
   	It's under tomcat/server/lib
   	If your server supports remote deploy, you can set a parameter to ant e.g. : -Dhost=localhost, this will the host to deploy to
   	You must make sure tomcat-users.xml is configured to let tomcat user and tomcat as password, of course it's to change that :) if you do, change in the build.properties too

5. At your first access, if you configured corretly the hibernate.properties file, the personalblog installation wizard will tell you that it'll create the tables.

5. The next step is configuring the properties, there's a lot of them, so make sure it's correctly configured :

	- Email properties : this properties are for sending an email when someone comments a post (if you configured the post to do this)
	- Weblog properties : this are 
	...
	   
6. 	You can change the log level changing the config/log4j/log4j.xml file, and modifing the priority attribute. Default value is debug (debug,info,warn,error,fatal). 
	<priority value="debug" />


