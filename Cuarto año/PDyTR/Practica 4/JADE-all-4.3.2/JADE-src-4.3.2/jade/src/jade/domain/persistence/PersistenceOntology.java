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

package jade.domain.persistence;

import jade.core.ContainerID;
import jade.content.onto.*;
import jade.content.schema.*;
import jade.domain.FIPAAgentManagement.ExceptionOntology;
import jade.domain.FIPAAgentManagement.NotRegistered;

/**
   This class represents the JADE-Persistence ontology i.e. the set of
   concepts, actions and predicates that relates to the JADE
   Persistence Service features that can be accessed through the JADE
   AMS.
   <p>
   The actual <code>Ontology</code> object representing the
   JADE-Persistence ontology is a singleton and is accessible through
   the static method <code>getInstance()</code>

   @author Giovanni Rimassa -  FRAMeTech s.r.l.
 */
public class PersistenceOntology extends Ontology implements PersistenceVocabulary {
 
    // The singleton instance of this ontology
    private static Ontology theInstance = new PersistenceOntology();
	
    /**
       This method returns the unique instance (according to the singleton 
       pattern) of the JADE-Persistence ontology.
       @return The singleton <code>Ontology</code> object, containing the 
       schemas for the elements of the JADE-Persistence ontology.
    */
    public static Ontology getInstance() {
	return theInstance;
    }

