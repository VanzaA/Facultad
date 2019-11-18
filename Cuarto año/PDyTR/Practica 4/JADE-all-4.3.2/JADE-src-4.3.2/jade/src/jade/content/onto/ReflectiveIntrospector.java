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

import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.lang.reflect.*;
import jade.core.CaseInsensitiveString;

/**
   The default introspector for user defined ontologies that uses 
   Java Reflection to translate java objects to/from abstract
   descriptors.
   <br>
   <b>NOT available in MIDP</b>
   <br>
   @author Federico Bergenti - Universita` di Parma
   @author Giovanni Caire - TILAB
 */
public class ReflectiveIntrospector implements Introspector {
	
	public Object getSlotValue(String slotName, Object obj, ObjectSchema schema) throws OntologyException {
		String methodName = "get" + translateName(slotName);
		Method getMethod = findMethodCaseInsensitive(methodName, obj.getClass());
		return invokeAccessorMethod(getMethod, obj);
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected boolean isAggregateObject(Object obj) {
		return obj instanceof List;
	}

	protected Object invokeAccessorMethod(Method method, Object obj) throws OntologyException {
		try {
			return method.invoke(obj, (Object[]) null);
		} 
		catch (Exception e) {
			throw new OntologyException("Error invoking accessor method "+method.getName()+" on object "+obj, e);
		} 
	} 
	//#APIDOC_EXCLUDE_END

	public void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) throws OntologyException {
		String methodName = "set" + translateName(slotName);
		Method setMethod = findMethodCaseInsensitive(methodName, obj.getClass());
		invokeSetterMethod(setMethod, obj, slotValue);
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected void invokeSetterMethod(Method method, Object obj, 
			Object value) throws OntologyException {
		try {
			Object[] params = new Object[] {value};
			try {
				method.invoke(obj, params);
			}
			catch (IllegalArgumentException iae) {
				// Maybe the method required an int argument and we supplied 
				// a Long. Similarly maybe the method required a float and 
				// we supplied a Double. Try these possibilities
				params[0] = BasicOntology.adjustPrimitiveValue(value, method.getParameterTypes()[0]);

				method.invoke(obj, params);
			}
		} 
		catch (Exception e) {
			throw new OntologyException("Error invoking setter method "+method.getName()+" on object "+obj+" with parameter "+value, e);
		}
	} 
	//#APIDOC_EXCLUDE_END

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
	public void checkClass(ObjectSchema schema, Class javaClass, Ontology onto) throws OntologyException {
		// FIXME: Not yet implemented
	}

	//#APIDOC_EXCLUDE_BEGIN
	protected Method findMethodCaseInsensitive(String name, Class c) throws OntologyException {
		Method[] methods = c.getMethods();
		for(int i = 0; i < methods.length; i++) {
			String ithName = methods[i].getName();
			if(CaseInsensitiveString.equalsIgnoreCase(ithName, name))
				return methods[i];
		}
		throw new OntologyException("Method " + name + " not found in class "+c.getName());
	}

	protected String translateName(String name) {
		StringBuffer buf = new StringBuffer();

		// Capitalize the first char so that e.g. getxxx becomes getXxx 
		boolean capitalize = true;

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			switch (c) {
			case ':':
				// Just ignore it
				break;
			case '-':
				// Don't copy the character, but capitalize the next
				// one so that x-y becomes xY
				capitalize = true;
				break;
			default:
				if (capitalize) {
					buf.append(Character.toUpperCase(c));
					capitalize = false;
				} 
				else {
					buf.append(c);
				} 
			}
		} 
		return buf.toString();
	} 
	//#APIDOC_EXCLUDE_END
	
	public AbsAggregate externalizeAggregate(String slotName, Object obj, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
		if (!isAggregateObject(obj)) {
			throw new NotAnAggregate();
		}
		
		AbsAggregate absAggregateValue = null;
		List l = (List) obj;
		if (!l.isEmpty() || schema.isMandatory(slotName)) {
			String slotSchemaTypeName = schema.getSchema(slotName).getTypeName();
			absAggregateValue = new AbsAggregate(slotSchemaTypeName);
			try {
				for (int i = 0; i < l.size(); i++) {
					absAggregateValue.add((AbsTerm)Ontology.externalizeSlotValue(l.get(i), this, referenceOnto));
				}
			}
			catch (ClassCastException cce) {
				throw new OntologyException("Non term object in aggregate");
			}
		}
		return absAggregateValue;
	}

	public Object internalizeAggregate(String slotName, AbsAggregate abs, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
		List l = new ArrayList();
		for (int i = 0; i < abs.size(); i++) {
			Object element = Ontology.internalizeSlotValue(abs.get(i), this, referenceOnto);
			// Check if the element is a Term, a primitive an AID or a List
			Ontology.checkIsTerm(element);
			l.add(element);
		}
		return l;
	}

	public AbsObject externalizeSpecialType(Object obj, ObjectSchema schema, Class javaClass, Ontology referenceOnto) throws OntologyException {
		throw new NotASpecialType();
	}
	
	public Object internalizeSpecialType(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) throws OntologyException {
		throw new NotASpecialType();
	}
}

