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

package jade.core.messaging;

//#J2ME_EXCLUDE_FILE


import jade.lang.acl.ACLMessage;


/**
   This interface can be used 

*/
public interface PersistentDeliveryFilter {

    /**
       The constant to specifiy an immediate failure notification
       (i.e. no message buffering).
    */
    static final long NOW = 0;

    /**
       The constant to specify no failure notification (i.e. message
       buffering for infinitely long).
    */
    static final long NEVER = -1;

    /**
       The application-specific method to control which messages are
       to be buffered and for how long.

       @param msg The undelivered ACL message, that is to be tested
       against this filter and possibly buffered.
       @return The delay, in milliseconds, within which the message is
       to be delivered. If the message is still undelivered after that
       delay, the delivery process aborts and a <code>failure</code>
       message is sent back to the message originator. The specially
       defined <code>NOW</code> and <code>NEVER</code> constants can
       be used to request immediate abort or message storage for
       unlimited time.
    */
    long delayBeforeExpiration(ACLMessage msg);

}
