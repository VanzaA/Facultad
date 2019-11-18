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

package jade.core.event;


//#MIDP_EXCLUDE_FILE


import jade.core.Service;
import jade.core.AID;
import jade.core.IMTPException;


/**
   The horizontal interface for the JADE kernel-level service managing
   the event notification subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface NotificationSlice extends Service.Slice {


    /**
       The name of this service.
    */
    static final String NAME = "jade.core.event.Notification";

    /**
       This command name represents the action of activating an
       instance of the Sniffer agent.
    */
    static final String SNIFF_ON = "Sniff-On";

    /**
       This command name represents the action of deactivating a
       previously started instance of the Sniffer agent.
    */
    static final String SNIFF_OFF = "Sniff-Off";

    /**
       This command name represents the action of activating an
       instance of the Introspector agent.
    */
    static final String DEBUG_ON = "Debug-On";

    /**
       This command name represents the action of deactivating a
       previously started instance of the Introspector agent.
    */
    static final String DEBUG_OFF = "Debug-Off";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>MessagePosted</code> event.
    */
    static final String NOTIFY_POSTED = "Notify-Posted";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>MessageReceived</code> event.
    */
    static final String NOTIFY_RECEIVED = "Notify-Received";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>ChangedAgentState</code> event.
    */
    static final String NOTIFY_CHANGED_AGENT_STATE = "Notify-Changed-Agent-State";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>ChangedAgentPrincipal</code> event.
    */
    static final String NOTIFY_CHANGED_AGENT_PRINCIPAL = "Notify-Changed-Agent-Principal";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>BehaviourAdded</code> event.
    */
    static final String NOTIFY_BEHAVIOUR_ADDED = "Notify-BehaviourAdded";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>BehaviourRemoved</code> event.
    */
    static final String NOTIFY_BEHAVIOUR_REMOVED = "Notify-Behaviour-Removed";

    /**
       This command name represents the action of submitting to the
       Notification Service a <code>ChangedBehaviourState</code> event.
    */
    static final String NOTIFY_CHANGED_BEHAVIOUR_STATE = "Notify-Changed-Behaviour-State";



    // Constants for the names of horizontal commands associated to methods
    static final String H_SNIFFON = "1";
    static final String H_SNIFFOFF = "2";
    static final String H_DEBUGON = "3";
    static final String H_DEBUGOFF = "4";

    void sniffOn(AID snifferName, AID targetName) throws IMTPException;
    void sniffOff(AID snifferName, AID targetName) throws IMTPException;
    void debugOn(AID introspectorName, AID targetName) throws IMTPException;
    void debugOff(AID introspectorName, AID targetName) throws IMTPException;

}
