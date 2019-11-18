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


package examples.yellowPages;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.domain.FIPANames;

/**
This is an example of an agent that plays the role of a sub-df by 
automatically registering with a parent DF.
Notice that exactly the same might be done by using the GUI of the DF.
<p>
This SUBDF inherits all the functionalities of the default DF, including
its GUI.
@author Giovanni Rimassa - Universita` di Parma
@version $Date: 2003-12-03 17:57:03 +0100 (mer, 03 dic 2003) $ $Revision: 4638 $
*/

public class SubDF extends jade.domain.df {

  
  public void setup() {

   // Input df name
   int len = 0;
   byte[] buffer = new byte[1024];

   try {

     AID parentName = getDefaultDF(); 
     
     //Execute the setup of jade.domain.df which includes all the default behaviours of a df 
     //(i.e. register, unregister,modify, and search).
     super.setup();
    
     //Use this method to modify the current description of this df. 
     setDescriptionOfThisDF(getDescription());
     
     //Show the default Gui of a df.
     super.showGui();

     DFService.register(this,parentName,getDescription());
     addParent(parentName,getDescription());
		 System.out.println("Agent: " + getName() + " federated with default df.");
     
    }catch(FIPAException fe){fe.printStackTrace();}
  }
  
  private DFAgentDescription getDescription()
  {
     DFAgentDescription dfd = new DFAgentDescription();
     dfd.setName(getAID());
     ServiceDescription sd = new ServiceDescription();
     sd.setName(getLocalName() + "-sub-df");
     sd.setType("fipa-df");
     sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
     sd.addOntologies("fipa-agent-management");
     sd.setOwnership("JADE");
     dfd.addServices(sd);
     return dfd;
  }

}
