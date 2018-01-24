	  CREATE TABLE ForumCategory (
	  cat_id INT4 NOT NULL DEFAULT  nextval('ForumCategory_seq'), 
	  catTitle varchar(100) NOT NULL, 
	  catDescription varchar(300) NOT NULL,
	  componentPosition INT4 NOT NULL,
	  CONSTRAINT ForumCategory_pkey PRIMARY KEY (cat_id)
	 );
  CREATE TABLE ForumItem (
  ForumItem_id INT4 NOT NULL DEFAULT nextval('Forumitem_seq'), 
  forumItemTitle varchar(100) NOT NULL, 
  forumItemDescription varchar(300) NOT NULL,
  componentPosition INT4,
  fk_cat_id INT4 NOT NULL,
  CONSTRAINT forumitem_pkey PRIMARY KEY (forumitem_id),
  CONSTRAINT category_exists FOREIGN KEY(fk_cat_id) REFERENCES ForumCategory(cat_id)
  ON DELETE CASCADE ON UPDATE CASCADE
);

 CREATE TABLE ForumThread (
  ForumThread_id INT4 NOT NULL DEFAULT  nextval('ForumThread_seq'),
  forumThreadTitle varchar(100) NOT NULL, 
  fk_forumItem_id INT4 NOT NULL,
  componentPosition INT,
  CONSTRAINT ForumThread_pkey PRIMARY KEY (ForumThread_id),
  CONSTRAINT forumItem_exists  FOREIGN KEY(fk_forumItem_id) REFERENCES ForumItem(ForumItem_id)
  ON DELETE CASCADE ON UPDATE CASCADE
);
 

 CREATE TABLE ForumMessage (
  Message_id INT4 NOT NULL DEFAULT  nextval('ForumMessage_seq'), 
  message varchar(1000) NOT NULL, 
  user_id int4 NOT NULL, 
  creationDate timestamp NOT NULL,
  viewsNumber INT4 NOT NULL,
  componentPosition INT,
  fk_forumThread_id INT4 NOT NULL,
  CONSTRAINT Message_id_pkey PRIMARY KEY (Message_id),
  CONSTRAINT forumItem_exists  FOREIGN KEY(fk_forumThread_id) REFERENCES ForumThread(ForumThread_id)
  ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE UserTable (
 UserID SERIAL NOT NULL DEFAULT  ,
 Password varchar(50) NOT NULL,
 UserName varchar(50)
);

CREATE TABLE RoleMap (
 UserID INT4 NOT NULL  REFERENCES UserTable(UserID) ON DELETE CASCADE ON UPDATE CASCADE,
 RoleID INT4 NOT NULL   REFERENCES Role(RoleID) ON DELETE CASCADE ON UPDATE CASCADE,
 PRIMARY KEY(UserID,RoleID) 
 );

CREATE TABLE Role  (
RoleID INT4 NOT NULL DEFAULT DEFAULT nextval('Role_seq'),
RoleName varchar(50)
);

CREATE TABLE ForumUsers (
 pseudonym varchar(50) NOT NULL DEFAULT,
 password varchar(50),
 firstName varchar(50),
 lastName varchar(50),
 icq INT4 ,
 aim varchar(50),
 msn varchar(50),
 yahoo varchar(50),
 webSite varchar(50),
 localisation varchar(50),
 job varchar(50),
 hobbies varchar(50),
 signature varchar(50),
 CONSTRAINT ForumUsers_pkey PRIMARY KEY (pseudonym),
 );



CREATE SEQUENCE ForumCategory_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE ForumItem_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE ForumThread_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE ForumMessage_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE User_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE Role_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE RoleMap_seq INCREMENT 1 minvalue 1;
CREATE SEQUENCE ForumUsers_seq INCREMENT 1 minvalue 1;

create view newThread as select * from ForumThread ft, ForumMessage msg  where ft.forumThread_id = msg.fk_forumthread_id ;
