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

package examples.O2AInterface;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.*;

/**
 * This class is an example of how you can embed JADE runtime environment within
 * your applications.
 * 
 * @author Giovanni Iavarone - Michele Izzo
 */

public class O2AInterfaceExample {
	

	public static void main(String[] args) throws StaleProxyException,
			InterruptedException {
		Runtime rt = Runtime.instance();
		// Launch a complete platform on the 8888 port
		// create a default Profile
		Profile pMain = new ProfileImpl(null, 8888, null);

		System.out.println("Launching a whole in-process platform..." + pMain);
		AgentContainer mc = rt.createMainContainer(pMain);

		// set now the default Profile to start a container
		ProfileImpl pContainer = new ProfileImpl(null, 8888, null);
		System.out.println("Launching the agent container ..." + pContainer);

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma",
				new Object[0]);
		rma.start();

		// Get a hold on JADE runtime

		Profile p = new ProfileImpl(false);
		AgentContainer ac = rt.createAgentContainer(p);

		// Create a new agent, CounterAgent
		AgentController controller = ac.createNewAgent("CounterAgent",
				CounterAgent.class.getName(), new Object[0]);

		// Fire up the agent
		System.out.println("Starting up a CounterAgent...");
		controller.start();
		CounterManager1 o2a1 = null;

		try {
			o2a1 = controller.getO2AInterface(CounterManager1.class);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		//Adds a ticker behavior that increases the counter every 5 seconds and prints the value
		o2a1.activateCounter();

		CounterManager2 o2a2 = null;

		try {
			o2a2 = controller.getO2AInterface(CounterManager2.class);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		// Wait for 25 seconds
		Thread.sleep(25000);
		
		// Remove ticker behaviour 
		o2a2.deactivateCounter();
	}
}