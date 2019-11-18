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
import jade.util.leap.Collection;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import java.lang.reflect.*;

/**
 * Backward Compatible reflective introspector. This Introspector 
 * uses Java Reflection to translate java objects to/from abstract
 * descriptors as the <code>ReflectiveIntrospector</code> does, but 
 * it assumes the accessors methods for aggregate slots to be in the 
 * "old JADE style" i.e.
 * <i> For every aggregate <b>slot</b> named <code>XXX</code>,
 * with elements of type <code>T</code>, the Java class must have 
 * two accessible methods, with the following signature:</i>
 * <ul>
 *  	<li> <code>Iterator getAllXXX()</code>
 *  	<li> <code>void addXXX(T t)</code>
 * </ul> 
 * <br>
 * <b>NOT available in MIDP</b>
 * <br>
 * @author Giovanni Caire - TILAB
 */
public class BCReflectiveIntrospector extends ReflectiveIntrospector {
	
	protected boolean isAggregateObject(Object obj) {
		//#J2ME_EXCLUDE_BEGIN
		return obj instanceof java.util.Iterator;
		//#J2ME_EXCLUDE_END
		/*#J2ME_INCLUDE_BEGIN
		return obj instanceof Iterator;
		#J2ME_INCLUDE_END*/
	}
	
	public Object getSlotValue(String slotName, Object obj, ObjectSchema schema) throws OntologyException {
		ObjectSchema slotSchema = schema.getSchema(slotName);
		if (slotSchema != null) {
			if (slotSchema instanceof AggregateSchema) {
				return getAggregateSlotValue(slotName, obj);
			}
			else {
				return getScalarSlotValue(slotName, obj);
			}
		}
		else {
			throw new OntologyException("No slot named "+slotName+" found in schema "+schema.getTypeName());
		}
	}
	
	private Object getScalarSlotValue(String slotName, Object obj) throws OntologyException {
		String methodName = "get" + translateName(slotName);
		Method getMethod = findMethodCaseInsensitive(methodName, obj.getClass());
		return invokeAccessorMethod(getMethod, obj);
	}

	private Object getAggregateSlotValue(String slotName, Object obj) throws OntologyException {
		String methodName = "getAll" + translateName(slotName);
		Method getMethod = findMethodCaseInsensitive(methodName, obj.getClass());
		return invokeAccessorMethod(getMethod, obj);
	}
	
