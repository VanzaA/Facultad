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
package jade.content;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
 * Utility class to deal with a list of content elements as a
 * content element itself.
 * @author Federico Bergenti - Universita` di Parma
 */
public class ContentElementList implements ContentElement {
  	private List elements = new ArrayList();
	
  	/**
   	 * Construct a ContentElementList object 
   	 */
  	public ContentElementList() {
  	}
  
    /**
     * Adds a new element (that must be a content element) to this 
     * content element list.
     * @param element The element to add.
     */
    public void add(ContentElement element) {
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
    public ContentElement get(int i) {
        return (ContentElement) elements.get(i);
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
    public boolean contains (ContentElement element) {
			return elements.contains(element);
    }

   	/**
     * Returns the position of an element within this content element list.
     * @return The position of an element within this content element list
     * or -1 if the given element is not contained in this content element 
     * list.
     */
    public int indexOf (ContentElement element) {
			return elements.indexOf(element);
    }

   	/**
     * Removes the element at the given position from this content
     * element list.
     * @return The removed element.
     */
    public ContentElement remove (int index) {
			return (ContentElement)elements.remove(index);
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
    public ContentElement[] toArray () {
			int size = elements.size();
      ContentElement[] tmp = new ContentElement[size];
      for (int i = 0; i < size; i++)
      	tmp[i] = (ContentElement)elements.get(i);
      return tmp;
    }

}
