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

import jade.content.schema.ContentElementListSchema;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import java.io.PrintStream;

/**
 * An abstract descriptor that can hold a list of
 * generic content element expressions.
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public class AbsContentElementList implements AbsContentElement {
	private List elements = new ArrayList();
	private String    typeName = null;
	private boolean isAContentExpression = false;

	/**
	 * Construct an Abstract descriptor to hold a content element list      
	 */
	public AbsContentElementList() {
		typeName = ContentElementListSchema.BASE_NAME;
	}

	/**
	 * Add a new element (that must be a content element) to this 
	 * content element list.
	 * @param element The element to add.
	 */
	public void add(AbsContentElement element) {
		elements.add(element);
	} 

	/**
	 * Retrieves the number of elements in this content element list.
	 * @return The number of elements.
	 */
	public int size() {
		return elements.size();
	} 

	/**
	 * Retrieves the <code>i</code>-th element in this content element list.
	 * @param i The index of the element to retrieve.
	 * @return The element.
	 */
	public AbsContentElement get(int i) {
		return (AbsContentElement) elements.get(i);
	} 

	/**
	 * @return An <code>Iterator</code> over the elements of this
	 * content element list.
	 */
	public Iterator iterator() {
		return elements.iterator();
	} 

	/**
	 * Clear all the elements in this content element list.
	 */
	public void clear() {
		elements.clear();
	}

	/**
	 * Test if a given content element is contained in this 
	 * content element list.
	 * @return <code>true</code> if the given content element is contained
	 * in this content element list.
	 */
	public boolean contains (AbsContentElement element) {
		return elements.contains(element);
	}

	/**
	 * Returns the position of an element within this content element list.
	 * @return The position of an element within this content element list
	 * or -1 if the given element is not contained in this content element 
	 * list.
	 */
	public int indexOf (AbsContentElement element) {
		return elements.indexOf(element);
	}

	/**
	 * Removes the element at the given position from this content
	 * element list.
	 * @return The removed element.
	 */
	public AbsContentElement remove (int index) {
		return (AbsContentElement)elements.remove(index);
	}

	/**
	 * Test if the content element list is empty.
	 * @return <code>true</code> if this content element list does 
	 * not contain any element.
	 */
	public boolean isEmpty () {
		return elements.isEmpty();
	}

	/**
	 * Retrieve all elements in this content element list in the form of 
	 * an array.
	 * @return An array containing all elements in this content element 
	 * list.
	 */
	public AbsContentElement[] toArray () {
		int size = elements.size();
		AbsContentElement[] tmp = new AbsContentElement[size];
		for (int i = 0; i < size; i++)
			tmp[i] = (AbsContentElement)elements.get(i);
		return tmp;
	}

	/*protected void dump(int indent, PrintStream ps) {
    	ps.println(toString());
    }*/

	/**
	 * @return The name of the type of the object held by this
	 * abstract descriptor.
	 * @see AbsObject#getTypeName()
	 */
	public String getTypeName() {
		return typeName;
	} 

	/**
       Makes no sense in the case of an AbsContentElementList that has no attribute
       --> Just return null
	 */
	public AbsObject getAbsObject(String name) {
		return null;
	}

	/**
       Makes no sense in the case of an AbsContentElementList that has no attribute
       --> Just return null
	 */
	public String[] getNames() {
		return null;
	}

	/**
	 * Tests if this AbsContentElementList is grounded, i.e., if no one of its elements 
	 * is associated with a variable
	 * @return <code>true</code> if the object is grounded.
	 */
	public boolean isGrounded() {
		Iterator it = elements.iterator();
		while (it.hasNext()) {
			AbsContentElement abs = (AbsContentElement) it.next();
			if (!abs.isGrounded()) {
				return false;
			}
		}
		return true;
	}

	/**
       Return the number of elements in the list. Is equivalent to 
       <code>size()</code>
	 */
	public int getCount() {
		return size();
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
			sb.append(" #");
			sb.append(i);
			sb.append(" ");
			sb.append(it.next());
			++i;
		}
		sb.append(")");
		return sb.toString();
	}

	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * @see AbsContent.isAContentExpression
	 */
	public boolean isAContentExpression() {
		return isAContentExpression;
	}

	/**
	 * @see AbsContent.setIsAContentExpression(boolean flag)
	 */
	public void setIsAContentExpression(boolean flag) {
		isAContentExpression = flag;
	}
	
    public int getAbsType() {
    	return ABS_CONTENT_ELEMENT_LIST;
    }
}

