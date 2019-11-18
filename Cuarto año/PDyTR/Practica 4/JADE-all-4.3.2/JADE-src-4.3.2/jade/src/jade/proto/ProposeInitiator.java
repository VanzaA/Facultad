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

package jade.proto;

//#CUSTOM_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.states.MsgReceiver;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Serializable;
import jade.domain.FIPANames.InteractionProtocol;

/**
 * This class implements the Fipa-Propose interaction protocol
 * with an API similar and homogeneous to <code>AchieveREInitiator</code>.
 * <br>
 * This implementation works both for 1:1 and 1:N conversation and, of course,
 * implements the role of the initiator of the protocol.
 * <p>
 * The following is a brief description of the protocol. The programmer
 * should however refer to the 
 * <a href=http://www.fipa.org/specs/fipa00036/SC00036H.html>FIPA Spec</a>
 * for a complete description.
 * <p>
 * The initiator sends a <code>PROPOSE</code> message to the Participant
 * indicating that it will perform some action if the Participant agrees.
 * The implementation of the callback method <code>prepareInitiations</code>
 * must return the vector of messages to be sent (eventually a single message 
 * with multiple receivers).
 * <p>
 * The responders can then reply by sending a <code>ACCEPT-PROPOSAL</code>
 * message. Alternatively, responders may send a <code>REJECT-PROPOSAL</code>,
 * to refuse the proposal or, eventually, a <code>NOT-UNDERSTOOD</code> to 
 * communicate communication problems.
 * This category of reply messages has been here identified as a 
 * response and can be handled via the <code>handleAllResponses</code>
 * callback method.
 * Specific handle callback methods for each type of communicative act are also
 * available when the programmer wishes to handle them separately:
 * <code>handleRejectProposal, handleAcceptProposal, handleNotUnderstood</code>
 * <p> 
 * If a message were received, with the same value of this 
 * <code>conversation-id</code>, but that does not comply with the FIPA 
 * protocol, than the method <code>handleOutOfSequence</code> would be called.
 * <p>
 * This class can be extended by the programmer by overriding all the needed
 * handle methods or, in alternative, appropriate behaviours can be
 * registered for each handle via the <code>registerHandle</code>-type
 * of methods. This last case is more difficult to use and proper
 * care must be taken to properly use the <code>DataStore</code> of the
 * <code>Behaviour</code> as a shared memory mechanism with the
 * registered behaviour.
 * <p>
 * Read carefully the section of the 
 * <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
 * that describes
 * the usage of this class.
 *
 * @author Jerome Picault - Motorola Labs
 * @version $Date: 2005-09-16 15:54:46 +0200 (ven, 16 set 2005) $ $Revision: 5780 $
 * @since JADE3.1
 * @see ProposeResponder
 * @see AchieveREInitiator
 * @see <a href=http://www.fipa.org/specs/fipa00036>FIPA Spec</a>
 **/
public class ProposeInitiator extends FSMBehaviour {

  // Private data store keys (can't be static since if we register another instance of this class as stare of the FSM 
  //using the same data store the new values overrides the old one. 
  /** 
   * key to retrieve from the DataStore of the behaviour the ACLMessage 
   *	object passed in the constructor of the class.
   **/
  protected final String INITIATION_K = "__initiation" + hashCode();

  /** 
   * key to retrieve from the DataStore of the behaviour the vector of
   * PROPOSE ACLMessage objects that have to be sent.
   **/
  protected final String ALL_INITIATIONS_K = "__all-initiations" +hashCode();
	
  /** 
   * key to retrieve from the DataStore of the behaviour the last
   * ACLMessage object that has been received (null if the timeout
   * expired). 
   **/
  public final String REPLY_KEY = "__reply" + hashCode();
  
  /** 
   * key to retrieve from the DataStore of the behaviour the vector of
   * ACLMessage objects that have been received as responses.
   **/
  public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();

