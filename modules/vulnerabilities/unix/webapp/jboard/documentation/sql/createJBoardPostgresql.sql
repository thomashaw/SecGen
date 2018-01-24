create table forummessage (message_id INT8 not null, message VARCHAR(255), viewsnumber INT8, creationdate TIMESTAMP, user_id INT8, fk_parent_id INT8, primary key (message_id))
create table user_role (user_id INT8 not null, role_id INT8 not null, primary key (role_id, user_id))
create table usertable (id INT8 not null, username VARCHAR(255), password VARCHAR(255), firstName VARCHAR(255), lastName VARCHAR(255), icq INT4, aim VARCHAR(255), msn VARCHAR(255), yahoo VARCHAR(255), webSite VARCHAR(255), localisation VARCHAR(255), job VARCHAR(255), hobbies VARCHAR(255), signature VARCHAR(255), primary key (id))
create table forumcategory (cat_id INT8 not null, cattitle VARCHAR(255), catdescription VARCHAR(255), componentposition INT8, creationdate TIMESTAMP, primary key (cat_id))
create table forumitem (forumitem_id INT8 not null, forumitemtitle VARCHAR(255), forumitemdescription VARCHAR(255), componentposition INT8, creationdate TIMESTAMP, fk_parent_id INT8, primary key (forumitem_id))
create table role (id INT8 not null, rolename VARCHAR(255), primary key (id))
create table forumthread (forumthread_id INT8 not null, forumthreadtitle VARCHAR(255), componentposition INT8, creationdate TIMESTAMP, fk_parent_id INT8, primary key (forumthread_id))
alter table forummessage add constraint FK9FB79BC66F0645D6 foreign key (fk_parent_id) references forumthread
alter table user_role add constraint FK143BF46A52119584 foreign key (role_id) references role
alter table user_role add constraint FK143BF46AF73AEE0F foreign key (user_id) references usertable
alter table forumitem add constraint FK7A458FB46F0645D6 foreign key (fk_parent_id) references forumcategory
alter table forumthread add constraint FK11427E6B6F0645D6 foreign key (fk_parent_id) references forumitem
create sequence role_seq
create sequence forummessage_seq
create sequence forumthread_seq
create sequence forumcategory_seq
create sequence user_seq
create sequence forumitem_seq
