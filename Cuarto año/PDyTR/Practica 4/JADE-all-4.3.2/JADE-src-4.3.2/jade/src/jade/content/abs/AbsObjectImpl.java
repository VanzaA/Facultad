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
package jade.content.abs;

import jade.core.CaseInsensitiveString;
import jade.util.leap.ArrayList;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;

/**
 * Base class for all non-primitive abstract descriptor classes.
 * This class is not intended to be used by programmers.
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public class AbsObjectImpl implements AbsObject {
	private HashMap elements = new HashMap();
	/** This list keeps the keys in the same order as they were added **/
	private ArrayList orderedKeys = new ArrayList();
	private String    typeName = null;
	/** true if this object is changed and its hash must be recomputed **/
	private boolean changed = true;
	private int hashCode = 0;

	/**
	 * Construct an Abstract descriptor to hold an object of
	 * the proper type.
	 * @param typeName The name of the type of the object held by this
	 * abstract descriptor.
	 */
	protected AbsObjectImpl(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return The name of the type of the object held by this
	 * abstract descriptor.
	 * @see AbsObject#getTypeName()
	 */
	public String getTypeName() {
		return typeName;
	} 

	void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	/**
	 * Sets an attribute of the object held by this
	 * abstract descriptor.
	 * @param name The name of the attribute to be set.
	 * @param value The new value of the attribute. If <code>value</code>
	 * is null the current mapping with <code>name</code> (if any) is
	 * removed.
	 */
	protected void set(String name, AbsObject value) {
		CaseInsensitiveString ciName = new CaseInsensitiveString(name);
		if (value == null) {
			orderedKeys.remove(name);
			elements.remove(ciName);
		}
		else {
			if (!orderedKeys.contains(name)) {
				orderedKeys.add(name);
			}
			elements.put(ciName, value);
		}
		changed = true;
	} 

	/**
	 * Gets the value of an attribute of the object held by this
	 * abstract descriptor.
	 * @param name The name of the attribute.
	 * @return value The value of the attribute.
	 * @see AbsObject#getAbsObject(String)
	 */
	public AbsObject getAbsObject(String name) {
		return (AbsObject) elements.get(new CaseInsensitiveString(name));
	} 

	/**
	 * @return the name of all attributes.
	 * @see AbsObject#getNames()
	 */
	public String[] getNames() {
		String[] names = new String[orderedKeys.size()];
		int j=0;
		for (Iterator i=orderedKeys.iterator(); i.hasNext(); )
			names[j++] = (String)i.next();
		return names;
	} 

	/**
	 * Tests if the object is grounded, i.e., if no one of its attributes 
	 * is associated with a variable
	 * @return <code>true</code> if the object is grounded.
	 * @see AbsObject#isGrounded()
	 */
	public boolean isGrounded() {
		for (Iterator i = elements.values().iterator(); i.hasNext(); ) {
			AbsObject abs = (AbsObject) i.next();
			if (!abs.isGrounded()) {
				return false;
			}
		}
		return true;
	} 

	/**
	 * Gets the number of attributes.
	 * @return the number of attributes.
	 * @see AbsObject#getCount()
	 */
	public int getCount() {
		return elements.size();
	} 

	/**
	 * This method is here just for debugging. Notice that it is highly innefficient.
	 * The method StringCodec.encode() should be used instead.
	 * @see StringCodec.encode(AbsContentElement content)
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("(");
		sb.append(getTypeName());

		String[] names = getNames();

		for (int i = 0; i < names.length; i++) {
			sb.append(" :");
			sb.append(names[i]);
			sb.append(" ");
			sb.append(getAbsObject(names[i]));
		}
		sb.append(")");

		return sb.toString();
	}

	//ADDED BY SANDER FAAS:
	/**
	 * Returns true if the attribute is equal to
	 * this abstract descriptor, based on the contents
	 * of both descriptors.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AbsObjectImpl) {
			AbsObjectImpl abs = (AbsObjectImpl) obj;
			if (abs.getClass().equals(getClass()) && abs.getTypeName().equals(typeName)) {
				return f(abs) == f(this);
			}
		}
		return false;
	}

	/**
	 * Returns an integer hashcode calculated from the 
	 * contents of this abstract descriptor
	 */
	public int hashCode() {
		// if this object is changed, then recompute its hash, otherwise use the previous value
		if (changed) {
			hashCode = f(this);
			changed = false;
		}
		return hashCode;
	}

	/**
	 * Calculates the hashcode according to a formula based on the 
	 * slot names and values taken in an lexicographical order
	 */
	private final int f(AbsObjectImpl o, int x) {
		String slotNames[] = o.getNames();
		sort(slotNames);
		int[] v = new int[2*slotNames.length + 1];
		int j=0;
		for(int i = 0; i < slotNames.length; i++)
		{
			v[j++] = slotNames[i].hashCode();
			v[j++] = o.getAbsObject(slotNames[i]).hashCode();
		}
		v[j++] = o.getTypeName().hashCode();

		int sum = 0;
		int counter = 0;
		for (int i=0; i<v.length; i++ ) {
			sum += v[i] * x^counter;
			counter++;
		}
		return sum;
	}

	private final int f(AbsObjectImpl o) {
		return f(o, 2);
	} 

	private final void sort(String[] strs) {
		for (int i = 1; i < strs.length; ++i) {
			for (int j = i; j > 0 && (strs[j-1].compareTo(strs[j]) > 0); --j) {
				swap(strs, j, j-1);
			}
		}
	}

	private final void swap(String[] strs, int x, int y) {
		String t = strs[x];
		strs[x] = strs[y];
		strs[y] = t;
	}
	
    public int getAbsType() {
    	return UNKNOWN;
    }
}

