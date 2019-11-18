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

package examples.Base64;

import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.DFService;
import jade.domain.FIPANames;

import java.util.*;
import java.io.*;

/**
This agent makes the following task:
1. searches in the DF for an ObjectReaderAgent;
2. sends an ACLMessage with a content encoded in Base64 to the 
   ObjectReaderAgent.
3. sends the same message by using the BitEfficient ACLCodec 
   first, and the XML ACLCodec then
4. sends an ACLMessage with a content encoded as a String
5. sends the same message by using again BitfficientACLCodec and XMLACLCodec

Notice that JADE actually encodes ACLMessages only and only if the receiver
of the message belongs to a remote platform. Notice that the ObjectReaderAgent
does not need any special provision to read bit-efficient or XML-coded messages
because all the burden is put into the platform that most provide the proper
decoding capabilities.
See also the bitefficient tutorial and the XMLACL tutorial that describe how
to launch the platform with this codec capabilities.

Because this agent implements a single sequential task, it does not use
any Behaviour.
@author Fabio Bellifemine - CSELT S.p.A
@version $Date: 2001-12-06 11:51:17 +0100 (gio, 06 dic 2001) $ $Revision: 2911 $
*/

public class ObjectWriterAgent extends Agent {


protected void setup() {

  /** Search with the DF for the name of the ObjectReaderAgent **/
  AID reader = new AID();
  DFAgentDescription dfd = new DFAgentDescription();  
  ServiceDescription sd = new ServiceDescription();
  sd.setType("ObjectReaderAgent"); 
  dfd.addServices(sd);
  try {
    while (true) {
      System.out.println(getLocalName()+ " waiting for an ObjectReaderAgent registering with the DF");
      SearchConstraints c = new SearchConstraints();
      c.setMaxDepth(new Long(3));
      DFAgentDescription[] result = DFService.search(this,dfd,c);
      if ((result != null) && (result.length > 0)) {
	dfd = result[0]; 
	reader = dfd.getName();
	break;
      }
      Thread.sleep(10000);
    }
  } catch (Exception fe) {
      fe.printStackTrace();
      System.err.println(getLocalName()+" search with DF is not succeeded because of " + fe.getMessage());
      doDelete();
  }

  System.out.println(getLocalName()+" agent sends ACLMessages whose content is a Java object");

   try {
      ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

      msg.addReceiver(reader);

      Person p = new Person("Name1", "Surname1", new Date(), 1);
      msg.setContentObject(p);
      msg.setLanguage("JavaSerialization");
      send(msg);
      System.out.println(getLocalName()+" sent 1st msg "+msg);

      msg.setDefaultEnvelope();
      msg.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.BITEFFICIENT);
      send(msg);
      System.out.println(getLocalName()+" sent 1st msg with bit-efficient aclCodec "+msg);

      msg.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.XML); 
      send(msg);
      System.out.println(getLocalName()+" sent 1st msg with xml aclCodec "+msg);

      p = new Person("Name2", "Surname2", new Date(), 2);
      msg.setContent(p.toString());
      msg.setLanguage("StringLanguage");
      msg.setDefaultEnvelope(); //reset the envelope to default ACLCodec
      send(msg);
      System.out.println(getLocalName()+" sent 2nd msg "+msg);

      msg.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.BITEFFICIENT); 
      send(msg);
      System.out.println(getLocalName()+" sent 2nd msg with bit-efficient aclCodec "+msg);
      
      msg.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.XML); 
      send(msg);
      System.out.println(getLocalName()+" sent 2nd msg with xml aclCodec "+msg);
  } catch (IOException e ) {
    e.printStackTrace();
  }
   doDelete(); // kill itself because it has completed its task.
  }
}
