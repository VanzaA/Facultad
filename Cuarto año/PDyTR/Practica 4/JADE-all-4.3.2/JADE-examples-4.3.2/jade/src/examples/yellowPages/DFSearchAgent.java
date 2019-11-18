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
package examples.yellowPages;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.util.leap.Iterator;

/**
   This example shows how to search for services provided by other agents 
   and advertised in the Yellow Pages catalogue managed by the DF agent.
   In this case in particular we search for agents providing a 
   "Weather-forecast" service.
   @author Giovanni Caire - TILAB
 */
public class DFSearchAgent extends Agent {

  protected void setup() {
  	// Search for services of type "weather-forecast"
  	System.out.println("Agent "+getLocalName()+" searching for services of type \"weather-forecast\"");
  	try {
  		// Build the description used as template for the search
  		DFAgentDescription template = new DFAgentDescription();
  		ServiceDescription templateSd = new ServiceDescription();
  		templateSd.setType("weather-forecast");
  		template.addServices(templateSd);
  		
  		SearchConstraints sc = new SearchConstraints();
  		// We want to receive 10 results at most
  		sc.setMaxResults(new Long(10));
  		
  		DFAgentDescription[] results = DFService.search(this, template, sc);
  		if (results.length > 0) {
  			System.out.println("Agent "+getLocalName()+" found the following weather-forecast services:");
  			for (int i = 0; i < results.length; ++i) {
  				DFAgentDescription dfd = results[i];
  				AID provider = dfd.getName();
  				// The same agent may provide several services; we are only interested
  				// in the weather-forcast one
  				Iterator it = dfd.getAllServices();
  				while (it.hasNext()) {
  					ServiceDescription sd = (ServiceDescription) it.next();
  					if (sd.getType().equals("weather-forecast")) {
  						System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
  					}
  				}
  			}
  		}	
  		else {
  			System.out.println("Agent "+getLocalName()+" did not find any weather-forecast service");
  		}
  	}
  	catch (FIPAException fe) {
  		fe.printStackTrace();
  	}
  } 
}

