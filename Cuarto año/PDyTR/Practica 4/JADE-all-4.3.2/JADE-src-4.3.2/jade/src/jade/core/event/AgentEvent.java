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

package jade.core.event;

import jade.core.AID;
import jade.core.BehaviourID;
import jade.core.ContainerID;
import jade.core.AgentState;

//__SECURITY__BEGIN
import jade.security.JADEPrincipal;
//__SECURITY__END

/**
   This class represents an event related to the agent life-cycle and
   configuration.

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date: 2004-04-23 11:08:16 +0200 (ven, 23 apr 2004) $ $Revision: 4988 $
 */
public class AgentEvent extends JADEEvent {

  public static final int CHANGED_AGENT_STATE = 1;
  public static final int ADDED_BEHAVIOUR = 2;
  public static final int REMOVED_BEHAVIOUR = 3;
  public static final int CHANGED_BEHAVIOUR_STATE = 4;
  public static final int CHANGED_AGENT_PRINCIPAL = 5;

  //private int myID;

  private AID agent;
  private BehaviourID behaviour = null;
  private AgentState from = null;
  private AgentState to = null;
  private String behaviourFrom = null;
  private String behaviourTo = null;
//__SECURITY__BEGIN
  private JADEPrincipal oldPrincipal = null;
  private JADEPrincipal newPrincipal = null;
//__SECURITY__END

  public AgentEvent(int id, AID aid, AgentState f, AgentState t, ContainerID cid) {
    super(id, cid);
    //myID = id;
    if(!isChangedAgentState()) {
      throw new InternalError("Bad Event kind: it must be a 'changed-agent-state' event.");
    }
    agent = aid;
    behaviour = null;
    from = f;
    to = t;
  }

//__SECURITY__BEGIN
  public AgentEvent(int id, AID aid, JADEPrincipal f, JADEPrincipal t, ContainerID cid) {
    super(id, cid);
    //myID = id;
    if(!isChangedAgentPrincipal()) {
      throw new InternalError("Bad Event kind: it must be a 'changed-agent-principal' event.");
    }
    agent = aid;
    oldPrincipal = f;
    newPrincipal = t;
  }
//__SECURITY__END

  public AgentEvent(int id, AID aid, BehaviourID bid, ContainerID cid) {
    super(id, cid);
    //myID = id;
    if(isChangedAgentState() || isChangedBehaviourState()) {
      throw new InternalError("Bad Event kind: it must be an 'added/removed-behaviour' event.");
    }
    agent = aid;
    behaviour = bid;
    from = null;
    to = null;
  }

  public AgentEvent(int id, AID aid, BehaviourID bid, String strFrom, String strTo, ContainerID cid) {
    super(id, cid);
    //myID = id;
    if(!isChangedBehaviourState()) {
      throw new InternalError("Bad Event kind: it must be a 'changed-behaviour-state' event.");
    }

    agent = aid;
    behaviour = bid;
    
    from = null;
    to = null;
    
    behaviourFrom = strFrom;
    behaviourTo = strTo;

  }

  public AID getAgent() {
    return agent;
  }

  public BehaviourID getBehaviour() {
    return behaviour;
  }

  public AgentState getFrom() {
    return from;
  }

  public AgentState getTo() {
    return to;
  }

  public String getBehaviourFrom() {
      return behaviourFrom;
  }
  
  public String getBehaviourTo() {
      return behaviourTo;
  }

//__SECURITY__BEGIN
  public JADEPrincipal getOldPrincipal() {
    return oldPrincipal;
  }

  public JADEPrincipal getNewPrincipal() {
    return newPrincipal;
  }
//__SECURITY__END

  private boolean isChangedAgentPrincipal() {
    return type == CHANGED_AGENT_PRINCIPAL; 
  }

  private boolean isChangedAgentState() {
    return type == CHANGED_AGENT_STATE; 
  }

  private boolean isChangedBehaviourState() {
    return type == CHANGED_BEHAVIOUR_STATE;
  }

}