    private PersistenceOntology() {
	//#MIDP_EXCLUDE_BEGIN
  	super(NAME, ExceptionOntology.getInstance(), new BCReflectiveIntrospector());
	//#MIDP_EXCLUDE_END
    	
	/*#MIDP_INCLUDE_BEGIN    	
	  super(NAME, BasicOntology.getInstance(), null);
	  #MIDP_INCLUDE_END*/

	try {
	    //#MIDP_EXCLUDE_BEGIN
	    // Concepts definitions
	    add(new ConceptSchema(AGENTGROUP), AgentGroup.class);
	    add(new ConceptSchema(LOCATION));
	    add(new ConceptSchema(CONTAINERID), ContainerID.class);


	    // AgentActions definitions
	    add(new AgentActionSchema(LOADAGENT), LoadAgent.class);
	    add(new AgentActionSchema(RELOADAGENT), ReloadAgent.class);
	    add(new AgentActionSchema(SAVEAGENT), SaveAgent.class);
	    add(new AgentActionSchema(DELETEAGENT), DeleteAgent.class);
	    add(new AgentActionSchema(FREEZEAGENT), FreezeAgent.class);
	    add(new AgentActionSchema(THAWAGENT), ThawAgent.class);
	    add(new AgentActionSchema(SAVECONTAINER), SaveContainer.class);
	    add(new AgentActionSchema(LOADCONTAINER), LoadContainer.class);
	    add(new AgentActionSchema(DELETECONTAINER), DeleteContainer.class);
	    add(new AgentActionSchema(LOADAGENTGROUP), LoadAgentGroup.class);
	    add(new AgentActionSchema(SAVEAGENTGROUP), SaveAgentGroup.class);
	    add(new AgentActionSchema(DELETEAGENTGROUP), DeleteAgentGroup.class);

	    // Predicates definitions

	    //#MIDP_EXCLUDE_END
    	
	    /*#MIDP_INCLUDE_BEGIN    	
	    // Concepts definitions
	    add(new ConceptSchema(AGENTGROUP));
	    add(new ConceptSchema(CONTAINERID));
	    add(new ConceptSchema(LOCATION));

	    // AgentActions definitions
	    add(new AgentActionSchema(LOADAGENT));
	    add(new AgentActionSchema(RELOADAGENT));
	    add(new AgentActionSchema(SAVEAGENT));
	    add(new AgentActionSchema(DELETEAGENT));
	    add(new AgentActionSchema(FREEZEAGENT));
	    add(new AgentActionSchema(THAWAGENT));
	    add(new AgentActionSchema(SAVECONTAINER));
	    add(new AgentActionSchema(LOADCONTAINER));
	    add(new AgentActionSchema(DELETECONTAINER));
	    add(new AgentActionSchema(LOADAGENTGROUP));
	    add(new AgentActionSchema(SAVEAGENTGROUP));
	    add(new AgentActionSchema(DELETEAGENTGROUP));

	    // Predicates definitions
	    add(new PredicateSchema(UNSUPPORTEDVALUE));
	    add(new PredicateSchema(UNRECOGNISEDVALUE));
	    add(new PredicateSchema(UNSUPPORTEDFUNCTION));
	    add(new PredicateSchema(MISSINGPARAMETER));
	    add(new PredicateSchema(UNEXPECTEDPARAMETER));
	    add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE));
	    add(new PredicateSchema(NOTREGISTERED));
	    add(new PredicateSchema(INTERNALERROR));
	    #MIDP_INCLUDE_END*/
   		
	    // Slots definitions
	    ConceptSchema cs = (ConceptSchema)getSchema(LOCATION);
	    cs.add(LOCATION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    cs.add(LOCATION_PROTOCOL, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    cs.add(LOCATION_ADDRESS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	    cs = (ConceptSchema) getSchema(CONTAINERID);
	    cs.addSuperSchema((ConceptSchema) getSchema(LOCATION));

	    AgentActionSchema as = (AgentActionSchema) getSchema(SAVEAGENT);
	    as.add(SAVEAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
	    as.add(SAVEAGENT_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));

	    as = (AgentActionSchema)getSchema(LOADAGENT);
	    as.add(LOADAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
	    as.add(LOADAGENT_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING)); 
	    as.add(LOADAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID));

	    as = (AgentActionSchema)getSchema(RELOADAGENT);
	    as.add(RELOADAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
	    as.add(RELOADAGENT_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));

	    as = (AgentActionSchema) getSchema(DELETEAGENT);
	    as.add(DELETEAGENT_AGENT, (ConceptSchema) getSchema(BasicOntology.AID));
	    as.add(DELETEAGENT_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    as.add(DELETEAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID));

	    as = (AgentActionSchema)getSchema(FREEZEAGENT);
	    as.add(FREEZEAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
	    as.add(FREEZEAGENT_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING)); 
	    as.add(FREEZEAGENT_BUFFERCONTAINER, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);

	    as = (AgentActionSchema)getSchema(THAWAGENT);
	    as.add(THAWAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
	    as.add(THAWAGENT_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    as.add(THAWAGENT_NEWCONTAINER, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);

	    as = (AgentActionSchema)getSchema(SAVECONTAINER);
	    as.add(SAVECONTAINER_CONTAINER, (ConceptSchema)getSchema(CONTAINERID));
	    as.add(SAVECONTAINER_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));

	    as = (AgentActionSchema)getSchema(LOADCONTAINER);
	    as.add(LOADCONTAINER_CONTAINER, (ConceptSchema)getSchema(CONTAINERID));
	    as.add(LOADCONTAINER_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));

	    as = (AgentActionSchema)getSchema(DELETECONTAINER);
	    as.add(DELETECONTAINER_CONTAINER, (ConceptSchema)getSchema(CONTAINERID));
	    as.add(DELETECONTAINER_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    as.add(DELETECONTAINER_WHERE, (ConceptSchema)getSchema(CONTAINERID));

	    as = (AgentActionSchema) getSchema(SAVEAGENTGROUP);
	    as.add(SAVEAGENTGROUP_GROUP, (ConceptSchema)getSchema(AGENTGROUP));
	    as.add(SAVEAGENTGROUP_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));

	    as = (AgentActionSchema) getSchema(DELETEAGENTGROUP);
	    as.add(DELETEAGENTGROUP_GROUP, (ConceptSchema)getSchema(AGENTGROUP));
	    as.add(DELETEAGENTGROUP_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));

	    as = (AgentActionSchema) getSchema(LOADAGENTGROUP);
	    as.add(LOADAGENTGROUP_GROUP, (ConceptSchema)getSchema(AGENTGROUP));
	    as.add(LOADAGENTGROUP_REPOSITORY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	}
	catch(OntologyException oe) {
	    oe.printStackTrace();
	}
    }
}
