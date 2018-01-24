<%@ taglib uri="/tags/struts/html-el" prefix="html" %>
<html:xhtml/>
le body du module forum

<html:form action="ForumCategory?method=create" method="POST">
<html:link action="PostAMessage">
  post a message
 </html:link>
 
<html:link action="ForumCategory?method=create">
  create a category 
 </html:link> 
 <html:link action="ForumItem?method=create">
  creer a forumItem
 </html:link> 
 <html:link action="ForumThread?method=create">
  creer a ForumThread
 </html:link> 
 <html:link action="ForumMessage?method=create">
  post a Message
 </html:link> 
 <input type="text" id="title" name="title" size="10" />
 <input type="text" id="description" name="description" size="50" />
 <input type="submit" id ="button" name ="button" value="post a message" />
 </html:form>