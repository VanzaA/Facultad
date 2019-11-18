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

package jade.core.behaviours;

//#APIDOC_EXCLUDE_FILE

import jade.util.leap.Iterator;
import jade.util.leap.Serializable;

import jade.core.Agent;

import jade.core.AID;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
   Behaviour for receiving an ACL message. This class encapsulates a
   <code>receive()</code> as an atomic operation. This behaviour
   terminates when an ACL message is received. 
   The method <code>getMessage()</code> allows to get the received message.
   @see jade.core.behaviours.SenderBehaviour
   @see jade.core.Agent#receive()
   @see jade.lang.acl.ACLMessage
 
   
   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2003-11-25 09:24:45 +0100 (mar, 25 nov 2003) $ $Revision: 4601 $

 */
public final class ReceiverBehaviour extends Behaviour {

  /**
   Exception class for timeouts. This exception is thrown when trying
   to obtain an <code>ACLMessage</code> object from an
   <code>Handle</code>, but no message was received within a specified
   timeout.
   @see jade.core.behaviours.ReceiverBehaviour.Handle#getMessage()
  */
  public static class TimedOut extends Exception {
    TimedOut() {
      super("No message was received before time limit.");
    }
  }

  /**
   Exception class for timeouts. This exception is thrown when trying
   to obtain an <code>ACLMessage</code> from an <code>Handle</code>
   and no message was received so far, but the time limit is not yet
   reached.
   @see jade.core.behaviours.ReceiverBehaviour.Handle#getMessage()
  */
  public static class NotYetReady extends Exception {
    NotYetReady() {
      super("Requested message is not ready yet.");
    }
  }

  /**
   An interface representing ACL messages due to arrive within a time
   limit. This interface is used to create a
   <code>ReceiverBehaviour</code> object to receive an ACL message
   within a user specified time limit. When the user tries to read the
   message represented by the handle, either gets it or gets an
   exception.
   @see jade.core.behaviours.ReceiverBehaviour#newHandle()
   @see jade.core.behaviours.ReceiverBehaviour#ReceiverBehaviour(Agent
   a, ReceiverBehaviour.Handle h, long millis)
   */
  public static interface Handle {

    /**
     Tries to retrieve the <code>ACLMessage</code> object represented
     by this handle.
     @return The ACL message, received by the associated
     <code>ReceiverBehaviour</code>, if any.
     @exception TimedOut If the associated
     <code>ReceiverBehaviour</code> did not receive a suitable ACL
     message within the time limit.
     @exception NotYetReady If the associated
     <code>ReceiverBehaviour</code> is still waiting for a suitable
     ACL message to arrive.
     @see jade.core.behaviours.ReceiverBehaviour#ReceiverBehaviour(Agent
     a, ReceiverBehaviour.Handle h, long millis)
    */
    ACLMessage getMessage() throws TimedOut, NotYetReady;

  }

  private static class MessageFuture implements Handle, Serializable {

    private static final int OK = 0;
    private static final int NOT_YET = 1;
    private static final int TIMED_OUT = 2;

    private int state = NOT_YET;
    private ACLMessage message;

    public void reset() {
      message = null;
      state = NOT_YET;
    }

    public void setMessage(ACLMessage msg) {
      message = msg;
      if(message != null)
	state = OK;
      else
	state = TIMED_OUT;
    }

    public ACLMessage getMessage() throws TimedOut, NotYetReady {
      switch(state) {
      case NOT_YET:
	throw new NotYetReady();
      case TIMED_OUT:
	throw new TimedOut();
      default:
	return message;
      }
    }
  }

  /**
   Factory method for message handles. This method returns a new
   <code>Handle</code> object, which can be used to retrieve an ACL
   message out of a <code>ReceiverBehaviour</code> object.
   @return A new <code>Handle</code> object.
   @see jade.core.behaviours.ReceiverBehaviour.Handle
  */
  public static Handle newHandle() {
    return new MessageFuture();
  }


  // The pattern to match incoming messages against
  /**
  @serial
  */
  private MessageTemplate template;

  // A future for the ACL message, used when a timeout was specified
  /**
  @serial
  */
  private MessageFuture future;

  // A time out value, when present
  /**
  @serial
  */
  private long timeOut;

  // A running counter for calling block(millis) until 'timeOut' milliseconds pass.
  /**
  @serial
  */
  private long timeToWait;

  // Timestamp holder, used when calling block(millis) many times.
  /**
  @serial
  */
  private long blockingTime = 0;
  /**
  @serial
  */
  private boolean finished;

