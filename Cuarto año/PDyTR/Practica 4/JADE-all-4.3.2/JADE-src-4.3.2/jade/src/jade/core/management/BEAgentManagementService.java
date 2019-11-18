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

//#J2ME_EXCLUDE_FILE

package jade.core.management;

import jade.core.ServiceFinder;
import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;

import jade.core.Profile;
import jade.core.Agent;
import jade.core.AgentState;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.AgentContainer;
import jade.core.BackEndContainer;
import jade.core.MainContainer;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;

import jade.util.Logger;

/**

   The JADE service to manage the basic agent life cycle: creation,
   destruction, suspension and resumption, in the special case of a
   Back-End Container.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
   @author Jerome Picault - Motorola Labs

 */
public class BEAgentManagementService extends BaseService {

	static final String NAME = "jade.core.management.AgentManagement";



	private static final String[] OWNED_COMMANDS = new String[] {
		AgentManagementSlice.REQUEST_CREATE,
		AgentManagementSlice.REQUEST_KILL,
		AgentManagementSlice.REQUEST_STATE_CHANGE,
		AgentManagementSlice.INFORM_CREATED,
		AgentManagementSlice.INFORM_KILLED,
		AgentManagementSlice.INFORM_STATE_CHANGED,
		AgentManagementSlice.KILL_CONTAINER
	};


	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);

		myContainer = (BackEndContainer)ac;
	}


	public String getName() {
		return AgentManagementSlice.NAME;
	}

	public Class getHorizontalInterface() {
		try {
			return Class.forName(AgentManagementSlice.NAME + "Slice");
		}
		catch(ClassNotFoundException cnfe) {
			return null;
		}
	}

	public Service.Slice getLocalSlice() {
		return localSlice;
	}

	public Filter getCommandFilter(boolean direction) {
		return null;
	}


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


	// This inner class handles the messaging commands on the command
	// issuer side, turning them into horizontal commands and
	// forwarding them to remote slices when necessary.
	private class CommandSourceSink implements Sink {

		public void consume(VerticalCommand cmd) {

			try {
				String name = cmd.getName();
				if(name.equals(AgentManagementSlice.INFORM_KILLED)) {
					handleInformKilled(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_STATE_CHANGED)) {
					handleInformStateChanged(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_CREATED)) {
					handleInformCreated(cmd);
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}
		}


		// Vertical command handler methods

		private void handleInformCreated(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException, ServiceException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];

			boolean startedOnBE = false;
			Agent previous = null;
			// If an actual agent instance was passed as second argument, then this agent 
			// is started within the Back-End container
			if((params.length > 1) && (params[1] instanceof Agent))  {
				Agent instance = (Agent)params[1];
				// If the instance is an AgentImage, this is a re-addition of an agent
				// living in the FE --> just do nothing
				if (!(instance instanceof BackEndContainer.AgentImage)) { 
					// Add the new agent in the LADT
					previous = myContainer.addLocalAgent(agentID, instance);
					startedOnBE = true;
				}
			}
			else {
				// Add the new agent in the images table 
				BackEndContainer.AgentImage image = (BackEndContainer.AgentImage) pendingImages.remove(agentID);
				if (image == null) {
					// The agent spontaneously born on the FrontEnd --> its image still has to be created
					image = myContainer.createAgentImage(agentID);
				}
				previous = (BackEndContainer.AgentImage) myContainer.addAgentImage(agentID, image);
			}

			// Notify the Main Container. Roll back if something fails
			try {
				ContainerID cid = myContainer.getID();
				AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);
				try {
					mainSlice.bornAgent(agentID, cid, cmd);
				}
				catch(IMTPException imtpe) {
					mainSlice = (AgentManagementSlice)getFreshSlice(jade.core.ServiceFinder.MAIN_SLICE);
					mainSlice.bornAgent(agentID, cid, cmd);
				}
			}
			catch (IMTPException imtpe) {
				rollBack(agentID, previous, startedOnBE);
				throw imtpe;
			}
			catch (NotFoundException nfe) {
				rollBack(agentID, previous, startedOnBE);
				throw nfe;
			}
			catch (NameClashException nce) {
				rollBack(agentID, previous, startedOnBE);
				throw nce;
			}
			catch (JADESecurityException jse) {
				rollBack(agentID, previous, startedOnBE);
				throw jse;
			}
			catch (ServiceException se) {
				rollBack(agentID, previous, startedOnBE);
				throw se;
			}
			catch (Exception e) {
				e.printStackTrace();
				rollBack(agentID, previous, startedOnBE);
				throw new IMTPException("Error creating agent " + agentID.getLocalName() + ". ", e);
			}
		}

		private void rollBack(AID agentID, Agent previous, boolean startedOnBE) {
			if (startedOnBE) {
				myContainer.removeLocalAgent(agentID);
				if(previous != null) {
					myContainer.addLocalAgent(agentID, previous);
				}
			}
			else {
				myContainer.removeAgentImage(agentID);
				if (previous != null) {
					myContainer.addAgentImage(agentID, (BackEndContainer.AgentImage) previous);
				}
			}
		}

		private void handleInformKilled(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			AID target = (AID)params[0];

			// Notify the main container through its slice
			AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

			try {
				mainSlice.deadAgent(target, cmd);
			}
			catch(IMTPException imtpe) {
				// Try to get a newer slice and repeat...
				mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
				mainSlice.deadAgent(target, cmd);
			}

			// Remove the dead agent from the agent images
			BackEndContainer.AgentImage image = (BackEndContainer.AgentImage) myContainer.removeAgentImage(target);
		}

		private void handleInformStateChanged(VerticalCommand cmd) {

			Object[] params = cmd.getParams();
			AID target = (AID)params[0];
			AgentState from = (AgentState)params[1];
			AgentState to = (AgentState)params[2];

			if (to.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
				try {
					// Notify the main container through its slice
					AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

					try {
						mainSlice.suspendedAgent(target);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
						mainSlice.suspendedAgent(target);
					}
				}
				catch(IMTPException re) {
					re.printStackTrace();
				}
				catch(NotFoundException nfe) {
					nfe.printStackTrace();
				}
				catch(ServiceException se) {
					se.printStackTrace();
				}
			}
			else if (from.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
				try {
					// Notify the main container through its slice
					AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

					try {
						mainSlice.resumedAgent(target);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
						mainSlice.resumedAgent(target);
					}
				}
				catch(IMTPException re) {
					re.printStackTrace();
				}
				catch(NotFoundException nfe) {
					nfe.printStackTrace();
				}
				catch(ServiceException se) {
					se.printStackTrace();
				}
			}
		}

		/*private void createAgentOnBE(AID target, Agent instance, VerticalCommand cmd) throws IMTPException, JADESecurityException, NameClashException, NotFoundException, ServiceException {
	    // Connect the new instance to the local container
	    Agent old = myContainer.addLocalAgent(target, instance);

	    try {
		    // Notify the main container through its slice
		    AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

		    try {
					mainSlice.bornAgent(target, myContainer.getID(), cmd);
		    }
		    catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
					mainSlice.bornAgent(target, myContainer.getID(), cmd);
		    }
	    }
	    catch(NameClashException nce) {
		myContainer.removeLocalAgent(target);
		if(old != null) {
		    myContainer.addLocalAgent(target, old);
		}
		throw nce;
	    }
	    catch(IMTPException imtpe) {
		myContainer.removeLocalAgent(target);
		throw imtpe;
	    }
	    catch(NotFoundException nfe) {
		myContainer.removeLocalAgent(target);
		throw nfe;
	    }
	    catch(JADESecurityException ae) {
		myContainer.removeLocalAgent(target);
		throw ae;
	    }
	}*/

	} // End of CommandSourceSink class


	private class CommandTargetSink implements Sink {

		public void consume(VerticalCommand cmd) {

			try {
				String name = cmd.getName();
				if(name.equals(AgentManagementSlice.REQUEST_CREATE)) {
					handleRequestCreate(cmd);
				}
				else if(name.equals(AgentManagementSlice.REQUEST_KILL)) {
					handleRequestKill(cmd);
				}
				else if(name.equals(AgentManagementSlice.REQUEST_STATE_CHANGE)) {
					handleRequestStateChange(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_STATE_CHANGED)) {
					handleInformStateChanged(cmd);
				}
				else if(name.equals(AgentManagementSlice.KILL_CONTAINER)) {
					handleKillContainer(cmd);
				}
			}
			catch(IMTPException imtpe) {
				cmd.setReturnValue(new UnreachableException("Remote container is unreachable", imtpe));
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
				cmd.setReturnValue(new UnreachableException("A Service Exception occurred", se));
			}

		}


		// Vertical command handler methods

		private void handleRequestCreate(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, NameClashException, ServiceException {

			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			String className = (String)params[1];
			Object[]args = (Object[])params[2];
			JADEPrincipal owner = (JADEPrincipal) params[3];
			Credentials initialCredentials = (Credentials) params[4];
			createAgent(agentID, className, args, owner, initialCredentials);
		}

		private void handleRequestKill(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, ServiceException {

			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];

			killAgent(agentID);
		}

		private void handleRequestStateChange(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, ServiceException {

			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			int newState = ((Integer)params[1]).intValue();

			changeAgentState(agentID, newState);
		}

		private void handleInformStateChanged(VerticalCommand cmd) throws NotFoundException {

			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			String newState = (String)params[1];

			if (newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
				suspendedAgent(agentID);
			}
			else if(newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
				resumedAgent(agentID);
			}

		}

		private void handleKillContainer(VerticalCommand cmd) {
			myContainer.shutDown();
		}

		/**
	   Force the creation of an agent on the FrontEnd.
	   Note that the agent to create can have a different owner with respect 
	   to the owner of this "container" --> Its image holding the agent's
	   ownership information must be created now and not in the bornAgent()
	   method. This image is stored in the pendingImages map for later 
	   retrieval (see bornAgent()).
		 */
		private void createAgent(AID agentID, String className, Object[] args, JADEPrincipal ownership, Credentials creds) throws IMTPException {

			BackEndContainer.AgentImage image = myContainer.createAgentImage(agentID);

			// Store the image so that it can be retrieved when the new agent starts
			BackEndContainer.AgentImage previous = (BackEndContainer.AgentImage) pendingImages.put(agentID, image);

			try {
				// Arguments can only be Strings
				String[] sargs = null;
				if (args != null) {
					sargs = new String[args.length];
					for (int i = 0; i < args.length; ++i) {
						sargs[i] = (String) args[i];
					}
				}
				myContainer.createAgentOnFE(agentID.getLocalName(), className, sargs);
			}
			catch (IMTPException imtpe) {
				// Roll back if necessary and forward the exception
				pendingImages.remove(agentID);
				if (previous != null) {
					pendingImages.put(agentID, previous);
				}
				throw imtpe;
			}
			catch (ClassCastException cce) {
				// Roll back if necessary and forward the exception
				pendingImages.remove(agentID);
				if (previous != null) {
					pendingImages.put(agentID, previous);
				}
				throw new IMTPException("Non-String argument");
			}
		}

		private void killAgent(AID agentID) throws IMTPException, NotFoundException {

			if (myContainer.getAgentImage(agentID) != null) {
				String name = agentID.getLocalName();
				myContainer.killAgentOnFE(name);
			}
			else {
				throw new NotFoundException("KillAgent failed to find " + agentID);
			}
		}

		private void changeAgentState(AID agentID, int newState) throws IMTPException, NotFoundException {
			BackEndContainer.AgentImage a = myContainer.getAgentImage(agentID);
			if(a == null)
				throw new NotFoundException("Change-Agent-State failed to find " + agentID);

			if(newState == Agent.AP_SUSPENDED) {
				myContainer.suspendAgentOnFE(agentID.getLocalName());
			}
			else if(newState == Agent.AP_ACTIVE) {
				myContainer.resumeAgentOnFE(agentID.getLocalName());
			}
		}

		private void suspendedAgent(AID name) throws NotFoundException {

		}

		private void resumedAgent(AID name) throws NotFoundException {

		}

		/*private void exitContainer() {

	    // "Kill" all agent images
	    AID[] targets = myContainer.getAgentImages();
	    for(int i = 0; i < targets.length; i++) {
		AID target = targets[i];
		try {
		    // Remove the dead agent from the agent images
		    BackEndContainer.AgentImage image = myContainer.removeAgentImage(target);

		    // Notify the main container through its slice
		    AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

		    try {
			mainSlice.deadAgent(target);
		    }
		    catch(IMTPException imtpe) {
			// Try to get a newer slice and repeat...
			mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
			mainSlice.deadAgent(target);
		    }
		}
		catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }

	    myContainer.shutDown();
	}*/


	} // End of CommandTargetSink class



	/**
       Inner mix-in class for this service: this class receives
       commands from the service <code>Sink</code> and serves them,
       coordinating with remote parts of this service through the
       <code>Service.Slice</code> interface.
	 */
	private class ServiceComponent implements Service.Slice {

		// Implementation of the Service.Slice interface

		public Service getService() {
			return BEAgentManagementService.this;
		}

		public Node getNode() throws ServiceException {
			try {
				return BEAgentManagementService.this.getLocalNode();
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

				if(cmdName.equals(AgentManagementSlice.H_CREATEAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.REQUEST_CREATE, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					String className = (String)params[1];
					Object[] arguments = (Object[])params[2];
					String ownership = (String)params[3];
					Credentials certs = (Credentials)params[4];
					gCmd.addParam(agentID);
					gCmd.addParam(className);
					gCmd.addParam(arguments);
					gCmd.addParam(ownership);
					gCmd.addParam(certs);
					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_KILLAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.REQUEST_KILL, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					gCmd.addParam(agentID);

					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_CHANGEAGENTSTATE)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.REQUEST_STATE_CHANGE, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					Integer newState = (Integer)params[1];
					gCmd.addParam(agentID);
					gCmd.addParam(newState);

					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_BORNAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_CREATED, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					ContainerID cid = (ContainerID)params[1];
					String ownership = (String) params[2];
					gCmd.addParam(agentID);
					gCmd.addParam(cid);
					gCmd.addParam(ownership);

					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_DEADAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_KILLED, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					gCmd.addParam(agentID);

					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_SUSPENDEDAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_STATE_CHANGED, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					gCmd.addParam(agentID);
					gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED);

					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_RESUMEDAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_STATE_CHANGED, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					gCmd.addParam(agentID);
					gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE);

					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_EXITCONTAINER)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.KILL_CONTAINER, AgentManagementSlice.NAME, null);

					result = gCmd;
				}
			}
			catch(Throwable t) {
				cmd.setReturnValue(t);
			}

			return result;
		}

	} // End of AgentManagementSlice class



	// The concrete agent container, providing access to LADT, etc.
	private BackEndContainer myContainer;

	// The local slice for this service
	private final ServiceComponent localSlice = new ServiceComponent();

	// The command sink, source side
	private final CommandSourceSink senderSink = new CommandSourceSink();

	// The command sink, target side
	private final CommandTargetSink receiverSink = new CommandTargetSink();

	// Service specific data
	private Map pendingImages = new HashMap(1);

}