  // FSM states names
  protected static final String PREPARE_INITIATIONS = "Prepare-initiations";
  protected static final String SEND_INITIATIONS = "Send-initiations";
  protected static final String RECEIVE_REPLY = "Receive-reply";
  protected static final String CHECK_IN_SEQ = "Check-in-seq";
  protected static final String HANDLE_NOT_UNDERSTOOD = "Handle-not-understood";
  protected static final String HANDLE_OUT_OF_SEQ = "Handle-out-of-seq";
  protected static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
  protected static final String HANDLE_REJECT_PROPOSAL = "Handle-reject-proposal";
  protected static final String HANDLE_ACCEPT_PROPOSAL = "Handle-accept-proposal";
  private static final String CHECK_AGAIN = "Check-again";  
  protected static final String CHECK_SESSIONS = "Check-sessions";
  protected static final String DUMMY_FINAL = "Dummy-final";


    //#APIDOC_EXCLUDE_BEGIN

  // This maps the AID of each responder to a Session object
  // holding the status of the protocol as far as that responder
  // is concerned. Sessions are protocol-specific
  protected Map sessions = new HashMap();	
  // The MsgReceiver behaviour used to receive replies 
  protected MsgReceiver replyReceiver = null;
  // The MessageTemplate used by the replyReceiver
  protected MessageTemplate replyTemplate = null; 

    //#APIDOC_EXCLUDE_END

  private ACLMessage initiation;
  
  // States exit values
  private static final int ALL_RESPONSES_RECEIVED = 1;
  private static final int TERMINATED = 2;

    //#APIDOC_EXCLUDE_BEGIN
 
	// These states must be reset before they are visited again.
  // Note that resetting a state before visiting it again is required
  // only if
  // - The onStart() method is redefined
  // - The state has an "internal memory"
  // The states below must be reset as the user can redefine them -->
  // They can fall in one of the above categories.
  protected String[] toBeReset = new String[] {
    HANDLE_ACCEPT_PROPOSAL,
    HANDLE_REJECT_PROPOSAL,
    HANDLE_NOT_UNDERSTOOD,
    HANDLE_OUT_OF_SEQ
  };

    //#APIDOC_EXCLUDE_END


  // If set to true all expected responses have been received
  private boolean allResponsesReceived = false;
	
  /**
   * Construct a <code>ProposeInitiator</code> with an empty DataStore
   * @see #ProposeInitiator(Agent, ACLMessage, DataStore)
   **/
  public ProposeInitiator(Agent a, ACLMessage msg){
    this(a,msg,new DataStore());
  }

  /**
   * Construct a <code>ProposeInitiator</code> with a given DataStore
   * @param a The agent performing the protocol
   * @param initiation The message that must be used to initiate the protocol.
   * Notice that the default implementation of the 
   * <code>prepareInitiations()</code> method returns
   * an array composed of only this message.
   * The values of the slot 
   * <code>reply-with</code> is ignored and a different value is assigned
   * automatically by this class for each receiver.
   * @param store The <code>DataStore</code> that will be used by this 
   * <code>ProposeInitiator</code>
   */
  public ProposeInitiator(Agent a, ACLMessage initiation, DataStore store) {
    super(a);

    setDataStore(store);
    this.initiation = initiation;

    // Register the FSM transitions
    registerDefaultTransition(PREPARE_INITIATIONS, SEND_INITIATIONS);
    registerTransition(SEND_INITIATIONS, DUMMY_FINAL, 0); // Exit the protocol if no initiation message is sent
    registerDefaultTransition(SEND_INITIATIONS, RECEIVE_REPLY);
    registerTransition(RECEIVE_REPLY, CHECK_SESSIONS, MsgReceiver.TIMEOUT_EXPIRED); 
    registerTransition(RECEIVE_REPLY, CHECK_SESSIONS, MsgReceiver.INTERRUPTED); 
    registerDefaultTransition(RECEIVE_REPLY, CHECK_IN_SEQ);
    registerTransition(CHECK_IN_SEQ, HANDLE_NOT_UNDERSTOOD, ACLMessage.NOT_UNDERSTOOD);		
    registerTransition(CHECK_IN_SEQ, HANDLE_REJECT_PROPOSAL, ACLMessage.REJECT_PROPOSAL);
    registerTransition(CHECK_IN_SEQ, HANDLE_ACCEPT_PROPOSAL, ACLMessage.ACCEPT_PROPOSAL);
    registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQ);		
    registerDefaultTransition(HANDLE_NOT_UNDERSTOOD, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_REJECT_PROPOSAL, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_ACCEPT_PROPOSAL, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_OUT_OF_SEQ, RECEIVE_REPLY);
    registerDefaultTransition(CHECK_SESSIONS, RECEIVE_REPLY, toBeReset);
    registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
    registerTransition(CHECK_SESSIONS, DUMMY_FINAL, TERMINATED); 
    registerDefaultTransition(HANDLE_ALL_RESPONSES, CHECK_AGAIN);
    registerTransition(CHECK_AGAIN, DUMMY_FINAL, 0);
    registerDefaultTransition(CHECK_AGAIN, RECEIVE_REPLY, toBeReset);
			
