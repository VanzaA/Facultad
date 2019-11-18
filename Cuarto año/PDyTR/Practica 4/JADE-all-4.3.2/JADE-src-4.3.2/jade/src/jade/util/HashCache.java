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

package jade.util;

//#APIDOC_EXCLUDE_FILE

import jade.util.leap.Set;
import jade.util.leap.Map;
import jade.util.leap.Collection;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.LinkedList;


/**
 * This class is a cache with fixed dimension that can be set in the constructur.
 * All element are indexed with an hashcode. 
 * 
 * When an element is added and the cache is already full,the oldest element is deleted. 
 *
 * @author Alessandro Chiarotto, Fabio Bellifemine - TILAB S.p.A.
 * @version $Date: 2004-09-27 14:50:46 +0200 (lun, 27 set 2004) $ $Revision: 5378 $
**/
public class HashCache implements Map
{
	private List list; 
	private Map ht;
	private int cs;
	/**
	* Constructs a new, empty HashCache with the specified size.
	* @param cacheSize is the size of this cache
	**/
	public HashCache(int cacheSize) 
	{
		list = new LinkedList();
		ht = new HashMap(cacheSize);
		cs = cacheSize;
	}

	
	 /**
	 * Adds the specified element to this hashcache if it is not already
	 * present.
	 * If the cache is already full,the oldest element is deleted.
	 * 
	 * @param o element to be added to this set.
	 * @return o the specified added object
	 * element.
	 */
	public synchronized Object add(Object o) {
	    return put(o, o);
	}

        /**
	 * Adds a key-value pair to this cache
	 * @param key The key with which the value can be retrieved in
	 * the future.
	 * @param value The value to store in the cache.
	 * @return The value previously associated to the key, if any.
	 */
        public synchronized Object put(Object key, Object value) {
	    if (list.size() >= cs) 
		{
		    // remove the oldest (LRU-wise) element
		    remove(list.get(0));
		}
	    ht.put(key, value);
	    list.add(key);
	    return key;
	}

        /**
	 * Remove an existing key-value pair from the cache
	 *
	 * @param o The key to be removed (together with its associated value.
	 * @return The value associated to the given key, if any.
	 */
         public synchronized Object remove(Object key)
         {
	     list.remove(key);
	     return ht.remove(key);
	 }


	/**
	 * Tests if the specified object is a key in this hashcache.
	 * present.
	 * the oldest element is deleted. 
	 * @param o element to be added to this set.
	 * @return true if the haschcache contains the object <CODE>o</CODE>,
	 * otherwise false
	 * 
	 */
	public synchronized boolean contains(Object o) 
	{
		return ht.containsKey(o);
	}

        /**
	 * Retrieves a cached element. The retrieved element is also
	 * marked as the last used one, so that the cache replacement
	 * policy becomes LRU instead of FIFO.
	 * @param o The 
	 */
         public synchronized Object get(Object key)
         {
	     if(list.remove(key)) {
		 list.add(key);
	     }

	     return ht.get(key);
	 }

        /**
         * Clears the cache, removing all key-value pairs
	 *
	 */
        public synchronized void clear() {
	     ht.clear();
	     list.clear();
	}



    // Remaining methods needed to implement jade.util.leap.Map are
    // simply delegated to the inner HashMap...

    public synchronized boolean isEmpty() {
	return ht.isEmpty();
    }

    public synchronized Set keySet() {
	return ht.keySet();
    }

    public synchronized Collection values() {
	return ht.values();
    }

    public synchronized boolean containsKey(Object key) {
	return ht.containsKey(key);
    }

    public synchronized int size() {
	return ht.size();
    }

}
