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

/**
 * An extended abstract descriptor that can hold a generic primitive types 
 * (eg. java.math.BigDecimal) not supported by <code>AbsPrimitive</code>.
 */
public class AbsExtendedPrimitive implements AbsTerm {
	
	public static final int ABS_EXTENDED_PRIMITIVE = 10;
	
	private Object value = null;
	private String typeName = null;

	/**
	 * Construct an extended abstract descriptor to hold a primitive of
	 * the proper type (e.g. java.math.BigDecimal...) and set its value.
	 */
	private AbsExtendedPrimitive(String typeName, Object value) {
		this.typeName = typeName;
		this.value = value;
	}

	/**
	 * Construct an Abstract descriptor to hold a extended-primitive of
	 * the proper type (e.g. java.math.BigInteger...).
	 * 
	 * @param typeName The name of the type of the extended-primitive held by 
	 * this descriptor.
	 */
	public AbsExtendedPrimitive(String typeName) {
		this(typeName, null);
	}

	/**
	 * Create an AbsExtendedPrimitive of type <code>value.getClass()</code>
	 * containing a given value.
	 */
	public static AbsExtendedPrimitive wrap(Object value) {
		AbsExtendedPrimitive ret = null;
		if (value != null) { 
			ret = new AbsExtendedPrimitive(value.getClass().getName(), value);
		}
		return ret;
	} 

	/**
	 * Set the value of this AbsExtendedPrimitive to the given value.
	 * 
	 * @param value The new value
	 * @throws IllegalArgumentException If the type of this AbsExtendedPrimitive 
	 * is not correct.
	 */
	public void set(Object value) {
		if (!getTypeName().equals(value.getClass().getName()))
			throw new IllegalArgumentException("Wrong type");
		this.value = value;
	} 

	/**
	 * @return the value of this AbsExtendedPrimitive.
	 */
	public Object get() {
		return value;
	} 

	/**
	 * @return The name of the type of the object held by this
	 * abstract descriptor.
	 */
	public String getTypeName() {
		return typeName;
	} 
	
	/**
    Makes no sense in the case of an AbsExtendedPrimitive that has no attribute
    --> Just return null
	 */
	public AbsObject getAbsObject(String name) {
		return null;
	}
	
	/**
    Makes no sense in the case of an AbsExtendedPrimitive that has no attribute
    --> Just return null
	 */
	public String[] getNames() {
		return null;
	}
	
	/**
	 * Tests if this AbsExtendedPrimitive is grounded. It always returns true
	 */
	public boolean isGrounded() {
		return true;
	}

	/**
       Makes no sense in the case of an AbsExtendedPrimitive that has no attribute
       --> Just return 0
	 */
	public int getCount() {
		return 0;
	}


	public String toString() {
		return value.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof AbsExtendedPrimitive)
			return get().equals(((AbsExtendedPrimitive) obj).get());
		else
			return false;
	}

	public int hashCode() {
		return get().hashCode();
	}
	
    public int getAbsType() {
    	return ABS_EXTENDED_PRIMITIVE;
    }
}

