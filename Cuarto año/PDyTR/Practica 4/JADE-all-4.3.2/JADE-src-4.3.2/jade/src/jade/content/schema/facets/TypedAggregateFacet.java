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
package jade.content.schema.facets;

import jade.content.onto.*;
import jade.content.schema.*;
import jade.content.abs.*;
import jade.util.leap.Iterator;

/**
 * This facet forces the elements in an AbsAggregate
 * to be compliant to a given schema.
 * @author Giovanni Caire - TILAB
 */
public class TypedAggregateFacet implements Facet {
	private ObjectSchema type;
	
	/**
	   Construct a <code>TypedAggregateFacet</code> that forces
	   the elements in an AbsAggregate to be instances of a given 
	   schema
	 */
	public TypedAggregateFacet(ObjectSchema s) {
		type = s;
	}
	
	/**
	  Get the schema associated to this facet
	*/
	public ObjectSchema getType() {
		return type;
	}
	/**
	   Check whether a given value for the slot this Facet applies
	   to is valid.
	   @param value The value to be checked
	   @throws OntologyException If the value is not valid
	 */
	public void validate(AbsObject value, Ontology onto) throws OntologyException {
		if (value instanceof AbsVariable) {
			// A variable can always be used where whatever term is required
			return;
		}
		
  	if (!(value instanceof AbsAggregate)) {
  		throw new OntologyException(value+" is not an AbsAggregate");
  	}
  	
  	AbsAggregate agg = (AbsAggregate) value;
		Iterator it = agg.iterator();
		while (it.hasNext()) {
			AbsTerm el = (AbsTerm) it.next();
			ObjectSchema s = onto.getSchema(el.getTypeName());
			if (!s.isCompatibleWith(type)) {
				throw new OntologyException("Schema "+s+" for element "+el+" is not compatible with type "+type);
			}
		}
	}
}