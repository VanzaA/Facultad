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

import jade.core.AID;
import jade.core.AgentContainer;
import jade.core.BaseService;
import jade.core.CaseInsensitiveString;
import jade.core.ContainerID;
import jade.core.Filter;
import jade.core.GenericCommand;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;
import jade.core.MainContainer;
import jade.core.Node;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.Service;
import jade.core.ServiceException;
import jade.core.ServiceFinder;
import jade.core.Sink;
import jade.core.UnreachableException;
import jade.core.VerticalCommand;

import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.InternalError;

import jade.domain.FIPANames;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;

import jade.security.JADESecurityException;

import jade.util.leap.Iterator;

import java.util.Date;


/**
 * A minimal version of the JADE service to manage the message passing
 * subsystem installed on the platform. This class just supports
 * direct ACL message delivery, and relies on another one for any
 * other feature (such as message routing and MTP management).
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @author Jerome Picault - Motorola Labs
 *
 */
public class LightMessagingService extends BaseService
    implements MessageManager.Channel {
    private static final String[] OWNED_COMMANDS = new String[] {
            MessagingSlice.SEND_MESSAGE, MessagingSlice.NOTIFY_FAILURE,
            MessagingSlice.INSTALL_MTP, MessagingSlice.UNINSTALL_MTP,
            MessagingSlice.NEW_MTP, MessagingSlice.DEAD_MTP,
            MessagingSlice.SET_PLATFORM_ADDRESSES
        };

    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;

    // The local slice for this service
    private ServiceComponent localSlice;

    // The component managing asynchronous message delivery and retries
    private MessageManager myMessageManager;

	// The ID of the Platform this service belongs to
	private String platformID;
	
    /**
     * Performs the passive initialization step of the service. This
     * method is called <b>before</b> activating the service. Its role
     * should be simply the one of a constructor, setting up the
     * internal data as needed.
     * Service implementations should not use the Service Manager and
     * Service Finder facilities from within this method. A
     * distributed initialization protocol, if needed, should be
     * exectuted within the <code>boot()</code> method.
     * @param ac The agent container this service is activated on.
     * @param p The configuration profile for this service.
     * @throws ProfileException If the given profile is not valid.
     */
    public void init(AgentContainer ac, Profile p) throws ProfileException {
        super.init(ac, p);
        myContainer = ac;

		platformID = myContainer.getPlatformID();
        // Initialize its own ID
        // String platformID = myContainer.getPlatformID();
        myMessageManager = MessageManager.instance(p);

        String helperSliceName = p.getParameter("accRouter", MAIN_SLICE);

        // Create a local slice
        localSlice = new ServiceComponent(helperSliceName);
    }

    /**
     * Retrieve the name of this service, that can be used to look up
     * its slices in the Service Finder.
     * @return The name of this service.
     * @see jade.core.ServiceFinder
     */
    public String getName() {
        return MessagingSlice.NAME;
    }

    /**
     * Retrieve the interface through which the different service
     * slices will communicate, that is, the service <i>Horizontal
     * Interface</i>.
     * @return A <code>Class</code> object, representing the interface
     * that is implemented by the slices of this service.
     */
    public Class getHorizontalInterface() {
        try {
            return Class.forName(MessagingSlice.NAME + "Slice");
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    /**
     * Retrieve the locally installed slice of this service.
     */
    public Service.Slice getLocalSlice() {
        return localSlice;
    }

    /**
     * Access the command filter this service needs to perform its
     * tasks. This filter will be installed within the local command
     * processing engine.
     * @param direction One of the two constants
     * <code>Filter.INCOMING</code> and <code>Filter.OUTGOING</code>,
     * distinguishing between the two filter chains managed by the
     * command processor.
     * @return A <code>Filter</code> object, used by this service to
     * intercept and process kernel-level commands.
     * @see jade.core.CommandProcessor
     */
    public Filter getCommandFilter(boolean direction) {
        if (direction == Filter.OUTGOING) {
            return localSlice;
        } else {
            return null;
        }
    }

    /**
     * Access the command sink this service uses to handle its own
     * vertical commands.
     */
    public Sink getCommandSink(boolean side) {
        return null;
    }

    /**
     * Access the names of the vertical commands this service wants to
     * handle as their final destination. This set must not overlap
     * with the owned commands set of any previously installed
     * service, or an exception will be raised and service
     * activation will fail.
     *
     * @see jade.core.Service#getCommandSink()
     */
    public String[] getOwnedCommands() {
        return OWNED_COMMANDS;
    }

    public void boot(Profile myProfile) {
        // Do nothing
    }

    public void deliverNow(GenericMessage msg, AID receiverID)
        throws UnreachableException {
        try {
            if (livesHere(receiverID)) {
                localSlice.deliverNow(msg, receiverID);
            } else {
                // Dispatch it through the ACC
                Iterator addresses = receiverID.getAllAddresses();

                while (addresses.hasNext()) {
                    String address = (String) addresses.next();

                    try {
                        forwardMessage(msg, receiverID, address);

                        return;
                    } catch (MTPException mtpe) {
                        System.out.println("Bad address [" + address +
                            "]: trying the next one...");
                    }
                }

                notifyFailureToSender(msg, receiverID,
                    new InternalError("No valid address contained within the AID " +
                        receiverID.getName()));
            }
        } 
        catch (NotFoundException nfe) {
            // The receiver does not exist --> Send a FAILURE message
            notifyFailureToSender(msg, receiverID,
                new InternalError("Agent not found: " + nfe.getMessage()));
        }
        catch (JADESecurityException jse) {
            // The receiver does not exist --> Send a FAILURE message
            notifyFailureToSender(msg, receiverID,
                new InternalError("Not authorized: " + jse.getMessage()));
        }
    }

	private boolean livesHere(AID id) {
		String hap = id.getHap();
		return CaseInsensitiveString.equalsIgnoreCase(hap, platformID);
	}
	
    private void forwardMessage(GenericMessage msg, AID receiver, String address)
        throws MTPException {
        try {
            localSlice.routeOut(msg.getEnvelope(), msg.getPayload(), receiver,
                address);
        } catch (IMTPException imtpe) {
            throw new MTPException("Error during message routing", imtpe);
        }
    }

    /**
     * This method is used internally by the platform in order
     * to notify the sender of a message that a failure was reported by
     * the Message Transport Service.
     * Package scoped as it can be called by the MessageManager
     */
    public void notifyFailureToSender(GenericMessage msg, AID receiver,
        InternalError ie) {
        GenericCommand cmd = new GenericCommand(MessagingSlice.NOTIFY_FAILURE,
                MessagingSlice.NAME, null);
        cmd.addParam(receiver);
        cmd.addParam(msg);
        cmd.addParam(ie);

        try {
            submit(cmd);
        } catch (ServiceException se) {
            // It should never happen
            se.printStackTrace();
        }
    }

    // Vertical command handler methods
    private void handleSendMessage(VerticalCommand cmd)
        throws JADESecurityException {
        Object[] params = cmd.getParams();
        AID sender = (AID) params[0];
        GenericMessage msg = (GenericMessage) params[1];
        AID dest = (AID) params[2];

        // Since message delivery is asynchronous we use the GenericMessage
        // as a temporary holder for the sender principal and credentials to the 
        msg.setSenderPrincipal(cmd.getPrincipal());
        msg.setSenderCredentials(cmd.getCredentials());
        msg.setSender(sender);
        myMessageManager.deliver(msg, dest, this);
    }

    private void handleNotifyFailure(VerticalCommand cmd)
        throws JADESecurityException {
        Object[] params = cmd.getParams();
        GenericMessage msg = (GenericMessage) params[0];
        AID receiver = (AID) params[1];
        InternalError ie = (InternalError) params[2];

        // If (the sender is not the AMS and the performative is not FAILURE)
        ACLMessage aclmsg = msg.getACLMessage();

        if ((aclmsg.getSender() == null) ||
                ((aclmsg.getSender().equals(myContainer.getAMS())) &&
                (aclmsg.getPerformative() == ACLMessage.FAILURE))) { // sanity check to avoid infinite loops

            return;
        }

        // Send back a failure message
        final ACLMessage failure = aclmsg.createReply();
        failure.setPerformative(ACLMessage.FAILURE);

        //System.err.println(failure.toString());
        final AID theAMS = myContainer.getAMS();
        failure.setSender(theAMS);
        failure.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

        // FIXME: the content is not completely correct, but that should
        // also avoid creating wrong content
        String content = "( (action " + msg.getSender().toString();
        content = content + " (ACLMessage) ) (MTS-error " + receiver + " \"" +
            ie.getMessage() + "\") )";
        failure.setContent(content);

        try {
            GenericCommand command = new GenericCommand(MessagingSlice.SEND_MESSAGE,
                    MessagingSlice.NAME, null);
            command.addParam(theAMS);
            command.addParam(new GenericMessage(failure));
            command.addParam((AID) (failure.getAllReceiver().next()));

            // FIXME: We should set the AMS principal and credentials
            submit(command);
        } catch (ServiceException se) {
            // It should never happen
            se.printStackTrace();
        }
    }

    private MTPDescriptor handleInstallMTP(VerticalCommand cmd)
        throws IMTPException, ServiceException, NotFoundException, MTPException {
        Object[] params = cmd.getParams();
        String address = (String) params[0];
        ContainerID cid = (ContainerID) params[1];
        String className = (String) params[2];

        MessagingSlice targetSlice = (MessagingSlice) getSlice(cid.getName());

        return targetSlice.installMTP(address, className);
    }

    private void handleUninstallMTP(VerticalCommand cmd)
        throws IMTPException, ServiceException, NotFoundException, MTPException {
        Object[] params = cmd.getParams();
        String address = (String) params[0];
        ContainerID cid = (ContainerID) params[1];

        MessagingSlice targetSlice = (MessagingSlice) getSlice(cid.getName());
        targetSlice.uninstallMTP(address);
    }

    private void handleSetPlatformAddresses(VerticalCommand cmd) {
        // Do nothing...
    }

    // Work-around for PJAVA compilation
    protected Service.Slice getFreshSlice(String name)
        throws ServiceException {
        return super.getFreshSlice(name);
    }

    /**
     * Inner mix-in class for this service: this class receives
     * commands through its <code>Filter</code> interface and serves
     * them, coordinating with remote parts of this service through
     * the <code>Slice</code> interface (that extends the
     * <code>Service.Slice</code> interface).
     */
    private class ServiceComponent extends Filter implements Service.Slice  {
        private String myHelperName;
        private MessagingSlice myHelper;

        /**
         * Builds a new messaging service lightweight component,
         * relying on a remote slice for most operations.
         **/
        public ServiceComponent(String helperName) {
            myHelperName = helperName;
        }

        // Entry point for the ACL message dispatching process
        public void deliverNow(GenericMessage msg, AID receiverID)
            throws UnreachableException, NotFoundException, JADESecurityException {
            try {
                if (myHelper == null) {
                    myHelper = (MessagingSlice) getSlice(myHelperName);
                }

                deliverUntilOK(msg, receiverID);
            } catch (IMTPException imtpe) {
                throw new UnreachableException("Unreachable network node", imtpe);
            } catch (ServiceException se) {
                throw new UnreachableException("Unreachable service slice:", se);
            }
        }

        private void deliverUntilOK(GenericMessage msg, AID receiverID)
            throws IMTPException, NotFoundException, ServiceException, JADESecurityException {
            boolean ok = false;

            do {
                MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
                ContainerID cid;

                try {
                    cid = mainSlice.getAgentLocation(receiverID);
                } catch (IMTPException imtpe) {
                    // Try to get a newer slice and repeat...
                    mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
                    cid = mainSlice.getAgentLocation(receiverID);
                }

                MessagingSlice targetSlice = (MessagingSlice) getSlice(cid.getName());

                try {
                    targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
                    ok = true;
                } catch (NotFoundException nfe) {
                    ok = false; // Stale proxy again, maybe the receiver is running around. Try again...
                }
            } while (!ok);
        }

        // Implementation of the Filter interface
        public boolean accept(VerticalCommand cmd) {
            try {
                String name = cmd.getName();

                if (name.equals(MessagingSlice.SEND_MESSAGE)) {
                    handleSendMessage(cmd);
                } else if (name.equals(MessagingSlice.NOTIFY_FAILURE)) {
                    handleNotifyFailure(cmd);
                } else if (name.equals(MessagingSlice.INSTALL_MTP)) {
                    Object result = handleInstallMTP(cmd);
                    cmd.setReturnValue(result);
                } else if (name.equals(MessagingSlice.UNINSTALL_MTP)) {
                    handleUninstallMTP(cmd);
                } else if (name.equals(MessagingSlice.SET_PLATFORM_ADDRESSES)) {
                    handleSetPlatformAddresses(cmd);
                }
            } catch (JADESecurityException ae) {
                cmd.setReturnValue(ae);
            } catch (IMTPException imtpe) {
                imtpe.printStackTrace();
            } catch (NotFoundException nfe) {
                nfe.printStackTrace();
            } catch (ServiceException se) {
                se.printStackTrace();
            } catch (MTPException mtpe) {
                mtpe.printStackTrace();
            }

            // Never veto a command
            return true;
        }

        public void setBlocking(boolean newState) {
            // Do nothing. Blocking and Skipping not supported
        }

        public boolean isBlocking() {
            return false; // Blocking and Skipping not implemented
        }

        public void setSkipping(boolean newState) {
            // Do nothing. Blocking and Skipping not supported
        }

        public boolean isSkipping() {
            return false; // Blocking and Skipping not implemented
        }

        // Implementation of the Service.Slice interface
        public Service getService() {
            return LightMessagingService.this;
        }

        public Node getNode() throws ServiceException {
            try {
                return LightMessagingService.this.getLocalNode();
            } catch (IMTPException imtpe) {
                throw new ServiceException("Problem in contacting the IMTP Manager",
                    imtpe);
            }
        }

        public VerticalCommand serve(HorizontalCommand cmd) {
            try {
                String cmdName = cmd.getName();
                Object[] params = cmd.getParams();

                if (cmdName.equals(MessagingSlice.H_DISPATCHLOCALLY)) {
                    AID senderID = (AID) params[0];
                    GenericMessage msg = (GenericMessage) params[1];
                    AID receiverID = (AID) params[2];
                    dispatchLocally(senderID, msg, receiverID);
                } else if (cmdName.equals(MessagingSlice.H_ROUTEOUT)) {
                    Envelope env = (Envelope) params[0];
                    byte[] payload = (byte[]) params[1];
                    AID receiverID = (AID) params[2];
                    String address = (String) params[3];

                    routeOut(env, payload, receiverID, address);
                } else if (cmdName.equals(MessagingSlice.H_GETAGENTLOCATION)) {
                    AID agentID = (AID) params[0];

                    cmd.setReturnValue(getAgentLocation(agentID));
                } else if (cmdName.equals(MessagingSlice.H_INSTALLMTP)) {
                    String address = (String) params[0];
                    String className = (String) params[1];

                    cmd.setReturnValue(installMTP(address, className));
                } else if (cmdName.equals(MessagingSlice.H_UNINSTALLMTP)) {
                    String address = (String) params[0];

                    uninstallMTP(address);
                } else if (cmdName.equals(MessagingSlice.H_NEWMTP)) {
                    MTPDescriptor mtp = (MTPDescriptor) params[0];
                    ContainerID cid = (ContainerID) params[1];

                    newMTP(mtp, cid);
                } else if (cmdName.equals(MessagingSlice.H_DEADMTP)) {
                    MTPDescriptor mtp = (MTPDescriptor) params[0];
                    ContainerID cid = (ContainerID) params[1];

                    deadMTP(mtp, cid);
                } else if (cmdName.equals(MessagingSlice.H_ADDROUTE)) {
                    MTPDescriptor mtp = (MTPDescriptor) params[0];
                    String sliceName = (String) params[1];

                    addRoute(mtp, sliceName);
                } else if (cmdName.equals(MessagingSlice.H_REMOVEROUTE)) {
                    MTPDescriptor mtp = (MTPDescriptor) params[0];
                    String sliceName = (String) params[1];

                    removeRoute(mtp, sliceName);
                }
            } catch (Throwable t) {
                cmd.setReturnValue(t);
            }

            if (cmd instanceof VerticalCommand) {
                return (VerticalCommand) cmd;
            } else {
                return null;
            }
        }

        // Implementation of the service-specific horizontal interface MessagingSlice
        public void dispatchLocally(AID senderAID, GenericMessage msg,
            AID receiverID) throws IMTPException, NotFoundException {
            boolean found = myContainer.postMessageToLocalAgent(msg.getACLMessage(),
                    receiverID);

            if (!found) {
                throw new NotFoundException(
                    "Messaging service slice failed to find " + receiverID);
            }
        }

        public void routeOut(Envelope env, byte[] payload, AID receiverID,
            String address) throws IMTPException, MTPException {
            try {
                if (myHelper == null) {
                    myHelper = (MessagingSlice) getSlice(myHelperName);
                }

                myHelper.routeOut(env, payload, receiverID, address);
            } catch (ServiceException se) {
                throw new MTPException("No suitable route found for address " +
                    address + ".");
            }
        }

        public ContainerID getAgentLocation(AID agentID)
            throws IMTPException, NotFoundException {
            throw new NotFoundException(
                "Agent location lookup not supported by this slice");
        }

        public MTPDescriptor installMTP(String address, String className)
            throws IMTPException, ServiceException, MTPException {
            throw new MTPException(
                "Installing MTPs is not supported by this slice");
        }

        public void uninstallMTP(String address)
            throws IMTPException, ServiceException, NotFoundException, 
                MTPException {
            throw new MTPException(
                "Uninstalling MTPs is not supported by this slice");
        }

        public void newMTP(MTPDescriptor mtp, ContainerID cid)
            throws IMTPException, ServiceException {
            // Do nothing
        }

        public void deadMTP(MTPDescriptor mtp, ContainerID cid)
            throws IMTPException, ServiceException {
            // Do nothing
        }

        public void addRoute(MTPDescriptor mtp, String sliceName)
            throws IMTPException, ServiceException {
            // Do nothing
        }

        public void removeRoute(MTPDescriptor mtp, String sliceName)
            throws IMTPException, ServiceException {
            // Do nothing
        }
    } // End of ServiceComponent class
      /*
       Activates the ACL codecs and MTPs as specified in the given
       <code>Profile</code> instance.
       @param myProfile The <code>Profile</code> instance containing
       the list of ACL codecs and MTPs to activate on this node.
    */}
