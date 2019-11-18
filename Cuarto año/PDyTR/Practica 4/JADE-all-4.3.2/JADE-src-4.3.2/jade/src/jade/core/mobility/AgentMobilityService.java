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

package jade.core.mobility;

//#MIDP_EXCLUDE_FILE

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InterruptedIOException;
import java.util.StringTokenizer;
import java.util.zip.*;

import jade.core.ServiceFinder;
import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.Command;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.ServiceHelper;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;
import jade.core.LifeCycle;

import jade.core.Profile;
import jade.core.Agent;
import jade.core.Agent.Interrupted;
import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.AgentDescriptor;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;

import jade.core.management.AgentManagementService;
import jade.core.management.AgentManagementSlice;
//#J2ME_EXCLUDE_BEGIN
import jade.core.management.CodeLocator;
//#J2ME_EXCLUDE_END
import jade.core.replication.MainReplicationHandle;

import jade.lang.acl.ACLMessage;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;
import jade.security.CredentialsHelper;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.Logger;

/**
 The JADE service to manage mobility-related agent life cycle: migration
 and clonation.
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 @author Giovanni Caire - TILAB
 */
public class AgentMobilityService extends BaseService {
	public static final String NAME = AgentMobilitySlice.NAME;
	
	public static final int AP_TRANSIT = 7;
	public static final int AP_COPY = 8;
	public static final int AP_GONE = 9;
	
	
	private static final String[] OWNED_COMMANDS = new String[] {
		AgentMobilityHelper.REQUEST_MOVE,
		AgentMobilityHelper.REQUEST_CLONE,
		AgentMobilityHelper.INFORM_MOVED,
		AgentMobilityHelper.INFORM_CLONED
	};
	
	private static final int SIZE_JAR_BUFFER = 4096;
	
	static final boolean MIGRATION = false;
	static final boolean CLONING = true;
	
	static final boolean CREATE_AND_START = true;
	static final boolean CREATE_ONLY = false;
	
	static final boolean TRANSFER_ABORT = false;
	static final boolean TRANSFER_COMMIT = true;
	
	
	// The command sink, source side
	private final CommandSourceSink senderSink = new CommandSourceSink();
	
	// The command sink, target side
	private final CommandTargetSink receiverSink = new CommandTargetSink();
	
	//#J2ME_EXCLUDE_BEGIN
	// Filter for outgoing commands
	private final Filter _outFilter = new CommandOutgoingFilter();
	//#J2ME_EXCLUDE_END
	
