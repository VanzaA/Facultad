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

package jade.util.leap;

/**
   The LEAP (environment-dependent) version of the &qote;java.util.Collection&qote; interface.
   This interface appears to be exactly the same in J2SE, PJAVA and MIDP.
   The internal implementation is different in the three cases however.
 */
public interface Collection {

    /**
     * Adds an element.
     * 
     * @return <tt>true</tt> if the element has been added.
     */
    boolean add(Object o);

    /**
     * Checks if the collection contains elements.
     * 
     * @return <tt>true</tt> if this collection contains no elements
     */
    boolean isEmpty();

    /**
     * Removes one instance of the specified element.
     * 
     * @param o the element to be removed
     * @return <tt>true</tt> if the element has been removed
     */
    boolean remove(Object o);

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned.
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    Iterator iterator();

    /**
     * Returns an array containing all of the elements in this collection.
     * 
     * @return an array containing all of the elements in this collection
     */
    Object[] toArray();

    /**
     * Returns the number of elements in this collection.
     * 
     * @return the number of elements in this collection.
     */
    int size();
}

