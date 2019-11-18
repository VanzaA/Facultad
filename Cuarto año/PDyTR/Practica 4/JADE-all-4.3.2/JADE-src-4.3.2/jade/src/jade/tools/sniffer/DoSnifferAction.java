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

import jade.gui.AgentTree;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.Logger;

   /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2004-04-06 12:56:07 +0200 (mar, 06 apr 2004) $ $Revision: 4970 $
   */

   /**
  * For sniff the Agent in the tree.
  * @see jade.tools.sniffer.DoNotSnifferAction
  * @see jade.tools.sniffer.ShowOnlyAction
  */

public class DoSnifferAction extends AgentAction{

private MainPanel mainPanel;
private Agent agent;
private Sniffer mySniffer;
private List sniffedAgents=new ArrayList();

 public DoSnifferAction(ActionProcessor actPro,MainPanel mainPanel,Sniffer mySniffer ) {
   super("DoSnifferActionIcon","Do sniff this agent(s)",actPro);
   this.mainPanel=mainPanel;
   this.mySniffer=mySniffer;
 }

  public void doAction(AgentTree.AgentNode node){
     doSniff(node.getName());
  }

  /** Given an agent name, sniff it.
   * @param agentName - the String name of the agent.
   * Note, this was originally buried in doAction, but we want the ability to
   * sniff with just a name.
   */
  public void doSniff (String agentName) {
     String realName=checkString(agentName);
     agent=new Agent(realName);

     //Check if the agent is in the canvas
     if (!mainPanel.panelcan.canvAgent.isPresent(realName))
       mainPanel.panelcan.canvAgent.addAgent(agent);   // add Agent in the Canvas

     mainPanel.panelcan.canvAgent.rAgfromNoSniffVector(agent);

     sniffedAgents.add(agent);
     mySniffer.sniffMsg(sniffedAgents,Sniffer.SNIFF_ON);   // Sniff the Agents
     sniffedAgents.clear();
  }

  private String checkString(String nameAgent) {
   int index;

   index=nameAgent.indexOf("@");
   if (index != -1) return nameAgent.substring(0,index);
   else {
      Logger.getMyLogger(this.getClass().getName()).log(Logger.WARNING,"The agent's name is not correct");
     return null;
   }
  }

} 