	// The handle to the MainReplicationService to keep GADT in synch when agents move
	private MainReplicationHandle replicationHandle;
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		
		myContainer = ac;
	}
	
	public void boot(Profile myProfile) throws ServiceException {
		// Initialize the MainReplicationHandle
		replicationHandle = new MainReplicationHandle(this, myContainer.getServiceFinder());
	}

	public String getName() {
		return AgentMobilitySlice.NAME;
	}
	
	public Class getHorizontalInterface() {
		return AgentMobilitySlice.class;
	}
	
	public Service.Slice getLocalSlice() {
		return localSlice;
	}
	
	public ServiceHelper getHelper(Agent a) {
		return new AgentMobilityHelperImpl();
	}
	
	//#J2ME_EXCLUDE_BEGIN
	public Filter getCommandFilter(boolean direction) {
		if (direction == Filter.OUTGOING) {
            return _outFilter;
		} else return null;
	}
	//#J2ME_EXCLUDE_END
	
	public Sink getCommandSink(boolean side) {
		if(side == Sink.COMMAND_SOURCE) {
			return senderSink;
		}
		else {
			return receiverSink;
		}
	}
	
	public String[] getOwnedCommands() {
		return OWNED_COMMANDS;
	}
	
	/**
	 * Retrieve the name of the container where the classes of a given agent can be found
	 */
	public String getClassSite(Agent a) {
		return (String) sites.get(a);
	}
	
	// This inner class handles the messaging commands on the command
	// issuer side, turning them into horizontal commands and
	// forwarding them to remote slices when necessary.
	private class CommandSourceSink implements Sink {
		
		public void consume(VerticalCommand cmd) {
			try {
				String name = cmd.getName();
				if(name.equals(AgentMobilityHelper.REQUEST_MOVE)) {
					handleRequestMove(cmd);
				}
				else if(name.equals(AgentMobilityHelper.REQUEST_CLONE)) {
					handleRequestClone(cmd);
				}
				else if(name.equals(AgentMobilityHelper.INFORM_MOVED)) {
					handleInformMoved(cmd);
				}
				else if(name.equals(AgentMobilityHelper.INFORM_CLONED)) {
					handleInformCloned(cmd);
				}
			}
			catch(IMTPException imtpe) {
				cmd.setReturnValue(imtpe);
			}
			catch(NotFoundException nfe) {
				cmd.setReturnValue(nfe);
			}
			catch(NameClashException nce) {
				cmd.setReturnValue(nce);
			}
			catch(JADESecurityException ae) {
				cmd.setReturnValue(ae);
			}
			catch(ServiceException se) {
				cmd.setReturnValue(new IMTPException("Service error", se));
			}
		}
		
		
		// Vertical command handler methods
		
		private void handleRequestMove(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Location where = (Location)params[1];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				ContainerID cid = impl.getContainerID(agentID);
				AgentMobilitySlice targetSlice = (AgentMobilitySlice)getSlice(cid.getName());
				try {
					targetSlice.moveAgent(agentID, where);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					targetSlice = (AgentMobilitySlice)getFreshSlice(cid.getName());
					targetSlice.moveAgent(agentID, where);
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleRequestClone(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Location where = (Location)params[1];
			String newName = (String)params[2];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				ContainerID cid = impl.getContainerID(agentID);
				AgentMobilitySlice targetSlice = (AgentMobilitySlice)getSlice(cid.getName());
				try {
					targetSlice.copyAgent(agentID, where, newName);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					targetSlice = (AgentMobilitySlice)getFreshSlice(cid.getName());
					targetSlice.copyAgent(agentID, where, newName);
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleInformMoved(VerticalCommand cmd) throws IMTPException, ServiceException, JADESecurityException, NotFoundException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Location where = (Location)params[1];
			
			if(myLogger.isLoggable(Logger.CONFIG))
				myLogger.log(Logger.CONFIG,"Moving agent " + agentID.getName() + " on container " + where.getName());
			
			Agent a = myContainer.acquireLocalAgent(agentID);
			if (a == null) {
				myLogger.log(Logger.SEVERE,"Internal error: handleMove() called with a wrong name (" + agentID.getName() + ") !!!");
				return;
			}
			
			int transferState = 0;
			List messages = new ArrayList();
			AgentMobilitySlice dest = null;
			try {
				// If the destination container is the same as this one, there is nothing to do
				if (CaseInsensitiveString.equalsIgnoreCase(where.getName(), myContainer.here().getName())) {
					return;
				}
				
				dest = (AgentMobilitySlice) getSlice(where.getName());
				if (dest == null) {
					myLogger.log(Logger.SEVERE,"Destination "+where.getName()+" does not exist or does not support mobility");
					return;
				}
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE,"Destination container for agent " + agentID + " found");
				}
				
				transferState = 1;
				
				// Serialize the agent
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream encoder = new ObjectOutputStream(out);
				encoder.writeObject(a);
				byte[] bytes = out.toByteArray();
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE,"Agent " + agentID.getName() + " correctly serialized");
				}
				
				// Gets the container where the agent classes can be retrieved (possibly the agent arrived in this container from another container)
				String classSiteName = (String)sites.get(a);			
				if (classSiteName == null) {
					// The agent was born on this container
					classSiteName = getLocalNode().getName();
				}
				
				// Create the agent on the destination container
				try {
					dest.createAgent(agentID, bytes, classSiteName, MIGRATION, CREATE_ONLY);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					dest = (AgentMobilitySlice)getFreshSlice(where.getName());
					dest.createAgent(agentID, bytes, classSiteName, MIGRATION, CREATE_ONLY);
				}
				
				transferState = 2;
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE,"Agent " + agentID.getName() + " correctly created on destination container");
				}
				
				AgentMobilitySlice mainSlice = (AgentMobilitySlice)getSlice(MAIN_SLICE);
				
				// Perform an atomic transaction for agent identity transfer
				// From now on, messages for the moving agent will be routed to the
				// destination container
				boolean transferResult = false;
				try {
					transferResult = mainSlice.transferIdentity(agentID, (ContainerID) myContainer.here(), (ContainerID) where);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					mainSlice = (AgentMobilitySlice)getFreshSlice(MAIN_SLICE);
					transferResult = mainSlice.transferIdentity(agentID, (ContainerID) myContainer.here(), (ContainerID) where);
				}
				
				transferState = 3;
				
				if (transferResult == TRANSFER_COMMIT) {
					if(myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE,"Identity of agent " + agentID.getName() + " correctly transferred");
					}
					
					// Send received messages to the destination container. Note that
					// there is no synchronization problem as the agent is locked in the LADT
					myContainer.fillListFromMessageQueue(messages, a);
					
					dest.handleTransferResult(agentID, transferResult, messages);
					
					try {
						// Cause the termination of the agent thread
						a.changeStateTo(new LifeCycle(AP_GONE) {
							public boolean alive() {
								return false;
							}
						});
						
						// Remove the gone agent from the LADT
						myContainer.removeLocalAgent(a.getAID());
					}
					catch (Exception e) {
						// Should never happen
						e.printStackTrace();
					}
					sites.remove(a);
					if(myLogger.isLoggable(Logger.FINE)) {
						myLogger.log(Logger.FINE,"Agent " + agentID.getName() + " correctly gone");
					}
				}
				else {
					myLogger.log(Logger.WARNING,"Error transferring identity of agent " + agentID.getName());
					
					a.restoreBufferedState();
					dest.handleTransferResult(agentID, transferResult, messages);
					myLogger.log(Logger.WARNING,"Migration of agent " + agentID.getName() + "aborted");
				}
			}
			//#DOTNET_EXCLUDE_BEGIN
			catch (IOException ioe) {
				// Error in agent serialization
				myLogger.log(Logger.SEVERE,"Error in agent serialization. Abort transfer. " + ioe);
			}
			catch (JADESecurityException ae) {
				// Permission to move not owned
				myLogger.log(Logger.SEVERE,"Permission to move not owned. Abort transfer. " + ae.getMessage());
			}
			catch(NotFoundException nfe) {
				if(transferState == 0) {
					myLogger.log(Logger.SEVERE,"Destination container does not exist. Abort transfer. " + nfe.getMessage());
				}
				else if(transferState == 2) {
					myLogger.log(Logger.SEVERE,"Transferring agent does not seem to be part of the platform. Abort transfer. " + nfe.getMessage());
				}
				else if(transferState == 3) {
					// PANIC !!!
					myLogger.log(Logger.SEVERE,"Transferred agent not found on destination container. Can't roll back. " + nfe.getMessage());
				}
			}
			catch(NameClashException nce) {
				// This should not happen, because the agent is not changing its name but just its location...
				nce.printStackTrace();
			}
			catch(IMTPException imtpe) {
				// Unexpected remote error
				if (transferState == 0) {
					myLogger.log(Logger.SEVERE,"Can't retrieve destination container. Abort transfer. " + imtpe.getMessage());
				}
				else if (transferState == 1) {
					myLogger.log(Logger.SEVERE,"Error creating agent on destination container. Abort transfer. " + imtpe.getMessage());
				}
				else if (transferState == 2) {
					myLogger.log(Logger.SEVERE,"Error transferring agent identity. Abort transfer. " + imtpe.getMessage());
					
					try {
						dest.handleTransferResult(agentID, TRANSFER_ABORT, messages);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if (transferState == 3) {
					// PANIC !!!
					myLogger.log(Logger.SEVERE,"Error activating transferred agent. Can't roll back!!!. " + imtpe.getMessage());
				}
			}
			//#DOTNET_EXCLUDE_END
			/*#DOTNET_INCLUDE_BEGIN
			 catch(System.Exception exc)
			 {
			 if(myLogger.isLoggable(Logger.SEVERE))
			 myLogger.log(Logger.SEVERE,"Error in agent serialization. Abort transfer. " + exc.get_Message());
			 }
			 #DOTNET_INCLUDE_END*/
			finally {
				if (transferState <= 2) {
					// Something went wrong --> Roll back.
					a.restoreBufferedState();
				}
				myContainer.releaseLocalAgent(agentID);
			}
		}
		
		private void handleInformCloned(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException { // HandleInformCloned start
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Location where = (Location)params[1];
			String newName = (String)params[2];
			
			try {
				//#J2ME_EXCLUDE_BEGIN	
				//Register the clone in the Code Locator in the case its father is a jar agent
				String containerName = myContainer.getID().getName();
				Agent agent = myContainer.acquireLocalAgent(agentID);
				String codeContainerName = getClassSite(agent);
				myContainer.releaseLocalAgent(agentID);

				//Check if the code is in the same container or in a remote one.
				AgentManagementService amSrv = (AgentManagementService) myFinder.findService(AgentManagementService.NAME);
				CodeLocator codeLocator = amSrv.getCodeLocator();
				
				if (codeContainerName == null) codeContainerName = containerName;
				if (containerName.equals(codeContainerName)) {
					if (codeLocator.isRegistered(agentID)) {
						if(myLogger.isLoggable(Logger.FINE)) {
							myLogger.log(Logger.FINE," adding clone " + newName  + " to code locator.");
						}
						
						codeLocator.cloneAgent(agentID, new AID(newName,AID.ISLOCALNAME));
					}	
				} else {
					//Send a CLONE_CODE_LOCATOR_ENTRY command to the container with the agent code.
					AgentMobilitySlice codeSlice = (AgentMobilitySlice) getSlice(codeContainerName);
					try {
						codeSlice.cloneCodeLocatorEntry(agentID, new AID(newName,AID.ISLOCALNAME));
					} catch (IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						codeSlice = (AgentMobilitySlice) getSlice(codeContainerName);
						codeSlice.cloneCodeLocatorEntry(agentID, new AID(newName,AID.ISLOCALNAME));
					}
				}
				//#J2ME_EXCLUDE_END	
				
				//log("Cloning agent " + agentID + " on container " + where.getName(), 1);
				if(myLogger.isLoggable(Logger.CONFIG))
					myLogger.log(Logger.CONFIG,"Cloning agent " + agentID + " on container " + where.getName());
				
				Agent a = myContainer.acquireLocalAgent(agentID);
				if (a == null) {
					//System.out.println("Internal error: handleClone() called with a wrong name (" + agentID + ") !!!");
					if(myLogger.isLoggable(Logger.SEVERE))
						myLogger.log(Logger.SEVERE,"Internal error: handleClone() called with a wrong name (" + agentID + ") !!!");
					return;
				}
				
				AgentMobilitySlice dest = (AgentMobilitySlice)getSlice(where.getName());
				if (dest == null) {
					myLogger.log(Logger.SEVERE,"Destination "+where.getName()+" does not exist or does not support mobility");
					return;
				}
				if(myLogger.isLoggable(Logger.FINE))
					myLogger.log(Logger.FINE,"Destination container for agent " + agentID + " found");
				
				
				// Serialize the agent
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream encoder = new ObjectOutputStream(out);
				encoder.writeObject(a);
				byte[] bytes = out.toByteArray();
				//log("Agent " + agentID + " correctly serialized", 2);
				if(myLogger.isLoggable(Logger.FINE))
					myLogger.log(Logger.FINE,"Agent " + agentID + " correctly serialized");
				
				
				// Gets the container where the agent classes can be retrieved
				String classSiteName = (String)sites.get(a);
				if (classSiteName == null) {
					// The agent was born on this container
					classSiteName = getLocalNode().getName();
				}
				
				// Create the agent on the destination container with the new AID
				AID newID = new AID(newName, AID.ISLOCALNAME);
				try {
					dest.createAgent(newID, bytes, classSiteName, CLONING, CREATE_AND_START);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					dest = (AgentMobilitySlice)getFreshSlice(where.getName());
					dest.createAgent(newID, bytes, classSiteName, CLONING, CREATE_AND_START);
				}
				if(myLogger.isLoggable(Logger.FINE))
					myLogger.log(Logger.FINE,"Cloned Agent " + newID + " correctly created on destination container");
				
			}
			catch (IOException ioe) {
				// Error in agent serialization
				throw new IMTPException("I/O serialization error in handleInformCloned()", ioe);
			}
			catch(ServiceException se) {
				throw new IMTPException("Destination container not found in handleInformCloned()", se);
			}
			//catch(Exception e){
				//throw new IMTPException("Error accessing to agent's code in handleInformCloned()", e);
			//}
			finally {
				myContainer.releaseLocalAgent(agentID);
			}
		}
		
	} // End of CommandSourceSink class
	
	
	// This inner class handles the messaging commands on the command
	// issuer side, turning them into horizontal commands and
	// forwarding them to remote slices when necessary.
	private class CommandTargetSink implements Sink {
		
		public void consume(VerticalCommand cmd) {
			
			try {
				String name = cmd.getName();
				if(name.equals(AgentMobilityHelper.REQUEST_MOVE)) {
					handleRequestMove(cmd);
				}
				else if(name.equals(AgentMobilityHelper.REQUEST_CLONE)) {
					handleRequestClone(cmd);
				}
				else if(name.equals(AgentMobilityHelper.INFORM_MOVED)) {
					handleInformMoved(cmd);
				}
				else if(name.equals(AgentMobilityHelper.INFORM_CLONED)) {
					handleInformCloned(cmd);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
		}
		
		private void handleRequestMove(VerticalCommand cmd) throws IMTPException, NotFoundException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Location where = (Location)params[1];
			
			moveAgent(agentID, where);
		}
		
		private void handleRequestClone(VerticalCommand cmd) throws IMTPException, NotFoundException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Location where = (Location)params[1];
			String newName = (String)params[2];
			
			copyAgent(agentID, where, newName);
		}
		
		private void handleInformMoved(VerticalCommand cmd) throws IMTPException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			Agent instance = (Agent)params[1];
			String classSiteName = (String)params[2];
			boolean isCloned = ((Boolean)params[3]).booleanValue();
			boolean startIt = ((Boolean)params[4]).booleanValue();
			
			try {
				// Nothing to do here: INFORM_MOVED has no target-side action...
				/* --- This code should go into the Security Service ---
				 
				 // agent is about to be created on the destination Container,
				  // let's check for permissions before
				   
				   // does the agent come from a MOVE or a CLONE ?
				    switch (instance.getState()) {
				    case Agent.AP_TRANSIT:  // MOVED
				    // checking CONTAINER_MOVE_TO...
				     myContainer.getAuthority().checkAction(
				     Authority.CONTAINER_MOVE_TO,
				     myContainer.getContainerPrincipal(),
				     instance.getCertificateFolder()  );
				     break;
				     case Agent.AP_COPY:  // CLONED
				     // checking CONTAINER_CLONE_TO...
				      myContainer.getAuthority().checkAction(
				      Authority.CONTAINER_CLONE_TO,
				      myContainer.getContainerPrincipal(),
				      instance.getCertificateFolder()  );
				      break;
				      } // end switch
				      
				      log("Permissions for agent " + agentID + " OK", 2);
				      
				      // --- End of code that should go into the Security Service ---
				       */
				
				Credentials agentCerts = null;
				//#MIDP_EXCLUDE_BEGIN
				//CertificateFolder agentCerts = instance.getCertificateFolder();
				//#MIDP_EXCLUDE_END
				
				/*# MIDP_INCLUDE_BEGIN
				 CertificateFolder agentCerts = new CertificateFolder();
				 # MIDP_INCLUDE_END*/
				
				if(isCloned) {
					// Notify the main slice that a new agent is born
					AgentMobilitySlice mainSlice = (AgentMobilitySlice)getSlice(MAIN_SLICE);
					
					try {
						mainSlice.clonedAgent(agentID, myContainer.getID(), agentCerts);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						mainSlice = (AgentMobilitySlice)getFreshSlice(MAIN_SLICE);
						mainSlice.clonedAgent(agentID, myContainer.getID(), agentCerts);
					}
				}
				
				// Store the container where the classes for this agent can be
				// retrieved
				sites.put(instance, classSiteName);
				
				// Connect the new instance to the local container
				Agent old = myContainer.addLocalAgent(agentID, instance);
				if(myLogger.isLoggable(Logger.FINE))
					myLogger.log(Logger.FINE,"Agent " + agentID.getName() + " inserted into LADT");
				
				if(startIt) {
					// Actually start the agent thread
					myContainer.powerUpLocalAgent(agentID);
				}
			}
			catch (IMTPException imtpe) {
				throw imtpe;
			}
			catch (Throwable t) {
				t.printStackTrace();
				throw new IMTPException("Unexpected error managing incoming agent.", t);
			}
		}
		
		private void handleInformCloned(VerticalCommand cmd) throws JADESecurityException, NotFoundException, NameClashException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			ContainerID cid = (ContainerID)params[1];
			Credentials creds = (Credentials)params[2];
			
			clonedAgent(agentID, cid, creds);
		}
		
		private void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
			Agent a = myContainer.acquireLocalAgent(agentID);
			
			if(a == null) {
				throw new NotFoundException("Move-Agent failed to find " + agentID);
			}
			a.doMove(where);
			
			myContainer.releaseLocalAgent(agentID);
		}
		
		private void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
			Agent a = myContainer.acquireLocalAgent(agentID);
			
			if(a == null)
				throw new NotFoundException("Clone-Agent failed to find " + agentID);
			a.doClone(where, newName);
			
			myContainer.releaseLocalAgent(agentID);
		}
		
		// FIXME: adjust principal
		private void clonedAgent(AID agentID, ContainerID cid, Credentials credentials) throws JADESecurityException, NotFoundException, NameClashException {
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				// Retrieve the ownership from the credentials
				String ownership = "NONE";
				if (credentials != null) {
					JADEPrincipal ownerPr = credentials.getOwner();
					if (ownerPr != null) {
						ownership = ownerPr.getName();
					}
				}
				// If the name is already in the GADT, throws NameClashException
				bornAgent(agentID, cid, null, ownership, false);
				// Since bornAgent() succeeded, directly apply forceReplacement on replicated Main Containers 
				replicationHandle.invokeReplicatedMethod("bornAgent", new Object[]{agentID, cid, null, ownership, new Boolean(true)});
			}
		}
		
		
	} // End of CommandTargetSink class
	
	
	//#J2ME_EXCLUDE_BEGIN
	private class CommandOutgoingFilter extends Filter {

		protected boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			if (name.equals(AgentManagementSlice.INFORM_KILLED)) {
				try {
					handleInformKilled(cmd);
				} catch (NotFoundException nfe) {
					if (myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,
								"CommandOutgoingFilter: Error deleting remote CodeLocator entry: " + nfe);
				} catch (ServiceException se) {
					if (myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,
								"CommandOutgoingFilter: Error deleting remote CodeLocator entry: " + se);
				} catch (IMTPException imtpe) {
					if (myLogger.isLoggable(Logger.WARNING))
						myLogger.log(Logger.WARNING,
								"CommandOutgoingFilter: Error deleting remote CodeLocator entry: " + imtpe);
				}
				
			}
			
			return true;
		}
		
		private void handleInformKilled(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
			
			Object[] params = cmd.getParams();
			AID target = (AID)params[0];

			//log("Source Sink consuming command INFORM_KILLED. Name is "+target.getName(), 3);
			if(myLogger.isLoggable(Logger.CONFIG))
				myLogger.log(Logger.CONFIG,"Outgoing Filer accepting command INFORM_KILLED. Name is "+target.getName());
			
			// Remove CodeLocator entry.
			String containerName = myContainer.getID().getName();
			Agent agent = myContainer.acquireLocalAgent(target);
			String codeContainerName = getClassSite(agent);
			myContainer.releaseLocalAgent(target);

			//Check if the agent have migrated or not.
			if (codeContainerName != null) {
		
				// Check if the code is in a remote container (if its local it has
				// been removed by the AgentManagementService).
				if (!containerName.equals(codeContainerName)) {

					//Send a REMOVE_CODE_LOCATOR_ENTRY command to the container with the agent code.
					AgentMobilitySlice codeSlice = (AgentMobilitySlice) getSlice(codeContainerName);
					// Note that the Code-Container can be exited in the meanwhile
					if (codeSlice != null) {
						try {
							try {
								codeSlice.removeCodeLocatorEntry(target);						
							} catch (IMTPException imtpe) {
								// Try to get a newer slice and repeat...
								codeSlice = (AgentMobilitySlice) getSlice(codeContainerName);
								codeSlice.removeCodeLocatorEntry(target);
							}
						}
						catch (Exception e) {
							// We can't notify the Code-Container --> print a warning, but do not stop the agent termination process
							myLogger.log(Logger.WARNING, "Error notifying home container "+codeContainerName+" of terminating agent "+target.getName(), e);
						}
					}
				}
			}
		}
	} // End of CommandOutgoingFilter class
	//#J2ME_EXCLUDE_END
	
	
	/**
	 Inner mix-in class for this service: this class receives
	 commands through its <code>Filter</code> interface and serves
	 them, coordinating with remote parts of this service through
	 the <code>Slice</code> interface (that extends the
	 <code>Service.Slice</code> interface).
	 */
	private class ServiceComponent implements Service.Slice {
		
		
		// Implementation of the Service.Slice interface
		
		public Service getService() {
			return AgentMobilityService.this;
		}
		
		public Node getNode() throws ServiceException {
			try {
				return AgentMobilityService.this.getLocalNode();
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

				if(cmdName.equals(AgentMobilitySlice.H_CREATEAGENT)) {
					AID agentID = (AID)params[0];
					byte[] serializedInstance = (byte[])params[1];
					String classSiteName = (String)params[2];
					boolean isCloned = ((Boolean)params[3]).booleanValue();
					boolean startIt = ((Boolean)params[4]).booleanValue();
					
					Agent instance = deserializeAgent(agentID, serializedInstance, classSiteName, isCloned, startIt);
					
					GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.INFORM_MOVED, AgentMobilitySlice.NAME, null);
					gCmd.addParam(agentID);
					gCmd.addParam(instance);
					gCmd.addParam(classSiteName);
					gCmd.addParam(new Boolean(isCloned));
					gCmd.addParam(new Boolean(startIt));
					
					result = gCmd;
				}
				else if(cmdName.equals(AgentMobilitySlice.H_FETCHCLASSFILE)) {
					String className = (String)params[0];
					String agentName = (String)params[1];
					
					cmd.setReturnValue(fetchClassFile(className, agentName));
				}
				else if(cmdName.equals(AgentMobilitySlice.H_MOVEAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.REQUEST_MOVE, AgentMobilitySlice.NAME, null);
					AID agentID = (AID)params[0];
					Location where = (Location)params[1];
					gCmd.addParam(agentID);
					gCmd.addParam(where);
					
					result = gCmd;
				}
				else if(cmdName.equals(AgentMobilitySlice.H_COPYAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.REQUEST_CLONE, AgentMobilitySlice.NAME, null);
					AID agentID = (AID)params[0];
					Location where = (Location)params[1];
					String newName = (String)params[2];
					gCmd.addParam(agentID);
					gCmd.addParam(where);
					gCmd.addParam(newName);
					
					result = gCmd;
				}
				else if(cmdName.equals(AgentMobilitySlice.H_PREPARE)) {
					
					cmd.setReturnValue(new Boolean(prepare()));
				}
				else if(cmdName.equals(AgentMobilitySlice.H_TRANSFERIDENTITY)) {
					AID agentID = (AID)params[0];
					Location src = (Location)params[1];
					Location dest = (Location)params[2];
					
					cmd.setReturnValue(new Boolean(transferIdentity(agentID, src, dest)));
				}
				else if(cmdName.equals(AgentMobilitySlice.H_HANDLETRANSFERRESULT)) {
					AID agentID = (AID)params[0];
					boolean transferResult = ((Boolean)params[1]).booleanValue();
					List messages = (List)params[2];
					
					handleTransferResult(agentID, transferResult, messages);
				}
				else if(cmdName.equals(AgentMobilitySlice.H_CLONEDAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.INFORM_CLONED, AgentMobilitySlice.NAME, null);
					AID agentID = (AID)params[0];
					ContainerID cid = (ContainerID)params[1];
					Credentials creds = (Credentials)params[2];
					gCmd.addParam(agentID);
					gCmd.addParam(cid);
					gCmd.addParam(creds);
					
					result = gCmd;
				}
				//#J2ME_EXCLUDE_BEGIN
				else if(cmdName.equals(AgentMobilitySlice.H_CLONECODELOCATORENTRY)) {
					AID oldAgentID = (AID)params[0];
					AID newAgentID = (AID)params[1];
					
					handleCloneCodeLocatorEntry(oldAgentID, newAgentID);
				}
				else if(cmdName.equals(AgentMobilitySlice.H_REMOVECODELOCATORENTRY)) {
					AID agentID = (AID)params[0];

					handleRemoveCodeLocatorEntry(agentID);
				}
				//#J2ME_EXCLUDE_END
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
				if(result != null) {
					result.setReturnValue(t);
				}
			}
			
			return result;
		}
		
		
		private Agent deserializeAgent(AID agentID, byte[] serializedInstance, String classSiteName, boolean isCloned, boolean startIt) throws IMTPException, ServiceException, NotFoundException, NameClashException, JADESecurityException {
			try {
				if(myLogger.isLoggable(Logger.CONFIG))
					myLogger.log(Logger.CONFIG,"Incoming agent " + agentID.getName());
				
				
				// Reconstruct the serialized agent
				//#DOTNET_EXCLUDE_BEGIN
				ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance), agentID.getName(), classSiteName, myContainer.getServiceFinder());
				Agent instance = (Agent)in.readObject();
				//#DOTNET_EXCLUDE_END
				/*#DOTNET_INCLUDE_BEGIN
				 ubyte[] ubyteSerializedInstance = new ubyte[serializedInstance.length];
				 System.Buffer.BlockCopy(serializedInstance, 0, ubyteSerializedInstance, 0, serializedInstance.length);
				 ByteArrayInputStream in = new ByteArrayInputStream(serializedInstance);
				 ObjectInputStream decoder = new ObjectInputStream(in);
				 Object obj = decoder.readObject();
				 
				 Agent instance = (Agent) obj;
				 #DOTNET_INCLUDE_END*/
				
				//log("Agent " + agentID + " reconstructed", 2);
				if(myLogger.isLoggable(Logger.FINE))
					myLogger.log(Logger.FINE,"Agent " + agentID + " reconstructed");
				
				return instance;
				
			}
			catch(IOException ioe) {
				throw new IMTPException("An I/O error occurred during de-serialization", ioe);
			}
			catch(ClassNotFoundException cnfe) {
				throw new IMTPException("A class was not found during de-serialization", cnfe);
			}
			catch(Throwable t) {
				t.printStackTrace();
				throw new IMTPException("Unexpected error in agent deserialization.", t);
			}
		}
		
		private byte[] fetchClassFile(String className, String agentName) throws IMTPException, ClassNotFoundException {
			if (myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE, "Fetching class " + className);
			
			String fileName = className.replace('.', '/') + ".class";
			InputStream classStream = getClass().getClassLoader().getResourceAsStream(fileName);
			if (classStream == null) {
				// This is likely redundant, but...
				classStream = ClassLoader.getSystemResourceAsStream(fileName);
			}
			if (classStream == null) {
				// In PJAVA for some misterious reason getSystemResourceAsStream()
				// does not work --> Try to do it by hand
				if (myLogger.isLoggable(Logger.FINER))
					myLogger.log(Logger.FINER, "Class not found as a system resource. Try manually");

				classStream = manualGetResourceAsStream(fileName);
			}
			
			//#J2ME_EXCLUDE_BEGIN
			if (classStream == null && agentName != null) {
				// Maybe the class belongs to a separate Jar file --> Try with the CodeLocator
				try {
					AgentManagementService amSrv = (AgentManagementService) myFinder.findService(AgentManagementService.NAME);
					ClassLoader cLoader = amSrv.getCodeLocator().getAgentClassLoader(new AID(agentName, AID.ISGUID));
					classStream = cLoader.getResourceAsStream(fileName);
				}
				catch (NullPointerException npe) {
					// No jarfile or class not found in jarfile. Ignore
				}
				catch (Exception e) {
					// Should never happen since findService() never throws exceptions
					e.printStackTrace();
				}
			}
			//#J2ME_EXCLUDE_END
			
			if (classStream == null) {
				if (myLogger.isLoggable(Logger.WARNING)) {
					myLogger.log(Logger.WARNING, "Class " + className + " not found");
				}
				throw new ClassNotFoundException(className);
			}
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] bytes = new byte[SIZE_JAR_BUFFER];
				int read = 0;		
				DataInputStream dis = new DataInputStream(classStream);
				while ((read = dis.read(bytes)) >= 0) {
					baos.write(bytes, 0, read);
				}
				dis.close();				
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Class " + className + " fetched");
				}
				return (baos.toByteArray());
			} 
			catch (IOException ioe) {
				throw new ClassNotFoundException("IOException reading class bytes. " + ioe.getMessage());
			}
		}
		
		private InputStream manualGetResourceAsStream(String fileName) {
			InputStream classStream = null;
			String currentCp = System.getProperty("java.class.path");
			StringTokenizer st = new StringTokenizer(currentCp, File.pathSeparator);
			while (st.hasMoreTokens()) {
				try {
					String path = st.nextToken();
					if (myLogger.isLoggable(Logger.FINER)) {
						myLogger.log(Logger.FINER, "Searching in path " + path);
					}
					if (path.endsWith(".jar")) {
						if (myLogger.isLoggable(Logger.FINER)) {
							myLogger.log(Logger.FINER, "It's a jar file");
						}

						ClassInfo info = getClassStreamFromJar(fileName, path);
						if (info != null) {
							classStream = info.getClassStream();
							break;
						}
					} 
					else {
						if (myLogger.isLoggable(Logger.FINER)) {
							myLogger.log(Logger.FINER, "Trying file " + path + "/" + fileName);
						}
						
						File f = new File(path + "/" + fileName);
						if (f.exists()) {
							if (myLogger.isLoggable(Logger.FINER)) {
								myLogger.log(Logger.FINER, "File exists");
							}
							classStream = new FileInputStream(f);
							break;
						}
					}
				} 
				catch (Exception e) {
					if (myLogger.isLoggable(Logger.WARNING)) {
						myLogger.log(Logger.WARNING, e.toString());
					}
				}
			}
			return classStream;
		}
		
		private ClassInfo getClassStreamFromJar(String classFileName, String jarName) throws IOException {
			File f = new File(jarName);
			if (f.exists()) {
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Jar file exists");
				}
			}
			ZipFile zf = new ZipFile(f);
			ZipEntry e = zf.getEntry(classFileName);
			if (e != null) {
				if (myLogger.isLoggable(Logger.FINER)) {
					myLogger.log(Logger.FINER, "Entry " + classFileName + " found");
				}
				
				return new ClassInfo(zf.getInputStream(e), (int) e.getSize());
			}
			return null;
		}
		
		
		/**
		 * Inner class ClassInfo
		 * This utility bean class is used only to keep together some pieces of information related to a class
		 */
		private class ClassInfo {
			private InputStream classStream;
			private int length = -1;
			
			public ClassInfo(InputStream is, int l) {
				classStream = is;
				length = l;
			}
			
			public InputStream getClassStream() {
				return classStream;
			}
			
			public int getLength() {
				return length;
			}
		} // END of inner class ClassInfo
		
		
		private void handleTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
			if(myLogger.isLoggable(Logger.FINER))
				myLogger.log(Logger.FINER,"Activating incoming agent "+agentID);
			
			try {
				Agent agent = myContainer.acquireLocalAgent(agentID);
				
				if ((agent == null) || (agent.getState() != AP_TRANSIT)) {
					throw new NotFoundException("handleTransferResult() unable to find a suitable agent.");
				}
				
				if (result == TRANSFER_ABORT) {
					myContainer.removeLocalAgent(agentID);
				}
				else {
					// Insert received messages at the start of the queue
					for (int i = messages.size(); i > 0; i--) {
						agent.putBack((ACLMessage)messages.get(i - 1));
					}
					
					myContainer.powerUpLocalAgent(agentID);
					if(myLogger.isLoggable(Logger.CONFIG))
						myLogger.log(Logger.CONFIG,"Incoming agent " + agentID.getName() + " activated");
				}
			}
			finally {
				myContainer.releaseLocalAgent(agentID);
			}
		}
		
		private boolean prepare() {
			// Just return 'true', because this method is simply used as a 'ping', for now...
			return true;
		}
		
		private boolean transferIdentity(AID agentID, Location src, Location dest) throws IMTPException, NotFoundException {
			//log("Transferring identity of agent "+agentID+" from "+src.getName()+" to "+dest.getName(), 2);
			if(myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE,"Transferring identity of agent "+agentID+" from "+src.getName()+" to "+dest.getName());
			
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				AgentDescriptor ad = impl.acquireAgentDescriptor(agentID);
				if (ad != null) {
					try {
						AgentMobilitySlice srcSlice = (AgentMobilitySlice)getSlice(src.getName());
						AgentMobilitySlice destSlice = (AgentMobilitySlice)getSlice(dest.getName());
						boolean srcReady = false;
						boolean destReady = false;
						
						try {
							srcReady = srcSlice.prepare();
						}
						catch(IMTPException imtpe) {
							srcSlice = (AgentMobilitySlice)getFreshSlice(src.getName());
							srcReady = srcSlice.prepare();
						}
						//log("Source "+src.getName()+" "+srcReady, 2);
						if(myLogger.isLoggable(Logger.FINE))
							myLogger.log(Logger.FINE,"Source "+src.getName()+" "+srcReady);
						
						
						try {
							destReady = destSlice.prepare();
						}
						catch(IMTPException imtpe) {
							destSlice = (AgentMobilitySlice)getFreshSlice(dest.getName());
							destReady = destSlice.prepare();
						}
						//log("Destination "+dest.getName()+" "+destReady, 2);
						if(myLogger.isLoggable(Logger.FINE))
							myLogger.log(Logger.FINE,"Destination "+dest.getName()+" "+destReady);
						
						
						if(srcReady && destReady) {
							// FIXME: We should issue a TRANSFER_IDENTITY V-Command to allow migration tracing and prevention
							// Commit transaction
							movedAgent(agentID, (ContainerID)src, (ContainerID)dest);
							replicationHandle.invokeReplicatedMethod("movedAgent", new Object[]{agentID, (ContainerID)src, (ContainerID)dest});
							return true;
						}
						else {
							// Problems on a participant slice: abort transaction
							return false;
						}
					}
					catch(Exception e) {
						// Link failure: abort transaction
						//log("Link failure!", 2);
						if(myLogger.isLoggable(Logger.WARNING))
							myLogger.log(Logger.WARNING,"Link failure!");
						
						return false;
					}
					finally {
						impl.releaseAgentDescriptor(agentID);
					}
				}
				else {
					throw new NotFoundException("Agent agentID not found");
				}
			}
			else {
				// Do nothing for now, but could also use another slice as transaction coordinator...
				//log("Not a main!", 2);
				if(myLogger.isLoggable(Logger.WARNING))
					myLogger.log(Logger.WARNING,"Not a main!");
				
				return false;
			}
		}
		
		//#J2ME_EXCLUDE_BEGIN		
		private void handleCloneCodeLocatorEntry(AID oldAgentID, AID newAgentID) throws ServiceException, IMTPException, NotFoundException {
			
			AgentManagementService amSrv = (AgentManagementService) myFinder.findService(AgentManagementService.NAME);
			CodeLocator codeLocator = amSrv.getCodeLocator();
			
			if (codeLocator.isRegistered(oldAgentID)) {
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE," adding clone " + newAgentID.getName()  + " to code locator.");
				}
				
				codeLocator.cloneAgent(oldAgentID, newAgentID);
			}

		}
		
		private void handleRemoveCodeLocatorEntry(AID agentID) throws IMTPException, ServiceException {
			
			if(myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE,"Target sink consuming command REMOVE_CODE_LOCATOR_ENTRY");
			}

			// Remove entry from CodeLocator.
			AgentManagementService amSrv = (AgentManagementService) myFinder.findService(AgentManagementService.NAME);
			CodeLocator codeLocator = amSrv.getCodeLocator();
			codeLocator.removeAgent(agentID);
		}
		//#J2ME_EXCLUDE_END
	} // End of ServiceComponent class
	
	
	// Modify GADT to reflect an agent transfer 
	// Public since it is replicated by the MainReplicationService
	public void movedAgent(AID agentID, ContainerID src, ContainerID dest) throws NotFoundException {
		myContainer.getMain().movedAgent(agentID, src, dest);
	}
	
	// Modify GADT to reflect an agent clonation 
	// Public since it is replicated by the MainReplicationService
	public void bornAgent(AID agentID, ContainerID cid, JADEPrincipal principal, String ownership, boolean forceReplacement) throws NameClashException, NotFoundException {
		MainContainer impl = myContainer.getMain();
		try {
			impl.bornAgent(agentID, cid, principal, ownership, forceReplacement);
		}
		catch(NameClashException nce) {
			try {
				ContainerID oldCid = impl.getContainerID(agentID);
				Node n = impl.getContainerNode(oldCid).getNode();
				
				// Perform a non-blocking ping to check...
				n.ping(false);
				
				// Ping succeeded: rethrow the NameClashException
				throw nce;
			}
			catch(NameClashException nce2) {
				throw nce2; // Let this one through...
			}
			catch(Exception e) {
				// Ping failed: forcibly replace the dead agent...
				impl.bornAgent(agentID, cid, null, ownership, true);
			}
		}
	}
	
	/**
	 * Inner class Deserializer
	 */
	private class Deserializer extends ObjectInputStream {
		private String agentName;
		private String classSiteName;
		private ServiceFinder finder;
		
		/**
		 */
		public Deserializer(InputStream inner, String an, String sliceName, ServiceFinder sf) throws IOException {
			super(inner);
			agentName = an;
			classSiteName = sliceName;
			finder = sf;
		}
		
		/**
		 */
		protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
			String key = createClassLoaderKey(agentName, classSiteName);
			MobileAgentClassLoader cl = (MobileAgentClassLoader)loaders.get(key);
			if (cl == null) {
				try {
					cl = new MobileAgentClassLoader(agentName, classSiteName, finder, AgentMobilityService.this.getClass().getClassLoader());
					loaders.put(key, cl);
				}
				catch (IMTPException imtpe) {
					// We are loading an incoming agent --> Should never happen
					imtpe.printStackTrace();
					throw new ClassNotFoundException("Error creating MobileAgent ClassLoader. "+imtpe.getMessage());
				}
				catch (ServiceException se) {
					// We are loading an incoming agent --> Should never happen
					se.printStackTrace();
					throw new ClassNotFoundException("Error creating MobileAgent ClassLoader. "+se.getMessage());
				}
			}
			//#J2ME_EXCLUDE_BEGIN
			Class c;
			try {
				c = Class.forName(v.getName(), true, cl);
			} catch (ClassNotFoundException ex) {
			    c = (Class) primitiveJavaClasses.get(v.getName());
			    if (c == null) {
			    	throw ex;
			    }
			}
			//#J2ME_EXCLUDE_END
			/*#J2ME_INCLUDE_BEGIN
			Class c = cl.loadClass(v.getName());
			#J2ME_INCLUDE_END*/
			return c;
		}
		
		private String createClassLoaderKey(String agentName, String classSiteName) {
			return agentName+'#'+classSiteName;
		}
		
	}    // END of inner class Deserializer
	
	private static final java.util.HashMap primitiveJavaClasses = new java.util.HashMap(8, 1.0F);
    static {
    	primitiveJavaClasses.put("boolean", boolean.class);
    	primitiveJavaClasses.put("byte", byte.class);
    	primitiveJavaClasses.put("char", char.class);
    	primitiveJavaClasses.put("short", short.class);
    	primitiveJavaClasses.put("int", int.class);
    	primitiveJavaClasses.put("long", long.class);
    	primitiveJavaClasses.put("float", float.class);
		primitiveJavaClasses.put("double", double.class);
		primitiveJavaClasses.put("void", void.class);
    }
    
	// This Map holds the mapping between a container/agent pair and the class loader
	// that can retrieve agent classes from that container.
	private final Map loaders = new HashMap();
	
	// This Map holds the mapping between an agent that arrived on this
	// container and the service slice where its classes can be found
	private final Map sites = new HashMap();
	
	// The concrete agent container, providing access to LADT, etc.
	private AgentContainer myContainer;
	
	// The local slice for this service
	private final ServiceComponent localSlice = new ServiceComponent();
	
	
	/**
	 Inner class AgentMobilityHelperImpl.
	 The actual implementation of the AgentMobilityHelper interface.
	 */
	private class AgentMobilityHelperImpl implements AgentMobilityHelper {
		private Agent myAgent;
		private Movable myMovable;
		
		public void init(Agent a) {
			myAgent = a;
		}
		
		public void registerMovable(Movable m) {
			myMovable = m;
		}
		
		public void move(Location destination) {
			myAgent.changeStateTo(new TransitLifeCycle(destination, myMovable, AgentMobilityService.this));
		}
		
		public void clone(Location destination, String newName) {
			myAgent.changeStateTo(new CopyLifeCycle(destination, newName, myMovable, AgentMobilityService.this));
		}
		
		//#J2ME_EXCLUDE_BEGIN
		public ClassLoader getContainerClassLoader(String codeSourceContainer, ClassLoader parent) throws ServiceException {
			try {
				return new MobileAgentClassLoader(null, codeSourceContainer, AgentMobilityService.this.myFinder, parent);
			}
			catch (IMTPException imtpe) {
				throw new ServiceException("Communication error retrieving code source container slice.", imtpe);
			}
		}
		//#J2ME_EXCLUDE_END
	}  // END of inner class AgentMobilityHelperImpl
	
	
	/**
	 Inner class TransitLifeCycle
	 */
	private static class TransitLifeCycle extends LifeCycle {
		private Location myDestination;
		private Movable myMovable;
		private transient AgentMobilityService myService;
		private Logger myLogger;
		private boolean firstTime = true;
		private boolean messageAware = false;
		
		private TransitLifeCycle(Location l, Movable m, AgentMobilityService s) {
			super(AP_TRANSIT);
			myDestination = l;
			myMovable = m;
			myService = s;
			myLogger = Logger.getMyLogger(myService.getName());
		}
		
		public void init() {
			myAgent.restoreBufferedState();
			if (myMovable != null) {
				myMovable.afterMove();
			}
		}
		
		public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
			try {
				// Call beforeMove() and issue an INFORM_MOVED vertical command
				if (firstTime) {
					firstTime = false;
					if (myMovable != null) {
						messageAware = true;
						myMovable.beforeMove();
						messageAware = false;
					}
					informMoved(myAgent.getAID(), myDestination);
				}
			}
			catch (Exception e) {
				if (myAgent.getState() == myState) {
					// Something went wrong during the transfer. Rollback
					myAgent.restoreBufferedState();
					myDestination = null;
					if (e instanceof JADESecurityException) {
						// Will be caught together with all other JADESecurityException-s
						throw (JADESecurityException) e;
					}
					else {
						e.printStackTrace();
					}
				}
				else {
					throw new Interrupted();
				}
			}
		}
		
		public void end() {
			if(myLogger.isLoggable(Logger.SEVERE))
				myLogger.log(Logger.SEVERE,"***  Agent " + myAgent.getName() + " moved in a forbidden situation ***");
			
			myAgent.clean(true);
		}
		
		public boolean transitionTo(LifeCycle newLF) {
			int s = newLF.getState();
			return (s == AP_GONE || s == Agent.AP_ACTIVE || s == Agent.AP_DELETED);
		}
		
		public boolean isMessageAware() {
			return messageAware;
		}
		
		public void informMoved(AID agentID, Location where) throws ServiceException, JADESecurityException, NotFoundException, IMTPException {
			GenericCommand cmd = new GenericCommand(AgentMobilityHelper.INFORM_MOVED, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(where);
			// Set the credentials of the moving agent
			myService.initCredentials(cmd, agentID);
			
			Object lastException = myService.submit(cmd);
			if(lastException != null) {
				if(lastException instanceof JADESecurityException) {
					throw (JADESecurityException)lastException;
				}
				if(lastException instanceof NotFoundException) {
					throw (NotFoundException)lastException;
				}
				if(lastException instanceof IMTPException) {
					throw (IMTPException)lastException;
				}
			}
		}
	} // END of inner class TransitLifeCycle
	
	
	/**
	 Inner class CopyLifeCycle
	 */
	private static class CopyLifeCycle extends LifeCycle {
		private Location myDestination;
		private String myNewName;
		private Movable myMovable;
		private transient AgentMobilityService myService;
		private Logger myLogger;
		private boolean firstTime = true;
		private boolean messageAware = false;
		
		private CopyLifeCycle(Location l, String newName, Movable m, AgentMobilityService s) {
			super(AP_COPY);
			myDestination = l;
			myNewName = newName;
			myMovable = m;
			myService = s;
			myLogger = Logger.getMyLogger(myService.getName());
		}
		
		public void init() {
			myAgent.restoreBufferedState();
			if (myMovable != null) {
				myMovable.afterClone();
			}
		}
		
		public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
			try {
				// Call beforeClone() and issue an INFORM_CLONED vertical command
				if (firstTime) {
					firstTime = false;
					if (myMovable != null) {
						messageAware = true;
						myMovable.beforeClone();
						messageAware = false;
					}
					informCloned(myAgent.getAID(), myDestination, myNewName);
				}
			}
			catch (Exception e) {
				if (myAgent.getState() == myState) {
					// Something went wrong during the clonation. Rollback
					myDestination = null;
					myNewName = null;
					myAgent.restoreBufferedState();
					if (e instanceof JADESecurityException) {
						// Will be catched together with all other JADESecurityException-s
						throw (JADESecurityException) e;
					}
					else {
						e.printStackTrace();
						return;
					}
				}
				else {
					throw new Interrupted();
				}
			}
			// Once cloned go back to the previous state
			myAgent.restoreBufferedState();
		}
		
		public boolean transitionTo(LifeCycle newLF) {
			int s = newLF.getState();
			return (s == Agent.AP_ACTIVE || s == Agent.AP_DELETED);
		}
		
		public boolean isMessageAware() {
			return messageAware;
		}
		
		public void end() {
			//System.err.println("***  Agent " + myAgent.getName() + " cloned in a forbidden situation ***");
			if(myLogger.isLoggable(Logger.SEVERE))
				myLogger.log(Logger.SEVERE,"***  Agent " + myAgent.getName() + " cloned in a forbidden situation ***");
			myAgent.clean(true);
		}
		
		public void informCloned(AID agentID, Location where, String newName) throws ServiceException, JADESecurityException, IMTPException, NotFoundException, NameClashException {
			GenericCommand cmd = new GenericCommand(AgentMobilityHelper.INFORM_CLONED, AgentMobilitySlice.NAME, null);
			cmd.addParam(agentID);
			cmd.addParam(where);
			cmd.addParam(newName);
			// Set the credentials of the cloning agent
			myService.initCredentials(cmd, agentID);
			
			Object lastException = myService.submit(cmd);
			if(lastException != null) {
				if(lastException instanceof JADESecurityException) {
					throw (JADESecurityException)lastException;
				}
				if(lastException instanceof NotFoundException) {
					throw (NotFoundException)lastException;
				}
				if(lastException instanceof IMTPException) {
					throw (IMTPException)lastException;
				}
				if(lastException instanceof NameClashException) {
					throw (NameClashException)lastException;
				}
			}
		}
	} // END of inner class CopyLifeCycle
	
	
	// Work-around for PJAVA compilation
	protected Service.Slice getFreshSlice(String name) throws ServiceException {
		return super.getFreshSlice(name);
	}
	
	private void initCredentials(Command cmd, AID id) {
		Agent agent = myContainer.acquireLocalAgent(id);
		if (agent != null) {
			try {
				CredentialsHelper ch = (CredentialsHelper) agent.getHelper("jade.core.security.Security");
				cmd.setPrincipal(ch.getPrincipal());
				cmd.setCredentials(ch.getCredentials());
			}
			catch (ServiceException se) {
				// The security plug-in is not there. Just ignore it
			}
		}
		myContainer.releaseLocalAgent(id);
	}
}

