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

import java.util.Date;

/**
 * This class is not intended to be used by programmers.
 * @author Giovanni Caire - TILAB
 */
public class AbsPrimitiveSlotsHolder extends AbsObjectImpl {

    /**
     * Construct an Abstract descriptor to hold an object of
     * the proper type.
     * @param typeName The name of the type of the object held by 
     * this abstract descriptor.
     */
    protected AbsPrimitiveSlotsHolder(String typeName) {
        super(typeName);
    }

    /**
     * Utility method that allows setting attributes of type
     * <code>String</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, String value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    public void set(String name, AbsObject value) {
        super.set(name, value);
    } 
    
    /**
     * Utility method that allows setting attributes of type
     * <code>boolean</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, boolean value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Utility method that allows setting attributes of type
     * <code>int</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, int value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Utility method that allows setting attributes of type
     * <code>long</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, long value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    //#MIDP_EXCLUDE_BEGIN
    /**
     * Utility method that allows setting attributes of type
     * <code>float</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, float value) {
        set(name, AbsPrimitive.wrap(value));
    } 
    
    /**
     * Utility method that allows setting attributes of type
     * <code>double</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, double value) {
        set(name, AbsPrimitive.wrap(value));
    } 
    //#MIDP_EXCLUDE_END

    /**
     * Utility method that allows setting attributes of type
     * <code>Date</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, Date value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Utility method that allows setting attributes of type
     * <code>byte[]</code> without the need of wrapping the new value
     * into an <code>AbsPrimitive</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, byte[] value) {
        set(name, AbsPrimitive.wrap(value));
    } 

   /**
     * Utility method that allows getting the value of attributes 
     * of type <code>String</code> directly as a <code>String</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public String getString(String name) {
        AbsPrimitive p = (AbsPrimitive) getAbsObject(name);
        if (p != null) {
        	return p.getString();
        }
        else {
        	return null;
        }
    }

    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>boolean</code> directly as a <code>boolean</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public boolean getBoolean(String name) {
      	return ((AbsPrimitive) getAbsObject(name)).getBoolean();
    }

    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>int</code> directly as an <code>int</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public int getInteger(String name) {
    	return ((AbsPrimitive) getAbsObject(name)).getInteger();
    }

    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>long</code> directly as a <code>long</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public long getLong(String name) {
    	return ((AbsPrimitive) getAbsObject(name)).getLong();
    }

    //#MIDP_EXCLUDE_BEGIN
    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>float</code> directly as a <code>float</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public float getFloat(String name) {
    	return ((AbsPrimitive) getAbsObject(name)).getFloat();
    }
    
    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>double</code> directly as a <code>double</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public double getDouble(String name) {
    	return ((AbsPrimitive) getAbsObject(name)).getDouble();
    }
    //#MIDP_EXCLUDE_END
    
    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>Date</code> directly as a <code>Date</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public Date getDate(String name) {
        AbsPrimitive p = (AbsPrimitive) getAbsObject(name);
        if (p != null) {
        	return p.getDate();
        }
        else {
        	return null;
        }
    }
    
    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>byte[]</code> directly as a <code>byte[]</code>
     * i.e. not wrapped into an <code>AbsPrimitive/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public byte[] getByteSequence(String name) {
        AbsPrimitive p = (AbsPrimitive) getAbsObject(name);
        if (p != null) {
        	return p.getByteSequence();
        }
        else {
        	return null;
        }
    }
}

