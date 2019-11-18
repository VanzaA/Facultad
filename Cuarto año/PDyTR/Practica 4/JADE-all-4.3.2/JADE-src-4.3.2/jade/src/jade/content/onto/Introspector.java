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

import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.Serializable;

/** 
   This interface defines the methods to convert objects of
   ontological classes into/from abstract descriptors. Each ontology
   has an <code>Introspector</code> and delegates it the conversion.
   @author Federico Bergenti - Universita` di Parma
 */
public interface Introspector extends Serializable {

	/**
       Check the structure of a java class associated to an ontological element 
       to ensure that translations to/from abstract descriptors and java objects
       (instances of that class) can be accomplished by this introspector.
       @param schema The schema of the ontological element
       @param javaClass The java class associated to the ontologcal element
       @param onto The Ontology that uses this Introspector
       @throws OntologyException if the java class does not have the correct 
       structure
	 */
	void checkClass(ObjectSchema schema, Class javaClass, Ontology onto) 
	throws OntologyException;

	Object getSlotValue(String slotName, Object obj, ObjectSchema schema) 
	throws OntologyException;

	void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) 
	throws OntologyException;
	
	AbsAggregate externalizeAggregate(String slotName, Object obj, ObjectSchema schema, Ontology referenceOnto) 
	throws OntologyException;

	Object internalizeAggregate(String slotName, AbsAggregate abs, ObjectSchema schema, Ontology referenceOnto) 
	throws OntologyException;

	AbsObject externalizeSpecialType(Object obj, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
	throws OntologyException;

	Object internalizeSpecialType(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) 
	throws OntologyException;
}

