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
import java.util.Hashtable;
import java.util.Enumeration;

/**
   The LEAP (environment-dependent) version of the &qote;java.util.HashMap&qote; class.
   This class appears to be exactly the same in J2SE, PJAVA and MIDP.
   The internal implementation is different in the three cases however.
   
   @author  Nicolas Lhuillier
   @version 1.0, 29/09/00
   
   @see java.util.HashMap
 */
public class HashMap implements Map, Serializable {
    private transient java.util.Map realHiddenMap = null;
    /**
     * Proxy to the realHiddenMap keys Set
     */
    private transient Set               keySet = null;

    /**
     * Proxy to the realHiddenMap values Collection
     */
    private transient Collection        values = null;

  	/**
   	   The following elements are required to ensure compatibility with 
   	   the J2ME version of this class in serialization/deserialization 
   	   operations.
   	 */
    private static final long     serialVersionUID = 3487495895819395L;
  	private static final Long     nullValue = new Long(serialVersionUID);
    private Hashtable             hiddenMap;

    /**
     * Default constructor, creates a new empty Map
     */
    public HashMap() {
        realHiddenMap = new java.util.HashMap();
    }

    /**
     * Constructor, creates a new Map with initial size
     */
    public HashMap(int s) {
        realHiddenMap = new java.util.HashMap(s);
    }

    /**
     * Constructor, creates a new Map with initial size and load factor
     */
    public HashMap(int s, float lf) {
        realHiddenMap = new java.util.HashMap(s, lf);
    }

    /**
     * @see jade.util.leap.Map interface
     */
    public boolean isEmpty() {
        return realHiddenMap.isEmpty();
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Object remove(Object o) {
        return realHiddenMap.remove(o);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Object put(Object key, Object value) {
        return realHiddenMap.put(key, value);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Object get(Object key) {
        return realHiddenMap.get(key);
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public int size() {
        return realHiddenMap.size();
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public boolean containsKey(Object key) {
        return realHiddenMap.containsKey(key);
    }
	
	/**
	 * @see java.util.Map interface
	 */
	public void clear(){
		realHiddenMap.clear();
	} 

	public String toString() {
		return realHiddenMap.toString();
	}
	
    /**
     * @see jade.util.leap.Map interface
     */
    public Set keySet() {
        if (keySet == null) {
            keySet = new Set() {

                /**
                 * @see jade.util.leap.Set interface
                 */
                public Object[] toArray() {
                    return HashMap.this.realHiddenMap.keySet().toArray();
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public boolean add(Object o) {
                    return HashMap.this.realHiddenMap.keySet().add(o);
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public boolean isEmpty() {
                    return HashMap.this.realHiddenMap.keySet().isEmpty();
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public boolean remove(Object o) {
                    return HashMap.this.realHiddenMap.keySet().remove(o);
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public Iterator iterator() {
                    return new Iterator() {
                        java.util.Iterator it = 
                            HashMap.this.realHiddenMap.keySet().iterator();

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public boolean hasNext() {
                            return it.hasNext();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public Object next() {
                            return it.next();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public void remove() {
                            it.remove();
                        } 

                    };
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public int size() {
                    return HashMap.this.realHiddenMap.keySet().size();
                } 

            };
        } 

        return keySet;
    } 

    /**
     * @see jade.util.leap.Map interface
     */
    public Collection values() {
        if (values == null) {
            values = new Collection() {

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public Object[] toArray() {
                    return HashMap.this.realHiddenMap.values().toArray();
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public boolean add(Object o) {
                    return HashMap.this.realHiddenMap.values().add(o);
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public boolean isEmpty() {
                    return HashMap.this.realHiddenMap.values().isEmpty();
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public boolean remove(Object o) {
                    return HashMap.this.realHiddenMap.values().remove(o);
                } 

                /**
                 * @see jade.util.leap.Collection interface
                 */
                public Iterator iterator() {
                    return new Iterator() {
                        java.util.Iterator it = 
                            HashMap.this.realHiddenMap.values().iterator();

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public boolean hasNext() {
                            return it.hasNext();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public Object next() {
                            return it.next();
                        } 

                        /**
                         * @see jade.util.leap.Iterator interface
                         */
                        public void remove() {
                            it.remove();
                        } 

                    };
                } 

                /**
                 * @see jade.util.leap.Set interface
                 */
                public int size() {
                    return HashMap.this.realHiddenMap.values().size();
                } 

            };
        } 

        return values;
    } 

    /**
       A customized writeObject() method is needed to ensure compatibility with  
       the J2ME version of this class in serialization/deserialization 
       operations
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        hiddenMap = new Hashtable();

        java.util.Iterator it = realHiddenMap.keySet().iterator();

        while (it.hasNext()) {
            Object key = it.next();
            Object value = realHiddenMap.get(key);
            key = (key != null ? key : nullValue);
            value = (value != null ? value : nullValue);

            hiddenMap.put(key, value);
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
        realHiddenMap = new java.util.HashMap();

        in.defaultReadObject();

        Enumeration e = hiddenMap.keys();

        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = hiddenMap.get(key);
            key = (nullValue.equals(key) ? null : key);
            value = (nullValue.equals(value) ? null : value);

            realHiddenMap.put(key, value);
        } 
    } 


    // For persistence service
    private void setData(java.util.Map data) {
	realHiddenMap = data;
    }

    // For persistence service
    private java.util.Map getData() {
	return realHiddenMap;
    }

}

