UPDATE instructions for mysql :

To update for 1.2.6 (from 1.2.5) I did the following :

1- Using mysqlcc, you will create the property table based on config/db/mysql.xml file (last node)
obs: there's no createtables-*.sql anymore, since the tables are 
	 created by the wizard (first install, update is better done by hand).
2- Table post:
	- change the title lenght to 90.
	- add the field "emailComments int(11)" (we'll have to change this type afterwards)
	  please leave your message at my blog if have something to add, since I've updated my blog a week 
	  ago and don't remember what exactly I changed to get it running
	  
3- keep the old files you must have at your images dir
4 - deploy the new version or
4.1 extract the personalblog.war inside your old instalattion (keep a copy!), I did this way	  