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


package jade.domain.JADEAgentManagement;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.core.ContainerID;
import jade.content.AgentAction;

import jade.security.JADEPrincipal;
import jade.security.Credentials;

/**
  This class represents the <code>create-agent</code> action of 
  the <code>JADE-agent-management ontology</code>.
  This action can be requested to the JADE AMS to create a new agent 
  on a given container.

  @author Giovanni Rimassa -  Universita' di Parma
  @version $Date: 2004-04-28 11:04:38 +0200 (mer, 28 apr 2004) $ $Revision: 4999 $
*/
public class CreateAgent implements AgentAction {

    private String agentName;
    private String className;
    private List arguments = new ArrayList();
    private ContainerID container;
    //#MIDP_EXCLUDE_BEGIN
    private JADEPrincipal owner;
    private Credentials initialCredentials;
    //#MIDP_EXCLUDE_END

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public CreateAgent() {
    }

    /**
       Set the <code>agent-name</code> slot of this action.
       @param an The local name (i.e. without the platform ID) of the
       agent to create.
    */
    public void setAgentName(String an) {
	agentName = an;
    }

    /**
       Retrieve the value of the <code>agent-name</code> slot of this
       event, containing the local name (i.e. without the platform ID)
       of the agent to create.
       @return The value of the <code>agent-name</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getAgentName() {
	return agentName;
    }

    /**
       Set the <code>class-name</code> slot of this action.
       @param cn The name of the Java class implementing the agent to
       create.
    */
    public void setClassName(String cn) {
	className = cn;
    }

    /**
       Retrieve the value of the <code>class-name</code> slot of this
       event, containing the name of the Java class implementing the
       agentto create.
       @return The value of the <code>class-name</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getClassName() {
	return className;
    }

    /**
       Set the <code>container</code> slot of this action.
       @param cid The container identifier of the container where the
       agent is to be created.
    */
    public void setContainer(ContainerID cid) {
	container = cid;
    }

    /**
       Retrieve the value of the <code>container</code> slot of this
       event, containing the container identifier of the container
       where the agent is to be created.
       @return The value of the <code>container</code> slot, or
       <code>null</code> if no value was set.
    */
    public ContainerID getContainer() {
	return container;
    }

    //#MIDP_EXCLUDE_BEGIN
    /**
       Set the principal of the owner of the agent to be created.
       @param p The principal of the owner of the agent to be created.
    */
    public void setOwner(JADEPrincipal p) {
	owner = p;
    }

    /**
       @return The principal of the owner of the agent to be created.
    */
    public JADEPrincipal getOwner() {
	return owner;
    }

    /**
       Set the initial credentials to be granted to the agent to be created.
       @param c The initial credentials to be granted to the agent to be created.
    */
    public void setInitialCredentials(Credentials c) {
	initialCredentials = c;
    }

    /**
       @return The initial credentials to be granted to the agent to be created.
    */
    public Credentials getInitialCredentials() {
	return initialCredentials;
    }
    //#MIDP_EXCLUDE_END

    /**
       Add an object to the <code>arguments</code> slot collection of
       this object.
       @param a The object to add to the collection.
    */
    public void addArguments(Object a) {
	arguments.add(a);
    }
  
    /**
       Remove all objects from the <code>arguments</code> slot
       collection of this object.
    */
    public Iterator getAllArguments() {
  	return arguments.iterator();
    }

}
