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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2001-10-09 17:15:36 +0200 (mar, 09 ott 2001) $ $Revision: 2768 $
  */

  /**
 * The List for the agents on the Agent Canvas. Implements Serializable for saving 
 * data to the binary snapshot file.
 */


public class AgentList implements Serializable{

  public List agents;

  /**
   * Default constructor for the class <em>AgentList</em>
   */
 public AgentList() {
  agents = new ArrayList(50);
  String n = "";

  /* First we put a dummy agent called "Other" */
  agents.add(new Agent());

 }

  /**
   * Add an agent to the list.
   *
   * @param agent the agent to add
   */
  public void addAgent(Agent agent) {
    agents.add(agent);
  }

  /**
   * Removes an agent from the list
   *
   * @param agentName name of the agent to remove
   */
  public void removeAgent(String agentName) {
    Iterator it = agents.iterator();
    while(it.hasNext()) {
      Agent agent = (Agent)it.next();
      if(agentName.equals(agent.agentName) && agent.onCanv == true) {
	agents.remove(agent);
      }
    }
  }

  /**
   * Clears the agent list
   */
  public void removeAllAgents() {
   agents.clear();
  }

  /**
   * Verifies if an agent is present on the canvas
   *
   * @param agName name of the agent to check for
	 */
  public boolean isPresent (String agName) {
    Iterator it = agents.iterator();
    while(it.hasNext()) {
      Agent agent = (Agent)it.next();
      if(agent.equals(agName)) {
				return true;
      }
    }
    return false;
  }


  /**
   * Gives back the position inside the agents
   *
   * @param agName name of the agent for its position to search
   */
  public int getPos(String agName) {
    int i = 0;

    Iterator it = agents.iterator();
    while(it.hasNext()) {
      Agent agent = (Agent)it.next();
      if(agent.equals(agName)) {
				return i;
      }
      i = i + 1;
    }

     /* 0 is the return value for an agent not present */

    return 0;
  }

  public Iterator getAgents() {
    return agents.iterator();
  }

 public int size() {
  return agents.size();
 }

}  // End class AgentList
