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

package jade.tools.SocketProxyAgent;

import java.io.*;
import java.util.*;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.*;
import jade.util.Logger;

/**
 * behaviour to wait for answer to message sent
 */
class WaitAnswersBehaviour extends SimpleBehaviour {
    ACLMessage msg;
    PrintStream out;
    long timeout;
    private final static long ONE_SEC_AS_MS = 1000;
    private final static long TEN_SEC_AS_MS = 10 * ONE_SEC_AS_MS;
    final static long DEFAULT_TIMEOUT = TEN_SEC_AS_MS;
    boolean finished;
    MessageTemplate mt;
    Agent myAgent=null;
    private String myThreadName;
    /** my logger */
    private final static Logger logger = 
        Logger.getMyLogger(WaitAnswersBehaviour.class.getName());
    
    /** 
     * construct behaviour to wait for answer to message sent
     * @param a my agent
     * @param m message whose reply awaiting
     * @param o output stream to copy reply onto
     */
    WaitAnswersBehaviour(Agent a, ACLMessage m, PrintStream o) {
        super(a);
        myAgent = a;
        out = o;
        myThreadName = Thread.currentThread().getName();
        
        // filtering so that only messages returned by the first receiver
        // is too restrictive: problems if another receiver sends the message
        // or if another agent gives the response.
//        try {
//            mt = MessageTemplate
//            .and(MessageTemplate
//                    .MatchSender((AID) m.getAllReceiver()
//                            .next()), MessageTemplate
//                            .MatchInReplyTo(m.getReplyWith()));
//        }
//        catch (Exception e) {
            mt = MessageTemplate.MatchInReplyTo(m.getReplyWith());
//        }
        
        Date d = m.getReplyByDate();
        
        if (d != null) {
            timeout = d.getTime() - (new Date()).getTime();
            if (timeout <= ONE_SEC_AS_MS) {
                timeout = ONE_SEC_AS_MS;
            }
        }
        else {
            timeout = DEFAULT_TIMEOUT;
        }
        
        finished = false;
        
        logger.log( Logger.CONFIG, 
                myThreadName + ": Constructed "+this.getClass().getName()+
                " using message template " + mt + 
                " and timeout " + timeout +
                " to wait for answer to:" + m );
    }

    /**
     * behaviour action
     * blocking receive for message, followed by copying that message
     * to the output stream.
     */
    public void action() {
	if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, 
                myThreadName + ": About to block, message template "+ mt +
                ", timeout "+timeout+
                "ms, waiting for reply..." );
	}
        msg = myAgent.blockingReceive(mt, timeout);
        
	if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, 
			myThreadName + ": No longer blocked" );
	}
        
        if (msg == null) {
            logger.log( Logger.INFO, myThreadName + ": Reply was null" );
            msg = new ACLMessage(ACLMessage.FAILURE);
            msg.setContent("( \"Timed-out waiting for response from agent\" )");
        }
	if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, 
			myThreadName + ": writing reply...:" + msg );
	}
        
        out.print(msg.toString());
        out.flush();//Added by Sebastien_Siva@hp.com
        
        // an AGREE implies a subsequent message
        finished = (msg.getPerformative() != ACLMessage.AGREE);
	if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, 
			myThreadName + ": set finished=" + finished );
	}
        
    }
    
    /**
     * Method done
     * @return boolean indicating if done or not.
     */
    public boolean done() {
        return finished;
    }
}
