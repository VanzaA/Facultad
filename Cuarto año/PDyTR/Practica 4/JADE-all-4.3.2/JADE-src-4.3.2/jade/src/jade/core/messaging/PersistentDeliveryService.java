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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jade.core.ServiceFinder;

import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Filter;
import jade.core.Node;

import jade.core.AgentContainer;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;

import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.util.Logger;



/**

   The JADE service to manage the persistent storage of undelivered
   ACL messages installed on the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public class PersistentDeliveryService extends BaseService {
	/**
     This constant is the name of the property whose value contains
     the name of the application-specific class that will be used by the
     PersistentDeliveryService on the local container as a filter for 
     undelivered ACL messages
	 */
	public static final String PERSISTENT_DELIVERY_FILTER = "persistent-delivery-filter";

	/**
    This constant is the name of the property whose value contains
    the semicolon separated names of the nodes where messages can be stored.
    By default all nodes are considered. This implies that whenever the delivery of a message 
    fails all slices are asked to see if they have a filer that must store the message. 
    Similarly whenever a new agent is created all slices are asked to see if they have messages
    to flush. When the platform is composed by 100 or more containers (e.g. when there are 
    mobile terminals running split containers) this may be very un-efficient --> using this
    option to limit the number of slices to be asked is highly recommended 
	 */
	public static final String PERSISTENT_DELIVERY_STORAGENODES = "persistent-delivery-storagenodes";

	/**
     This constant is the name of the property whose value contains an
     integer representing how often (in milliseconds) the 
     PersistentDeliveryService will try to
     send again previously undelivered ACL messages which have been
     buffered.
	 */
	public static final String PERSISTENT_DELIVERY_SENDFAILUREPERIOD = "persistent-delivery-sendfailureperiod";

	/**
     This constant is the name of the property whose value contains
     the storage method used to persist undelivered ACL messages by
     the PersistentDeliveryService on the local container.
     The supported values for this parameter are:
     <ul>
     <li><b>file</b> - A directory tree on the local filesystem is used.</li>
     </ul>
     If this property is not specified undelivered ACL messages are
     kept in memory and not persisted at all.
	 */
	public static final String PERSISTENT_DELIVERY_STORAGEMETHOD = "persistent-delivery-storagemethod";

	/**
     This constant is the name of the property whose value contains
     the root of the directory tree that is used to persist
     undelivered ACL messages when the <i>file</i> storage
     method is selected.
	 */
	public static final String PERSISTENT_DELIVERY_BASEDIR = "persistent-delivery-basedir";


	static final String ACL_USERDEF_DUE_DATE = "JADE-persistentdelivery-duedate";


	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		myContainer = ac;
		myServiceFinder = myContainer.getServiceFinder();
	}

	public String getName() {
		return PersistentDeliverySlice.NAME;
	}

	public Class getHorizontalInterface() {
		try {
			return Class.forName(PersistentDeliverySlice.NAME + "Slice");
		}
		catch(ClassNotFoundException cnfe) {
			return null;
		}
	}

	public Service.Slice getLocalSlice() {
		return localSlice;
	}

	public Filter getCommandFilter(boolean direction) {
		if(direction == Filter.INCOMING) {
			return inFilter;
		}
		else {
			return outFilter;
		}
	}


	/**
       Outgoing command FILTER.
       Processes the NOTIFY_FAILURE command
	 */
	private class CommandOutgoingFilter extends Filter {

		public boolean accept(VerticalCommand cmd) {

			try {
				String name = cmd.getName();

				if(name.equals(jade.core.messaging.MessagingSlice.NOTIFY_FAILURE)) {
					return handleNotifyFailure(cmd);
				}
			}
			catch(IMTPException imtpe) {
				cmd.setReturnValue(imtpe);
			}
			catch(ServiceException se) {
				cmd.setReturnValue(se);
			}

			// Let the command through
			return true;
		}

		private boolean handleNotifyFailure(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			GenericMessage msg = (GenericMessage)params[0];//FIXME: check object type
			AID receiver = (AID)params[1];
			ACLMessage acl = msg.getACLMessage();

			if(myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE,"Persistent-Delivery - Processing failed message "+MessageManager.stringify(msg)+" for agent "+receiver.getName());


			// FIXME: We should check if the failure is due to a "not found receiver"

			// Ask all storage-enabled slices whether the failed message should be stored
			Service.Slice[] slices = getStorageEnabledSlices();
			for(int i = 0; i < slices.length; i++) {
				PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
				String sliceName = null;
				try {
					sliceName = slice.getNode().getName();
					boolean firstTime = (acl.getUserDefinedParameter(ACL_USERDEF_DUE_DATE) == null);
					boolean accepted = false;
					try {
						accepted = slice.storeMessage(null, msg, receiver);
					}
					catch(IMTPException imtpe) {
						// Try to get a fresh slice and repeat...
						slice = (PersistentDeliverySlice)getFreshSlice(sliceName);
						accepted = slice.storeMessage(null, msg, receiver);
					}

					if(accepted) {
						myLogger.log((firstTime ? Logger.INFO : Logger.FINE) ,"Persistent-Delivery - Message "+MessageManager.stringify(msg)+" for agent "+receiver.getName()+" stored on node "+sliceName);
						// The message was stored --> Veto the NOTIFY_FAILURE command
						return false;
					}
				}
				catch(Exception e) {
					myLogger.log(Logger.WARNING,"Persistent-Delivery - Error trying to store message "+MessageManager.stringify(msg)+" for agent "+receiver.getName()+" on node "+sliceName);
					// Ignore it and try other slices...
				}
			}

			return true;
		}

	} // End of CommandOutgoingFilter class


	/**
       Incoming command FILTER.
       Processes the INFORM_CREATED command. Note that we do this 
       in the postProcess() method so that we are sure the newly
       born agent is already in the GADT.
	 */
	private class CommandIncomingFilter extends Filter {

		@Override
		public void postProcess(VerticalCommand cmd) {
			try {
				String name = cmd.getName();

				if(name.equals(jade.core.management.AgentManagementSlice.INFORM_CREATED)) {
					handleInformCreated(cmd);
				}
			}
			catch(IMTPException imtpe) {
				cmd.setReturnValue(imtpe);
			}
			catch(ServiceException se) {
				cmd.setReturnValue(se);
			}
		}

		private void handleInformCreated(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			myLogger.log(Logger.FINE, "Persistent-Delivery - Flushing persisted messages (if any) for newly started agent "+agentID.getLocalName());
			flushMessages(agentID);
		}

	} // End of CommandIncomingFilter class


	/**
       The SLICE.
	 */
	private class ServiceComponent implements Service.Slice {

		// Implementation of the Service.Slice interface
		public Service getService() {
			return PersistentDeliveryService.this;
		}

		public Node getNode() throws ServiceException {
			try {
				return PersistentDeliveryService.this.getLocalNode();
			}
			catch(IMTPException imtpe) {
				throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
			}
		}

		public VerticalCommand serve(HorizontalCommand cmd) {
			VerticalCommand result = null;
			try {
				String cmdName = cmd.getName();
				Object[] params = cmd.getParams();
				if (cmdName.equals(PersistentDeliverySlice.H_STOREMESSAGE)) {
					String storeName = (String)params[0];
					// NOTE that we can't send the GenericMessage directly as a parameter
					// since we would loose the embedded ACLMessage
					ACLMessage acl = (ACLMessage) params[1];
					Envelope env = (Envelope) params[2];
					byte[] payload = (byte[]) params[3];
					Boolean foreignRecv = (Boolean) params[4];
					String traceId = (String) params[5];
					GenericMessage msg = new GenericMessage();
					msg.update(acl, env, payload);
					msg.setTraceID(traceId);
					msg.setForeignReceiver(foreignRecv.booleanValue());
					AID receiver = (AID)params[6];

					boolean stored = storeMessage(storeName, msg, receiver);
					cmd.setReturnValue(new Boolean(stored));
				}
				else if(cmdName.equals(PersistentDeliverySlice.H_FLUSHMESSAGES)) {
					AID receiver = (AID)params[0];

					flushMessages(receiver);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}

			return result;
		}

		/**
	   This is called following a message delivery failure to check
	   whether or not the message must be stored.
		 */
		private boolean storeMessage(String storeName, GenericMessage msg, AID receiver) throws IMTPException, ServiceException {
			// We store a message only if there is a message filter
			if (messageFilter != null) {
				boolean	firstTime = false;
				long now = System.currentTimeMillis();
				long dueDate = now;
				try {
					// If the due-date parameter is already set, this is a re-transmission
					// attempt --> Use the due-date value
					String dd = msg.getACLMessage().getUserDefinedParameter(ACL_USERDEF_DUE_DATE);
					dueDate = Long.parseLong(dd);
				}
				catch (Exception e) {
					// Due date not yet set (or unknown value)
					long delay = messageFilter.delayBeforeExpiration(msg.getACLMessage());
					if (delay != PersistentDeliveryFilter.NOW) {
						dueDate = (delay == PersistentDeliveryFilter.NEVER ? delay : now+delay);
						msg.getACLMessage().addUserDefinedParameter(ACL_USERDEF_DUE_DATE, String.valueOf(dueDate));
						firstTime = true;
					}
				}

				if (dueDate > now || dueDate == PersistentDeliveryFilter.NEVER) {
					try {
						if (firstTime) {
							if(myLogger.isLoggable(Logger.INFO))
								myLogger.log(Logger.INFO,"Persistent-Delivery - Storing message\n"+MessageManager.stringify(msg)+" for agent "+receiver.getName()+"\nDue date is "+dueDate);
						}
						else {
							if(myLogger.isLoggable(Logger.FINE))
								myLogger.log(Logger.FINE,"Persistent-Delivery - Re-storing message\n"+MessageManager.stringify(msg)+" for agent "+receiver.getName()+"\nDue date is "+dueDate);
						}
						myManager.storeMessage(storeName, msg, receiver);
						return true;
					}
					catch(IOException ioe) {
						throw new ServiceException("I/O Error in message storage", ioe);
					}
				}
			}
			return false;
		}

		/**
	   This is called when a new agent is born to send him the stored
	   messages (if any)
		 */
		private void flushMessages(AID receiver) {
			myLogger.log(Logger.FINE,"Persistent-Delivery - flushing messages for agent "+receiver.getLocalName());
			int cnt = myManager.flushMessages(receiver);
			if (cnt > 0) {
				myLogger.log(Logger.INFO,"Persistent-Delivery - "+cnt+" messages delivered to agent "+receiver);
			}
		}

	} // End of ServiceComponent class



	/**
       Activate the PersistentDeliveryManager and instantiate the 
       PersistentDeliveryFilter.
       Note that getting the MessagingService (required to instantiate
       the PersistentDeliveryManager) cannot be done in the init() method
       since at that time the MessagingService may not be installed yet.
	 */
	public void boot(Profile myProfile) throws ServiceException {
		// getting the delivery channel
		try {
			String str = myProfile.getParameter(PERSISTENT_DELIVERY_STORAGENODES, null);
			if (str != null) {
				storageEnabledSliceNames = str.split(";");
				myLogger.log(Logger.CONFIG, "Persistent-Delivery - Storage enabled nodes: "+str);
			}
			
			MessageManager.Channel ch = (MessageManager.Channel)myServiceFinder.findService(MessagingSlice.NAME);
			if (ch == null)
				throw new ServiceException("Can't locate delivery channel");
			myManager = PersistentDeliveryManager.instance(myProfile, ch);
			myManager.start();
		}
		catch(IMTPException imtpe) {
			imtpe.printStackTrace();
			throw new ServiceException("Cannot retrieve the delivery channel",imtpe);
		}

		try {
			// Load the supplied class to filter messages if any
			String className = myProfile.getParameter(PERSISTENT_DELIVERY_FILTER, null);
			if(className != null) {
				Class c = Class.forName(className);
				messageFilter = (PersistentDeliveryFilter)c.newInstance();
				myLogger.log(Logger.INFO, "Persistent-Delivery - Using message filter of type "+messageFilter.getClass().getName());
			}
		}
		catch(Exception e) {
			throw new ServiceException("Exception in message filter initialization", e);
		}
	}

	/**
	 * Requests all slices to flush the stored messages for a newly born target agent.
	 * Do it in a separated thread since this may take time.
	 * This happens on the main container only.
	 */
	private void flushMessages(final AID target) {
		Thread t = new Thread() {
			public void run() {
				try {
					Service.Slice[] slices = getStorageEnabledSlices();
					String sliceName = null;
					for(int i = 0; i < slices.length; i++) {
						PersistentDeliverySlice slice = (PersistentDeliverySlice)slices[i];
						try {
							sliceName = slice.getNode().getName();
							slice.flushMessages(target);
						}
						catch(Exception e) {
							myLogger.log(Logger.WARNING,"Persistent-Delivery - Error trying to flush messages for agent "+target.getName()+" on node "+sliceName);
							// Ignore it and try other slices...
						}
					}
				}
				catch (ServiceException se) {
					myLogger.log(Logger.WARNING,"Persistent-Delivery - Error retrieving storage-enabled slices to flush persisted messages for agent "+target.getName());
				}
			}
		};
		t.start();
	}
	
	private Service.Slice[] getStorageEnabledSlices() throws ServiceException {
		if (storageEnabledSliceNames != null) {
			List<Service.Slice> ss = new ArrayList<Service.Slice>(storageEnabledSliceNames.length);
			for (int i = 0; i < storageEnabledSliceNames.length; ++i) {
				try {
					Service.Slice s = getSlice(storageEnabledSliceNames[i]);
					ss.add(s);
				}
				catch (ServiceException se) {
					// Slice not present
				}
			}
			return ss.toArray(new Service.Slice[0]);
		}
		else {
			// No storage enabled slices explicitly specified --> Use all
			return getAllSlices();
		}
	}
	
	// The concrete agent container, providing access to LADT, etc.
	private AgentContainer myContainer;

	// The service finder component
	private ServiceFinder myServiceFinder;

	// The component managing ACL message storage and delayed delivery
	private PersistentDeliveryManager myManager;

	// The local slice for this service
	private final ServiceComponent localSlice = new ServiceComponent();

	// The command filter, outgoing direction
	private final CommandOutgoingFilter outFilter = new CommandOutgoingFilter();

	// The command filter, incoming direction
	private final CommandIncomingFilter inFilter = new CommandIncomingFilter();

	// The filter to be matched by undelivered ACL messages
	private PersistentDeliveryFilter messageFilter;

	private String[] storageEnabledSliceNames;
}
