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

import jade.core.ContainerID;
import jade.content.AgentAction;

/**
  This class represents the <code>install-MTP</code> action of 
  the <code>JADE-agent-management ontology</code>.
  This action can be requested to the JADE AMS to install a new MTP for
  inter-platform communication on a given container.


   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
*/
public class InstallMTP implements AgentAction {

    private String address;
    private ContainerID container;
    private String className;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public InstallMTP() {
    }

    /**
       Set the <code>address</code> slot of this action.
       @param a The address URL of the MTP endpoint to install.
    */
    public void setAddress(String a) {
	address = a;
    }

    /**
       Retrieve the value of the <code>address</code> slot of this
       action, containing the address URL of the MTP to install.
       @return The value of the <code>address</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getAddress() {
	return address;
    }

    /**
       Set the <code>container</code> slot of this action.
       @param cid The container identifier of the container where the
       new MTP is to be deployed.
    */
    public void setContainer(ContainerID cid) {
	container = cid;
    }

    /**
       Retrieve the value of the <code>container</code> slot of this
       action, containing the container identifier of the container
       where the new MTP is to be deployed.
       @return The value of the <code>container</code> slot, or
       <code>null</code> if no value was set.
    */
    public ContainerID getContainer() {
	return container;
    }

    /**
       Set the <code>class-name</code> slot of this action.
       @param a The name of the Java class implementing the MTP
       endpoint to install.
    */
    public void setClassName(String a) {
	className = a;
    }

    /**
       Retrieve the value of the <code>class-name</code> slot of this
       action, containing the name of the Java class implementing the
       MTP endpoint to install.
       @return The value of the <code>class-name</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getClassName() {
	return className;
    }

}
