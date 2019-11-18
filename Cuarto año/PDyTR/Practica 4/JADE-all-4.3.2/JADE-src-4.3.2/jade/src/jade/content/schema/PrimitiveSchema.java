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
 * This class represent the schema of primitive entities in
 * an ontology.
 * @author Federico Bergenti - Universita` di Parma
 */
public class PrimitiveSchema extends TermSchema {
    public static final String         BASE_NAME = "Primitive";
    private static PrimitiveSchema baseSchema = new PrimitiveSchema();

    /**
     * Construct a schema that vinculates an entity to be a generic
     * primitive element
     */
    private PrimitiveSchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>PrimitiveSchema</code> with a given type-name.
     *
     * @param typeName The name of this <code>PrimitiveSchema</code>.
     */
    public PrimitiveSchema(String typeName) {
        super(typeName);
    }

    /**
     * Retrieve the generic base schema for all primitives.
     *
     * @return the generic base schema for all primitives.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold a primitive of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsPrimitive(getTypeName());
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
  		if (!(abs instanceof AbsPrimitive)) {
				throw new OntologyException(abs+" is not an AbsPrimitive");
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
  	
	public boolean isAssignableFrom(ObjectSchema s) {
		if (s != null &&
			s instanceof PrimitiveSchema) {

			String srcTypeName = s.getTypeName();
			String destTypeName = getTypeName();
			if (destTypeName.equals(srcTypeName)) {
				return true;
			}
		}
		return false;
	}
  	
}

