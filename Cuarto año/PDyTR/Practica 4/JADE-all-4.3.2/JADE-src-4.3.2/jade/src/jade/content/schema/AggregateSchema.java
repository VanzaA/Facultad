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
import jade.util.leap.Iterator;

/**
 * This class represent the schema of aggregate entities in
 * an ontology.
 * @author Federico Bergenti - Universita` di Parma
 */
public class AggregateSchema extends TermSchema {
	public static final String         BASE_NAME = "Aggregate";
	private static AggregateSchema baseSchema = new AggregateSchema();
	private TermSchema elementsSchema;

	/**
	 * Construct a schema that vinculates an entity to be a generic
	 * aggregate
	 */
	private AggregateSchema() {
		super(BASE_NAME);
	}

	/**
	 * Creates an <code>AggregateSchema</code> with a given type-name.
	 *
	 * @param typeName The name of this <code>AggregateSchema</code>.
	 */
	public AggregateSchema(String typeName) {
		super(typeName);
	}

	/**
	 * Creates an <code>AggregateSchema</code> with a given type-name.
	 *
	 * @param typeName The name of this <code>AggregateSchema</code>.
	 */
	public AggregateSchema(String typeName, TermSchema elementsSchema) {
		super(typeName);
		this.elementsSchema = elementsSchema;
	}
	
	/**
	 * Retrieve the generic base schema for all aggregates.
	 *
	 * @return the generic base schema for all aggregates.
	 */
	public static ObjectSchema getBaseSchema() {
		return baseSchema;
	} 

	/**
	 * Creates an Abstract descriptor to hold an aggregate of
	 * the proper type.
	 */
	public AbsObject newInstance() throws OntologyException {
		return new AbsAggregate(getTypeName());
	} 

	public TermSchema getElementsSchema() {
		if (elementsSchema != null) {
			return elementsSchema;
		}
		else {
			return (TermSchema) TermSchema.getBaseSchema();
		}
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
		if (abs.getAbsType() != AbsObject.ABS_AGGREGATE) {
			throw new OntologyException(abs+" is not an AbsAggregate");
		}

		// Validate the elements in the aggregate against their schemas.
		// Note that there is no need to check that these schemas are
		// compliant with TermSchema.getBaseSchema() because the
		// AbsAggregate class already forces that.
		AbsAggregate agg = (AbsAggregate) abs;
		Iterator it = agg.iterator();
		while (it.hasNext()) {
			AbsTerm el = (AbsTerm) it.next();
			ObjectSchema s = onto.getSchema(el.getTypeName());
			s.validate(el, onto);
		}
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

	/**
  	   The difference between types of aggregates (such as SET and 
  	   SEQUENCE) is quite fuzy. Therefore we don't throw a validation
  	   exception if a SET is found where a SEQUENCE is expected and VV.
	 */
	public boolean equals(Object o) {
		if (o != null) {
			return (o instanceof AggregateSchema);
		}
		else {
			return false;
		}
	}

	// Propagate the assignability check to the aggregate elements schemas
	public boolean isAssignableFrom(ObjectSchema s) {
		if (s != null &&
			s instanceof AggregateSchema &&
			s.getTypeName().equals(getTypeName())) {
			
			return getElementsSchema().isAssignableFrom(((AggregateSchema)s).getElementsSchema());
		}
		return false;
	}

}
