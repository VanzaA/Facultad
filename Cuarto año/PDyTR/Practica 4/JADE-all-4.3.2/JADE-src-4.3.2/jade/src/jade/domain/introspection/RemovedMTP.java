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

package jade.domain.introspection;

import jade.core.ContainerID;

/**
   An introspection event, recording the removal of an MTP within the
   platform.

   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2003-11-19 17:04:37 +0100 (mer, 19 nov 2003) $ $Revision: 4567 $
 */
public class RemovedMTP implements Event {

    /**
       A string constant for the name of this event.
    */
    public static final String NAME = "Removed-MTP";

    private String address;
    private String proto;
    private ContainerID where;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public RemovedMTP() {
    }

    /**
       Retrieve the name of this event.
       @return A constant value for the event name.
    */
    public String getName() {
	return NAME;
    }

    /**
       Set the <code>address</code> slot of this event.
       @param s The address URL of the newly removed MTP.
    */
    public void setAddress(String s) {
	address = s;
    }

    /**
       Retrieve the value of the <code>address</code> slot of this
       event, containing the address URL of the newly removed MTP.
       @return The value of the <code>address</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getAddress() {
	return address;
    }

    /**
       Set the <code>proto</code> slot of this event.
       @param p The protocol name for the newly removed MTP.
    */
    public void setProto(String p) {
	proto = p;
    }

    /**
       Retrieve the value of the <code>proto</code> slot of this
       event, containing the protocol name for the newly removed MTP.
       @return The value of the <code>proto</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getProto() {
	return proto;
    }

    /**
       Set the <code>where</code> slot of this event.
       @param id The container identifier of the container where the
       newly removed MTP was deployed.
    */
    public void setWhere(ContainerID id) {
	where = id;
    }

    /**
       Retrieve the value of the <code>where</code> slot of this
       event, containing the container identifier of the container
       where the newly removed MTP was deployed.
       @return The value of the <code>where</code> slot, or
       <code>null</code> if no value was set.
    */
    public ContainerID getWhere() {
	return where;
    }

}
