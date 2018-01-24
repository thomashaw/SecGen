/*
jboard is a java bulletin board.
version $Name:  $
http://sourceforge.net/projects/jboard/
Copyright (C) 2003 Charles GAY
This file is part of jboard.
jboard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

jboard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jboard; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.sf.jboard.struts.plugins;

import org.apache.log4j.PropertyConfigurator;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import javax.servlet.ServletException;


/**
 * this class is lauched when the application parse the struts-config.xml file.
 * it initialize the log4j system for the entire application.
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.3 $
 */
public class Log4JPlugin implements PlugIn {

    //default value of the log4j configFileName
    private  String log4jConfigFileName ;
	//default value of the log4j refresh time in milliseconds
	private  String refreshTime ;
    /**
     * @see org.apache.struts.action.PlugIn#destroy()
     */
    public void destroy() {

        
    }

    /** 
     * @see org.apache.struts.action.PlugIn#init(org.apache.struts.action.ActionServlet, org.apache.struts.config.ModuleConfig)
     */
    public void init(ActionServlet arg0, ModuleConfig arg1) throws ServletException {

       

        try {
			System.out.println("refreshTime="+refreshTime);
			System.out.println("log4jConfigFileName="+log4jConfigFileName);
            //checks a configuration change every 3 seconds			           
            PropertyConfigurator.configureAndWatch(log4jConfigFileName, Integer.parseInt(refreshTime));
           
			

        } catch(Exception ex) {

            System.err.println("Can't initialize log4j.  log4j Error: " + ex.getMessage());
            System.out.println("Can't initialize log4j.  log4j Error: " + ex.getMessage());

        }

    }

	/**
	 * @return String
	 */
	public  String getLog4jConfigFileName() {
		return log4jConfigFileName;
	}

	/**
	 * @param string
	 */
	public  void setLog4jConfigFileName(String string) {
		log4jConfigFileName = string;
	}

	/**
	 * @return String
	 */
	public  String getRefreshTime() {
		return refreshTime;
	}

	/**
	 * @param i
	 */
	public  void setRefreshTime(String string) {
		refreshTime = string;
	}

}
