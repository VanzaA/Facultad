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
   The LEAP (environment-dependent) version of the &qote;java.util.List&qote; interface.
   This interface appears to be exactly the same in J2SE, PJAVA and MIDP.
   The internal implementation is different in the three cases however.
   
   @author  Nicolas Lhuillier
   @version 1.0, 23/10/00
   
   @see java.util.List (J2SE)
 */
public interface List extends Collection {

		/**
		   Inserts the specified element at the specified position in this list
     */
    void add(int index, Object o);
    
    /** 
     * Removes all of the elements from this list (optional operation).  This
     * list will be empty after this call returns.
     */
    void clear();

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * 
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    boolean contains(Object o);

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index &lt; 0 || index &gt;= size()).
     */
    Object get(int index);

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * 
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    int indexOf(Object o);

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     * 
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index &lt; 0 || index &gt;= size()).
     */
    Object remove(int index);
}

