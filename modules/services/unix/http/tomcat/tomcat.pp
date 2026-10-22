# Tomcat 9.0 supports the Java 17 that Debian 12 provides; 7.0.x does not.
tomcat::install { '/opt/tomcat':
  source_url => 'https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.98/bin/apache-tomcat-9.0.98.tar.gz',
}
# manage_service defaults to true in the new module, so tomcat::service starts
# catalina via catalina.sh (nothing else declares the service).
tomcat::instance { 'default':
  catalina_home => '/opt/tomcat',
}
