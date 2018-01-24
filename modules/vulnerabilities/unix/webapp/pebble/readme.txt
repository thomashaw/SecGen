Pebble 1.6-beta1
----------------
This ZIP file represents the Pebble 1.6-beta1 distribution and contains the following applications/components.

 - Pebble weblog
 - Pebble deskblog (alpha version)
 - Pebble moblog (alpha version)
 - Pebble Ant tasks
 - Documentation and javadocs

Pebble weblog
-------------
The documentation contains complete details of how to install and configure the Pebble weblog, but if you're in a hurry, here's a summary.

 (1) Take the pebble.war file from the pebble-weblog directory and deploy it to your web container.
 (2) Define one or more users in your web container with the security roles blog-owner, blog-contributor and pebble-admin.
 (3) Restart your web container and point your browser to the deployed web application.

Pebble deskblog
---------------
Pebble deskblog is a pure Java desktop client written in Swing. To run, just double-click the pebble-deskblog.jar file.

Pebble moblog
-------------
Pebble moblog is written to run on MIDP 1.0 compatible devices and has been tested on the Nokia series 60 phones. To install moblog, simply sent the pebble-moblog.jar file from the pebble-moblog directory to your device. Depending on the device, installation should automatically begin.

Pebble antblog
---------------
The pebble-antblog directory contains the files needed to post to your Blogger/MetaWeblog API compatible blog from Apache Ant. Just place the pebble-antblog.jar file on the Ant classpath and use the tasks as shown in the sample Ant build script (pebble-antblog.xml).

Getting help and support
------------------------
Please use the mailing list, pebble-users@lists.sourceforge.net, for help and support. 