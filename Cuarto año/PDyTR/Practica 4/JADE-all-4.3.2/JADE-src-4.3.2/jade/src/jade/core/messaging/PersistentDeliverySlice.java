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

import jade.core.Service;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;



/**
   The horizontal interface for the JADE kernel-level service managing
   the message passing subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface PersistentDeliverySlice extends Service.Slice {

    // Constants for the names of the service vertical commands

    /**
       The name of this service.
    */
    static final String NAME = "jade.core.messaging.PersistentDelivery";

    /**
       This command name represents the action of activating a message
       store for undelivered ACL messages, on the specified node.
    */
    //static final String ACTIVATE_MESSAGE_STORE = "Activate-Message-Store";

    /**
       This command name represents the action of deactivating a message
       store for undelivered ACL messages, on the specified node.
    */
    //static final String DEACTIVATE_MESSAGE_STORE = "Deactivate-Message-Store";

    /**
       This command name represents the action of adding a given message
       template to a message store, so that undelivered ACL messages
       addressed to that ID will be retained.
    */
    //static final String REGISTER_MESSAGE_TEMPLATE = "Register-Message-Template";

    /**
       This command name represents the action of removing a given
       message template from a message store. This stops the retention
       of undelivered ACL messages for that agent ID.
    */
    //static final String DEREGISTER_MESSAGE_TEMPLATE = "Deregister-Message-Template";


    // Constants for the names of horizontal commands associated to methods
    //static final String H_ACTIVATEMSGSTORE = "1";
    //static final String H_DEACTIVATEMSGSTORE = "2";
    //static final String H_REGISTERTEMPLATE = "3";
    //static final String H_DEREGISTERTEMPLATE = "4";
    static final String H_STOREMESSAGE = "5";
    static final String H_FLUSHMESSAGES = "6";


    /*void activateMsgStore(String name) throws IMTPException, NameClashException;
    void deactivateMsgStore(String name) throws IMTPException, NotFoundException;

    void registerTemplate(String storeName, MessageTemplate mt) throws IMTPException, NotFoundException, NameClashException;
    void deregisterTemplate(String storeName, MessageTemplate mt) throws IMTPException, NotFoundException;
    */

    boolean storeMessage(String storeName, GenericMessage msg, AID receiver) throws IMTPException, NotFoundException;
    void flushMessages(AID receiver) throws IMTPException;

}
