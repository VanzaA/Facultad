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

package examples.mobile;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Iterator;
import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.mobility.*;
import jade.domain.FIPANames;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


/**
This is an example of mobile agent. 
This class contains the resources used by the agent behaviours: the counter, 
the 
flag cntEnabled, and the list of visited locations. 
At the setup it creates a gui and adds behaviours to: get the list of
available locations from AMS, serve the incoming messages, and
to increment the counter. 
In particular, notice the usage of the two methods <code>beforeMove()</code> and
<code>afterMove()</code> to execute some application-specific tasks just before and just after
the agent migration takes effect.

Because this agent has a GUI, it extends the class GuiAgent that, in turn,
extends the class Agent. Being the GUI a different thread, the communication
between the agent and its GUI is based on event passing.
@see jade.gui.GuiAgent
@author Giovanni Caire - CSELT S.p.A
@version $Date: 2004-08-20 12:15:13 +0200 (ven, 20 ago 2004) $ $Revision: 5283 $
*/
public class MobileAgent extends GuiAgent {
  int     cnt;   // this is the counter
  public boolean cntEnabled;  // this flag indicates if counting is enabled
  transient protected MobileAgentGui gui;  // this is the gui
  Location nextSite;  // this variable holds the destination site

  // These constants are used by the Gui to post Events to the Agent
  public static final int EXIT = 1000;
  public static final int MOVE_EVENT = 1001;
  public static final int STOP_EVENT = 1002;
  public static final int CONTINUE_EVENT = 1003;
  public static final int REFRESH_EVENT = 1004;
  public static final int CLONE_EVENT = 1005;

  // this vector contains the list of visited locations
  Vector visitedLocations = new Vector();

  public void setup() {
	  // register the SL0 content language
	  getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
	  // register the mobility ontology
	  getContentManager().registerOntology(MobilityOntology.getInstance());

	  // creates and shows the GUI
	  gui = new MobileAgentGui(this);
	  gui.setVisible(true); 

	  // get the list of available locations and show it in the GUI
	  addBehaviour(new GetAvailableLocationsBehaviour(this));

	  // initialize the counter and the flag
	  cnt = 0;
	  cntEnabled = true;

	  ///////////////////////
	  // Add agent behaviours to increment the counter and serve
	  // incoming messages
	  Behaviour b1 = new CounterBehaviour(this);
	  addBehaviour(b1);	
	  Behaviour b2 = new ServeIncomingMessagesBehaviour(this);
	  addBehaviour(b2);	
	}

	public void takeDown() {
	  if (gui!=null) {
            gui.dispose();
	    gui.setVisible(false);
	  }
          System.out.println(getLocalName()+" is now shutting down.");
	}

  /**
   * This method stops the counter by disabling the flag
   */
   void stopCounter(){
    cntEnabled = false;
   }

  /**
   * This method resume counting by enabling the flag
   */
   void continueCounter(){
     cntEnabled = true;
   }

  /**
   * This method displays the counter in the GUI
   */
   void displayCounter(){
     gui.displayCounter(cnt);
   }
  
   
protected void beforeClone() {
  System.out.println(getLocalName()+" is now cloning itself.");
}

protected void afterClone() {
  System.out.println(getLocalName()+" has cloned itself.");
  afterMove();
}
  /**
   * This method is executed just before moving the agent to another
   * location. It is automatically called by the JADE framework.
   * It disposes the GUI and prints a bye message on the standard output.
   */
	protected void beforeMove() 
	{
		gui.dispose();
		gui.setVisible(false);
		System.out.println(getLocalName()+" is now moving elsewhere.");
	}

  /**
   * This method is executed as soon as the agent arrives to the new 
   * destination.
   * It creates a new GUI and sets the list of visited locations and
   * the list of available locations (via the behaviour) in the GUI.
   */
   protected void afterMove() {
     System.out.println(getLocalName()+" is just arrived to this location.");
     // creates and shows the GUI
     gui = new MobileAgentGui(this);
     //if the migration is via RMA the variable nextSite can be null.
     if(nextSite != null)
     {
     	visitedLocations.addElement(nextSite);
      for (int i=0; i<visitedLocations.size(); i++)
        gui.addVisitedSite((Location)visitedLocations.elementAt(i));
     }
     gui.setVisible(true); 	
			
     // Register again SL0 content language and JADE mobility ontology,
     // since they don't migrate.
     getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
	 getContentManager().registerOntology(MobilityOntology.getInstance());
     // get the list of available locations from the AMS.
     // FIXME. This list might be stored in the Agent and migrates with it.
     addBehaviour(new GetAvailableLocationsBehaviour(this));
   }

  public void afterLoad() {
      afterClone();
  }

  public void beforeFreeze() {
      beforeMove();
  }

  public void afterThaw() {
      afterMove();
  }

  public void beforeReload() {
      beforeMove();
  }

  public void afterReload() {
      afterMove();
  }


	/////////////////////////////////
	// GUI HANDLING
		

	// AGENT OPERATIONS FOLLOWING GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
		switch(ev.getType()) 
		{
		case EXIT:
			gui.dispose();
			gui = null;
			doDelete();
			break;
		case MOVE_EVENT:
      Iterator moveParameters = ev.getAllParameter();
      nextSite =(Location)moveParameters.next();
			doMove(nextSite);
			break;
		case CLONE_EVENT:
			Iterator cloneParameters = ev.getAllParameter();
			nextSite =(Location)cloneParameters.next();
			doClone(nextSite,"clone"+cnt+"of"+getName());
			break;
   	case STOP_EVENT:
		  stopCounter();
		  break;
		case CONTINUE_EVENT:
		  continueCounter();
		  break;
		case REFRESH_EVENT:
		  addBehaviour(new GetAvailableLocationsBehaviour(this));
		  break;
		}

	}

}

