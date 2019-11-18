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

import jade.util.leap.Serializable;

/**
 * The common ancestor of all abstract descriptors
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public interface AbsObject extends Serializable {
	public static final int UNKNOWN = -1;
	public static final int ABS_PREDICATE = 1;
	public static final int ABS_CONCEPT = 2;
	public static final int ABS_AGENT_ACTION = 3;
	public static final int ABS_PRIMITIVE = 4;
	public static final int ABS_AGGREGATE = 5;
	public static final int ABS_IRE = 6;
	public static final int ABS_VARIABLE = 7;
	public static final int ABS_CONTENT_ELEMENT_LIST = 8;
	public static final int ABS_CONCEPT_SLOT_FUNCTION = 9;
	
    /**
     * @return The name of the type of the object held by this
     * abstract descriptor.
     */
    public String getTypeName();
    
    /**
     * Gets the value of an attribute of the object held by this
     * abstract descriptor.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     */
    public AbsObject getAbsObject(String name);

    /**
     * @return the name of all attributes.
     */
    public String[] getNames();

    /**
     * Tests if the object is grounded, i.e., if no one of its attributes 
     * is associated with a variable
     * @return <code>true</code> if the object is grounded.
     */
    public boolean isGrounded();

    /**
     * Gets the number of attributes.
     * @return the number of attributes.
     */
    public int getCount();

    public int getAbsType();
}

