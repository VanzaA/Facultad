/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
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
 */

package jade.util.leap;

import jade.util.leap.LinkedList;
import jade.util.leap.Serializable;
import java.util.NoSuchElementException;

//#MIDP_EXCLUDE_BEGIN
import java.io.*; // used only for debugging purposes
//#MIDP_EXCLUDE_END

/**
 * Implementation of a RoundList with get/insert methods relative 
 * to the current element
 * @author Fabio Bellifemine - TILab 
 * @version $Date: 2005-11-03 11:40:16 +0100 (gio, 03 nov 2005) $ $Revision: 5810 $
 **/
public class RoundList implements Serializable{
	private int cur=-1;
	private LinkedList l=new LinkedList();


    /**
       Default constructor.
    */
    public RoundList() {
    }
	
	/** Inserts the <code>element</code> before the current element.
	 * If the list was empty, the inserted element becomes also the current element.
	 * <b> Note that this implementation uses a <code>LinkedList</code>
	 * and therefore it is not synchronized.
	 * @param element the element to insert
	 * @return true (as per the general contract of Collection.add).
	**/
	public boolean add(Object element) {
		if (cur < 0) { // the list was empty. 
			cur=0;
			l.add(element);
		} else if (cur == 0) { // the cursor was at the fist element, then insert at the end
			l.add(element);
		} else {
			l.add(cur, element);
			cur++; // no need to check cur>l.size
		}
		return true;
	}
	
	/** Returns the current <code>element</code> in the list and updates the pointer
	 * such that the current becomes the
	 * next element in the list.
	 * <br> Notice that if the list contains just 1 element each call to this method will return
	 * the same element.
	 * <br> Take care in avoiding infinite loops in calling this method. It
	 * must be called no more than <code>size()</code> times
	 * @throws NoSuchElementException if the list is empty 
	**/
	public Object get() throws NoSuchElementException {
		if (cur < 0)
			throw new NoSuchElementException("The RoundList is empty");
		Object val=l.get(cur);
		cur++;
		if (cur == l.size())
			cur=0;
		return val;
	}
	
	
	/** Removes the first occurrence of the specified element in this list
	 *  and updates the pointer to the current element.
	 *  If the list does not contain the element, it is unchanged. 
	 *  More formally, removes the element with the lowest index i such that 
	 * <code>(element==null ? get(i)==null : element.equals(get(i))) </code>
	 * (if such an element exists). 
	 * @param element the element to be removed from this list, if present. 
	 * @return true if the list contained the specified element.
    **/
	public boolean remove(Object element) {
		int ind=l.indexOf(element);
		if (ind<0) { // element not found
			return false;
		} 
		l.remove(element);
		if (l.size() == 0) {
			// There was just 1 element and we removed it
			cur=-1;
		} 
		else if (ind < cur) {
			// Shift down the current element otherwise it is skipped
			cur--;
		}
		else if (cur == l.size()) {
			// The current element was the last one (but not the only one) and we removed it
			cur=0;
		}
		return true;
	}
	
	
	/** Returns true if this list contains the specified element. 
	 * More formally, returns true if and only if this list contains at least 
	 * one element e such that <code>(element==null ? e==null : element.equals(e)).</code>
	 * @param element whose presence in this list is to be tested. 
	 * @return true if this list contains the specified element.
    **/
	public boolean contains(Object element) {
		return l.contains(element);
	}
	
	
	/** Returns the number of elements in this list. 
	* @return the number of elements in this list.
	**/
	public int size() {
		return l.size();
	}
		
	/** Returns an Iterator over the elements in this list. 
	* @return an Iterator over the elements in this list.
	**/
	public Iterator iterator() {
		return l.iterator();
	}
		
	public Object[] toArray() {
		return l.toArray();
	}
	
	/** Returns a string representation of this collection. 
	 * The string representation consists of a list of the collection's elements 
	 * in the order they are returned by its get() method, 
	 * enclosed in square brackets ("[]"). 
	 * Adjacent elements are separated by the characters ", " (comma and space). 
	 * Elements are converted to strings as by <code>String.valueOf(Object).</code>
	 * @return a String representation of this list
    **/
	public String toString() {
		StringBuffer str = new StringBuffer("[");
		for (int i=0; i<l.size(); i++) {
			str.append(String.valueOf(get()));
			str.append(", ");
		}
		str.append("]");
		return str.toString();
	}

	
	//#MIDP_EXCLUDE_BEGIN
	
	/**
	* Just for Debugging this implementation.
	**/
	public static void main(String args[]) {
        RoundList r = new RoundList();
  
         while (true) {
          try {	
        	BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("ENTER Operation add|get|remove");
			String op = buff.readLine();
			if (op.toLowerCase().startsWith("a")) {
				System.out.println("ENTER Element to add");
				String el = buff.readLine();
				r.add(el);
			} else if (op.toLowerCase().startsWith("g")) {
				System.out.println("Got Element: "+r.get());
			} else if (op.toLowerCase().startsWith("r")) {
				System.out.println("ENTER Element to remove");
				String el = buff.readLine();
				if (!r.remove(el))
					System.out.println("Element not found");
			} 
			System.out.println("The RoundList is now:"+r.toString());
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		}
    }
	//#MIDP_EXCLUDE_END
    

}
