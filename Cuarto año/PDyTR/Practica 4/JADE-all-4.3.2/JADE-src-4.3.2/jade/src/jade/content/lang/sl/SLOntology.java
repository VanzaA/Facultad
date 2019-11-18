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
package jade.content.lang.sl;

import jade.content.*;
import jade.content.onto.*;
import jade.content.schema.*;
/*#MIDP_INCLUDE_BEGIN
import jade.content.abs.*;
#MIDP_INCLUDE_END*/

/**
 * Ontology containing schemas for the full SL language operators.
 * see jade.content.Ontology
 * @author Giovanni Caire - TILAB
 */
public class SLOntology 
//#MIDP_EXCLUDE_BEGIN
	extends SL2Ontology 
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
	extends Ontology
#MIDP_INCLUDE_END*/
	implements SLVocabulary {
		
	// NAME
  public static final String ONTOLOGY_NAME = jade.domain.FIPANames.Ontology.SL_ONTOLOGY;;
	
  // The singleton instance of this ontology
	private static Ontology theInstance = new SLOntology();
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  protected SLOntology() {
		//#MIDP_EXCLUDE_BEGIN
  	super(ONTOLOGY_NAME, SL2Ontology.getInstance(), null);
		//#MIDP_EXCLUDE_END

		/*#MIDP_INCLUDE_BEGIN
  	super(ONTOLOGY_NAME, BasicOntology.getInstance(), null);
  	
  	try {
			// Schemas for the SL1 operators
  		add(new PredicateSchema(AND), AbsPredicate.getJavaClass());
  		add(new PredicateSchema(OR), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(NOT), AbsPredicate.getJavaClass());
  	
  		PredicateSchema ps = (PredicateSchema) getSchema(AND);
  		ps.add(AND_LEFT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps.add(AND_RIGHT, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(OR);
  		ps.add(OR_LEFT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps.add(OR_RIGHT, (PredicateSchema) PredicateSchema.getBaseSchema());
  		
  		ps = (PredicateSchema) getSchema(NOT);
	  	ps.add(NOT_WHAT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  
	  
			// Schemas for the SL2 operators
    	add(VariableSchema.getBaseSchema());
  		add(new IRESchema(IOTA));
  		add(new IRESchema(ANY));
  		add(new IRESchema(ALL));
	  	add(new PredicateSchema(FORALL), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(EXISTS), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(BELIEF), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(UNCERTAINTY), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(PERSISTENT_GOAL), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(INTENTION), AbsPredicate.getJavaClass());
	  	add(new PredicateSchema(FEASIBLE), AbsPredicate.getJavaClass());
	  	add(new AgentActionSchema(ACTION_SEQUENCE), AbsAgentAction.getJavaClass());
	  	add(new AgentActionSchema(ACTION_ALTERNATIVE), AbsAgentAction.getJavaClass());
  	
  		ps = (PredicateSchema) getSchema(EXISTS);
  		ps.add(EXISTS_WHAT, (VariableSchema) VariableSchema.getBaseSchema());
	  	ps.add(EXISTS_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(FORALL);
  		ps.add(FORALL_WHAT, (VariableSchema) VariableSchema.getBaseSchema());
	  	ps.add(FORALL_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(BELIEF);
  		ps.add(BELIEF_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(BELIEF_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(UNCERTAINTY);
  		ps.add(UNCERTAINTY_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(UNCERTAINTY_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  
  		ps = (PredicateSchema) getSchema(PERSISTENT_GOAL);
  		ps.add(PERSISTENT_GOAL_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(PERSISTENT_GOAL_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(INTENTION);
  		ps.add(INTENTION_AGENT, (ConceptSchema) getSchema(AID));
	  	ps.add(INTENTION_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
  		ps = (PredicateSchema) getSchema(FEASIBLE);
  		ps.add(FEASIBLE_ACTION, (VariableSchema) VariableSchema.getBaseSchema());
	  	ps.add(FEASIBLE_CONDITION, (PredicateSchema) PredicateSchema.getBaseSchema(), ObjectSchema.OPTIONAL);
  	
  		AgentActionSchema as = (AgentActionSchema) getSchema(ACTION_SEQUENCE);
  		as.add(ACTION_SEQUENCE_FIRST, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.add(ACTION_SEQUENCE_SECOND, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.setEncodingByOrder(true);
  	
  		as = (AgentActionSchema) getSchema(ACTION_ALTERNATIVE);
  		as.add(ACTION_ALTERNATIVE_FIRST, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.add(ACTION_ALTERNATIVE_SECOND, (AgentActionSchema) AgentActionSchema.getBaseSchema());
  		as.setEncodingByOrder(true);
    } 
    catch (OntologyException oe) {
      oe.printStackTrace();
    } 
		#MIDP_INCLUDE_END*/
	}

}
