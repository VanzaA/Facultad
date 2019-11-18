/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.content.onto;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.content.Term;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

class SlotAccessData implements Serializable{
	Class type;
	transient Method getter;
	transient Method setter;
	boolean aggregate;
	boolean mandatory;
	boolean manageAsSerializable;
	Class aggregateClass;
	int cardMin;
	int cardMax;
	Object defaultValue; 
	String regex;
	String[] permittedValues;
	String documentation;
	
	// Used only for the serialization
	Class declaringClass;
	String getterName;
	String setterName;

	SlotAccessData(Class type, Method getter, Method setter, boolean mandatory, Class aggregateClass, int cardMin, int cardMax, Object defaultValue, String regex, String[] permittedValues, String documentation, boolean manageAsSerializable) {
		this.type = type;
		this.getter = getter;
		this.setter = setter;
		aggregate = isAggregate(type);
		this.mandatory = mandatory;
		this.manageAsSerializable = manageAsSerializable;
		this.aggregateClass = aggregateClass;
		this.cardMin = cardMin;
		this.cardMax = cardMax;
		this.defaultValue = defaultValue;
		this.regex = regex;
		this.permittedValues = permittedValues;
		this.documentation = documentation;
		
		declaringClass = getter.getDeclaringClass();
		getterName = getter.getName(); 
		setterName = setter.getName();
	}

	boolean isTypized() {
		return type != null && type != Object.class && type != Term.class;
	}
	
	static boolean isAggregate(Class clazz) {
		return (clazz.isArray() && clazz != byte[].class) || java.util.Collection.class.isAssignableFrom(clazz) || jade.util.leap.Collection.class.isAssignableFrom(clazz);
	}

	static boolean isSequence(Class clazz) {
		return (clazz.isArray() && clazz != byte[].class) || java.util.List.class.isAssignableFrom(clazz) || jade.util.leap.List.class.isAssignableFrom(clazz);
	}

	static boolean isSet(Class clazz) {
		return java.util.Set.class.isAssignableFrom(clazz) || jade.util.leap.Set.class.isAssignableFrom(clazz);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SlotAccessData {");
		sb.append("type=");
		sb.append(type.getName());
		sb.append(" getter=");
		sb.append(getter.getName());
		sb.append(" setter=");
		sb.append(setter.getName());
		sb.append(" aggregate=");
		sb.append(aggregate);
		sb.append(" aggregateClass=");
		sb.append(aggregateClass != null ? aggregateClass.getName() : null);
		sb.append(" mandatory=");
		sb.append(mandatory);
		sb.append(" cardMin=");
		sb.append(cardMin);
		sb.append(" cardMax=");
		sb.append(cardMax);
		if (defaultValue != null) {
			sb.append(" defaultValue=");
			sb.append(defaultValue);
		}
		if (regex != null) {
			sb.append(" regex=");
			sb.append(regex);
		}
		if (permittedValues != null) {
			sb.append(" permittedValues=");
			sb.append(permittedValues);
		}
		sb.append('}');
		return sb.toString();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		try {
			getter = declaringClass.getMethod(getterName, (Class[])null);
			setter = declaringClass.getMethod(setterName, type);
		} catch (Exception e) {
			// Should never happen
			e.printStackTrace();
			throw new IOException("Error deserializing ontology associated to class "+declaringClass);
		}	
	}
}