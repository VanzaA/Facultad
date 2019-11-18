/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade;

//#MIDP_EXCLUDE_FILE
	
import java.applet.Applet;

import jade.core.MicroRuntime;
import jade.util.leap.Properties;


/**
   This class handles JADE start-up and shut-down when running JADE
   as an Applet. The split-container mode is used.
   <br>
   <b>Requires the LEAP add-on</b>
   <br>
   @author Giovanni Caire - TILAB
 */
public class AppletBoot extends Applet implements Runnable {

  // Start-up the JADE runtime system
  public void init() {
  	Properties pp = new Properties();
  	pp.setProperty(MicroRuntime.HOST_KEY, getCodeBase().getHost());
  	String s = getParameter(MicroRuntime.PORT_KEY);
  	if (s != null) {
	  	pp.setProperty(MicroRuntime.PORT_KEY, s);
  	}
  	s = getParameter(MicroRuntime.AGENTS_KEY);
  	if (s != null) {
	  	pp.setProperty(MicroRuntime.AGENTS_KEY, s);
  	}
  	s = getParameter(MicroRuntime.CONN_MGR_CLASS_KEY);
  	if (s != null) {
	  	pp.setProperty(MicroRuntime.CONN_MGR_CLASS_KEY, s);
  	}
  	MicroRuntime.startJADE(pp, this);
  } 

  public void destroy() {
  	MicroRuntime.stopJADE();
  }
  
  public String[][] getParameterInfo() {
  	String[][] info = {
  		{MicroRuntime.AGENTS_KEY, "semicolon-separated agent specifiers", "The agents to be started"},
  		{MicroRuntime.PORT_KEY, "integer", "The port of the JADE container on the applet codbase host"},
  		{MicroRuntime.CONN_MGR_CLASS_KEY, "String", "The class for BackEnd-to-FrontEnd communication"}
  	};
  	return info;
  }
  
  public void run() {
  	// When JADE terminates just do nothing
  }  
}



