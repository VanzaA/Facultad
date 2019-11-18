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
import jade.domain.FIPAAgentManagement.Property;

/**
   This example shows how to register an application specific service in the Yellow Pages
   catalogue managed by the DF Agent so that other agents can dynamically discover it.
   In this case in particular we register a "Weather-forecast" service for 
   Italy. The name of this service is specified as a command line argument.
   @author Giovanni Caire - TILAB
 */
public class DFRegisterAgent extends Agent {

  protected void setup() {
  	String serviceName = "unknown";
  	
  	// Read the name of the service to register as an argument
  	Object[] args = getArguments();
  	if (args != null && args.length > 0) {
  		serviceName = (String) args[0];
  	}
  	
  	// Register the service
  	System.out.println("Agent "+getLocalName()+" registering service \""+serviceName+"\" of type \"weather-forecast\"");
  	try {
  		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName(serviceName);
  		sd.setType("weather-forecast");
  		// Agents that want to use this service need to "know" the weather-forecast-ontology
  		sd.addOntologies("weather-forecast-ontology");
  		// Agents that want to use this service need to "speak" the FIPA-SL language
  		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
  		sd.addProperties(new Property("country", "Italy"));
  		dfd.addServices(sd);
  		
  		DFService.register(this, dfd);
  	}
  	catch (FIPAException fe) {
  		fe.printStackTrace();
  	}
  } 
}

