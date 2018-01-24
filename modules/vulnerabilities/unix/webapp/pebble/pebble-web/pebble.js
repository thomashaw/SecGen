function openWindow(url, windowName, width, height) {
  popup = window.open(url, windowName, 'width='+width+',height='+height+',toolbar=0,resizable=1,scrollbars=1,menubar=0,status=1');
  popup.focus();
}

function confirmRemove(url) {
  if (confirm("Are you sure?")) {
    window.location = url;
  }
}

function populateFilename(fromField, toField) {
  var indexOfSlash = fromField.value.lastIndexOf("/");
  if (indexOfSlash < 0) {
    indexOfSlash= fromField.value.lastIndexOf("\\");
  }
  if (indexOfSlash < 0) {
    indexOfSlash = 0;
  }

  toField.value = fromField.value.substring(indexOfSlash+1, fromField.value.length);
}
