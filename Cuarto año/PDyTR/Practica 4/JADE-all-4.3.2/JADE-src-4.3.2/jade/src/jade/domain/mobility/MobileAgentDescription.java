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

package jade.domain.mobility;

import jade.core.*;
import jade.content.*;

/**

  This class represents the <code>mobile-agent-description</code>
  concept.

  @author Giovanni Rimassa -  Universita' di Parma
  @version $Date: 2003-11-19 17:04:37 +0100 (mer, 19 nov 2003) $ $Revision: 4567 $
*/
public class MobileAgentDescription implements Concept {

    private AID name;
    private Location destination;
    private MobileAgentProfile agentProfile;
    private String agentVersion;
    private String signature;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public MobileAgentDescription() {
    }

    /**
       Set the <code>name</code> slot of this action.
       @param id The agent identifier of the described agent.
    */
    public void setName(AID id) {
      name = id;
    }

    /**
       Retrieve the value of the <code>name</code> slot of this event,
       containing the agent identifier of the described agent.
       @return The value of the <code>name</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getName() {
      return name;
    }

    /**
       Set the <code>destination</code> slot of this action.
       @param d The destination of the mobility operation performed by
       the described agent.
    */
    public void setDestination(Location d) {
      destination = d;
    }

    /**
       Retrieve the value of the <code>destination</code> slot of this
       event, containing the destination of the mobility operation
       performed by the described agent.
       @return The value of the <code>destination</code> slot, or
       <code>null</code> if no value was set.
    */
    public Location getDestination() {
      return destination;
    }

    /**
       Set the <code>agent-profile</code> slot of this action.
       @param ap The profile for the described agent.
    */
    public void setAgentProfile(MobileAgentProfile ap) {
      agentProfile = ap;
    }

    /**
       Retrieve the value of the <code>agent-profile</code> slot of
       this event, containing the profile for the descrbed agent.
       @return The value of the <code>agent-profile</code> slot, or
       <code>null</code> if no value was set.
    */
    public MobileAgentProfile getAgentProfile() {
      return agentProfile;
    }

    /**
       Set the <code>agent-version</code> slot of this action.
       @param v The version string for the described agent.
    */
    public void setAgentVersion(String v) {
      agentVersion = v;
    }

    /**
       Retrieve the value of the <code>agent-version</code> slot of
       this event, containing the version string for the described
       agent.
       @return The value of the <code>agent-version</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getAgentVersion() {
      return agentVersion;
    }

    /**
       Set the <code>signature</code> slot of this action.
       @param nn The signature string for the described agent.
    */
    public void setSignature(String s) {
      signature = s; 
    }

    /**
       Retrieve the value of the <code>signature</code> slot of this
       event, containing the signature string for the described agent.
       @return The value of the <code>signature</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getSignature() {
      return signature;
    }

}
