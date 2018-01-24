create table forummessage (message_id NUMBER(19,0) not null, message VARCHAR2(255), viewsnumber NUMBER(19,0), creationdate DATE, user_id NUMBER(19,0), fk_parent_id NUMBER(19,0), primary key (message_id))
create table user_role (user_id NUMBER(19,0) not null, role_id NUMBER(19,0) not null, primary key (role_id, user_id))
create table usertable (id NUMBER(19,0) not null, username VARCHAR2(255), password VARCHAR2(255), firstName VARCHAR2(255), lastName VARCHAR2(255), icq NUMBER(10,0), aim VARCHAR2(255), msn VARCHAR2(255), yahoo VARCHAR2(255), webSite VARCHAR2(255), localisation VARCHAR2(255), job VARCHAR2(255), hobbies VARCHAR2(255), signature VARCHAR2(255), primary key (id))
create table forumcategory (cat_id NUMBER(19,0) not null, cattitle VARCHAR2(255), catdescription VARCHAR2(255), componentposition NUMBER(19,0), creationdate DATE, primary key (cat_id))
create table forumitem (forumitem_id NUMBER(19,0) not null, forumitemtitle VARCHAR2(255), forumitemdescription VARCHAR2(255), componentposition NUMBER(19,0), creationdate DATE, fk_parent_id NUMBER(19,0), primary key (forumitem_id))
create table role (id NUMBER(19,0) not null, rolename VARCHAR2(255), primary key (id))
create table forumthread (forumthread_id NUMBER(19,0) not null, forumthreadtitle VARCHAR2(255), componentposition NUMBER(19,0), creationdate DATE, fk_parent_id NUMBER(19,0), primary key (forumthread_id))
alter table forummessage add constraint FK9FB79BC66F0645D6 foreign key (fk_parent_id) references forumthread
alter table user_role add constraint FK143BF46A52119584 foreign key (role_id) references role
alter table user_role add constraint FK143BF46AF73AEE0F foreign key (user_id) references usertable
alter table forumitem add constraint FK7A458FB46F0645D6 foreign key (fk_parent_id) references forumcategory
alter table forumthread add constraint FK11427E6B6F0645D6 foreign key (fk_parent_id) references forumitem
create sequence role_seq
create sequence forummessage_seq
create sequence forumthread_seq
create sequence user_seq
create sequence forumcategory_seq
create sequence forumitem_seq
