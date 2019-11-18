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
package jade.content.schema;

import jade.content.abs.*;
import jade.content.onto.*;

/**
 * The class to be used to define schemas of agent actions in 
 * an ontology.
 * Note that an AgentActionSchema should also be a ConceptSchema, but
 * this inheritance relation is cut as Java does not support
 * multiple inheritance. As a consequence in practice it will 
 * not be possible to define e.g. a ConceptSchema with a slot 
 * whose value must be instances of a certain type of agent-action 
 * even if in theory this should be
 * possible as a ConceptSchema can have slots of type term and 
 * an agent-action is a concept and therefore a term.
 * @author Federico Bergenti - Universita` di Parma
 */
public class AgentActionSchema extends ConceptSchema {
    public static final String         BASE_NAME = "AgentAction";
    private static AgentActionSchema baseSchema = new AgentActionSchema();
    

    /**
     * Construct a schema that vinculates an entity to be a generic
     * agent action
     */
    private AgentActionSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates an <code>AgentActionSchema</code> with a given type-name.
     * @param typeName The name of this <code>AgentActionSchema</code>.
     */
    public AgentActionSchema(String typeName) {
        super(typeName);
    }

    /**
     * Retrieve the generic base schema for all agent actions.
     * @return the generic base schema for all agent actions.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Add a mandatory slot of type PredicateSchema to this schema. 
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     */
    public void add(String name, PredicateSchema slotSchema) {
        super.add(name, slotSchema);
    } 

    /**
     * Add a slot of type PredicateSchema to this schema. 
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     * @param optionality The optionality, i.e. <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    public void add(String name, PredicateSchema slotSchema, int optionality) {
        super.add(name, slotSchema, optionality);
    } 

    /**
     * Creates an Abstract descriptor to hold an agent action of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsAgentAction(getTypeName());
    } 

		/**
	     Check whether a given abstract descriptor complies with this 
	     schema.
	     @param abs The abstract descriptor to be checked
	     @throws OntologyException If the abstract descriptor does not 
	     complies with this schema
	   */
  	public void validate(AbsObject abs, Ontology onto) throws OntologyException {
			// Check the type of the abstract descriptor
  		if (!(abs instanceof AbsAgentAction)) {
				throw new OntologyException(abs+" is not an AbsAgentAction");
			}
			
			// Check the slots
			validateSlots(abs, onto);
  	}
  	
  	/**
  	   Return true if 
  	   - s is the base schema for the XXXSchema class this schema is
  	     an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
  	     schema is an instance of ConceptSchema)
  	   - s is the base schema for a super-class of the XXXSchema class
  	     this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
  	     and this schema is an instance of ConceptSchema.
  	   Moreover, as AgentActionSchema extends GenericActionSchema, but should
  	   also extend ConceptSchema (this is not possible in practice as
  	   Java does not support multiple inheritance), this method
  	   returns true also in the case that s is equals to, or is an
  	   ancestor of, ConceptSchema.getBaseSchema() (i.e. TermSchema.getBaseSchema()
  	   descends from s)
  	 */
  	protected boolean descendsFrom(ObjectSchema s) {
			if (s != null) {
	  	 	if (s.equals(getBaseSchema())) {
		  		return true;
  			}
  			if (super.descendsFrom(s)) {
  				return true;
  			}
  			return ContentElementSchema.getBaseSchema().descendsFrom(s);
			}
			else {
				return false;
			}
  	}
  	
  	/**
  	 * Define that the result produced by the execution of an action described by this 
  	 * schema has a structure conforming to a given term schema.
  	 * @param resultSchema the schema of the result
  	 */
  	public void setResult(TermSchema resultSchema) {
  		// We treat the result as if it were a slot to inherit all mechanisms to deal with super-schemas and facets
  		add(RESULT_SLOT_NAME, resultSchema, OPTIONAL);
  	}
  	
  	/**
  	 * Define that the result produced by the execution of an action described by this 
  	 * schema is an aggregate of n (with n between cardMin and cardMax) elements each one having 
  	 * a structure conforming to a given term schema.
  	 * @param elementsSchema the schema of the elements in the result aggregate
  	 * @param cardMin the result must include at least <code>cardMin</code> elements
  	 * @param cardMax the result must include at most <code>cardMax</code> elements
  	 */
	public void setResult(TermSchema elementsSchema, int cardMin, int cardMax) {
		add(RESULT_SLOT_NAME, elementsSchema, cardMin, cardMax);
	}
	
	public TermSchema getResultSchema() {
		try {
			return (TermSchema) getSchema(RESULT_SLOT_NAME);
		}
		catch (OntologyException oe) {
			// Result schema not defined
			return null;
		}
	}
	
	public Facet[] getResultFacets() {
		return getFacets(RESULT_SLOT_NAME);
	}
}
