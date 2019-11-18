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


package jade.domain.FIPAAgentManagement;

import jade.content.onto.*;
import jade.content.schema.*;

/**
   This class represents the ontology defined by FIPA Agent Management 
   specifications (document no. 23). 
   <p>
   The actual <code>Ontology</code> object representing the 
   FIPA-Agent-Management-ontology is a singleton and is accessible through 
   the static method <code>getInstance()</code>
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date: 2004-03-15 15:27:54 +0100 (lun, 15 mar 2004) $ $Revision: 4911 $
 */
public class FIPAManagementOntology  extends Ontology implements FIPAManagementVocabulary {

  private static Ontology theInstance = new FIPAManagementOntology();
  
  /**
     This method returns the unique instance (according to the singleton 
     pattern) of the FIPA-Agent-Management-ontology.
     @return The singleton <code>Ontology</code> object, containing the 
     schemas for the elements of the FIPA-Agent-Management-ontology.
  */
  public static Ontology getInstance() {
    return theInstance;
  }

  private FIPAManagementOntology() {
    //#MIDP_EXCLUDE_BEGIN
  	super(NAME, new Ontology[]{ExceptionOntology.getInstance(), SerializableOntology.getInstance()}, new BCReflectiveIntrospector());
    //#MIDP_EXCLUDE_END
    	
		/*#MIDP_INCLUDE_BEGIN    	
  	super(NAME, ExceptionOntology.getInstance(), null);
   	#MIDP_INCLUDE_END*/

		try {
    	//#MIDP_EXCLUDE_BEGIN
	  	add(new ConceptSchema(DFAGENTDESCRIPTION), DFAgentDescription.class);
	  	add(new ConceptSchema(SERVICEDESCRIPTION), ServiceDescription.class);
	  	add(new ConceptSchema(SEARCHCONSTRAINTS), SearchConstraints.class);
	  	add(new ConceptSchema(AMSAGENTDESCRIPTION), AMSAgentDescription.class);
	  	add(new ConceptSchema(PROPERTY), Property.class);
	  	add(new ConceptSchema(ENVELOPE), Envelope.class);
	  	add(new ConceptSchema(RECEIVEDOBJECT), ReceivedObject.class);
	  	add(new ConceptSchema(APDESCRIPTION), APDescription.class);
	  	add(new ConceptSchema(APSERVICE), APService.class);
	 	 	
	  	add(new AgentActionSchema(REGISTER), Register.class);
	  	add(new AgentActionSchema(DEREGISTER), Deregister.class);
	  	add(new AgentActionSchema(MODIFY), Modify.class);
	  	add(new AgentActionSchema(SEARCH), Search.class);
	  	add(new AgentActionSchema(GETDESCRIPTION), GetDescription.class);
	  	
	  	add(new PredicateSchema(ALREADYREGISTERED), AlreadyRegistered.class);
	  	add(new PredicateSchema(NOTREGISTERED), NotRegistered.class);
    	//#MIDP_EXCLUDE_END
			   	  
			/*#MIDP_INCLUDE_BEGIN    	
	  	add(new ConceptSchema(DFAGENTDESCRIPTION));
	  	add(new ConceptSchema(SERVICEDESCRIPTION));
	  	add(new ConceptSchema(SEARCHCONSTRAINTS));
	  	add(new ConceptSchema(AMSAGENTDESCRIPTION));
	  	add(new ConceptSchema(PROPERTY));
	  	add(new ConceptSchema(ENVELOPE));
	  	add(new ConceptSchema(RECEIVEDOBJECT));
	  	add(new ConceptSchema(APDESCRIPTION));
	  	add(new ConceptSchema(APSERVICE));
	  	
	  	add(new AgentActionSchema(REGISTER));
	  	add(new AgentActionSchema(DEREGISTER));
	  	add(new AgentActionSchema(MODIFY));
	  	add(new AgentActionSchema(SEARCH));
	  	add(new AgentActionSchema(GETDESCRIPTION));
	  
	  	add(new PredicateSchema(ALREADYREGISTERED));
	  	add(new PredicateSchema(NOTREGISTERED));
   		#MIDP_INCLUDE_END*/
	  	
	  	ConceptSchema cs = (ConceptSchema)getSchema(DFAGENTDESCRIPTION);
	  	cs.add(DFAGENTDESCRIPTION_NAME, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.OPTIONAL);
	  	cs.add(DFAGENTDESCRIPTION_SERVICES, (ConceptSchema)getSchema(SERVICEDESCRIPTION), 0, ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(DFAGENTDESCRIPTION_PROTOCOLS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(DFAGENTDESCRIPTION_LANGUAGES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(DFAGENTDESCRIPTION_ONTOLOGIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(DFAGENTDESCRIPTION_LEASE_TIME, (PrimitiveSchema)getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL); 

	  	cs = (ConceptSchema)getSchema(SERVICEDESCRIPTION);
	  	cs.add(SERVICEDESCRIPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(SERVICEDESCRIPTION_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(SERVICEDESCRIPTION_OWNERSHIP, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(SERVICEDESCRIPTION_PROTOCOLS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(SERVICEDESCRIPTION_LANGUAGES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(SERVICEDESCRIPTION_ONTOLOGIES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0 , ObjectSchema.UNLIMITED, BasicOntology.SET);
	  	cs.add(SERVICEDESCRIPTION_PROPERTIES, (ConceptSchema)getSchema(PROPERTY), 0, ObjectSchema.UNLIMITED, BasicOntology.SET);

	  	cs = (ConceptSchema)getSchema(SEARCHCONSTRAINTS);
	  	cs.add(SEARCHCONSTRAINTS_MAX_DEPTH, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	  	cs.add(SEARCHCONSTRAINTS_MAX_RESULTS, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
	  	cs.add(SEARCHCONSTRAINTS_SEARCH_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  
	  	cs = (ConceptSchema)getSchema(AMSAGENTDESCRIPTION);
	  	cs.add(AMSAGENTDESCRIPTION_NAME, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.OPTIONAL);
	  	cs.add(AMSAGENTDESCRIPTION_OWNERSHIP, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(AMSAGENTDESCRIPTION_STATE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  
	  	cs = (ConceptSchema)getSchema(PROPERTY);
	  	cs.add(PROPERTY_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(PROPERTY_VALUE, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.OPTIONAL);  // In a template we can specify a null value

	  	cs = (ConceptSchema)getSchema(ENVELOPE);
	  	cs.add(ENVELOPE_TO, (ConceptSchema)getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);
	  	cs.add(ENVELOPE_FROM, (ConceptSchema)getSchema(BasicOntology.AID));
	  	cs.add(ENVELOPE_COMMENTS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	  	cs.add(ENVELOPE_ACLREPRESENTATION, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(ENVELOPE_PAYLOADLENGTH, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	  	cs.add(ENVELOPE_PAYLOADENCODING, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(ENVELOPE_DATE, (PrimitiveSchema)getSchema(BasicOntology.DATE));
	  	cs.add(ENVELOPE_INTENDEDRECEIVER, (ConceptSchema)getSchema(BasicOntology.AID), 0, ObjectSchema.UNLIMITED);
	  	cs.add(ENVELOPE_RECEIVED, (ConceptSchema)getSchema(RECEIVEDOBJECT), ObjectSchema.OPTIONAL);
	  	cs.add(ENVELOPE_PROPERTIES, (ConceptSchema)getSchema(PROPERTY), 0, ObjectSchema.UNLIMITED, BasicOntology.SET);

	  	cs = (ConceptSchema)getSchema(RECEIVEDOBJECT);
	  	cs.add(RECEIVEDOBJECT_BY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(RECEIVEDOBJECT_FROM, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(RECEIVEDOBJECT_DATE, (PrimitiveSchema)getSchema(BasicOntology.DATE));
	  	cs.add(RECEIVEDOBJECT_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(RECEIVEDOBJECT_VIA, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	
	  	cs = (ConceptSchema)getSchema(APDESCRIPTION);
	  	cs.add(APDESCRIPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(APDESCRIPTION_SERVICES, (ConceptSchema)getSchema(APSERVICE), 0, ObjectSchema.UNLIMITED);

	  	cs = (ConceptSchema)getSchema(APSERVICE);
	    cs.add(APSERVICE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    cs.add(APSERVICE_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    cs.add(APSERVICE_ADDRESSES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	  	  	
	  	AgentActionSchema as = (AgentActionSchema)getSchema(REGISTER);
	  	as.add(REGISTER_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  	
	  	as = (AgentActionSchema)getSchema(DEREGISTER);
	  	as.add(DEREGISTER_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  
	  	as = (AgentActionSchema)getSchema(MODIFY);
	  	as.add(MODIFY_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
	  
	  	as = (AgentActionSchema)getSchema(SEARCH);
	  	as.add(SEARCH_DESCRIPTION, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.MANDATORY);
	  	as.add(SEARCH_CONSTRAINTS, (ConceptSchema)getSchema(SEARCHCONSTRAINTS), ObjectSchema.MANDATORY);
	  	as.setEncodingByOrder(true);
    } 
    catch(OntologyException oe) {
    	oe.printStackTrace();
    }
  } 
}