	public void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) throws OntologyException {
		ObjectSchema slotSchema = schema.getSchema(slotName);
		if (slotSchema != null) {
			String methodName;
			// Note that here checking if absSlotValue is an AbsAggregate would be wrong as we have add methods only if the schema of the slot is AggregateSchema
			if (slotSchema instanceof AggregateSchema) {
				// FIXME: Here we should check for Long --> Integer casting, but how?
				methodName = "add" + translateName(slotName);
				Method addMethod = findMethodCaseInsensitive(methodName, obj.getClass());
				invokeAddMethod(addMethod, obj, slotValue);
			}
			else {
				methodName = "set" + translateName(slotName);
				Method setMethod = findMethodCaseInsensitive(methodName, obj.getClass());
				invokeSetterMethod(setMethod, obj, slotValue);
			}
		}
		else {
			throw new OntologyException("No slot named "+slotName+" found in schema "+schema.getTypeName());
		}
	}
	
	private void invokeAddMethod(Method method, Object obj, 
			Object value) throws OntologyException {
		try {
			Collection c = (Collection) value;
			
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Object ithValue = it.next();
				invokeSetterMethod(method, obj, ithValue);
			}
		} 
		catch (ClassCastException cce) {
			throw new OntologyException("Can't apply recursively method "+method.getName()+" to object "+obj+" as value "+value+" is not a List", cce);
		} 
	} 
	
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
		String[] slotNames = schema.getNames();
		
		for (int i = 0; i < slotNames.length; ++i) {
			String sName = slotNames[i];
			ObjectSchema slotSchema = schema.getSchema(sName);
			String mName = translateName(sName);
			try {
				// Check for correct set and get methods for the current
				// slot and retrieve the implementation type for values.
				Class slotGetSetClass;
				if (slotSchema instanceof AggregateSchema)
					slotGetSetClass = checkGetAndSet2(mName, javaClass);
				else
					slotGetSetClass = checkGetAndSet(mName, javaClass);
				// If slotSchema is a complex schema and some class C is registered 
				// for that schema, then the implementation class must be a supertype 
				// of C.
				if(!(slotSchema instanceof PrimitiveSchema)) { 
					Class slotClass = onto.getClassForElement(slotSchema.getTypeName());
					if (slotClass != null) {
						if(!slotGetSetClass.isAssignableFrom(slotClass)) {
							throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": expected class="+slotClass+", Get/Set method class="+slotGetSetClass);
						}
					}
				} 
				else {	
					// The slot has a primitive type
					String type = slotSchema.getTypeName();
					if (type.equals(BasicOntology.STRING)) {
						if (!slotGetSetClass.isAssignableFrom(String.class)) { 
							throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": expected class="+String.class+", Get/Set method class="+slotGetSetClass);
						}
					}
					else if (type.equals(BasicOntology.INTEGER)) {
						if ((!slotGetSetClass.equals(Integer.TYPE)) &&
								(!slotGetSetClass.equals(Integer.class)) &&
								(!slotGetSetClass.equals(Long.TYPE)) &&
								(!slotGetSetClass.equals(Long.class)) ) { 
							throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": expected class=INTEGER, Get/Set method class="+slotGetSetClass);
						}
					}
				}
			}
			catch(Exception e) {
				throw new OntologyException("Wrong class for schema: "+schema.getTypeName()+". Slot "+sName+": unexpected error. "+e.getMessage()); 
			}
		}
	}
	
	/**
	 */
	private Class checkGetAndSet(String name, Class c) throws OntologyException {
		Class result;
		Method getMethod = findMethodCaseInsensitive("get" + name, c);
		Method setMethod = findMethodCaseInsensitive("set" + name, c);
		
		// Make sure "get" method takes no arguments.
		Class[] getParams = getMethod.getParameterTypes();
		if(getParams.length > 0)
			throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must take no arguments.");
		
		// Now find a matching set method.
		result = getMethod.getReturnType();
		
		Class[] setParams = setMethod.getParameterTypes();
		if((setParams.length != 1) || (!setParams[0].equals(result)))
			throw new OntologyException("Wrong class: method " +  setMethod.getName() + "() must take a single argument of type " + result.getName() + ".");
		Class setReturn = setMethod.getReturnType();
		if(!setReturn.equals(Void.TYPE))
			throw new OntologyException("Wrong class: method " +  setMethod.getName() + "() must return void.");
		
		return result;
	}
	
	/**
	 */
	private Class checkGetAndSet2(String name, Class c) throws OntologyException {
		Method getMethod = findMethodCaseInsensitive("getAll" + name, c);
		Method addMethod = findMethodCaseInsensitive("add" + name, c);
		Class result = getArgumentType(addMethod,0);  
		
		// check "get" method 
		if (getArgumentLength(getMethod) != 0)
			throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must take no arguments.");
		// MODIFIED by GC
		// The return value of the getAllXXX() method of the user defined class 
		// must be a jade.util.leap.Iterator or a super-class/interface of it -->
		// OK if it is a java.util.Iterator.
		if (!(getReturnType(getMethod)).isAssignableFrom(jade.util.leap.Iterator.class))
			throw new OntologyException("Wrong class: method " +  getMethod.getName() + "() must return a jade.util.leap.Iterator." + getReturnType(getMethod).toString());
		
		// check 'add' method 
		if (getArgumentLength(addMethod) != 1)
			throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() must take one argument.");
		if (!getArgumentType(addMethod,0).equals(result))
			throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() has the wrong argument type.");
		if (!getReturnType(addMethod).equals(Void.TYPE))
			throw new OntologyException("Wrong class: method " +  addMethod.getName() + "() must return a void.");
		
		return result;
	}
	
	/**
	 @ return the Class of the argument type number no. of the method m
	 */
	private Class getArgumentType(Method m, int no) {
		Class[] setParams = m.getParameterTypes();
		return setParams[no];
	}
	
	/**
	 * @return the number of arguments of the method m
	 */
	private int getArgumentLength(Method m) {
		Class[] getParams = m.getParameterTypes();
		return getParams.length;
	}
	
	/**
	 @ return the Class of the return type of the method m
	 */
	private Class getReturnType(Method m) {
		return m.getReturnType();
	}
	
	public AbsAggregate externalizeAggregate(String slotName, Object obj, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
		if (!isAggregateObject(obj)) {
			throw new NotAnAggregate();
		}
		
		AbsAggregate absAggregateValue = null;
		//#J2ME_EXCLUDE_BEGIN
		java.util.Iterator it = (java.util.Iterator) obj;
		//#J2ME_EXCLUDE_END
		/*#J2ME_INCLUDE_BEGIN
		Iterator it = (Iterator) obj;
		#J2ME_INCLUDE_END*/
		if (it.hasNext() || schema.isMandatory(slotName)) {
			String slotSchemaTypeName = schema.getSchema(slotName).getTypeName();
			absAggregateValue = new AbsAggregate(slotSchemaTypeName);
			try {
				while(it.hasNext())
					absAggregateValue.add((AbsTerm)Ontology.externalizeSlotValue(it.next(), this, referenceOnto));
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
}

