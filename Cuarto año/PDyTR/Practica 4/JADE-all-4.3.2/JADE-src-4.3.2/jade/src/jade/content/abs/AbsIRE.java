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

import jade.content.onto.*;
import jade.content.schema.*;
import jade.content.Term;

/**
 * An Abstract descriptor that can hold an Identifying
 * Referential Expression (IRE).
 * Note that an IRE is both a content element (as in the case of
 * a QUERY-REF communicative act) and a Term (as in the case of
 * (== (X) (iota ?x P(?x))
 * @author Paola Turci, Federico Bergenti - Universita` di Parma
 */
public class AbsIRE extends AbsObjectImpl implements AbsContentElement, AbsTerm {

	private boolean isAContentExpression = false;
	/**
	 * Construct an Abstract descriptor to hold a IRE of
	 * the proper type (e.g. ANY, IOTA, ALL...).
	 * @param typeName The name of the type of the IRE held by 
	 * this abstract descriptor.
	 */
	public AbsIRE(String typeName) {
		super(typeName);
	}

	/**
	 * Sets the variable of this IRE.
	 * @param variable The abstract descriptor holding the variable.
	 */
	public void setVariable(AbsVariable variable) {
		set(IRESchema.VARIABLE, variable);
	} 

	/**
	 * Sets the sequence of variables of this IRE.
	 * @param variables The abstract descriptor holding the sequence of variables.
	 */
	public void setVariables(AbsAggregate variables) {
		set(IRESchema.VARIABLE, variables);
	} 

	/**
	 * Sets the variable term of this IRE.
	 * @param t The abstract descriptor holding the variable or sequence of variables.
	 */
	public void setTerm(AbsTerm t) {
		if (t instanceof AbsVariable) {
			setVariable((AbsVariable) t);
		}
		else if (t instanceof AbsAggregate) {
			setVariables((AbsAggregate) t);
		}
		else {
			throw new IllegalArgumentException("Invalid term "+t+" for an AbsIRE");
		}
	} 

	/**
	 * Sets the proposition of this IRE.
	 * @param proposition The abstract descriptor holding the proposition.
	 */
	public void setProposition(AbsPredicate proposition) {
		set(IRESchema.PROPOSITION, proposition);
	} 

	/**
	 * Gets the variable of this IRE.
	 * @return the abstract descriptor holding the variable of this IRE.
	 */
	public AbsVariable getVariable() {
		return (AbsVariable) getAbsObject(IRESchema.VARIABLE);
	} 

	/**
	 * Gets the sequence of variables of this IRE.
	 * @return the abstract descriptor holding the sequence of variables of this IRE.
	 */
	public AbsAggregate getVariables() {
		return (AbsAggregate) getAbsObject(IRESchema.VARIABLE);
	} 

	/**
	 * Gets the variable term of this IRE.
	 * @return the abstract descriptor holding the variable term of this IRE.
	 */
	public AbsTerm getTerm() {
		return (AbsTerm) getAbsObject(IRESchema.VARIABLE);
	} 

	/**
	 * Gets the proposition of this IRE.
	 * @return the abstract descriptor holding the proposition of this IRE.
	 */
	public AbsPredicate getProposition() {
		return (AbsPredicate) getAbsObject(IRESchema.PROPOSITION);
	} 

	/**
	 * Redefine the <code>isGrounded()</code> method in order to 
	 * always return <code>false</code>. Infact an IRE always
	 * includes a variable.
	 */
	public boolean isGrounded() {
		return false;
	} 

	// Easy way to access the Java class representing AbsIRE.
	// Useful in MIDP where XXX.class is not available
	private static Class absIREClass = null;
	public static Class getJavaClass() {
		if (absIREClass == null) {
			try {
				absIREClass = Class.forName("jade.content.abs.AbsIRE");
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
		}
		return absIREClass;
	}


	/**
	 * @see AbsContent.isAContentExpression
	 */
	public boolean isAContentExpression() {
		return isAContentExpression;
	}

	/**
	 * @see AbsContent.setIsAContentExpression(boolean flag)
	 */
	public void setIsAContentExpression(boolean flag) {
		isAContentExpression = flag;
	}
	
    public int getAbsType() {
    	return ABS_IRE;
    }
}

