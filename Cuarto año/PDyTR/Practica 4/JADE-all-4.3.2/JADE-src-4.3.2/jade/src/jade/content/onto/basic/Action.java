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
import jade.core.AID;

/**
   This class implements the <code>action</code> operator of the
   FIPA SL0 action.
   @author Giovanni Caire - TILAB
 */
public class Action implements AgentAction, Introspectable {
	private AID actor;
	private Concept action;
	
	public Action() {
		actor = null;
		action = null;
	}
	
	public Action(AID id, Concept a) {
		setActor(id);
		setAction(a);
	}
	
	public AID getActor() {
		return actor;
	}
	
	public void setActor(AID id) {
		actor = id;
	}	
	
	public Concept getAction() {
		return action;
	}
	
	public void setAction(Concept a) {
		action = a;
	}
	
  public void externalise(AbsObject abs, Ontology onto) throws OntologyException {
  	try {
  		AbsAgentAction absAction = (AbsAgentAction) abs;
  		absAction.set(BasicOntology.ACTION_ACTOR, (AbsTerm) onto.fromObject(getActor()));
  		absAction.set(BasicOntology.ACTION_ACTION, (AbsTerm) onto.fromObject(getAction()));
  	}
  	catch (ClassCastException cce) {
  		throw new OntologyException("Error externalising Action");
  	}
  }

  public void internalise(AbsObject abs, Ontology onto) throws UngroundedException, OntologyException {
    try {
  		AbsAgentAction absAction = (AbsAgentAction) abs;
  		setActor((AID) onto.toObject(absAction.getAbsObject(BasicOntology.ACTION_ACTOR))); 
  		setAction((Concept) onto.toObject(absAction.getAbsObject(BasicOntology.ACTION_ACTION))); 
  	}
  	catch (ClassCastException cce) {
  		throw new OntologyException("Error internalising Action");
  	}
  }
}
