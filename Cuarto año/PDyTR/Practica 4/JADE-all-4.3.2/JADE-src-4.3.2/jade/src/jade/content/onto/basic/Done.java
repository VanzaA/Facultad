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
package jade.content.onto.basic;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;

/**
   This class implements the <code>done</code> operator of the
   FIPA SL0 action.
   @author Giovanni Caire - TILAB
 */
public class Done implements Predicate {
	private Concept action;
	private Predicate condition;
	
	public Done() {
		action = null;
		condition = null;
	}
	
	public Done(Concept a) {
		setAction(a);
	}
	
	public Concept getAction() {
		return action;
	}
	
	public void setAction(Concept a) {
		action = a;
	}	
	
	public Predicate getCondition() {
		return condition;
	}
	
	public void setCondition(Predicate c) {
		condition = c;
	}	
	
}
