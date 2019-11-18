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

//#APIDOC_EXCLUDE_FILE

import jade.domain.FIPAAgentManagement.Envelope;
import jade.content.Concept;

/**

  This class represents an ACL message.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $

*/
public class ACLMessage implements Concept {

    private Envelope env;
    private String payload;

    /**
       Default constructor. A default constructor is necessaty for
       JADE ontological classes.
    */
    public ACLMessage() {
    }

    /**
       Set the message envelope.
       @param e The transport-level envelope to attach to this ACL
       message.
    */
    public void setEnvelope(Envelope e) {
	env = e;
    }

    /**
       Retrieve the message envelope.
       @return The transport-level envelope attached to this ACL
       message, or <code>null</code> if no envelope was set.
    */
    public Envelope getEnvelope() {
	return env;
    }

    /**
       Set the representation of the message payload.
       @param r The name of the representation expressing the message
       payload.
    */
    public void setAclRepresentation(String r) {
	if(env != null)
	    env.setAclRepresentation(r);
    }

    /**
       Retrieve the representation of the message payload.
       @return The ACL representation of the message.
    */
    public String getAclRepresentation() {
	if(env != null)
	    return env.getAclRepresentation();
	else
	    return null;
    }

    /**
       Set the payload (i.e. the speech act level part) of this ACL
       message.
       @param p A string containing the encoding of the payload
       according to a concrete ACL representaiton.
    */
    public void setPayload(String p) {
	payload = p;
    }

    /**
       Retrieve the payload (i.e. the speech act level part) of this
       ACL message.
       @return A string containing the encoding of the payload
       according to a concrete ACL representation.
    */
    public String getPayload() {
	return payload;
    }

}
