/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.core.behaviours.TickerBehaviour;

/**
 * This example shows the usage of the behaviours that allow scheduling actions
 * at a given point in time: <code>WakerBehaviour</code> and
 * <code>TickerBehaviour</code>.
 * More in details this agent executes a <code>TickerBehaviour</code>
 * that prints the agent name every second and a <code>WakerBehaviour</code> ]
 * that kill the agent after 10 seconds.
 * @author Giovanni Caire - TILAB
 */
public class TimeAgent extends Agent {

  protected void setup() {
    System.out.println("Agent "+getLocalName()+" started.");

    // Add the TickerBehaviour (period 1 sec)
    addBehaviour(new TickerBehaviour(this, 1000) {
      protected void onTick() {
        System.out.println("Agent "+myAgent.getLocalName()+": tick="+getTickCount());
      } 
    });

    // Add the WakerBehaviour (wakeup-time 10 secs)
    addBehaviour(new WakerBehaviour(this, 10000) {
      protected void handleElapsedTimeout() {
        System.out.println("Agent "+myAgent.getLocalName()+": It's wakeup-time. Bye...");
        myAgent.doDelete();
      } 
    });
  } 
}
