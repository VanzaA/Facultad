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

//#J2ME_EXCLUDE_FILE

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import jade.util.leap.*;

/**
 * Class description
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public class TwoPh1Initiator extends Initiator {
  // Data store keys 
	// Private data store keys (can't be static since if we register another instance of this class as state of the FSM 
	// using the same data store the new values overrides the old one.
  /** 
     key to retrieve from the DataStore of the behaviour the ACLMessage 
     object passed in the constructor of the class.
   */
  public final String QUERYIF_KEY = INITIATION_K;
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     QUERY_IF messages that have to be sent.
   */
  public final String ALL_QUERYIFS_KEY = ALL_INITIATIONS_K;
  /** 
     key to retrieve from the DataStore of the behaviour the last
     ACLMessage object that has been received (null if the timeout
     expired). 
   */
  public final String REPLY_KEY = REPLY_K;
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     all messages that have been received as response.
   */
  public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     CONFIRM messages that have been received as response.
   */
  public final String ALL_CONFIRMS_KEY = "__all-confirms" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     DISCONFIRM messages that have been received as response.
   */
  public final String ALL_DISCONFIRMS_KEY = "__all-disconfirms" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     INFORM messages that have been received as response.
   */
  public final String ALL_INFORMS_KEY = "__all-informs" + hashCode();
  /** 
     key to retrieve from the DataStore of the behaviour the Vector of
     QUERY_IF messages for which a response has not been received yet.
   */
  public final String ALL_PENDINGS_KEY = "__all-pendings" + hashCode();
    
  /* FSM states names */
  private static final String HANDLE_CONFIRM = "Handle-Confirm";
  private static final String HANDLE_DISCONFIRM = "Handle-Disconfirm";
  private static final String HANDLE_INFORM = "Handle-Inform";
  private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";

  private static final int ALL_RESPONSES_RECEIVED = 1;
  
  /* Data store output key */
  private String outputKey = null;
  
  private int totSessions;


    /**
     * Constructs a <code>TwoPh1Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase1's protocol.
     * @param inputKey Data store key where behaviour can get queryIf messages
     * prepared in the previous phase.
     * @param outputKey Data store key where the behaviour prepares a vector of
     * messages which will be send by a <code>TwoPh2Initiator</code> behaviour.
     * If phase 1 ends with all confirm or inform than messages prepared are
     * <code>accept-proposal</code>, otherwise they are <code>reject-proposal</code>.
     */
    public TwoPh1Initiator(Agent a, ACLMessage queryIf, String outputKey) {
        this(a, queryIf, outputKey, new DataStore());
    }

    /**
     * Constructs a <code>TwoPh1Initiator</code> behaviour.
     * @param a The agent performing the protocol.
     * @param conversationId <code>Conversation-id</code> slot used for all the
     * duration of phase1's protocol.
     * @param inputKey Data store key where behaviour can get queryIf messages
     * prepared in the previous phase.
     * @param outputKey Data store key where the behaviour prepares a vector of
     * messages which will be send by a <code>TwoPh2Initiator</code> behaviour.
     * If phase 1 ends with all confirm or inform than messages prepared are
     * <code>accept-proposal</code>, otherwise they are <code>reject-proposal</code>.
     * @param store <code>DataStore</code> that will be used by this <code>TwoPh1Initiator</code>.
     */
    public TwoPh1Initiator(Agent a, ACLMessage queryIf, String outputKey, DataStore store) {
        super(a, queryIf, store);
        //this.inputKey = inputKey;
        this.outputKey = outputKey;
        /* Register the FSM transitions specific to the Two-Phase1-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_CONFIRM, ACLMessage.CONFIRM);
        registerTransition(CHECK_IN_SEQ, HANDLE_DISCONFIRM, ACLMessage.DISCONFIRM);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerDefaultTransition(HANDLE_CONFIRM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_DISCONFIRM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        //registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, SOME_DISCONFIRM);
        //registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, PH1_TIMEOUT_EXPIRED);
        //registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_CONFIRM_OR_INFORM);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        /* Create and register the states specific to the Two-Phase1-Commit protocol */
        Behaviour b = null;

        /* HANDLE_CONFIRM state activated if arrived a confirm message compliant with
        conversationId and a receiver of one of queryIf messages sent. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage confirm = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleConfirm(confirm);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_CONFIRM);

        /* HANDLE_DISCONFIRM state activated if arrived a disconfirm message
        compliant with conversationId and a receiver of one of queryIf messages
        sent. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage disconfirm = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleDisconfirm(disconfirm);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_DISCONFIRM);

        /* HANDLE_INFORM state activated if arrived an inform message
        compliant with conversationId and a receiver of one of queryIf messages
        sent. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage inform = (ACLMessage) (getDataStore().get(REPLY_KEY));
                handleInform(inform);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_INFORM);

        /* HANDLE_ALL_RESPONSES state activated when timeout is expired or
        all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
                Vector confirms = (Vector) getDataStore().get(ALL_CONFIRMS_KEY);
                Vector disconfirms = (Vector) getDataStore().get(ALL_DISCONFIRMS_KEY);
                Vector informs = (Vector) getDataStore().get(ALL_INFORMS_KEY);
                Vector pendings = (Vector) getDataStore().get(ALL_PENDINGS_KEY);
          			Vector nextPhMsgs = (Vector) getDataStore().get(TwoPh1Initiator.this.outputKey);
                handleAllResponses(responses, confirms, disconfirms, informs,
                        pendings, nextPhMsgs);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);
    }

    public int onEnd() {
      Vector nextPhMsgs = (Vector) getDataStore().get(outputKey);
      if (nextPhMsgs.size() != 0) {
        return ((ACLMessage) nextPhMsgs.get(0)).getPerformative();
      }
      else {
      	return -1;
      }
    }

	private String[] toBeReset = null;
		
    /* User can override these methods */

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object (a QUERY_IF) passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of QUERY_IF objects for 1:N conversations.
     * @param queryIf the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects. The value of the <code>reply-with</code>
     * slot is ignored and regenerated automatically by this
     * class. Instead user can specify <code>reply-by</code> slot representing phase0
     * timeout.
     */
    protected Vector prepareQueryIfs(ACLMessage queryIf) {
        Vector v = new Vector(1);
        v.addElement(queryIf);
        return v;
    }

    /**
     * This method is called every time a <code>confirm</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param confirm the received propose message
     */
    protected void handleConfirm(ACLMessage confirm) {
    }

    /**
     * This method is called every time a <code>disconfirm</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param disconfirm the received propose message
     */
    protected void handleDisconfirm(ACLMessage disconfirm) {
    }

    /**
     * This method is called every time a <code>inform</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     * @param inform the received propose message
     */
    protected void handleInform(ACLMessage inform) {
    }

    /**
     * This method is called when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     * <code>reply-By</code> of all the sent messages. By response message we
     * intend here all the <code>disconfirm, confirm, inform</code> received messages,
     * which are not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event by analysing all the messages in
     * just one call.
     * @param confirms all confirms received
     * @param disconfirms all disconfirms received
     * @param pendings all queryIfs still pending
     * @param responses prepared responses for next phase: <code>accept-proposal</code>
     * or <code>reject-proposal</code>
     */
    protected void handleAllResponses(Vector responses, Vector confirms, Vector disconfirms,
                                      Vector informs, Vector pendings, Vector nextPhMsgs) {
    }

    /** This method allows to register a user-defined <code>Behaviour</code> in the
     * PREPARE_QUERYIFS state. This behaviour would override the homonymous method. This
     * method also set the data store of the registered <code>Behaviour</code> to the
     * DataStore of this current behaviour. It is responsibility of the registered
     * behaviour to put the <code>Vector</code> of ACLMessage objects to be sent into
     * the datastore at the <code>ALL_QUERYIFS_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareQueryIfs(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_CONFIRM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>confirm</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleConfirm(Behaviour b) {
        registerState(b, HANDLE_CONFIRM);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_DISCONFIRM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>disconfirm</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleDisconfirm(Behaviour b) {
        registerState(b, HANDLE_DISCONFIRM);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_INFORM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the DataStore of this current behaviour. The registered behaviour can retrieve
     * the <code>inform</code> ACLMessage object received from the datastore at the
     * <code>REPLY_KEY</code> key.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleInform(Behaviour b) {
        registerState(b, HANDLE_INFORM);
        b.setDataStore(getDataStore());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code> to
     * the DataStore of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage confirms, disconfirms, informs, pending and responses
     * from the datastore at <code>ALL_CONFIRMS_KEY</code>, <code>ALL_DISCONFIRMS_KEY</code>,
     * <code>ALL_INFORMS_KEY</code>, <code>ALL_PH1_PENDINGS_KEY</code> and
     * <code>output</code> field.
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setDataStore(getDataStore());
    }

    /* User CAN'T override these methods */
    //#APIDOC_EXCLUDE_BEGIN

  /**
   */
  protected String[] getToBeReset() {
  	if (toBeReset == null) {
			toBeReset = new String[] {
				HANDLE_CONFIRM, 
				HANDLE_DISCONFIRM, 
				HANDLE_INFORM, 
				HANDLE_NOT_UNDERSTOOD,
				HANDLE_FAILURE,
				HANDLE_OUT_OF_SEQ
			};
  	}
  	return toBeReset;
  }
    
    /**
     * Prepare vector containing queryIfs.
     * @param initiation queryIf passed in the constructor
     * @return Vector of queryIfs
     */
    protected final Vector prepareInitiations(ACLMessage initiation) {
      return prepareQueryIfs(initiation);
    }

    /**
     * This method sets for all prepared queryIfs <code>conversation-id</code> slot (with
     * value passed in the constructor), <code>protocol</code> slot and
     * <code>reply-with</code> slot with a unique value constructed by concatenating
     * receiver's agent name and phase number (i.e. 1). After that it sends all cfps.
     * @param initiations vector prepared in PREPARE_QUERYIFS state
     */
    protected final void sendInitiations(Vector initiations) {
      getDataStore().put(ALL_PENDINGS_KEY, new Vector());
      
      super.sendInitiations(initiations);
      
      totSessions = sessions.size();
    }
    
    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding queryif from vector of pendings.
     * @param reply message received
     * @return true if reply is compliant with flow of protocol, false otherwise
     */
    protected final boolean checkInSequence(ACLMessage reply) {
      boolean ret = false;
      String inReplyTo = reply.getInReplyTo();
      Session s = (Session) sessions.get(inReplyTo);
      if(s != null) {
        int perf = reply.getPerformative();
        if(s.update(perf)) {
          // The reply is compliant to the protocol 
          ((Vector) getDataStore().get(ALL_RESPONSES_KEY)).add(reply);
          
          switch(perf) {
              case ACLMessage.CONFIRM: {
                  ((Vector) getDataStore().get(ALL_CONFIRMS_KEY)).add(reply);
                  break;
              }
              case ACLMessage.DISCONFIRM: {
                  ((Vector) getDataStore().get(ALL_DISCONFIRMS_KEY)).add(reply);
                  break;
              }
              case ACLMessage.INFORM: {
                  ((Vector) getDataStore().get(ALL_INFORMS_KEY)).add(reply);
                  break;
              }
          }
          updatePendings(inReplyTo);
          ret = true;
        }
        if (s.isCompleted()) {
            sessions.remove(inReplyTo);
        }
      } 
      return ret;
    }

    private void updatePendings(String key) {
    	Vector pendings = (Vector) getDataStore().get(ALL_PENDINGS_KEY);
      for(int i=0; i<pendings.size(); i++) {
          ACLMessage pendingMsg = (ACLMessage) pendings.get(i);
          if(pendingMsg.getReplyWith().equals(key)) {
              pendings.remove(i);
              break;
          }
      }
    }
    	
    /**
     * Check if there are still active sessions or if timeout is expired.
     * @param reply last message received
     * @return ALL_RESPONSES_RECEIVED or -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
    	if (reply == null) {
    		// Timeout expired --> clear all sessions 
    		sessions.clear();
    	}
    	if (sessions.size() == 0) {
    		// We have finished --> fill the Vector of initiation messages for next 
    		// phase (unless already filled by the user)
        DataStore ds = getDataStore();
    		Vector nextPhMsgs = (Vector) ds.get(outputKey);
    		if (nextPhMsgs.size() == 0) {
	        Vector confirms = (Vector) ds.get(ALL_CONFIRMS_KEY);
	        Vector informs = (Vector) ds.get(ALL_INFORMS_KEY);
	        Vector pendings = (Vector) ds.get(ALL_PENDINGS_KEY);
	        fillNextPhInitiations(nextPhMsgs, confirms, informs, pendings);
    		}
        return ALL_RESPONSES_RECEIVED;
    	}
    	else {
    		// We are still waiting for some responses
    		return -1;
    	}
    }

    private void fillNextPhInitiations(Vector nextPhMsgs, Vector confirms, Vector informs, Vector pendings) {
    	if ((confirms.size()+informs.size()) == totSessions) {
    		// All responders replied with CONFIRM or INFORM --> Fill the vector 
    		// of initiation messages for next phase with ACCEPT_PROPOSAL
        for(int i=0; i<confirms.size(); i++) {
          ACLMessage msg = (ACLMessage) confirms.get(i);
          ACLMessage accept = msg.createReply();
          accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
          nextPhMsgs.add(accept);
        }
    	}
    	else {
    		// At least one responder disconfirmed, failed or didn't reply --> Fill the vector 
    		// of initiation messages for next phase with REJECT_PROPOSALS
        for(int i=0; i<confirms.size(); i++) {
          ACLMessage msg = (ACLMessage) confirms.get(i);
          ACLMessage reject = msg.createReply();
          reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
          nextPhMsgs.add(reject);
        }
        for(int i=0; i<pendings.size(); i++) {
          ACLMessage msg = (ACLMessage) pendings.get(i);
          ACLMessage reject = (ACLMessage) msg.clone();
          reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
          nextPhMsgs.add(reject);
        }
    	}
    }
    
    /**
     * Initialize the data store.
     * @param msg Ignored
     */
    protected void initializeDataStore(ACLMessage msg) {
        super.initializeDataStore(msg);
        getDataStore().put(ALL_RESPONSES_KEY, new Vector());
        getDataStore().put(ALL_CONFIRMS_KEY, new Vector());
        getDataStore().put(ALL_DISCONFIRMS_KEY, new Vector());
        getDataStore().put(ALL_INFORMS_KEY, new Vector());
        getDataStore().put(outputKey, new Vector());
    }
    //#APIDOC_EXCLUDE_END
    		
    		
    
  protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
    Vector pendings = (Vector) getDataStore().get(ALL_PENDINGS_KEY);
    pendings.add(msg);
		
  	return new Session("R" + hashCode()+  "_" + Integer.toString(sessionIndex) + "_" + TwoPhConstants.PH1);
  }
  
    /**
     * Inner class Session
     */
    class Session implements ProtocolSession, Serializable {
        // Session states 
        static final int INIT = 0;
        static final int REPLY_RECEIVED = 1;

        private int state = INIT;
        private String myId;

        public Session(String id) {
        	myId = id;
        }
        
				public String getId() {
					return myId;
				}
				

        /**
         * Return true if received ACLMessage is consistent with the protocol.
         * @param perf
         * @return Return true if received ACLMessage is consistent with the protocol
         */
        public boolean update(int perf) {
            if(state == INIT) {
                switch(perf) {
                    case ACLMessage.CONFIRM: ;
                    case ACLMessage.DISCONFIRM: ;
                    case ACLMessage.INFORM: ;
                    case ACLMessage.NOT_UNDERSTOOD:
                    case ACLMessage.FAILURE:
                      state = REPLY_RECEIVED;
                    	return true;
                    default: 
                    	return false;
                }
            }
            else {
                return false;
            }
        }

        public int getState() {
            return state;
        }

        public boolean isCompleted() {
            return (state == REPLY_RECEIVED);
        }
    }
}


