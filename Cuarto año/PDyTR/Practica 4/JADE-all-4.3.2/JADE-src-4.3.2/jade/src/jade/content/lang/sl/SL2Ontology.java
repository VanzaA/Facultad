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
import jade.content.abs.*;
import jade.core.CaseInsensitiveString;

/**
 * Ontology containing schemas for the SL2 language operators.
 * see jade.content.Ontology
 * @author Giovanni Caire - TILAB
 */
class SL2Ontology extends SL1Ontology implements SL2Vocabulary {
	// NAME
  public static final String ONTOLOGY_NAME = jade.domain.FIPANames.Ontology.SL2_ONTOLOGY;;
	
  // The singleton instance of this ontology
	private static Ontology theInstance = new SL2Ontology(ONTOLOGY_NAME, SL1Ontology.getInstance(), null);
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  protected SL2Ontology(String name, Ontology base, Introspector intro) {
  	super(name, base, intro);
  	
  	try {
  		add(new PredicateSchema(IMPLIES), AbsPredicate.getJavaClass());
  		add(new PredicateSchema(EQUIV), AbsPredicate.getJavaClass());
  		PredicateSchema ps = (PredicateSchema) getSchema(IMPLIES);
  		ps.add(IMPLIES_LEFT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps.add(IMPLIES_RIGHT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps = (PredicateSchema) getSchema(EQUIV);
  		ps.add(EQUIV_LEFT, (PredicateSchema) PredicateSchema.getBaseSchema());
	  	ps.add(EQUIV_RIGHT, (PredicateSchema) PredicateSchema.getBaseSchema());
  	
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
	}

	boolean isQuantifier(String symbol) {
		return (CaseInsensitiveString.equalsIgnoreCase(EXISTS, symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(FORALL, symbol));
	}
	
	boolean isModalOp(String symbol) {
		return (CaseInsensitiveString.equalsIgnoreCase(BELIEF, symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(UNCERTAINTY, symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(PERSISTENT_GOAL, symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(INTENTION, symbol));
	}
	
	boolean isActionOp(String symbol) {
		return (super.isActionOp(symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(FEASIBLE, symbol));
	}
	
	boolean isSLFunctionWithoutSlotNames(String symbol) {
		return (super.isSLFunctionWithoutSlotNames(symbol) || 
			CaseInsensitiveString.equalsIgnoreCase(ACTION_SEQUENCE, symbol) ||
			CaseInsensitiveString.equalsIgnoreCase(ACTION_ALTERNATIVE, symbol));
	}
	
	boolean isBinaryLogicalOp(String symbol) {
		return (super.isBinaryLogicalOp(symbol) || 
				CaseInsensitiveString.equalsIgnoreCase(IMPLIES, symbol) || 
				CaseInsensitiveString.equalsIgnoreCase(EQUIV, symbol));
	}
}
