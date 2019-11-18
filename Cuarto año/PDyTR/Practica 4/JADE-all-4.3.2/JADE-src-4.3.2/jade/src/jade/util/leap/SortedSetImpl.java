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
   The LEAP (environment-dependent) implementation of the &qote;SortedSet&qote; interface.
   This class appears to be exactly the same in J2SE, PJAVA and MIDP.
   The internal implementation is different in the three cases however.
   
   @author  Nicolas Lhuillier
   @version 1.0, 20/10/00
   
   @see java.util.SortedSet
   @see java.util.TreeSet
 */
public class SortedSetImpl implements SortedSet {
    private java.util.TreeSet hiddenSet = null;

    /**
     * Default Constructor, creates an empty Set,
     * according to the elements' natural order.
     */
    public SortedSetImpl() {
        hiddenSet = new java.util.TreeSet();
    }

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean add(Object o) {
        return hiddenSet.add(o);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean isEmpty() {
        return hiddenSet.isEmpty();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public boolean remove(Object o) {
        return hiddenSet.remove(o);
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public Iterator iterator() {
        return new Iterator() {
            java.util.Iterator it = SortedSetImpl.this.hiddenSet.iterator();

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
        return hiddenSet.toArray();
    } 

    /**
     * @see jade.util.leap.Collection interface
     */
    public int size() {
        return hiddenSet.size();
    } 

    /**
     * @see jade.util.leap.SortedSet interface
     */
    public Object first() {
        return hiddenSet.first();
    } 

}

