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

import jade.content.onto.*;
import jade.content.abs.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import jade.content.schema.facets.*;
import jade.util.leap.Serializable;

/**
 * The common ancestor of all ontological schemas.
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public abstract class ObjectSchema implements Serializable {
	/** 
	 Canstant value indicating that a slot in a schema is mandatory,
	 i.e. its value must not be null
	 */
	public static final int MANDATORY = 0;
	/** 
	 Canstant value indicating that a slot in a schema is optional,
	 i.e. its value can be null
	 */
	public static final int OPTIONAL = 1;
	/** 
	 Canstant value indicating that a slot in a schema has an 
	 infinite maximum cardinality
	 */
	public static final int UNLIMITED = -1;
	
	public static final String         BASE_NAME = "Object";
	protected static ObjectSchema baseSchema = null;
	
	protected boolean encodingByOrder = false;
	
	/**
	 * Retrieve the generic base schema for all objects.
	 * @return the generic base schema for all objects.
	 */
	public static ObjectSchema getBaseSchema() {
		return baseSchema;
	} 
	
	/**
	 * Add a slot to the schema.
	 * @param name The name of the slot.
	 * @param slotSchema The schema defining the type of the slot.
	 * @param optionality The optionality, i.e., <code>OPTIONAL</code> 
	 * or <code>MANDATORY</code>
	 */
	protected abstract void add(String name, ObjectSchema slotSchema, int optionality);
	
	/**
	 * Add a mandatory slot to the schema.
	 * @param name name of the slot.
	 * @param slotSchema schema of the slot.
	 */
	protected abstract void add(String name, ObjectSchema slotSchema);
	
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
	protected abstract void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax);
	
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
	 * @see #add(String, ObjectSchema, int, int)
	 */
	protected abstract void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax, String aggType);
	
	/**
	 * Add a super schema tho this schema, i.e. this schema will
	 * inherit all characteristics from the super schema
	 * @param superSchema the super schema.
	 */
	protected abstract void addSuperSchema(ObjectSchema superSchema);
	
	/** 
	 Add a <code>Facet</code> on a slot of this schema
	 @param slotName the name of the slot the <code>Facet</code>
	 must be added to.
	 @param f the <code>Facet</code> to be added.
	 @throws OntologyException if slotName does not identify
	 a valid slot in this schema
	 */
	protected abstract void addFacet(String slotName, Facet f) throws OntologyException; 
	
	/**
	 * Sets an indication about whether the preferred encoding for the 
	 * slots of concepts compliants to this schema is by oredr or by name. 
	 * It should be noted however that the Content Language encoder is 
	 * free to use or ignore this indication depending on the CL grammar 
	 * and actual implementation.
	 */
	public void setEncodingByOrder(boolean b) {
		encodingByOrder = b;
	}
	
	/**
	 * Get the indication whether the preferred encoding for the slots 
	 * of concepts compliant to this schema is by order or by name.
	 */
	public boolean getEncodingByOrder() {
		return encodingByOrder;
	}
	
	/**
	 * Retrieves the name of the type of this schema.
	 * @return the name of the type of this schema.
	 */
	public abstract String getTypeName();
	
	/**
	 * Returns the names of all the slots in this <code>Schema</code> 
	 * (including slots defined in super schemas).
	 *
	 * @return the names of all slots.
	 */
	public abstract String[] getNames();
	
	/**
	 * Returns the names of the slots defined in this <code>Schema</code> 
	 * (excluding slots defined in super schemas).
	 *
	 * @return the names of the slots defined in this <code>Schema</code>.
	 */
	public abstract String[] getOwnNames();
	
	/**
	 * Retrieves the schema of a slot of this <code>Schema</code>.
	 *
	 * @param name The name of the slot.
	 * @return the <code>Schema</code> of slot <code>name</code>
	 * @throws OntologyException If no slot with this name is present
	 * in this schema.
	 */
	public abstract ObjectSchema getSchema(String name) throws OntologyException;
	
	/**
	 * Indicate whether a given <code>String</code> is the name of a
	 * slot defined in this <code>Schema</code> including super-schemas
	 *
	 * @param name The <code>String</code> to test.
	 * @return <code>true</code> if <code>name</code> is the name of a
	 * slot defined in this <code>Schema</code> including super-schemas.
	 */
	public abstract boolean containsSlot(String name);
	
	/**
	 * Indicate whether a given <code>String</code> is the name of a
	 * slot actually defined in this <code>Schema</code> (excluding super-schemas)
	 *
	 * @param name The <code>String</code> to test.
	 * @return <code>true</code> if <code>name</code> is the name of a
	 * slot actually defined in this <code>Schema</code> (excluding super-schemas).
	 */
	public abstract boolean isOwnSlot(String name);
	
	/**
	 * Indicate whether a slot of this schema is mandatory
	 *
	 * @param name The name of the slot.
	 * @return <code>true</code> if the slot is mandatory.
	 * @throws OntologyException If no slot with this name is present
	 * in this schema.
	 */
	public abstract boolean isMandatory(String name) throws OntologyException;
	
	/**
	 * Creates an Abstract descriptor to hold an object compliant to 
	 * this <code>Schema</code>.
	 */
	public abstract AbsObject newInstance() throws OntologyException;
	
	/**
	 Check whether a given abstract descriptor complies with this 
	 schema.
	 @param abs The abstract descriptor to be checked
	 @throws OntologyException If the abstract descriptor does not 
	 complies with this schema
	 */
	public abstract void validate(AbsObject abs, Ontology onto) throws OntologyException;
	
	
	/**
	 Check if this schema is compatible with a given schema s.
	 This is the case if 
	 1) This schema is equals to s
	 2) s is one of the super-schemas of this schema
	 3) This schema descends from s i.e.
	 - s is the base schema for the XXXSchema class this schema is
	 an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
	 schema is an instance of ConceptSchema)
	 - s is the base schema for a super-class of the XXXSchema class
	 this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
	 and this schema is an instance of ConceptSchema)
	 */
	public abstract boolean isCompatibleWith(ObjectSchema s);
	
	/**
	 Return true if 
	 - s is the base schema for the XXXSchema class this schema is
	 an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
	 schema is an instance of ConceptSchema)
	 - s is the base schema for a super-class of the XXXSchema class
	 this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
	 and this schema is an instance of ConceptSchema)
	 */
	protected abstract boolean descendsFrom(ObjectSchema s);
	
	/**
	 * This method checks whether or not an abstract object that is an instance 
	 * of schema s can be used in place of an abstract object that is an instance 
	 * of this schema.
	 * Note that unlike the method isCompatibleWith() that
	 * perform "ontological" checks (i.e. they are related to the reference ontology),
	 * this method only performs a structural check on the slots regardless of the fact 
	 * that the two schemas belong to the same ontology or not. 
	 */
	public abstract boolean isAssignableFrom(ObjectSchema s);
	
	/** 
	 * Retrieves an array containing the direct super-schemas of this schema. 
	 * If this schema has no super-schema an empty array is returned 
	 * @return an array containing the direct super-schemas of this schema.
	 * @since JADE 3.7
	 */
	public abstract ObjectSchema[] getSuperSchemas();
	
	/**
	 * Retrieves the facets defined upon a slot. More in details this method returns
	 * all facets defined in this schema plus all facets defined in super-schemas 
	 * up to the schema actually declaring the given slot. 
	 * @param slotName the name of the slot 
	 * @return the facets defined upon a slot or null if the specified slot is not found.
	 */
	public abstract Facet[] getFacets(String slotName);

}

