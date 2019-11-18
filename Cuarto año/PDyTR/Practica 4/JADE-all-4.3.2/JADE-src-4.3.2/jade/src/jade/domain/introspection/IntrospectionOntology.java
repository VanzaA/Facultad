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

import jade.core.AgentState;
import jade.core.BehaviourID;
import jade.core.ContainerID;
import jade.core.Channel;

import jade.content.onto.*;
import jade.content.schema.*;

import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ExceptionOntology;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.FIPAAgentManagement.APService;


/**
 This class represents the ontology <code>jade-introspection</code>,
 containing all JADE extensions related to agent and platform
 monitoring. There is only a single instance of this class.
 
 <p>The package contains one class for each action, object or
 predicate defined in the ontology.</p><p></p>
 
 @author Giovanni Rimassa -  Universita' di Parma
 @version $Date: 2007-03-08 17:52:29 +0100 (gio, 08 mar 2007) $ $Revision: 5940 $
 
 */
public class IntrospectionOntology extends Ontology implements IntrospectionVocabulary {
	
	/**
	 A symbolic constant, containing the name of this ontology.
	 */
	public static final String NAME = "JADE-Introspection";
	
	private static Ontology theInstance = new IntrospectionOntology();
	
	
	/**
	 This method grants access to the unique instance of the
	 ontology.
	 @return An <code>Ontology</code> object, containing the concepts
	 of the ontology.
	 */
	public static Ontology getInstance() {
		return theInstance;
	}
	
