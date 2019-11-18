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

/**
 * This class represents the schema of an Identifying Referential
 * Expression (IRE) in an ontology. 
 * Note that an IRESchema should also be a TermSchema, but
 * this inheritance relation is cut as Java does not support
 * multiple inheritance. As a consequence in practice it will 
 * not be possible to define e.g. a ConceptSchema with a slot 
 * whose value must be instances of a certain type of IRE even if in theory 
 * this should be possible as a ConceptSchema can have slots 
 * of type term and an IRE is a term.
 * @author Federico Bergenti - Universita` di Parma
 */
public class IRESchema extends TermSchema {
    public static final String BASE_NAME = "IRE";
    private static IRESchema   baseSchema = new IRESchema();
    
    public static final String VARIABLE = "Variable";
    public static final String PROPOSITION = "Proposition";

    /**
     * Construct a schema that vinculates an entity to be a generic
     * ire
     */
    private IRESchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>IRESchema</code> with a given type-name.
     * All ire-s have a variable and a proposition.
     * @param typeName The name of this <code>IRESchema</code> 
     * (e.g. IOTA, ANY, ALL).
     */
    public IRESchema(String typeName) {
        super(typeName);

        add(VARIABLE, TermSchema.getBaseSchema()); 
        add(PROPOSITION, PredicateSchema.getBaseSchema());
    }

    /**
     * Retrieve the generic base schema for all ire-s.
     * @return the generic base schema for all ire-s.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold a ire of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsIRE(getTypeName());
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
  		if (!(abs instanceof AbsIRE)) {
				throw new OntologyException(abs+" is not an AbsIRE");
			}
			
			// Check the slots
			validateSlots(abs, onto);
  	}
  	
  	/**
  	   An IRE can be put whereever a term of whatever type is
  	   required --> An IRESchema is
  	   compatible with s if s descends from TermSchema.getBaseSchema()
  	 */
  	public boolean isCompatibleWith(ObjectSchema s) {
  		if (s != null) {
  			return s.descendsFrom(TermSchema.getBaseSchema());
  		}
  		else {
  			return false;
  		}
  	}
  		
  	/**
  	   Return true if 
  	   - s is the base schema for the XXXSchema class this schema is
  	     an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
  	     schema is an instance of ConceptSchema)
  	   - s is the base schema for a super-class of the XXXSchema class
  	     this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
  	     and this schema is an instance of ConceptSchema.
  	   Moreover, as IRESchema extends ContentElementSchema, but should
  	   also extend TermSchema (this is not possible in practice as
  	   Java does not support multiple inheritance), this method
  	   returns true also in the case that s is equals to, or is an
  	   ancestor of, TermSchema.getBaseSchema() (i.e. TermSchema.getBaseSchema()
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
  			// An IRE is also a ContentElement
  			return ContentElementSchema.getBaseSchema().descendsFrom(s);
  		}
  		else {
  			return false;
  		}
  	}
}
