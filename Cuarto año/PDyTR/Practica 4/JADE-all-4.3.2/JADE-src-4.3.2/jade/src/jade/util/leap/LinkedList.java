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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Vector;
import java.util.Enumeration;

/**
   The LEAP (environment-dependent) version of the &qote;java.util.ArrayList&qote; class.
   This class appears to be exactly the same in J2SE, PJAVA and MIDP.
   The internal implementation is different in the three cases however.
   
   @author  Nicolas Lhuillier
   @version 1.0, 29/09/00
   
   @see java.util.LinkedList
 */
public class LinkedList implements List, Serializable {
    private transient java.util.List realHiddenList = null;
    private Vector                         hiddenList;
    
    // This is needed to ensure compatibility with the J2ME version of 
    // this class in serialization/deserialization operations
    private static final long              serialVersionUID = 3487495895819394L;

    /**
     * Default Constructor, creates an empty List
     */
    public LinkedList() {
        realHiddenList = new java.util.LinkedList();
    }

    /**
     * @see jade.util.leap.List interface
     */
    public void clear() {
        realHiddenList.clear();
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public boolean contains(Object o) {
        return realHiddenList.contains(o);
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public Object get(int index) {
        return realHiddenList.get(index);
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public int indexOf(Object o) {
        return realHiddenList.indexOf(o);
    } 

    /**
     * @see jade.util.leap.List interface
     */
    public Object remove(int index) {
        return realHiddenList.remove(index);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean add(Object o) {
        return realHiddenList.add(o);
    } 

		/**
		   Inserts the specified element at the specified position in this list
     */
    public void add(int index, Object o) {
    	realHiddenList.add(index, o);
    }
    
    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean isEmpty() {
        return realHiddenList.isEmpty();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean remove(Object o) {
        return realHiddenList.remove(o);
    } 

    public String toString() {
    	return realHiddenList.toString();
    }
    
    /**
     * @see jade.util.leap.Collection interface
     */
    public Iterator iterator() {
        return new Iterator() {
            java.util.Iterator it = LinkedList.this.realHiddenList.iterator();

            /**
             * Method declaration
             * 
             * @return
             * 
             * @see
             */
            public boolean hasNext() {
                return it.hasNext();
            } 

            /**
             * Method declaration
             * 
             * @return
             * 
             * @see
             */
            public Object next() {
                return it.next();
            } 

            /**
             * Method declaration
             * 
             * @see
             */
            public void remove() {
                it.remove();
            } 

        };
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public Object[] toArray() {
        return realHiddenList.toArray();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public int size() {
        return realHiddenList.size();
    } 

    /**
     * Removes and returns the first element from this list.
     * 
     * @return the first element from this list.
     * @throws NoSuchElementException if this list is empty.
     */
    public Object removeFirst() {
        return ((java.util.LinkedList)realHiddenList).removeFirst();
    } 

    /**
     * Inserts the given element at the beginning of this list.
     * 
     * @param o the element to be inserted at the beginning of this list.
     */
    public void addFirst(Object o) {
        ((java.util.LinkedList)realHiddenList).addFirst(o);
    } 

    /**
     * Appends the given element to the end of this list.  (Identical in
     * function to the <tt>add</tt> method; included only for consistency.)
     * 
     * @param o the element to be inserted at the end of this list.
     */
    public void addLast(Object o) {
        ((java.util.LinkedList)realHiddenList).addLast(o);
    } 

    // private Object writeReplace() throws java.io.ObjectStreamException {
    // return new LinkedListSerializer(this);
    // }

    /**
       A customized writeObject() method is needed to ensure compatibility with  
       the J2ME version of this class in serialization/deserialization 
       operations
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        hiddenList = new Vector();

        java.util.Iterator it = realHiddenList.iterator();

        while (it.hasNext()) {
            hiddenList.add(it.next());
        } 

        out.defaultWriteObject();
    } 

    /**
       A customized readObject() method is needed to ensure compatibility with  
       the J2ME version of this class in serialization/deserialization 
       operations
     */
    private void readObject(ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        realHiddenList = new java.util.LinkedList();

        in.defaultReadObject();

        Enumeration e = hiddenList.elements();

        while (e.hasMoreElements()) {
            realHiddenList.add(e.nextElement());
        } 
    }

    // For persistence service
    private void setData(java.util.List data) {
	realHiddenList = data;
    }

    // For persistence service
    private java.util.List getData() {
	return realHiddenList;
    }

}

