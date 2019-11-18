/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by
the IST-1999-10211 LEAP Project

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

/**
   This interface contains all the string constants for frame and slot
   names of exceptions defined in the <code>jade-introspection</code>
   ontology.
*/
public interface IntrospectionVocabulary {
	
// Concepts

  public static final String APDESCRIPTION				= "ap-description";
  public static final String APDESCRIPTION_NAME				= "name";
  public static final String APDESCRIPTION_SERVICES			= "ap-services";
  
  public static final String APSERVICE          			= "ap-service";
  public static final String APSERVICE_NAME          			= "name";
  public static final String APSERVICE_TYPE          			= "type";
  public static final String APSERVICE_ADDRESSES      			= "addresses"; 

  public static final String EVENTRECORD = "event-record";
  public static final String EVENTRECORD_WHAT = "what";
  public static final String EVENTRECORD_WHEN = "when";
  public static final String EVENTRECORD_WHERE = "where";

  public static final String META_RESETEVENTS = "meta_reset-events";
  
  public static final String ADDEDCONTAINER = "added-container";
  public static final String ADDEDCONTAINER_CONTAINER = "container";
  public static final String ADDEDCONTAINER_OWNERSHIP = "ownership";

  public static final String REMOVEDCONTAINER = "removed-container";
  public static final String REMOVEDCONTAINER_CONTAINER = "container";
  
  public static final String KILLCONTAINERREQUESTED = "kill-container-requested";
  public static final String KILLCONTAINERREQUESTED_CONTAINER = "container";
  
  public static final String SHUTDOWNPLATFORMREQUESTED = "shutdown-platform-requested";
  
  public static final String ADDEDMTP = "added-mtp";
  public static final String ADDEDMTP_ADDRESS = "address";
  public static final String ADDEDMTP_WHERE = "where";
    
  public static final String REMOVEDMTP = "removed-mtp";
  public static final String REMOVEDMTP_ADDRESS = "address";
  public static final String REMOVEDMTP_WHERE = "where";
  
  public static final String BORNAGENT = "born-agent";
  public static final String BORNAGENT_AGENT = "agent";
  public static final String BORNAGENT_WHERE = "where";
  public static final String BORNAGENT_STATE = "state";
  public static final String BORNAGENT_OWNERSHIP = "ownership";
  public static final String BORNAGENT_CLASS_NAME = "class-name";
  
  public static final String DEADAGENT = "dead-agent";
  public static final String DEADAGENT_AGENT = "agent";
  public static final String DEADAGENT_WHERE = "where";
  public static final String DEADAGENT_CONTAINER_REMOVED = "container-removed";

  public static final String SUSPENDEDAGENT = "suspended-agent";
  public static final String SUSPENDEDAGENT_AGENT = "agent";
  public static final String SUSPENDEDAGENT_WHERE = "where";

  public static final String RESUMEDAGENT = "resumed-agent";
  public static final String RESUMEDAGENT_AGENT = "agent";
  public static final String RESUMEDAGENT_WHERE = "where";

  public static final String FROZENAGENT = "frozen-agent";
  public static final String FROZENAGENT_AGENT = "agent";
  public static final String FROZENAGENT_WHERE = "where";
  public static final String FROZENAGENT_BUFFERCONTAINER = "buffer-container";

  public static final String THAWEDAGENT = "thawed-agent";
  public static final String THAWEDAGENT_AGENT = "agent";
  public static final String THAWEDAGENT_WHERE = "where";
  public static final String THAWEDAGENT_BUFFERCONTAINER = "buffer-container";

  public static final String CHANGEDAGENTOWNERSHIP = "changed-agent-ownership";
  public static final String CHANGEDAGENTOWNERSHIP_AGENT = "agent";
  public static final String CHANGEDAGENTOWNERSHIP_FROM = "from";
  public static final String CHANGEDAGENTOWNERSHIP_TO = "to";
  public static final String CHANGEDAGENTOWNERSHIP_WHERE = "where";
    
  public static final String MOVEDAGENT = "moved-agent";
  public static final String MOVEDAGENT_AGENT = "agent";
  public static final String MOVEDAGENT_TO = "to";
  public static final String MOVEDAGENT_FROM = "from";
  
  public static final String CHANGEDAGENTSTATE = "changed-agent-state";
  public static final String CHANGEDAGENTSTATE_AGENT = "agent";
  public static final String CHANGEDAGENTSTATE_FROM = "from";
  public static final String CHANGEDAGENTSTATE_TO = "to";

  
  public static final String ADDEDBEHAVIOUR = "added-behaviour";
  public static final String ADDEDBEHAVIOUR_AGENT = "agent";
  public static final String ADDEDBEHAVIOUR_BEHAVIOUR = "behaviour";
  
  public static final String REMOVEDBEHAVIOUR = "removed-behaviour";
  public static final String REMOVEDBEHAVIOUR_AGENT = "agent";
  public static final String REMOVEDBEHAVIOUR_BEHAVIOUR = "behaviour";
  
