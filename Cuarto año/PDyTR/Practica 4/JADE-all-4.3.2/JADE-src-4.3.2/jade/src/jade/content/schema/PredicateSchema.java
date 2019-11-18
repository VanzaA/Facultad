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
 * The class to be used to define schemas of predicates in 
 * an ontology.
 * @author Federico Bergenti - Universita` di Parma
 */
public class PredicateSchema extends ContentElementSchema {
    public static final String         BASE_NAME = "Predicate";
    private static PredicateSchema baseSchema = new PredicateSchema();

    /**
     * Construct a schema that vinculates an entity to be a generic
     * predicate
     */
    private PredicateSchema() {
        super(BASE_NAME);
        encodingByOrder = true;
    }

    /**
     * Creates a <code>PredicateSchema</code> with a given type-name,
     * e.g. FATHER_OF, WORKS_FOR...
     * @param typeName The name of this <code>PredicateSchema</code>.
     */
    public PredicateSchema(String typeName) {
        super(typeName);
        encodingByOrder = true;
    }

    /**
     * Retrieve the generic base schema for all predicates.
     * @return the generic base schema for all predicates.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Add a mandatory slot to this schema. 
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     */
    public void add(String name, ObjectSchema slotSchema) {
        super.add(name, slotSchema);
    } 

    /**
     * Add a slot to this schema. 
     *
     * @param name The name of the slot.
     * @param slotSchema The schema of the slot.
     * @param optionality The optionality, i.e. <code>OPTIONAL</code> 
     * or <code>MANDATORY</code>
     */
    public void add(String name, ObjectSchema slotSchema, int optionality) {
        super.add(name, slotSchema, optionality);
    } 

    /**
     * Add a slot with cardinality between <code>cardMin</code>
     * and <code>cardMax</code> to this schema. 
     * Adding such a slot corresponds to add a slot
     * of type Aggregate and then to add proper facets (constraints)
     * to check that the type of the elements in the aggregate are
     * compatible with <code>elementsSchema</code> and that the 
     * aggregate contains at least <code>cardMin</code> elements and
     * at most <code>cardMax</code> elements. By default the Aggregate 
     * is of type <code>BasicOntology.SEQUENCE</code>.
     * @param name The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin This slot must get at least <code>cardMin</code>
     * values
     * @param cardMax This slot can get at most <code>cardMax</code>
     * values
     */
    public void add(String name, TermSchema elementsSchema, int cardMin, int cardMax) {
      super.add(name, elementsSchema, cardMin, cardMax);
    } 

    /**
     * Add a slot with cardinality between <code>cardMin</code>
     * and <code>cardMax</code> to this schema and allow specifying the type
     * of Aggregate to be used for this slot.
     * @param name The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin This slot must get at least <code>cardMin</code>
     * values
     * @param cardMax This slot can get at most <code>cardMax</code>
     * values
     * @param aggType The type of Aggregate to be used
     * @see #add(String, TermSchema, int, int)
     */
    public void add(String name, TermSchema elementsSchema, int cardMin, int cardMax, String aggType) {
      super.add(name, elementsSchema, cardMin, cardMax, aggType);
    } 
    	
	/**
	 * Add a slot with optionality and cardinality between <code>cardMin</code>
	 * and <code>cardMax</code> to this schema and allow specifying the type
	 * of Aggregate to be used for this slot.
	 * @param name The name of the slot.
	 * @param elementsSchema The schema for the elements of this slot.
	 * @param cardMin This slot must get at least <code>cardMin</code>
	 * values
	 * @param cardMax This slot can get at most <code>cardMax</code>
	 * values
	 * @param aggType The type of Aggregate to be used
	 * @param optionality The optionality, i.e., <code>OPTIONAL</code>
	 * @see #add(String, ObjectSchema, int, int)
	 */
	public void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax, String aggType, int optionality) {
		super.add(name, elementsSchema, cardMin, cardMax, aggType, optionality);
	}
    
    /**
     * Adds a super-schema to this schema. This allows defining 
     * inheritance relationships between ontological predicates.
     * It must be noted that a predicate always inherits from another 
     * predicate --> A super-schema of a <code>PredicateSchema</code>
     * must be a <code>PredicateSchema</code> too.
     *
     * @param superClassSchema The super-schema to be added.
     */
    public void addSuperSchema(PredicateSchema superClassSchema) {
        super.addSuperSchema(superClassSchema);
    } 

    /** 
       Add a <code>Facet</code> on a slot of this schema
       @param slotName the name of the slot the <code>Facet</code>
       must be added to.
       @param f the <code>Facet</code> to be added.
       @throws OntologyException if slotName does not identify
       a valid slot in this schema
     */
		public void addFacet(String slotName, Facet f) throws OntologyException {
			super.addFacet(slotName, f);
		}

		/**
     * Creates an Abstract descriptor to hold a predicate of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsPredicate(getTypeName());
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
  		if (!(abs instanceof AbsPredicate)) {
				throw new OntologyException(abs+" is not an AbsPredicate");
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
  	     and this schema is an instance of ConceptSchema)
  	 */
  	protected boolean descendsFrom(ObjectSchema s) {
  		if (s != null) {
  			if (s.equals(getBaseSchema())) {
	  			return true;
  			}
  			return super.descendsFrom(s);
  		}
  		else {
  			return false;
  		}
  	}
}
