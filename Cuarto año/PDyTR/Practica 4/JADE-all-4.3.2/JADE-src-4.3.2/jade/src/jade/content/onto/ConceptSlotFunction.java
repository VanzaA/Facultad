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
package jade.content.onto;

//#MIDP_EXCLUDE_FILE 

import jade.content.Concept;
import jade.content.abs.AbsConceptSlotFunction;
import jade.content.abs.AbsObject;
import jade.content.schema.ConceptSlotFunctionSchema;

/**
 * The ConceptSlotFunction class allows treating the slots of an ontological concept as functions.
 * For instance, if an ontology defines a concept <code>Person</code> with a slot <code>name</code> and a slot <code>age</code>,
 * it is possible to create expressions such as<br>
 * (= (age (Person :name John)) 41) <br>
 * (> (age (Person :name John)) (age (Person :name Bill)))<br>
 * (iota ?x (= (age (Person :name John)) ?x))
 * In order to exploit this feature it is necessary to instruct an ontology to use concept slots as functions by means of the
 * <code>useConceptSlotsAsFunctions</code> method of the <code>Ontology</code> class.
 * @since JADE 3.7
 */
public class ConceptSlotFunction extends AbsConceptSlotFunction {
	private Concept concept;
	private Ontology onto;
	
	ConceptSlotFunction(String slotName, Concept concept, Ontology onto) {
		super(slotName);
		this.concept = concept;
		this.onto = onto;
	}
	
	public String getSlotName() {
		return getTypeName();
	}
	
	public Concept getConcept() {
		return concept;
	}

	public AbsObject getAbsObject(String name) {
		if (ConceptSlotFunctionSchema.CONCEPT_SLOT_FUNCTION_CONCEPT.equals(name)) {
			try {
				return onto.fromObject(concept);
			}
			catch (OntologyException oe) {
				throw new RuntimeException("Error externalizing Concept "+concept.getClass().getName(), oe);
			}
		}
		else {
			return null;
		}
	}
	
	public String[] getNames() {
		return new String[] {ConceptSlotFunctionSchema.CONCEPT_SLOT_FUNCTION_CONCEPT};
	}
	public boolean isGrounded() {
		return true;
	}
	
	public int getCount() {
		return 1;
	}
	
 	public Object apply() throws OntologyException {
		return apply(concept);
	}
	
	public Object apply(Concept c)  throws OntologyException {
		if (concept.getClass().isAssignableFrom(c.getClass())) {
			return onto.getSlotValue(getTypeName(), c);
		}
		else {
			throw new OntologyException("Concept "+c.getClass()+" is not compatible with internal concept "+concept.getClass());
		}
	}
	
	public void fill(Object val) throws OntologyException {
		fill(concept, val);
	}

	
	public void fill(Concept c, Object val) throws OntologyException {
		if (concept.getClass().isAssignableFrom(c.getClass())) {
			onto.setSlotValue(getTypeName(), val, c);
		}
		else {
			throw new OntologyException("Concept "+c.getClass()+" is not compatible with internal concept "+concept.getClass());
		}
	}
}