  public static final String CHANGEDBEHAVIOURSTATE = "changed-behaviour-state";
  public static final String CHANGEDBEHAVIOURSTATE_AGENT = "agent";
  public static final String CHANGEDBEHAVIOURSTATE_BEHAVIOUR = "behaviour";
  public static final String CHANGEDBEHAVIOURSTATE_FROM = "from";
  public static final String CHANGEDBEHAVIOURSTATE_TO = "to";
    
  public static final String SENTMESSAGE = "sent-message";
  public static final String SENTMESSAGE_SENDER = "sender";
  public static final String SENTMESSAGE_RECEIVER = "receiver";
  public static final String SENTMESSAGE_MESSAGE = "message";
  
  public static final String RECEIVEDMESSAGE = "received-message";
  public static final String RECEIVEDMESSAGE_SENDER = "sender";
  public static final String RECEIVEDMESSAGE_RECEIVER = "receiver";
  public static final String RECEIVEDMESSAGE_MESSAGE = "message";
   
  public static final String POSTEDMESSAGE = "posted-message";
  public static final String POSTEDMESSAGE_SENDER = "sender";
  public static final String POSTEDMESSAGE_RECEIVER = "receiver";
  public static final String POSTEDMESSAGE_MESSAGE = "message";
  
  public static final String ROUTEDMESSAGE = "routed-message";
  public static final String ROUTEDMESSAGE_FROM = "from";
  public static final String ROUTEDMESSAGE_TO = "to";
  public static final String ROUTEDMESSAGE_MESSAGE = "message";
  
  public static final String CONTAINERID = "container-ID";
  public static final String CONTAINERID_NAME = "name";
  public static final String CONTAINERID_ADDRESS = "address";
  public static final String CONTAINERID_MAIN = "main";
  public static final String CONTAINERID_PORT = "port";
  public static final String CONTAINERID_PROTOCOL = "protocol";

  
  public static final String AGENTSTATE = "agent-state";
  public static final String AGENTSTATE_NAME = "name";
  
  public static final String BEHAVIOURID = "behaviour-ID";
  public static final String BEHAVIOURID_NAME = "name";
  public static final String BEHAVIOURID_CLASS_NAME = "class-name";
  public static final String BEHAVIOURID_KIND = "kind";
  public static final String BEHAVIOURID_CHILDREN = "children";
  public static final String BEHAVIOURID_CODE = "code";
  
  public static final String ACLMESSAGE = "acl-message";
  public static final String ACLMESSAGE_ENVELOPE = "envelope";
  public static final String ACLMESSAGE_PAYLOAD = "payload";
  public static final String ACLMESSAGE_ACLREPRESENTATION = "acl-representation";
    
  public static final String ENVELOPE = "envelope";
  public static final String ENVELOPE_TO = "to";
  public static final String ENVELOPE_FROM = "from";
  public static final String ENVELOPE_COMMENTS = "comments";
  public static final String ENVELOPE_ACLREPRESENTATION = "acl-representation";
  public static final String ENVELOPE_PAYLOADLENGTH = "payload-length";
  public static final String ENVELOPE_PAYLOADENCODING = "payload-encoding";
  public static final String ENVELOPE_DATE = "date";
  public static final String ENVELOPE_INTENDEDRECEIVER = "intended-receiver";
  public static final String ENVELOPE_RECEIVED = "received";
  
  public static final String RECEIVEDOBJECT = "received-object";
  public static final String RECEIVEDOBJECT_BY = "by";
  public static final String RECEIVEDOBJECT_FROM = "from";
  public static final String RECEIVEDOBJECT_DATE = "date";
  public static final String RECEIVEDOBJECT_ID = "id";
  public static final String RECEIVEDOBJECT_VIA = "via";
  
  public static final String CHANNEL = "channel";
  public static final String CHANNEL_NAME = "name";
  public static final String CHANNEL_PROTOCOL = "protocol";
  public static final String CHANNEL_ADDRESS = "address";
  

  public static final String PLATFORMDESCRIPTION = "platform-description";
  public static final String PLATFORMDESCRIPTION_PLATFORM = "platform";
  
    // Actions
  public static final String STARTNOTIFY = "start-notify";
  public static final String STARTNOTIFY_OBSERVED = "observed";
  public static final String STARTNOTIFY_EVENTS = "events";
  
  public static final String STOPNOTIFY = "stop-notify";
  public static final String STOPNOTIFY_OBSERVED= "observed";
  public static final String STOPNOTIFY_EVENTS = "events";

  public static final String GETKEYS = "get-keys";
  
  public static final String GETVALUE = "get-value";
  public static final String GETVALUE_KEY = "key";
  
  // Predicates
  public static final String OCCURRED = "occurred";
  public static final String OCCURRED_WHAT = "what";
	
}
