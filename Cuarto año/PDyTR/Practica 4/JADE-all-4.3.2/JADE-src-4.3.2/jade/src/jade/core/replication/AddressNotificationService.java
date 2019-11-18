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


package jade.core.replication;

import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceManager;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;

import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;

import jade.core.AgentContainer;

import jade.util.Logger;


/**
   A kernel-level service to manage a ring of Main Containers,
   keeping the various replicas in sync and providing failure
   detection and recovery to make JADE tolerate Main Container
   crashes.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public class AddressNotificationService extends BaseService {

	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);

		myContainer = ac;

		// Create a local slice
		localSlice = new ServiceComponent(p);

	}

	public String getName() {
		return AddressNotificationSlice.NAME;
	}

	public Class getHorizontalInterface() {
		try {
			return Class.forName(AddressNotificationSlice.NAME + "Slice");
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
			return incomingFilter;
		}
		else {
			return null;
		}
	}

	public Sink getCommandSink(boolean side) {
		return null;
	}

	public String[] getOwnedCommands() {
		return null;
	}

	public void boot(Profile p) throws ServiceException {
		try {
			// Get the Service Manager address list, if this node isn't hosting one itself...
			Node n = getLocalNode();
			if(!n.hasPlatformManager()) {
				Object[] slices = getAllSlices();
				for(int i = 0; i < slices.length; i++) {
					AddressNotificationSlice slice = (AddressNotificationSlice)slices[i];
					if (slice.getNode().hasPlatformManager()) {
						addAddress(slice.getServiceManagerAddress());
					}
				}
			}
		}
		catch(Exception e) {
			throw new ServiceException("Boot failure", e);
		}
	}


	private class IncomingFilter extends Filter {

		public boolean accept(VerticalCommand cmd) {

			try {
				String name = cmd.getName();
				if(name.equals(Service.NEW_REPLICA)) {
					handleAddressAdded(cmd);
				}
			}
			catch(IMTPException imtpe) {
				imtpe.printStackTrace();
			}
			catch(ServiceException se) {
				se.printStackTrace();
			}
			return true;
		}

		// Vertical command handler methods

		public void handleAddressAdded(VerticalCommand cmd) throws IMTPException, ServiceException {
			Object[] params = cmd.getParams();
			String addr = (String)params[0];

			// Broadcast the new address to all the slices...
			GenericCommand hCmd = new GenericCommand(AddressNotificationSlice.H_ADDSERVICEMANAGERADDRESS, AddressNotificationSlice.NAME, null);
			hCmd.addParam(addr);

			broadcastToSlices(hCmd);
		}

	} // End of IncomingFilter class


	private class ServiceComponent implements Service.Slice {

		public ServiceComponent(Profile p) {
			myServiceManager = myContainer.getServiceManager();
		}


		// Implementation of the Service.Slice interface

		public Service getService() {
			return AddressNotificationService.this;
		}

		public Node getNode() throws ServiceException {
			try {
				return AddressNotificationService.this.getLocalNode();
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

				if(cmdName.equals(AddressNotificationSlice.H_ADDSERVICEMANAGERADDRESS)) {
					String addr = (String)params[0];
					addServiceManagerAddress(addr);
				}
				else if(cmdName.equals(AddressNotificationSlice.H_GETSERVICEMANAGERADDRESS)) {
					cmd.setReturnValue(getServiceManagerAddress());
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
				if(result != null) {
					result.setReturnValue(t);
				}
			}

			return result;
		}


		private void addServiceManagerAddress(String addr) throws IMTPException {
			try {
				String localSMAddr = myServiceManager.getLocalAddress();
				if(!addr.equals(localSMAddr)) {
					addAddress(addr);
				}
			}
			catch(IMTPException imtpe) {
				imtpe.printStackTrace();
			}
		}

		private String getServiceManagerAddress() throws IMTPException {
			return myServiceManager.getLocalAddress();
		}

	} // End of ServiceComponent class



	private AgentContainer myContainer;

	private ServiceComponent localSlice;

	// The command sink, source side
	private final IncomingFilter incomingFilter = new IncomingFilter();

	private ServiceManager myServiceManager;

	private void broadcastToSlices(HorizontalCommand cmd) throws IMTPException, ServiceException {

		Object[] slices = getAllSlices();
		for(int i = 0; i < slices.length; i++) {
			AddressNotificationSlice slice = (AddressNotificationSlice)slices[i];
			if (!slice.getNode().hasPlatformManager()) {
				slice.serve(cmd);
			}
		}

	}


	private void addAddress(String addr) throws IMTPException {
		if (myLogger.isLoggable(Logger.CONFIG))
			myLogger.log(Logger.CONFIG,"Adding PlatformManager address "+addr);
		myServiceManager.addAddress(addr);
	}


}
