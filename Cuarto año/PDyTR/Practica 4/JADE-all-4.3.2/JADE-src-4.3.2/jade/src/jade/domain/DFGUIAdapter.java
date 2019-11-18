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

package jade.domain;

//#APIDOC_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.core.AID;
import jade.gui.GuiEvent;

import jade.util.leap.Iterator;
import jade.util.leap.List;

/**
 * This interface must be implemented by a GUI that wants to interact
 * with the DF agent. Two implementations of this interface have been
 * realized: the class jade.domain.df (used by the DF agent itself) and
 * the class jade.applet.DFAppletCommunicator (used by the DFApplet).
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 * @version $Date: 2003-11-18 17:26:01 +0100 (mar, 18 nov 2003) $ $Revision: 4564 $
 */

public interface DFGUIAdapter {

	// GUI event types
  public static final int EXIT = 0;
  public static final int CLOSEGUI = 1;

  //DFGUI event Type
  public static final int REGISTER = 1001;
  public static final int DEREGISTER = 1002;
  public static final int MODIFY = 1003;
  public static final int SEARCH = 1004;
  public static final int FEDERATE = 1005;
  
  //only used by applet
  public static final int REFRESHAPPLET = 1006;

  
  /**
  * This method notifies an event to the df.
  */
  void postGuiEvent(GuiEvent ev);
  	
  /**
   * This method returns the agent description of an agent registered with the DF given the agent name
   */
  DFAgentDescription getDFAgentDsc(AID name) throws FIPAException;
  
  
  /**
  * This method returns the description of this df.
  */
  DFAgentDescription getDescriptionOfThisDF();

  /**
  * This method returns the description used by the df to federate with 
  * a given parent DF.
  */
  DFAgentDescription getDescriptionOfThisDF(AID parent);
}
