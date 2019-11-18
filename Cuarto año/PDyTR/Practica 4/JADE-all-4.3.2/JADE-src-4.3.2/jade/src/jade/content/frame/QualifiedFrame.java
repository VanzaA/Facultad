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
package jade.content.frame;

import java.util.Hashtable;

/**
   Generic class representing all frames (such as concepts and 
   predicates) whose composing elements can be retrieved by a 
   unique name.
   @author Giovanni Caire - TILAB
 */
public class QualifiedFrame extends Hashtable implements Frame {
	private String typeName;

	/**
	   Create a QualifiedFrame with a given type-name.
	   @param typeName The type-name of the QualifiedFrame to be created.
	 */
	public QualifiedFrame(String typeName) {
		super();
		this.typeName = typeName;
	}
	
	/**
	   Retrieve the type-name of this QualifiedFrame.
	   @return the type-name of this QualifiedFrame
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/** 
	   Redefine the put() method so that keys must be String and
	   setting a null value for a given key is interpreted as 
	   removing the entry.
	   @exception ClassCastException if <code>key</code> is not a String
	 */
	public Object put(Object key, Object val) {
		if (val != null) {
			return super.put((String) key, val);
		}
		else {
			return remove((String) key);
		}
	}	
	
	/**
	   Utility method to put a value of type <code>int</code> in this
	   Frame. 
	 */
	public Object putInteger(Object key, int val) {
		return put(key, new Long(val));
	}
	
	/**
	   Utility method to retrieve a value of type <code>int</code> from this
	   Frame. 
	 */
	public int getInteger(Object key) {
		return (int) (((Long) get(key)).longValue());
	}	
	
	/**
	   Utility method to put a value of type <code>boolean</code> in this
	   Frame. 
	 */
	public Object putBoolean(Object key, boolean val) {
		return put(key, new Boolean(val));
	}
	
	/**
	   Utility method to retrieve a value of type <code>boolean</code> from this
	   Frame. 
	 */
	public boolean getBoolean(Object key) {
		return ((Boolean) get(key)).booleanValue();
	}	
}

