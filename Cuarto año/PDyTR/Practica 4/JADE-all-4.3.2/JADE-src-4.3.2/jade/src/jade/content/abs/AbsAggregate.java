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

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import java.io.PrintStream;
import jade.content.lang.Codec;

/**
 * An abstract descriptor that can hold an aggregate entitiy
 * expression.
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public class AbsAggregate extends AbsConcept {
	private static final int UNNAMEDPREFIX_LENGTH = Codec.UNNAMEDPREFIX.length();
	private List elements = new ArrayList();
	private AbsTerm elementTemplate;
	
	/**
	 * Construct an Abstract descriptor to hold an aggregate of
	 * the proper type (i.e. SET, SEQUENCE...).
	 * @param typeName The name of the type of the aggregate held by 
	 * this abstract descriptor.
	 */
	public AbsAggregate(String typeName) {
		super(typeName);
	}

	/**
	 * Adds a new element (that must be a term) to this aggregate.
	 * @param element The element to add.
	 */
	public void add(AbsTerm element) {
		elements.add(element);
	} 

	/**
	 * Retrieves the number of elements in this aggregate.
	 * @return The number of elements.
	 */
	public int size() {
		return elements.size();
	} 

	/**
	 * Retrieves the <code>i</code>-th element in this aggregate.
	 * @param i The index of the element to retrieve.
	 * @return The element.
	 */
	public AbsTerm get(int i) {
		return (AbsTerm) elements.get(i);
	} 

	/**
	 * @return An <code>Iterator</code> over the elements of this
	 * aggregate.
	 */
	public Iterator iterator() {
		return elements.iterator();
	} 

	/**
	 * Clear all the elements in this aggregate.
	 */
	public void clear() {
		elements.clear();
	}

	/**
	 * Test if a given term is contained in this aggregate.
	 * @return <code>true</code> if the given term is contained
	 * in this aggregate.
	 */
	public boolean contains (AbsTerm element) {
		return elements.contains(element);
	}

	/**
	 * Returns the position of an element within this aggregate.
	 * @return The position of an element within this aggregate.
	 */
	public int indexOf (AbsTerm element) {
		return elements.indexOf(element);
	}

	/**
	 * Removes the element at the given position from this aggregate.
	 * @return The removed element.
	 */
	public AbsTerm remove (int index) {
		return (AbsTerm)elements.remove(index);
	}

	/**
	 * Removes an element from this aggregate.
	 * @return The removed element.
	 */
	public boolean remove (AbsTerm element) {
		return elements.remove(element);
	}

	/**
	 * Test if the aggregate is empty.
	 * @return <code>true</code> if this aggregate does not contain
	 * any element.
	 */
	public boolean isEmpty () {
		return elements.isEmpty();
	}

	/**
	 * Retrieve all elements in this aggregate in the form of an array.
	 * @return An array containing all elements in this aggregate.
	 */
	public AbsTerm[] toArray () {
		int size = elements.size();
		AbsTerm[] tmp = new AbsTerm[size];
		for (int i = 0; i < size; i++)
			tmp[i] = (AbsTerm)elements.get(i);
		return tmp;
	}

	/**
       Overrides this method to check that name is of the form
       Codec.UNNAMEDPERFIX+index
       @throws IllegalArgumentException if name is not of the form
       Codec.UNNAMEDPERFIX+index
	 */
	public void set(String name, AbsTerm value) {
		elements.add(toIndex(name), value);	
	} 

	private int toIndex(String name) { 
		try {
			if (name.startsWith(Codec.UNNAMEDPREFIX) ) {
				String index = name.substring(UNNAMEDPREFIX_LENGTH);
				return Integer.parseInt(index);
			}
			else {
				throw new IllegalArgumentException(name+" is not a valid index");
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException(name+" is not a valid index");
		}
	}

	protected void dump(int indent, PrintStream ps) {
		ps.println(toString());
	} 


	/**
       Overrides this method to check that name is of the form
       Codec.UNNAMEDPERFIX+index
	 */
	public AbsObject getAbsObject(String name) {
		return (AbsObject) elements.get(toIndex(name));
	}

	/**
       Overrides method in superclass
	 */
	public String[] getNames() {
		String names[] = new String[elements.size()];
		for (int i = 0; i < names.length; ++i) {
			names[i] = Codec.UNNAMEDPREFIX+i;
		}
		return names;
	}

	/**
	 * Tests if this AbsAggregate is grounded, i.e., if no one of its elements 
	 * is associated with a variable
	 * @return <code>true</code> if the object is grounded.
	 */
	public boolean isGrounded() {
		Iterator it = elements.iterator();
		while (it.hasNext()) {
			AbsTerm abs = (AbsTerm) it.next();
			if (!abs.isGrounded()) {
				return false;
			}
		}
		return true;
	}

	/**
       Overrides method in superclass
	 */
	public int getCount() {
		return elements.size();
	}

    public int getAbsType() {
    	return ABS_AGGREGATE;
    }

    /**
     * Get the abstract template of aggregate element
     */
	public AbsTerm getElementTemplate() {
		return elementTemplate;
	}

	/**
	 * Set the abstract template of aggregate element
	 */
	public void setElementTemplate(AbsTerm elementTemplate) {
		this.elementTemplate = elementTemplate;
	}

	/**
	 * This method is here just for debugging. Notice that it is highly innefficient.
	 * The method StringCodec.encode() should be used instead.
	 * @see StringCodec.encode(AbsContentElement content)
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("(");
		sb.append(getTypeName());
		Iterator it = elements.iterator();
		int i = 0;
		while (it.hasNext()) {
			sb.append(" #"+i+" "+it.next());
			++i;
		}
		sb.append(")");
		return sb.toString();
	}
	
}