	private IntrospectionOntology() {
		
		super(NAME, new Ontology[]{BasicOntology.getInstance(), SerializableOntology.getInstance()}, new BCReflectiveIntrospector());
		
		try {
			add(new ConceptSchema(META_RESETEVENTS), ResetEvents.class);
			add(new ConceptSchema(EVENTRECORD), EventRecord.class);
			add(new ConceptSchema(CONTAINERID), ContainerID.class);
			add(new ConceptSchema(AGENTSTATE), AgentState.class);
			add(new ConceptSchema(BEHAVIOURID), BehaviourID.class);
			add(new ConceptSchema(ACLMESSAGE), ACLMessage.class);
			add(new ConceptSchema(ENVELOPE), Envelope.class);
			add(new ConceptSchema(RECEIVEDOBJECT), ReceivedObject.class);
			add(new ConceptSchema(CHANNEL), Channel.class);
			
			
			add(new ConceptSchema(APDESCRIPTION), APDescription.class);
			add(new ConceptSchema(PLATFORMDESCRIPTION), PlatformDescription.class);
			add(new ConceptSchema(APSERVICE), APService.class);
			
			add(new PredicateSchema(OCCURRED), Occurred.class);  	
			add(new ConceptSchema(ADDEDCONTAINER), AddedContainer.class);  	
			add(new ConceptSchema(REMOVEDCONTAINER), RemovedContainer.class);
			add(new ConceptSchema(KILLCONTAINERREQUESTED), KillContainerRequested.class);
			add(new ConceptSchema(SHUTDOWNPLATFORMREQUESTED), ShutdownPlatformRequested.class);
			add(new ConceptSchema(ADDEDMTP), AddedMTP.class);
			add(new ConceptSchema(REMOVEDMTP), RemovedMTP.class);
			add(new ConceptSchema(BORNAGENT), BornAgent.class);
			add(new ConceptSchema(DEADAGENT), DeadAgent.class);
			add(new ConceptSchema(SUSPENDEDAGENT), SuspendedAgent.class);
			add(new ConceptSchema(RESUMEDAGENT), ResumedAgent.class);
			add(new ConceptSchema(FROZENAGENT), FrozenAgent.class);
			add(new ConceptSchema(THAWEDAGENT), ThawedAgent.class);
			add(new ConceptSchema(CHANGEDAGENTOWNERSHIP), ChangedAgentOwnership.class);
			add(new ConceptSchema(MOVEDAGENT), MovedAgent.class);
			add(new ConceptSchema(CHANGEDAGENTSTATE), ChangedAgentState.class);
			add(new ConceptSchema(ADDEDBEHAVIOUR), AddedBehaviour.class);
			add(new ConceptSchema(REMOVEDBEHAVIOUR), RemovedBehaviour.class);
			add(new ConceptSchema(CHANGEDBEHAVIOURSTATE), ChangedBehaviourState.class);
			add(new ConceptSchema(SENTMESSAGE), SentMessage.class);
			add(new ConceptSchema(RECEIVEDMESSAGE), ReceivedMessage.class);
			add(new ConceptSchema(POSTEDMESSAGE), PostedMessage.class);
			add(new ConceptSchema(ROUTEDMESSAGE), RoutedMessage.class);
			
			add(new AgentActionSchema(STARTNOTIFY), StartNotify.class);
			add(new AgentActionSchema(STOPNOTIFY), StopNotify.class);
			add(new AgentActionSchema(GETKEYS), GetKeys.class);
			add(new AgentActionSchema(GETVALUE), GetValue.class);
			
			ConceptSchema cs = (ConceptSchema)getSchema(EVENTRECORD);
			cs.add(EVENTRECORD_WHAT, (TermSchema)TermSchema.getBaseSchema());
			cs.add(EVENTRECORD_WHEN, (PrimitiveSchema)getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);
			cs.add(EVENTRECORD_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(CONTAINERID);
			cs.add(CONTAINERID_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(CONTAINERID_ADDRESS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(CONTAINERID_MAIN, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			cs.add(CONTAINERID_PORT, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(CONTAINERID_PROTOCOL, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			
			
			cs = (ConceptSchema)getSchema(AGENTSTATE);
			cs.add(AGENTSTATE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			
			cs = (ConceptSchema)getSchema(BEHAVIOURID);
			cs.add(BEHAVIOURID_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(BEHAVIOURID_CLASS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(BEHAVIOURID_KIND, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(BEHAVIOURID_CHILDREN, (ConceptSchema)getSchema(BEHAVIOURID), 0, ObjectSchema.UNLIMITED);
			cs.add(BEHAVIOURID_CODE, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(ACLMESSAGE);
			cs.add(ACLMESSAGE_ENVELOPE, (ConceptSchema)getSchema(ENVELOPE), ObjectSchema.OPTIONAL);
			cs.add(ACLMESSAGE_ACLREPRESENTATION, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(ACLMESSAGE_PAYLOAD, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(ENVELOPE);
			cs.add(ENVELOPE_TO, (ConceptSchema)getSchema(BasicOntology.AID), 0, ObjectSchema.UNLIMITED);
			cs.add(ENVELOPE_FROM, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.OPTIONAL);
			cs.add(ENVELOPE_COMMENTS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(ENVELOPE_PAYLOADLENGTH, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			cs.add(ENVELOPE_PAYLOADENCODING, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(ENVELOPE_DATE, (PrimitiveSchema)getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);
			cs.add(ENVELOPE_INTENDEDRECEIVER, (ConceptSchema)getSchema(BasicOntology.AID), 0, ObjectSchema.UNLIMITED);
			cs.add(ENVELOPE_RECEIVED, (ConceptSchema)getSchema(RECEIVEDOBJECT), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(RECEIVEDOBJECT);
			cs.add(RECEIVEDOBJECT_BY, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(RECEIVEDOBJECT_FROM, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(RECEIVEDOBJECT_DATE, (PrimitiveSchema)getSchema(BasicOntology.DATE), ObjectSchema.OPTIONAL);
			cs.add(RECEIVEDOBJECT_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(RECEIVEDOBJECT_VIA, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(CHANNEL);
			cs.add(CHANNEL_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(CHANNEL_PROTOCOL, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(CHANNEL_ADDRESS, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			
			cs = (ConceptSchema)getSchema(PLATFORMDESCRIPTION);
			cs.add(PLATFORMDESCRIPTION_PLATFORM, (ConceptSchema)getSchema(APDESCRIPTION));
			
			cs = (ConceptSchema)getSchema(ADDEDCONTAINER);
			cs.add(ADDEDCONTAINER_CONTAINER, (ConceptSchema)getSchema(CONTAINERID));
			cs.add(ADDEDCONTAINER_OWNERSHIP, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(REMOVEDCONTAINER);
			cs.add(REMOVEDCONTAINER_CONTAINER, (ConceptSchema)getSchema(CONTAINERID));
			
			cs = (ConceptSchema)getSchema(KILLCONTAINERREQUESTED);
			cs.add(KILLCONTAINERREQUESTED_CONTAINER, (ConceptSchema)getSchema(CONTAINERID));
			
			cs = (ConceptSchema)getSchema(ADDEDMTP);
			cs.add(ADDEDMTP_ADDRESS, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(ADDEDMTP_WHERE, (ConceptSchema)getSchema(CONTAINERID));
			
			cs = (ConceptSchema)getSchema(REMOVEDMTP);
			cs.add(REMOVEDMTP_ADDRESS, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(REMOVEDMTP_WHERE, (ConceptSchema)getSchema(CONTAINERID));
			
			cs = (ConceptSchema)getSchema(BORNAGENT);
			cs.add(BORNAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(BORNAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			cs.add(BORNAGENT_STATE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(BORNAGENT_OWNERSHIP, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(BORNAGENT_CLASS_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(DEADAGENT);
			cs.add(DEADAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(DEADAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			cs.add(DEADAGENT_CONTAINER_REMOVED, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(SUSPENDEDAGENT);
			cs.add(SUSPENDEDAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(SUSPENDEDAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(RESUMEDAGENT);
			cs.add(RESUMEDAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(RESUMEDAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(FROZENAGENT);
			cs.add(FROZENAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(FROZENAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			cs.add(FROZENAGENT_BUFFERCONTAINER, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(THAWEDAGENT);
			cs.add(THAWEDAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(THAWEDAGENT_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			cs.add(THAWEDAGENT_BUFFERCONTAINER, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(CHANGEDAGENTOWNERSHIP);
			cs.add(CHANGEDAGENTOWNERSHIP_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));	
			cs.add(CHANGEDAGENTOWNERSHIP_FROM, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(CHANGEDAGENTOWNERSHIP_TO, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(CHANGEDAGENTOWNERSHIP_WHERE, (ConceptSchema)getSchema(CONTAINERID), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(MOVEDAGENT);
			cs.add(MOVEDAGENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(MOVEDAGENT_FROM, (ConceptSchema)getSchema(CONTAINERID));
			cs.add(MOVEDAGENT_TO, (ConceptSchema)getSchema(CONTAINERID));
			
			cs = (ConceptSchema)getSchema(CHANGEDAGENTSTATE);
			cs.add(CHANGEDAGENTSTATE_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(CHANGEDAGENTSTATE_FROM, (ConceptSchema)getSchema(AGENTSTATE));
			cs.add(CHANGEDAGENTSTATE_TO, (ConceptSchema)getSchema(AGENTSTATE));
			
			cs = (ConceptSchema)getSchema(ADDEDBEHAVIOUR);
			cs.add(ADDEDBEHAVIOUR_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(ADDEDBEHAVIOUR_BEHAVIOUR, (ConceptSchema)getSchema(BEHAVIOURID));
			
			cs = (ConceptSchema)getSchema(REMOVEDBEHAVIOUR);
			cs.add(REMOVEDBEHAVIOUR_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(REMOVEDBEHAVIOUR_BEHAVIOUR, (ConceptSchema)getSchema(BEHAVIOURID));
			
			cs = (ConceptSchema)getSchema(CHANGEDBEHAVIOURSTATE);
			cs.add(CHANGEDBEHAVIOURSTATE_AGENT, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(CHANGEDBEHAVIOURSTATE_BEHAVIOUR, (ConceptSchema)getSchema(BEHAVIOURID));
			cs.add(CHANGEDBEHAVIOURSTATE_FROM, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(CHANGEDBEHAVIOURSTATE_TO, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			
			cs = (ConceptSchema)getSchema(SENTMESSAGE);
			cs.add(SENTMESSAGE_SENDER, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(SENTMESSAGE_RECEIVER, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(SENTMESSAGE_MESSAGE, (ConceptSchema)getSchema(ACLMESSAGE));
			
			cs = (ConceptSchema)getSchema(RECEIVEDMESSAGE);
			cs.add(RECEIVEDMESSAGE_SENDER, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(RECEIVEDMESSAGE_RECEIVER, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(RECEIVEDMESSAGE_MESSAGE, (ConceptSchema)getSchema(ACLMESSAGE));
			
			cs = (ConceptSchema)getSchema(POSTEDMESSAGE);
			cs.add(POSTEDMESSAGE_SENDER, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(POSTEDMESSAGE_RECEIVER, (ConceptSchema)getSchema(BasicOntology.AID));
			cs.add(POSTEDMESSAGE_MESSAGE, (ConceptSchema)getSchema(ACLMESSAGE));
			
			cs = (ConceptSchema)getSchema(ROUTEDMESSAGE);
			cs.add(ROUTEDMESSAGE_FROM, (ConceptSchema)getSchema(CHANNEL));
			cs.add(ROUTEDMESSAGE_TO, (ConceptSchema)getSchema(CHANNEL));
			cs.add(ROUTEDMESSAGE_MESSAGE, (ConceptSchema)getSchema(ACLMESSAGE));
			
			cs = (ConceptSchema)getSchema(APDESCRIPTION);
			cs.add(APDESCRIPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(APDESCRIPTION_SERVICES, (ConceptSchema)getSchema(APSERVICE), 0, ObjectSchema.UNLIMITED);
			
			cs = (ConceptSchema)getSchema(APSERVICE);
			cs.add(APSERVICE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(APSERVICE_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(APSERVICE_ADDRESSES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
			AgentActionSchema as = (AgentActionSchema)getSchema(STARTNOTIFY);
			as.add(STARTNOTIFY_OBSERVED, (ConceptSchema)getSchema(BasicOntology.AID));
			as.add(STARTNOTIFY_EVENTS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
			as = (AgentActionSchema)getSchema(STOPNOTIFY);
			as.add(STOPNOTIFY_OBSERVED, (ConceptSchema)getSchema(BasicOntology.AID));
			as.add(STOPNOTIFY_EVENTS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
			as = (AgentActionSchema)getSchema(GETVALUE);
			as.add(GETVALUE_KEY, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			
			PredicateSchema ps = (PredicateSchema)getSchema(OCCURRED);
			ps.add(OCCURRED_WHAT, (ConceptSchema)getSchema(EVENTRECORD));
		}
		catch(OntologyException oe) {
			oe.printStackTrace();
		}
	} //end of initInstance
	
	//#APIDOC_EXCLUDE_BEGIN
	
	// Used for debug
	public static void main(String args[]) {
		Ontology ontology = IntrospectionOntology.getInstance();
	}
	
	//#APIDOC_EXCLUDE_END
	
	
}
