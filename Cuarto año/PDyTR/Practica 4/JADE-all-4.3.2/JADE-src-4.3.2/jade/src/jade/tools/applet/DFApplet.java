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

package jade.tools.applet;

import jade.tools.dfgui.*;
import java.applet.Applet;

/**
 * This applet is a client of SocketProxyAgent and executes an applet
 * showing the GUI of the default DF.
 *
 * @see jade.applet.DFAppletCommunicator
 * @author Fabio Bellifemine - CSELT S.p.A
 * @version $Date: 2002-03-14 16:03:22 +0100 (gio, 14 mar 2002) $ $Revision: 3093 $
 *
 */
 
 //to start the applet the dfproxy agent must be launched on the platform 
 //and the file dfproxy.inf must be in the working directory (i.e. classes
 //if the platform was launched from that directory).
 //HTML Code:
 //<applet code = DFApplet.class width = 200 height=100></applet>

 public class DFApplet extends Applet {

  public void init() {
  
    DFAppletCommunicator dfc = new DFAppletCommunicator(this);
    DFGUI gui = new DFGUI(dfc);
    gui.enableRefreshButton();
    dfc.setGUI(gui);
    dfc.refreshDFGUI();
    gui.setVisible(true); 
   
  }
}