    Behaviour b = null;
    // PREPARE_INITIATIONS
    b = new OneShotBehaviour(myAgent) {
        private static final long serialVersionUID = 3487495895818000L;
        public void action() {
          DataStore ds = getDataStore();
          Vector allInitiations = prepareInitiations((ACLMessage) ds.get(INITIATION_K));
          getDataStore().put(ALL_INITIATIONS_K, allInitiations);
        }
      };
    b.setDataStore(getDataStore());		
    registerFirstState(b, PREPARE_INITIATIONS);
		
    // SEND_INITIATIONS
    b = new OneShotBehaviour(myAgent) {
        private static final long serialVersionUID = 3487495895818001L;
        public void action() {
          Vector allInitiations = (Vector) getDataStore().get(ALL_INITIATIONS_K);
          if (allInitiations != null) {
            sendInitiations(allInitiations);
          }
        }	
        public int onEnd() {
          return sessions.size();
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, SEND_INITIATIONS);
	
    // RECEIVE_REPLY
    replyReceiver = new MsgReceiver(myAgent, null, MsgReceiver.INFINITE, getDataStore(), REPLY_KEY);
    registerState(replyReceiver, RECEIVE_REPLY);
	
    // CHECK_IN_SEQ
    b = new OneShotBehaviour(myAgent) {
        int ret;
        private static final long     serialVersionUID = 3487495895818002L;
  			
        public void action() {
          ACLMessage reply = (ACLMessage) getDataStore().get(REPLY_KEY);
          if (checkInSequence(reply)) {
            ret = reply.getPerformative();
          }
          else {
            ret = -1;
          }
        }
        public int onEnd() {
          return ret;
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, CHECK_IN_SEQ);

    // HANDLE_ALL_RESPONSES
    b = new OneShotBehaviour(myAgent) {
        public void action() {
          handleAllResponses((Vector) getDataStore().get(ALL_RESPONSES_KEY));
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, HANDLE_ALL_RESPONSES);
	
    // HANDLE_ACCEPT_PROPOSAL
    b = new OneShotBehaviour(myAgent) {
        public void action() {
          handleAcceptProposal((ACLMessage) getDataStore().get(REPLY_KEY));
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, HANDLE_ACCEPT_PROPOSAL);
		
    // HANDLE_REJECT_PROPOSAL
    b = new OneShotBehaviour(myAgent) {
        public void action() {
          handleRejectProposal((ACLMessage) getDataStore().get(REPLY_KEY));
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, HANDLE_REJECT_PROPOSAL);
	
    // HANDLE_NOT_UNDERSTOOD
    b = new OneShotBehaviour(myAgent) {
        private static final long     serialVersionUID = 3487495895818005L;
  	
        public void action() {
          handleNotUnderstood((ACLMessage) getDataStore().get(REPLY_KEY));
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, HANDLE_NOT_UNDERSTOOD);
	
    // HANDLE_OUT_OF_SEQ
    b = new OneShotBehaviour(myAgent) {
        private static final long     serialVersionUID = 3487495895818008L;
  	
        public void action() {
          handleOutOfSequence((ACLMessage) getDataStore().get(REPLY_KEY));
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, HANDLE_OUT_OF_SEQ);
	
    // CHECK_SESSIONS
    b = new OneShotBehaviour(myAgent) {
        int ret;
        private static final long     serialVersionUID = 3487495895818009L;
  	
        public void action() {
          ACLMessage reply = (ACLMessage) getDataStore().get(REPLY_KEY);
          ret = checkSessions(reply);
        }		
        public int onEnd() {
          return ret;
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, CHECK_SESSIONS);
	
    // CHECK_AGAIN
    
    b = new OneShotBehaviour(myAgent) {
        public void action() {
        }
        public int onEnd() {
          return sessions.size();
        }
      };
    b.setDataStore(getDataStore());		
    registerState(b, CHECK_AGAIN);

    // DUMMY_FINAL
    b = new OneShotBehaviour(myAgent) {
        private static final long     serialVersionUID = 3487495895818010L;
  	
        public void action() {}
      };
    registerLastState(b, DUMMY_FINAL);
  }

  /**
   * Initialize the data store. 
   */
  protected void initializeDataStore(ACLMessage msg){
    getDataStore().put(INITIATION_K, initiation);
    Vector l = new Vector();
    getDataStore().put(ALL_RESPONSES_KEY, l);
  }

  /**
   * Create and initialize the Sessions and sends the initiation messages.
   * This method is called internally by the framework and is not intended 
   * to be called by the user
   */    
  protected void sendInitiations(Vector initiations) {
    long currentTime = System.currentTimeMillis();
    long minTimeout = -1;
    long deadline = -1;

    String conversationID = createConvId(initiations);
    replyTemplate = MessageTemplate.MatchConversationId(conversationID);
    int cnt = 0; // counter of sessions
    for (Enumeration e=initiations.elements(); e.hasMoreElements(); ) {
      ACLMessage proposal = (ACLMessage) e.nextElement();
      if (proposal != null) {
        // Update the list of sessions on the basis of the receivers
        // FIXME: Maybe this should take the envelope into account first
			    
        ACLMessage toSend = (ACLMessage)proposal.clone();
        toSend.setProtocol(InteractionProtocol.FIPA_PROPOSE);
        toSend.setConversationId(conversationID);
        for (Iterator receivers = proposal.getAllReceiver(); receivers.hasNext(); ) {
          toSend.clearAllReceiver();
          AID r = (AID)receivers.next();
          toSend.addReceiver(r);
          String sessionKey = "R" + hashCode()+  "_" + Integer.toString(cnt);
          toSend.setReplyWith(sessionKey);
          sessions.put(sessionKey, new Session());
          adjustReplyTemplate(toSend);
          myAgent.send(toSend);
          // Store the propose message actually sent. It can 
          // be useful to retrieve it to create the CANCEL message
          getDataStore().put(r, toSend);
          cnt++;
        }
			  
        // Update the timeout (if any) used to wait for replies according
        // to the reply-by field. Get the miminum  
        Date d = proposal.getReplyByDate();
        if (d != null) {
          long timeout = d.getTime()- currentTime;
          if (timeout > 0 && (timeout < minTimeout || minTimeout <= 0)) {
            minTimeout = timeout;
            deadline = d.getTime();
          }
        }
      }
    }
    // Finally set the MessageTemplate and timeout used in the RECEIVE_REPLY 
    // state to accept replies
    replyReceiver.setTemplate(replyTemplate);
    replyReceiver.setDeadline(deadline);
  }
    
  /**
   * Check whether a reply is in-sequence and update the appropriate Session.
   * This method is called internally by the framework and is not intended 
   * to be called by the user       
   */    
  protected boolean checkInSequence(ACLMessage reply) {
    String inReplyTo = reply.getInReplyTo();
    Session s = (Session) sessions.get(inReplyTo);
    if (s != null) {
      int perf = reply.getPerformative();
      if (s.update(perf)) {
        // The reply is compliant to the protocol 
        switch (s.getState()) {
        case Session.RESULT_NOTIFICATION_RECEIVED:
        case Session.NEGATIVE_RESPONSE_RECEIVED:
          // The reply is a response
          Vector allRsp = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
          allRsp.addElement(reply);
          break;
        default:
          // Something went wrong. Return false --> we will go to the HANDLE_OUT_OF_SEQ state
          return false;
        }
        // If the session is completed then remove it.
        if (s.isCompleted()) {
          sessions.remove(inReplyTo);
        }
        return true;
      }
    }
    return false;
  }
        
  /**
   * Check the status of the sessions after the reception of the last reply
   * or the expiration of the timeout.
   * This method is called internally by the framework and is not intended 
   * to be called by the user       
   */    
  protected int checkSessions(ACLMessage reply) {
    int ret = -1;
    if (getLastExitValue() == MsgReceiver.TIMEOUT_EXPIRED) {
      if (!allResponsesReceived) {
        // Special case 1: Timeout has expired
        // Remove all the sessions for which no response has been received yet
        List sessionsToRemove = new ArrayList(sessions.size());
        for (Iterator i=sessions.keySet().iterator(); i.hasNext(); ) {
          Object key = i.next();
          Session s = (Session)sessions.get(key);
          if ( s.getState() == Session.INIT ) {
            sessionsToRemove.add(key);
          }
        }
        for (Iterator i=sessionsToRemove.iterator(); i.hasNext(); ) {
          sessions.remove(i.next());
        }
        sessionsToRemove=null;  //frees memory
      }
      else {
        // Special case 2: All responses have already been received 
        // and an additional timeout (set e.g. through replyReceiver.setDeadline())
        // expired. Remove all sessions
        sessions.clear();
      }
    }
	  	
    if (!allResponsesReceived) {
      // Check whether all responses have been received (this is the 
      // case when no active session is still in the INIT state).
      allResponsesReceived = true;
      Iterator it = sessions.values().iterator();
      while (it.hasNext()) {
        Session s = (Session) it.next();
        if (s.getState() == Session.INIT) {
          allResponsesReceived = false;
          break;
        }
      }
      if (allResponsesReceived) {
        // Set an infite timeout to the replyReceiver.
        replyReceiver.setDeadline(MsgReceiver.INFINITE);
        ret = ALL_RESPONSES_RECEIVED;
      }
    }
    else {
      // Note that this check must be done only if the HANDLE_ALL_RESPONSES
      // has already been visited.
      if (sessions.size() == 0) {
        // There are no more active sessions --> Terminate
        ret = TERMINATED;
      }
    }
    return ret;
  }

  /**
   * This method must return the vector of ACLMessage objects to be
   * sent. It is called in the first state of this protocol.
   * This default implementation just returns the ACLMessage object (a PROPOSE)
   * passed in the constructor. Programmers might prefer to override
   * this method in order to return a vector of PROPOSE objects
   * for 1:N conversations
   * or also to prepare the messages during the execution of the behaviour.
   * @param propose the ACLMessage object passed in the constructor
   * @return a Vector of ACLMessage objects. The value of the slot
   * <code>reply-with</code> is ignored and regenerated automatically
   * by this class.
   **/    
  protected Vector prepareInitiations(ACLMessage propose) {
		Vector v = new Vector(1);
		v.addElement(propose);
		return v;
  }

  /**
   * This method is called every time an <code>accept-proposal</code>
   * message is received, which is not out-of-sequence according
   * to the protocol rules.
   * This default implementation does nothing; programmers might
   * wish to override the method in case they need to react to this event.
   * @param accept_proposal the received accept-proposal message
   **/
  protected void handleAcceptProposal(ACLMessage accept_proposal) {
  }

  /**
   * This method is called every time an <code>reject-proposal</code>
   * message is received, which is not out-of-sequence according
   * to the protocol rules.
   * This default implementation does nothing; programmers might
   * wish to override the method in case they need to react to this event.
   * @param reject_proposal the received reject-proposal message
   **/
  protected void handleRejectProposal(ACLMessage reject_proposal) {
  }

  /**
   * This method is called every time a <code>not-understood</code>
   * message is received, which is not out-of-sequence according
   * to the protocol rules.
   * This default implementation does nothing; programmers might
   * wish to override the method in case they need to react to this event.
   * @param notUnderstood the received not-understood message
   **/
  protected void handleNotUnderstood(ACLMessage notUnderstood) {
  }

  /**
   * This method is called every time a 
   * message is received, which is out-of-sequence according
   * to the protocol rules.
   * This default implementation does nothing; programmers might
   * wish to override the method in case they need to react to this event.
   * @param msg the received message
   **/
  protected void handleOutOfSequence(ACLMessage msg) {
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_ALL_RESPONSES state.
   * This behaviour would override the homonymous method.
   * This method also set the 
   * data store of the registered <code>Behaviour</code> to the
   * DataStore of this current behaviour.
   * The registered behaviour can retrieve
   * the vector of ACLMessage objects, received as a response,
   * from the datastore at the <code>ALL_RESPONSES_KEY</code>
   * key.
   * @param b the Behaviour that will handle this state
   */
  public void registerHandleAllResponses(Behaviour b) {
    registerState(b, HANDLE_ALL_RESPONSES);
    b.setDataStore(getDataStore());
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_ACCEPT_PROPOSAL state.
   * This behaviour would override the homonymous method.
   * This method also set the 
   * data store of the registered <code>Behaviour</code> to the
   * DataStore of this current behaviour.
   * The registered behaviour can retrieve
   * the <code>inform</code> ACLMessage object received
   * from the datastore at the <code>REPLY_KEY</code> key.
   * @param b the Behaviour that will handle this state
   */
  public void registerHandleAcceptProposal(Behaviour b) {
    registerState(b, HANDLE_ACCEPT_PROPOSAL);
    b.setDataStore(getDataStore());
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_REJECT_PROPOSAL state.
   * This behaviour would override the homonymous method.
   * This method also set the 
   * data store of the registered <code>Behaviour</code> to the
   * DataStore of this current behaviour.
   * The registered behaviour can retrieve
   * the <code>inform</code> ACLMessage object received
   * from the datastore at the <code>REPLY_KEY</code> key.
   * @param b the Behaviour that will handle this state
   */
  public void registerHandleRejectProposal(Behaviour b) {
    registerState(b, HANDLE_REJECT_PROPOSAL);
    b.setDataStore(getDataStore());
  }


  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_NOT_UNDERSTOOD state.
   * This behaviour would override the homonymous method.
   * This method also set the 
   * data store of the registered <code>Behaviour</code> to the
   * DataStore of this current behaviour.
   * The registered behaviour can retrieve
   * the <code>not-understood</code> ACLMessage object received
   * from the datastore at the <code>REPLY_KEY</code>
   * key.
   * @param b the Behaviour that will handle this state
   */
  public void registerHandleNotUnderstood(Behaviour b) {
    registerState(b, HANDLE_NOT_UNDERSTOOD);
    b.setDataStore(getDataStore());
  }

  /**
   * This method allows to register a user defined <code>Behaviour</code>
   * in the HANDLE_OUT_OF_SEQ state.
   * This behaviour would override the homonymous method.
   * This method also set the 
   * data store of the registered <code>Behaviour</code> to the
   * DataStore of this current behaviour.
   * The registered behaviour can retrieve
   * the <code>out of sequence</code> ACLMessage object received
   * from the datastore at the <code>REPLY_KEY</code>
   * key.
   * @param b the Behaviour that will handle this state
   */
  public void registerHandleOutOfSequence(Behaviour b) {
    registerState(b, HANDLE_OUT_OF_SEQ);
    b.setDataStore(getDataStore());
  }

  /**
   * This method is called when all the responses have been
   * collected or when the timeout is expired.
   * The used timeout is the minimum value of the slot <code>replyBy</code> 
   * of all the sent messages. 
   * By response message we intend here all the <code>accept-proposal,
   * reject-proposal, not-understood</code> received messages, which are not
   * not out-of-sequence according to the protocol rules.
   * This default implementation does nothing; programmers might
   * wish to override the method in case they need to react to this event
   * by analysing all the messages in just one call.
   * @param responses the Vector of ACLMessage objects that have been received 
   **/
  protected void handleAllResponses(Vector responses) {
  }

  /**
   * reset this behaviour by putting a null ACLMessage as message
   * to be sent
   **/
  public void reset(){
    reset(null);
  }

  /**
   * reset this behaviour
   * @param msg is the ACLMessage to be sent
   **/
  public void reset(ACLMessage msg){
    super.reset();
    replyReceiver.reset(null, MsgReceiver.INFINITE, getDataStore(),REPLY_KEY);
    initiation = msg;
    sessions.clear();
    allResponsesReceived = false;
  }

  /** 
   * Override the onStart() method to initialize the vectors that
   * will keep all the replies in the data store.
   */
  public void onStart() {
    initializeDataStore(initiation);
  }
    
  /** 
   * Override the setDataStore() method to initialize propagate this
   * setting to all children.
   */
  public void setDataStore(DataStore ds) {
    super.setDataStore(ds);
    Iterator it = getChildren().iterator();
    while (it.hasNext()) {
      Behaviour b = (Behaviour) it.next();
      b.setDataStore(ds);
    }
  }

    /**
       Create a new conversation identifier to begin a new
       interaction.
       @param msgs A vector of ACL messages. If the first one has a
       non-empty <code>:conversation-id</code> slot, its value is
       used, else a new conversation identifier is generated.
    */
  protected String createConvId(Vector msgs) {
    // If the conversation-id of the first message is set --> 
    // use it. Otherwise create a default one
    String convId = null;
    if (msgs.size() > 0) {
      ACLMessage msg = (ACLMessage) msgs.elementAt(0);
      if ((msg == null) || (msg.getConversationId() == null)) {
        convId = "C"+hashCode()+"_"+System.currentTimeMillis();
      }
      else {
        convId = msg.getConversationId();
      }
    }
    return convId;
  }

    //#APIDOC_EXCLUDE_BEGIN
  protected void adjustReplyTemplate(ACLMessage msg) {
    // If myAgent is among the receivers (strange case, but can happen)
    // then modify the replyTemplate to avoid intercepting the initiation
    // message msg as if it was a reply
    AID r = (AID) msg.getAllReceiver().next();
    if (myAgent.getAID().equals(r)) {
      replyTemplate = MessageTemplate.and(
                                          replyTemplate,
                                          MessageTemplate.not(MessageTemplate.MatchCustom(msg, true)));
    }
  }
    //#APIDOC_EXCLUDE_END


  /**
   * Inner class Session
   */
  private static class Session implements Serializable {
    // Session states
    static final int INIT = 0;
    static final int NEGATIVE_RESPONSE_RECEIVED = 2;
    static final int RESULT_NOTIFICATION_RECEIVED = 3;
		
    private int state = INIT;
	
    /**
       return true if the received performative is valid with respect to
       the current session state.
    */
    boolean update(int perf) {
	    switch (state) {
	    case INIT:
        switch (perf) {
        case ACLMessage.NOT_UNDERSTOOD:
          state = NEGATIVE_RESPONSE_RECEIVED;
          return true;
        case ACLMessage.REJECT_PROPOSAL:
        case ACLMessage.ACCEPT_PROPOSAL:
          state = RESULT_NOTIFICATION_RECEIVED;
          return true;
        default:
          return false;
        }
	    default:
        return false;
	    }
    }
	
    int getState() {
	    return state;
    }
	
    boolean isCompleted() {
	    return (state == NEGATIVE_RESPONSE_RECEIVED || state == RESULT_NOTIFICATION_RECEIVED);
    }
	
  } // End of inner class Session
}
