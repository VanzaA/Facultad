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



package jade.domain.FIPAAgentManagement;

//#J2ME_EXCLUDE_BEGIN
import java.util.List;
import java.util.Properties;
//#J2ME_EXCLUDE_END

import jade.content.Concept;


/** 
 This class implements the <code>property</code> type, a pair of a
 name and value.
 @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
 @author Fabio Bellifemine - CSELT S.p.A.
 @version $Date: 2013-11-06 10:33:23 +0100 (mer, 06 nov 2013) $ $Revision: 6692 $
 */
public class Property implements Concept {

	private String name;
	private Object value;

	/**
       Default constructor. A default constructor is needed for JADE
       ontological classes.
	 */
	public Property() {
	}

	/**
       Create a property object, with the given name and value pair.
       @param name The name of the property.
       @param value The Java object associated with the given name.
	 */
	public Property(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
       Set the name of the property object.
       @param n The new name for this property.
	 */
	public void setName(String n) {
		name = n;
	}

	/**
       Retrieve the name of this property object.
       @return The string that is the name of this property, or
       <code>null</code> if no name was set.
	 */
	public String getName() {
		return name;
	}

	/**
       Set the value for this property object, attached to the
       property name.
       @param o The new Java object to attach to the property name.
	 */
	public void setValue(Object o) {
		value = o;
	}

	/**
       Retrieve the value of this property object, associated with the
       property name.
       @return The value of this property, or <code>null</code> if no
       value was set.
	 */
	public Object getValue() {
		return value;
	}

	//#J2ME_EXCLUDE_BEGIN
	// For persistence only
	private void setsvalue(java.io.Serializable s) {
		value = s;
	}

	private java.io.Serializable getsvalue() {
		return (java.io.Serializable) value;
	}

	public static Properties convertPropertyListToProperties(List<Property> pp) {
		if (pp == null) {
			return null;
		}
		else {
			Properties props = new Properties();
			for (Property p : pp) {
				props.put(p.getName(), p.getValue());
			}
			return props;
		}
	}
	
	//#J2ME_EXCLUDE_END
	
	//#MIDP_EXCLUDE_BEGIN
	public boolean match(Property p) {
		boolean result = false;
		if (name.equals(p.name)) {
			// The property name matches. Check the value
			if (value == null) {
				result = true;
			}
			else if (value instanceof String) {
				result = ((String)value).equalsIgnoreCase(p.value.toString());
			}
			else {
				result = value.equals(p.value);
			}
		}
		return result;
	}
	//#MIDP_EXCLUDE_END
	
	public Object clone(){
		return new Property(name, value);
	}
	
}
