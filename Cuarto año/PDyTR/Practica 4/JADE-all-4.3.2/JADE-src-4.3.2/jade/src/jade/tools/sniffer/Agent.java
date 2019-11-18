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


package jade.tools.sniffer;

import java.awt.Color;
import java.io.Serializable;
import javax.swing.SwingUtilities;
import jade.gui.AgentTree;
import jade.core.AID;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2010-06-11 15:32:31 +0200 (ven, 11 giu 2010) $ $Revision: 6352 $
 */

/**
 * Adds properties and methods for representing
 * agents on the Agent Canvas as rectangles.
 *
 */


public class Agent implements Serializable{

	public static int i = 0;
	public static final int hRet = 30;
	public static final int bRet = 50;
	public static final int yRet = 20;
	public String agentName;
	private AID myAID;

	private int pos = 0;

	/**
	 * This flag is <em>true</em> for agents on canvas and <em>false</em> for agents
	 * out of the canvas.
	 */
	public boolean onCanv;
	private int x;

	/**
	 * Constructor for any named agent to be put on the Agent Canvas
	 */
	public Agent(AID id){
		myAID = id;
		agentName=id.getLocalName();
		onCanv = true;
	}

	public Agent(String localName){
		this(new AID(localName, AID.ISLOCALNAME));
	}

	/**
	 * Constructor for a special agent called <em>Other</em> which represents every agent
	 * not present on the Agent Canvas. It is displayed in color grey when every usual agent
	 * is displayed in color red and is the first on the left.
	 */

	public Agent(){
		agentName="Other";
		myAID = new AID(agentName, AID.ISLOCALNAME);
		onCanv = false;
	}

	public boolean equals(Object o) {

		if(o instanceof Agent) {
			Agent ag = (Agent)o;
			return agentName.equalsIgnoreCase(ag.agentName);
		}
		else {
			return myAID.equals(o);
		}
	}

}  // End of class Agent
