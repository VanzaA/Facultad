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
import jade.content.abs.AbsObject;
import jade.util.leap.Serializable;

/**
 * Represent a generic Facet (i.e. a constraint on the possible 
 * values) that can be applied on a slot in a schema of 
 * an ontological element.
 * @author Giovanni Caire - TILAB
 */
public interface Facet extends Serializable {
	/**
	   Check whether a given value for the slot this Facet applies
	   to is valid.
	   @param value The value to be checked
	   @throws OntologyException If the value is not valid
	 */
	void validate(AbsObject value, Ontology onto) throws OntologyException;
}