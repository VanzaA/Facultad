/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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


package jade.domain.mobility;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

import jade.core.AID;
import jade.core.Location;
import jade.core.ContainerID;

import jade.content.onto.*;
import jade.content.schema.*;
import jade.domain.JADEAgentManagement.*;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date: 2003-03-10 17:29:48 +0100 (lun, 10 mar 2003) $ $Revision: 3777 $
*/

/**
   This class represents the ontology used for JADE mobility. There is
   only a single instance of this class.
   @see jade.domain.mobility.MobilityOntology#getInstance()
 */
public class MobilityOntology extends Ontology implements MobilityVocabulary {

  public static final String NAME = "jade-mobility-ontology";
  
  private static Ontology theInstance = new MobilityOntology();

  public static Ontology getInstance() {
    return theInstance;
  }

  private MobilityOntology() {
  
  	super(NAME, JADEManagementOntology.getInstance());
  
  	try{
			// Adds the roles of the basic ontology (ACTION, AID,...)
	add(new ConceptSchema(MOBILE_AGENT_DESCRIPTION), MobileAgentDescription.class);
	add(new ConceptSchema(MOBILE_AGENT_PROFILE), MobileAgentProfile.class);
	add(new ConceptSchema(MOBILE_AGENT_SYSTEM), MobileAgentSystem.class);
	add(new ConceptSchema(MOBILE_AGENT_LANGUAGE), MobileAgentLanguage.class);
	add(new ConceptSchema(MOBILE_AGENT_OS), MobileAgentOS.class);

	
	add(new AgentActionSchema(CLONE), CloneAction.class);
	add(new AgentActionSchema(MOVE), MoveAction.class);
	
	ConceptSchema cs = (ConceptSchema)getSchema(MOBILE_AGENT_DESCRIPTION);
	cs.add(MOBILE_AGENT_DESCRIPTION_NAME, (ConceptSchema)getSchema(BasicOntology.AID));
	cs.add(MOBILE_AGENT_DESCRIPTION_DESTINATION, (ConceptSchema)getSchema(LOCATION));
	cs.add(MOBILE_AGENT_DESCRIPTION_AGENT_PROFILE, (ConceptSchema)getSchema(MOBILE_AGENT_PROFILE), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_DESCRIPTION_AGENT_VERSION, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_DESCRIPTION_SIGNATURE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	
	cs = (ConceptSchema)getSchema(MOBILE_AGENT_PROFILE);
	cs.add(MOBILE_AGENT_PROFILE_SYSTEM, (ConceptSchema)getSchema(MOBILE_AGENT_SYSTEM), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_PROFILE_LANGUAGE, (ConceptSchema)getSchema(MOBILE_AGENT_LANGUAGE), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_PROFILE_OS, (ConceptSchema)getSchema(MOBILE_AGENT_OS));
	
	cs = (ConceptSchema)getSchema(MOBILE_AGENT_SYSTEM);
	cs.add(MOBILE_AGENT_SYSTEM_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	cs.add(MOBILE_AGENT_SYSTEM_MAJOR_VERSION, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	cs.add(MOBILE_AGENT_SYSTEM_MINOR_VERSION, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_SYSTEM_DEPENDENCIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	cs = (ConceptSchema)getSchema(MOBILE_AGENT_LANGUAGE);
	cs.add(MOBILE_AGENT_LANGUAGE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	cs.add(MOBILE_AGENT_LANGUAGE_MAJOR_VERSION, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	cs.add(MOBILE_AGENT_LANGUAGE_MINOR_VERSION, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_LANGUAGE_DEPENDENCIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	cs = (ConceptSchema)getSchema(MOBILE_AGENT_OS);
	cs.add(MOBILE_AGENT_OS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	cs.add(MOBILE_AGENT_OS_MAJOR_VERSION, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	cs.add(MOBILE_AGENT_OS_MINOR_VERSION, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	cs.add(MOBILE_AGENT_OS_DEPENDENCIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	AgentActionSchema as = (AgentActionSchema)getSchema(MOVE);
	as.add(MOVE_MOBILE_AGENT_DESCRIPTION, (ConceptSchema)getSchema(MOBILE_AGENT_DESCRIPTION));
	
	as = (AgentActionSchema)getSchema(CLONE);
	as.addSuperSchema((AgentActionSchema)getSchema(MOVE));
	//as.add(CLONE_MOBILE_AGENT_DESCRIPTION, (ConceptSchema)getSchema(MOBILE_AGENT_DESCRIPTION));
	as.add(CLONE_NEW_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	
	

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  }

}
