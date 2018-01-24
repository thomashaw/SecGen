<%@ page language="java" import="net.eyde.personalblog.beans.Post"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%>


<script language="JavaScript">

  var viewMode = 1; // WYSIWYG

  function Init()
  {
    iView.document.designMode = 'On';
  }
  
  function selOn(ctrl)
  {
	ctrl.style.borderColor = '#000000';
	ctrl.style.backgroundColor = '#B5BED6';
	ctrl.style.cursor = 'hand';	
  }
  
  function selOff(ctrl)
  {
	ctrl.style.borderColor = '#D6D3CE';  
	ctrl.style.backgroundColor = '#D6D3CE';
  }
  
  function selDown(ctrl)
  {
	ctrl.style.backgroundColor = '#8492B5';
  }
  
  function selUp(ctrl)
  {
    ctrl.style.backgroundColor = '#B5BED6';
  }
    
  function doBold()
  {
	iView.document.execCommand('bold', false, null);
  }

  function doItalic()
  {
	iView.document.execCommand('italic', false, null);
  }

  function doUnderline()
  {
	iView.document.execCommand('underline', false, null);
  }
  
  function doLeft()
  {
    iView.document.execCommand('justifyleft', false, null);
  }

  function doCenter()
  {
    iView.document.execCommand('justifycenter', false, null);
  }

  function doRight()
  {
    iView.document.execCommand('justifyright', false, null);
  }

  function doOrdList()
  {
    iView.document.execCommand('insertorderedlist', false, null);
  }

  function doBulList()
  {
    iView.document.execCommand('insertunorderedlist', false, null);
  }
  
  function doForeCol()
  {
    var fCol = prompt('Enter foreground color', '');
    
    if(fCol != null)
      iView.document.execCommand('forecolor', false, fCol);
  }

  function doBackCol()
  {
    var bCol = prompt('Enter background color', '');
    
    if(bCol != null)
      iView.document.execCommand('backcolor', false, bCol);
  }

  function doLink()
  {
    iView.document.execCommand('createlink');
  }
  
  function doImage()
  {
    var imgSrc = prompt('Enter image location', '');
    
    if(imgSrc != null)    
     iView.document.execCommand('insertimage', false, imgSrc);
  }
  
  function doRule()
  {
    iView.document.execCommand('inserthorizontalrule', false, null);
  }
  
  function doFont(fName)
  {
    if(fName != '')
      iView.document.execCommand('fontname', false, fName);
  }
  
  function doSize(fSize)
  {
    if(fSize != '')
      iView.document.execCommand('fontsize', false, fSize);
  }
  
  function doHead(hType)
  {
    if(hType != '')
    {
      iView.document.execCommand('formatblock', false, hType);  
      doFont(newPostForm.selFont.options[newPostForm.selFont.selectedIndex].value);
    }
  }
  
  function doToggleView()
  {  
    if(viewMode == 1)
    {
      iHTML = iView.document.body.innerHTML;
      iView.document.body.innerText = iHTML;
      
      // Hide all controls
      tblCtrls.style.display = 'none';
      newPostForm.selFont.style.display = 'none';
      newPostForm.selSize.style.display = 'none';
      newPostForm.selHeading.style.display = 'none';
      iView.focus();
      
      viewMode = 2; // Code
    }
    else
    {
      iText = iView.document.body.innerText;
      iView.document.body.innerHTML = iText;
      
      // Show all controls
      tblCtrls.style.display = 'inline';
      newPostForm.selFont.style.display = 'inline';
      newPostForm.selSize.style.display = 'inline';
      newPostForm.selHeading.style.display = 'inline';
      iView.focus();
      
      viewMode = 1; // WYSIWYG
    }
  }

  function postWeblogEntry(publish)
  {
    document.newPostForm.content.value = iView.document.body.innerHTML;

  }
</script>

<style>

  .butClass
  {    
    border: 1px solid;
    border-color: #D6D3CE;
  }
  
  .tdClass
  {
    padding-left: 3px;
    padding-top:3px;
  }

</style>

<% Post p = (Post)request.getAttribute("editpost"); %>
<html:hidden property="content" value="<%=p.getContent()%>" /><p>

	<table id="tblCtrls" width="450px" height="30px" border="0" cellspacing="0" cellpadding="0" bgcolor="#D6D3CE">	
	<tr>
		<td class="tdClass">
			<img alt="Bold" class="butClass" src="images/bold.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doBold()">
			<img alt="Italic" class="butClass" src="images/italic.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doItalic()">
			<img alt="Underline" class="butClass" src="images/underline.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doUnderline()">
			
			<img alt="Left" class="butClass" src="images/left.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doLeft()">
			<img alt="Center" class="butClass" src="images/center.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doCenter()">
			<img alt="Right" class="butClass" src="images/right.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doRight()">
						
			<img alt="Ordered List" class="butClass" src="images/ordlist.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doOrdList()">
			<img alt="Bulleted List" class="butClass" src="images/bullist.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doBulList()">
			
			<img alt="Text Color" class="butClass" src="images/forecol.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doForeCol()">
			<img alt="Background Color" class="butClass" src="images/bgcol.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doBackCol()">
			
			<img alt="Hyperlink" class="butClass" src="images/link.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doLink()">
			<img alt="Image" class="butClass" src="images/image.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doImage()">
			<img alt="Horizontal Rule" class="butClass" src="images/rule.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doRule()">
			
		</td>
	</tr>
	</table>
	<iframe id="iView" style="width: 450px; height:205px"></iframe>
<script language="JavaScript">
    iView.document.designMode = 'On';
</script>
    <table width="450px" height="30px" border="0" cellspacing="0" cellpadding="0" bgcolor="#D6D3CE">	
    <tr>
		<td class="tdClass" colspan="1" width="80%">
		  <select name="selFont" onChange="doFont(this.options[this.selectedIndex].value)">
		    <option value="">-- Font --</option>
		    <option value="Arial">Arial</option>
		    <option value="Courier">Courier</option>
		    <option value="Sans Serif">Sans Serif</option>
		    <option value="Tahoma">Tahoma</option>
		    <option value="Verdana">Verdana</option>
		    <option value="Wingdings">Wingdings</option>
		  </select>
		  <select name="selSize" onChange="doSize(this.options[this.selectedIndex].value)">
		    <option value="">-- Size --</option>
		    <option value="1">Very Small</option>
		    <option value="2">Small</option>
		    <option value="3">Medium</option>
		    <option value="4">Large</option>
		    <option value="5">Larger</option>
		    <option value="6">Very Large</option>
		  </select>
		  <select name="selHeading" onChange="doHead(this.options[this.selectedIndex].value)">
		    <option value="">-- Heading --</option>
		    <option value="Heading 1">H1</option>
		    <option value="Heading 2">H2</option>
		    <option value="Heading 3">H3</option>
		    <option value="Heading 4">H4</option>
		    <option value="Heading 5">H5</option>
		    <option value="Heading 6">H6</option>
		  </select>
		</td>
		<td class="tdClass" colspan="1" width="20%" align="right">
		  <img alt="Toggle Mode" class="butClass" src="images/mode.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doToggleView()">
		  &nbsp;&nbsp;&nbsp;
		</td>
    </tr>
    </table>
<script language="JavaScript">
    iView.document.designMode = 'On';
    iView.document.open();
    iView.document.write(document.newPostForm.content.value);
    iView.document.close();
</script>

