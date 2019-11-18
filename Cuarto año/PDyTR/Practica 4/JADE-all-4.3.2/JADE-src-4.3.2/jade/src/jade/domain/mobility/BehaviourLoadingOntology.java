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


package jade.domain.mobility;

import jade.content.onto.*;
import jade.content.schema.*;
import jade.domain.FIPAAgentManagement.ExceptionOntology;

/**
   This class represents the BehaviourLoading ontology including
   the concepts and actions required to dynamically load and execute 
   jade behaviours whose code is not included in the JVM classpath.
   @see jade.core.behaviours.LoaderBehaviour
   @author Giovanni Caire - TILAB  
 */
public class BehaviourLoadingOntology  extends Ontology implements BehaviourLoadingVocabulary {

  private static Ontology theInstance = new BehaviourLoadingOntology();
  
  /**
     This method returns the singleton instance of the 
     Behaviour-Loading ontology.
     @return The singleton <code>Ontology</code> object, containing the 
     schemas for the elements of the Behaviour-Loading ontology.
  */
  public static Ontology getInstance() {
    return theInstance;
  }

  private BehaviourLoadingOntology() {
  	super(NAME, new Ontology[]{ExceptionOntology.getInstance(), SerializableOntology.getInstance()}, new ReflectiveIntrospector());
    	
		try {
	  	add(new ConceptSchema(PARAMETER), Parameter.class);	 	 	
	  	add(new AgentActionSchema(LOAD_BEHAVIOUR), LoadBehaviour.class);
	  	
	  	ConceptSchema cs = (ConceptSchema)getSchema(PARAMETER);
	  	cs.add(PARAMETER_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	cs.add(PARAMETER_VALUE, (TermSchema)TermSchema.getBaseSchema(), ObjectSchema.OPTIONAL);
	  	cs.add(PARAMETER_MODE, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
  	
	  	AgentActionSchema as = (AgentActionSchema)getSchema(LOAD_BEHAVIOUR);
	  	as.add(LOAD_BEHAVIOUR_CLASS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	  	as.add(LOAD_BEHAVIOUR_CODE, (PrimitiveSchema)getSchema(BasicOntology.BYTE_SEQUENCE), ObjectSchema.OPTIONAL);
	  	as.add(LOAD_BEHAVIOUR_ZIP, (PrimitiveSchema)getSchema(BasicOntology.BYTE_SEQUENCE), ObjectSchema.OPTIONAL);
	  	as.add(LOAD_BEHAVIOUR_PARAMETERS, (ConceptSchema)getSchema(PARAMETER), 0, ObjectSchema.UNLIMITED);	  	
    } 
    catch(OntologyException oe) {
    	oe.printStackTrace();
    }
  } 
}
