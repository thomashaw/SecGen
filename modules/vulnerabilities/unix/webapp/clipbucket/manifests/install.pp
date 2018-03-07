class clipbucket::install {

  $docroot = '/var/www/clipbucket'

  # TODO: parameterise me
  $dbname = 'test'
  $dbhost = 'localhost'
  $dbuser = 'test'
  $dbpass = 'test'
  $base_url = 'http://172.0.16.2'
  # TODO: parameterise me

  file { '/tmp/clipbucket/':
    ensure  => directory,
    recurse => true,
    source  => 'puppet:///modules/clipbucket',
  }



  # Upload All Files From Public_html Folder to your Root dir ie( www , public_html)
  # Chmod Following to 777
  # ‘Files’ directory and all files and folders under it (Important)
  # ‘Images’ directory and all files and folders under it (Important)
  # ‘Styles’ directory and all files and folders under it
  # ‘Includes/email_templates’ and files under it
  # ‘player’ and files under it (Important)
  # ‘Cache’ directory (Important)
  # ‘logs’ directory and logs.txt (Important)
  #

  file { $docroot:
    ensure  => directory,
    recurse => true,
    source  => '/tmp/clipbucket/upload'
  }


  # Uploading And Chmod
  #
  # Upload All Files From Public_html Folder to your Root dir ie( www , public_html)
  # Chmod Following to 777
  # ‘Files’ directory and all files and folders under it (Important)
  # ‘Images’ directory and all files and folders under it (Important)
  # ‘Styles’ directory and all files and folders under it
  # ‘Includes/email_templates’ and files under it
  # ‘player’ and files under it (Important)
  # ‘Cache’ directory (Important)
  # ‘logs’ directory and logs.txt (Important)
  #

  file { ["$docroot/files", "$docroot/images", "$docroot/styles", "$docroot/includes/email_templates",
    "$docroot/player", "$docroot/cache", "$docroot/logs"]:
    ensure  => directory,
    mode    => '0777',
    recurse => true,
  }




  # Sql Dumping & Database Settings
  #
  # Create a database and Import the 'clipbucket_lite.sql' from Sql folder
  # Now edit values in 'config' table as
  # 'base_dir' to relative path to directory where clipbucket is installed ie /home/username/public_html (without trailing slash "/" at the end )
  # 'base_url' to direct url to website where clipbucket is installed ie http://www.clip-bucket.com (without trailing slash "/" at the end )


  file { "/tmp/cb_new_install.sql":
    ensure => file,
    content => template('clipbucket/cb_new_install.sql.erb'),

  }

  ::mysql::db { $dbname:
    user     => $dbuser,
    password => $dbpass,
    host     => $dbhost,
    grant    => ['SELECT', 'UPDATE'],
    sql      => '/tmp/cb_new_install.sql',
    import_timeout => 100000,
    require => File['/tmp/cb_new_install.sql']
  }


  # Now Edit 'includes/dbconnect.php' as
  # $BDTYPE = 'mysql';
  # $DBHOST = 'localhost' ; Change the Host (Default Host is localhost so you may not need to change this)
  # $DBNAME = 'dbname'; Change this to the name of the database you created for clipbucket
  # $DBUSER = 'dbuser'; Change this to the username of the databse you assigned to the clipbucket
  # $DBPASS = 'password'; Change it to the password of the database

  file { "$docroot/includes/dbconnect.php":
    ensure => present,
    content => template('clipbucket/dbconnect.php.erb'),
  }


}




#
# now your website is almost ready to go, but you have to change some settings from the Administrator Panel
# Settings In Admin Panel
#
# First You have to access to the admin panel and by using this url http://yourwebsites.com/admin_area
#
# Enter username and password to login as Super Admin
# username : admin
# password : admin
# After Logging in go to Super Admin Settings
# Change the username and pass to whatever you want, but make is so complicate so that no one able to guess it
# Now Create an Admin by clicking 'Add Member' found under User Management Menu
# Fill The Form and Set its User Access Level to Admin and Submit it, and use this user as Admin
# NOTE : DO NOT PERFORM ANY ACTION OTHER THAN USERMANAGMENT USING SUPERADMIN
# Now logout and Login as Your newly created admin
#
# Setting Website Configurations
#
# Website Settings
# Website Title "Your Website Title" ie "Best Website"
# Website Slogan ie "We Are The Best";
# Website Closed (If You Are Editing or Doing Maintenance) Yes/No
# Website Closed Message is Displayed When You Have Closed Your Website
# Set Meta Description according to your website content
# Set Meta Keywords According to your website Content
# Turn on/of SEO Url
# Paths
# FFMPEG Binary Path (Set FFMPEG path, where FFMPEG is installed)
# FLVTool2 Path (Set Flv Tool 2 Path)
# Mencoder Path (Set Mencoder Path)
# Mplayer Path (Set Mplayer Path)
# PHP Path ( Set PHP Path)
# Video, Uploading and Conversion Settings
# Max Upload Size (Set the Maximum Size of the File you want to be uploaded )
# Video Comments (Turn on or off Video Comments) Yes/No
# Video Embedding (Turn on or off Video Embedding) Yes/No
# Video Rating (Turn on or off Video Rating) Yes/No
# Video Comments Rating (Turn on or off Video Comments Rating ) Yes/No
# Resize The Uploaded Video or Not
# if yes the Set Resize Height and Resized Width
# Set Other Video Settings Accordingly
# Keep Original File for Download (Turn on or off ) Yes/No
# Video Activation Required (Turn on or off ) Yes/No
# Display Settings
# Template Name ( Select Available Template From The list)
# Flash Player ( Select Available Flash Player From The list)
# Video List Per Page ( Number Of Videos List Per Page )
# Video List Per Box ( Number Of VIdeo List in a Box, Tab or other than Main or Videos Page)
# Channel List Per Page ( Number Of Channels List Per Page)
# Channels List Per Box ( Number OF Channels List Per Box , Tab or Other than main or Channels Page)
# Set Number Of Search Results Display
# Set Number of Recently Viewed Videos in a Flash Widget
# User Registration
# Turn On/Off User Registrations
# Set on/Off Email Verification
#
#
#
# Your Basic Installation is Done and Your website is ready to Launch