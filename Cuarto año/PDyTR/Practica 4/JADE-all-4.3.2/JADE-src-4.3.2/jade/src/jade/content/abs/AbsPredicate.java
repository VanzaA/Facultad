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

import jade.content.Predicate;

/**
 * An abstract descriptor that can hold a predicate expression.
 * @author Paola Turci, Federico Bergenti - Universita` di Parma
 */
public class AbsPredicate extends AbsPrimitiveSlotsHolder implements AbsContentElement, Predicate {

	private boolean isAContentExpression = false;
	private boolean isMeta = false;

	/**
	 * Construct an Abstract descriptor to hold a predicate of
	 * the proper type (e.g. FATHER_OF, WORKS_FOR...).
	 * @param typeName The name of the type of the predicate held by 
	 * this abstract descriptor.
	 */
	public AbsPredicate(String typeName) {
		super(typeName);
	}

	/**
	 * Sets an attribute of the predicate held by this
	 * abstract descriptor.
	 * @param name The name of the attribute to be set.
	 * @param value The new value of the attribute.
	 */
	public void set(String name, AbsObject value) {
		super.set(name, value);
	} 

	/**
	 * Gets the value (casted as an AbsTerm) of an attribute of 
	 * the predicate held by this abstract descriptor.
	 * This method is here mainly for backward compatibility.
	 * @param name The name of the attribute.
	 * @return value The value of the attribute.
	 */
	public AbsTerm getAbsTerm(String name) {
		return (AbsTerm) getAbsObject(name);
	} 

	// Easy way to access the Java class representing AbsPredicate.
	// Useful in MIDP where XXX.class is not available
	private static Class absPredicateClass = null;
	public static Class getJavaClass() {
		if (absPredicateClass == null) {
			try {
				absPredicateClass = Class.forName("jade.content.abs.AbsPredicate");
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
		}
		return absPredicateClass;
	}
	
	/**
	 * @see AbsContentElement.isAContentExpression
	 */
	public boolean isAContentExpression() {
		return isAContentExpression;
	}

	/**
	 * @see AbsContentElement.setIsAContentExpression(boolean flag)
	 */
	public void setIsAContentExpression(boolean flag) {
		isAContentExpression = flag;
	}


	/** Return true if this object represents a meta formula (symbol ??x) rather than a concrete formula.
	 * This method is currently used only by the semantics framework.
	 * @return true if this object represents a meta formula
	 * @since JADE3.4
	 **/
	public final boolean isMetaFormula() {return isMeta;}

	/** Sets the value of isMetaFormula, where the default is false.
	 * This method is currently used only by the semantics framework.
	 * @since JADE3.4
	 **/
	public final void setIsMetaFormula(boolean isMeta) { this.isMeta = isMeta;}
	
    public int getAbsType() {
    	return ABS_PREDICATE;
    }
}