  /**
     This constructor creates a
     <code>ReceiverBehaviour</code> object that ends as soon as an ACL
     message matching a given <code>MessageTemplate</code> arrives or
     the passed <code>millis<code> timeout expires.
     The received message can then be got via the method 
     <code>getMessage</code>.
     @param a The agent this behaviour belongs to, and that will
     <code>receive()</code> the message.
     @param millis The timeout expressed in milliseconds, an infinite timeout
     can be expressed by a value < 0.
     @param mt A Message template to match incoming messages against, null to
     indicate no template and receive any message that arrives.
  */
  public ReceiverBehaviour(Agent a, long millis, MessageTemplate mt) {
      this(a, newHandle(), millis, mt);
  }



  /**
     Receive any ACL message, waiting at most <code>millis</code>
     milliseconds (infinite time if <code>millis < 1</code>).
     When calling this constructor, a suitable <code>Handle</code>
     must be created and passed to it. When this behaviour ends, some
     other behaviour will try to get the ACL message out of the
     handle, and an exception will be thrown in case of a time out.
     The following example code explains this:

     <code><pre>
       // ReceiverBehaviour creation, e.g. in agent setup() method
       h = ReceiverBehaviour.newHandle(); // h is an agent instance variable
       addBehaviour(new ReceiverBehaviour(this, h, 10000); // Wait 10 seconds

       ...

       // Some other behaviour, later, tries to read the ACL message
       // in its action() method
       try {
         ACLMessage msg = h.getMessage();
	 // OK. Message received within timeout.
       }
       catch(ReceiverBehaviour.TimedOut rbte) {
         // Receive timed out
       }
       catch(ReceiverBehaviour.NotYetReady rbnyr) {
         // Message not yet ready, but timeout still active
       }
     </pre></code>
     @param a The agent this behaviour belongs to.
     @param h An <em>Handle</em> representing the message to receive.
     @param millis The maximum amount of time to wait for the message,
     in milliseconds.
     @see jade.core.behaviours.ReceiverBehaviour.Handle
     @see jade.core.behaviours.ReceiverBehaviour#newHandle()
   */
  public ReceiverBehaviour(Agent a, Handle h, long millis) {
    this(a, h, millis, null);
  }

  /**
     Receive any ACL message matching the given template, witing at
     most <code>millis</code> milliseconds (infinite time if
     <code>millis < 1</code>. When calling this constructor, a
     suitable <code>Handle</code> must be created and passed to it.
     @param a The agent this behaviour belongs to.
     @param h An <em>Handle</em> representing the message to receive.
     @param millis The maximum amount of time to wait for the message,
     in milliseconds.
     @param mt A Message template to match incoming messages against, null to
     indicate no template and receive any message that arrives.
     @see jade.core.behaviours.ReceiverBehaviour#ReceiverBehaviour(Agent a, Handle h, long millis)
  */
  public ReceiverBehaviour(Agent a, Handle h, long millis, MessageTemplate mt) {
    super(a);
    future = (MessageFuture)h;
    timeOut = millis;
    timeToWait = timeOut;
    template = mt;
  }

  /**
     Actual behaviour implementation. This method receives a suitable
     ACL message and copies it into the message provided by the
     behaviour creator. It blocks the current behaviour if no suitable
     message is available.
  */
  public void action() {
    ACLMessage msg = null;
    if(template == null)
      msg = myAgent.receive();
    else
      msg = myAgent.receive(template);

    if(msg == null) {
      if(timeOut < 0) {
	block();
	finished = false;
	return;
      }
      else {
	long elapsedTime = 0;
	if(blockingTime != 0)
	  elapsedTime = System.currentTimeMillis() - blockingTime;
	else
	  elapsedTime = 0;
	timeToWait -= elapsedTime;
	if(timeToWait > 0) {
	  blockingTime  = System.currentTimeMillis();
	  // System.out.println("Waiting for " + timeToWait + " ms.");
	  block(timeToWait);
	  return;
	}
	else {
	  future.setMessage(msg);
	  finished = true;
	}
      }
    }
    else {
	future.setMessage(msg);
	finished = true;
    }
  }

  /**
     Checks whether this behaviour ended.
     @return <code>true</code> when an ACL message has been received.
  */
  public boolean done() {
    return finished;
  }

  /**
     Resets this behaviour. This method allows to receive another
     <code>ACLMessage</code> with the same
     <code>ReceiverBehaviour</code> without creating a new object.
  */
  public void reset() {
    finished = false;
    future.reset();
    timeToWait = timeOut;
    blockingTime = 0;
    super.reset();
  }




    /**
     * This method allows the caller to get the received message.
     * @return the received message
     * @exception TimedOut if the timeout passed in the constructor of this
     * class expired before any message (that eventually matched the passed
     * message template) arrived
     * @exception NotYetReady if the message is not yet arrived and the
     * timeout is not yet expired.
     **/
    public ACLMessage getMessage() throws TimedOut, NotYetReady {
	return future.getMessage();
    }
} 
