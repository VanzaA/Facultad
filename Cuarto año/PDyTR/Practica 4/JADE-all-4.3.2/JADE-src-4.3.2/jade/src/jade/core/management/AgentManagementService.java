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

package jade.core.management;

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
import jade.core.MainContainer;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;

import jade.util.Logger;
import jade.util.leap.Properties;

//#J2ME_EXCLUDE_BEGIN
import jade.util.ObjectManager;

import java.io.IOException;
import java.io.File;
//#J2ME_EXCLUDE_END


/**
 
 The JADE service to manage the basic agent life cycle: creation,
 destruction, suspension and resumption.
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 
 */
public class AgentManagementService extends BaseService {
	public static final String NAME = AgentManagementSlice.NAME;
	
	// Class properties names
	public static final String CLASS_CODE = "code";
	public static final String CLASS_STATE = "state";
	
	
	/**
	 The path where to search agent jar files
	 */
	public static final String AGENTS_PATH = "jade_core_management_AgentManagementService_agentspath";
	
	private static final String[] OWNED_COMMANDS = new String[] {
		AgentManagementSlice.REQUEST_CREATE,
		AgentManagementSlice.REQUEST_KILL,
		AgentManagementSlice.REQUEST_STATE_CHANGE,
		AgentManagementSlice.INFORM_CREATED,
		AgentManagementSlice.INFORM_KILLED,
		AgentManagementSlice.INFORM_STATE_CHANGED,
		AgentManagementSlice.KILL_CONTAINER,
		AgentManagementSlice.ADD_TOOL,
		AgentManagementSlice.REMOVE_TOOL
	};
	
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		
		myContainer = ac;
		
		//#J2ME_EXCLUDE_BEGIN
		// Initialize the agent-loader for "jar agents" and the CodeLocator
		agentsPath = p.getParameter(AGENTS_PATH, ".");
		ObjectManager.addLoader(ObjectManager.AGENT_TYPE, new ObjectManager.Loader() {
			public Object load(String className, Properties pp) throws ClassNotFoundException, IllegalAccessException, InstantiationException { 
				String jarName = pp.getProperty(CLASS_CODE);
				boolean warnIfJarNotFound = true;
				if (jarName == null) {
					jarName = className.replace('.', '_') + ".jar";
					warnIfJarNotFound = false;
				}
				jarName = agentsPath + File.separator + jarName;
				File file = new File(jarName);
				try {
					if (file.exists()) {
						JarClassLoader loader = new JarClassLoader(file, getClass().getClassLoader());
						return (Agent) Class.forName(className, true, loader).newInstance();
					}
					else if (warnIfJarNotFound) {
						myLogger.log(Logger.WARNING, "Jar file "+jarName+" for class "+className+" does not exist");
					}
				}
				catch (IOException ioe) {
					myLogger.log(Logger.WARNING, "File "+file.getPath()+" is not a valid Jar file.");
				}
				return null;
			}
		});
		
		codeLocator = new CodeLocator();
		//#J2ME_EXCLUDE_END
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
	
	public void removeLocalAgent(AID target) {
		myContainer.removeLocalAgent(target);
	}
	
	//#J2ME_EXCLUDE_BEGIN
	public CodeLocator getCodeLocator() {
		return codeLocator;
	}
	//#J2ME_EXCLUDE_END
	
	// This inner class handles the messaging commands on the command
	// issuer side, turning them into horizontal commands and
	// forwarding them to remote slices when necessary.
	private class CommandSourceSink implements Sink {
		
		public void consume(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if(name.equals(AgentManagementSlice.REQUEST_CREATE)) {
					handleRequestCreate(cmd);
				}
				else if(name.equals(AgentManagementSlice.REQUEST_KILL)) {
					handleRequestKill(cmd);
				}
				else if(name.equals(AgentManagementSlice.REQUEST_STATE_CHANGE)) {
					handleRequestStateChange(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_CREATED)) {
					handleInformCreated(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_KILLED)) {
					handleInformKilled(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_STATE_CHANGED)) {
					handleInformStateChanged(cmd);
				}
				else if(name.equals(AgentManagementSlice.KILL_CONTAINER)) {
					handleKillContainer(cmd);
				}
				else if(name.equals(AgentManagementSlice.ADD_TOOL)) {
					handleAddTool(cmd);
				}
				else if(name.equals(AgentManagementSlice.REMOVE_TOOL)) {
					handleRemoveTool(cmd);
				}
			}
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
		}
		
		
		// Vertical command handler methods
		
		private void handleRequestCreate(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, NameClashException, ServiceException {
			
			Object[] params = cmd.getParams();
			String name = (String)params[0];
			String className = (String)params[1];
			Object[]args = (Object[])params[2];
			ContainerID cid = (ContainerID)params[3];
			JADEPrincipal owner = (JADEPrincipal) params[4];
			Credentials initialCredentials = (Credentials) params[5];
			
			if(myLogger.isLoggable(Logger.CONFIG))
				myLogger.log(Logger.CONFIG,"Source Sink consuming command REQUEST_CREATE. Name is "+name);
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				AID agentID = new AID(name, AID.ISLOCALNAME);
				AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
				if (targetSlice != null) {
					try {
						targetSlice.createAgent(agentID, className, args, owner, initialCredentials, AgentManagementSlice.CREATE_AND_START, cmd);
					}
					catch(IMTPException imtpe) {
						// Try to get a newer slice and repeat...
						targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
						targetSlice.createAgent(agentID, className, args, owner, initialCredentials, AgentManagementSlice.CREATE_AND_START, cmd);
					}
				}
				else {
					throw new NotFoundException("Container "+cid.getName()+" not found");
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleRequestKill(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, ServiceException {
			
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			
			//log("Source Sink consuming command REQUEST_KILL. Name is "+agentID.getName(), 3);
			if(myLogger.isLoggable(Logger.CONFIG))
				myLogger.log(Logger.CONFIG,"Source Sink consuming command REQUEST_KILL. Name is "+agentID.getName());
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				ContainerID cid = impl.getContainerID(agentID);
				// Note that since getContainerID() succeeded, targetSlice can't be null
				AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
				try {
					targetSlice.killAgent(agentID, cmd);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
					targetSlice.killAgent(agentID, cmd);
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleRequestStateChange(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, ServiceException {
			
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			AgentState as = (AgentState)params[1];
			
			int newState = Agent.AP_MIN;
			if(as.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
				newState = Agent.AP_SUSPENDED;
			}
			else if(as.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.WAITING)) {
				newState = Agent.AP_WAITING;
			}
			else if(as.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
				newState = Agent.AP_ACTIVE;
			}
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				ContainerID cid = impl.getContainerID(agentID);
				// Note that since getContainerID() succeeded, targetSlice can't be null
				AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
				try {
					targetSlice.changeAgentState(agentID, newState);
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
					targetSlice.changeAgentState(agentID, newState);
				}
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleInformCreated(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException, ServiceException {
			Object[] params = cmd.getParams();
			AID target = (AID)params[0];
			Agent instance = (Agent)params[1];
			JADEPrincipal owner = (JADEPrincipal)params[2];
			
			if(myLogger.isLoggable(Logger.CONFIG)) {
				String ownerInfo = owner != null ? ", Owner = "+owner : "";
				myLogger.log(Logger.CONFIG,"Source Sink consuming command INFORM_CREATED. Name is "+target.getName()+ownerInfo);
			}
			
			initAgent(target, instance, cmd);
		}
		
		private void handleInformKilled(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
			Object[] params = cmd.getParams();
			AID target = (AID)params[0];

			//log("Source Sink consuming command INFORM_KILLED. Name is "+target.getName(), 3);
			if(myLogger.isLoggable(Logger.CONFIG))
				myLogger.log(Logger.CONFIG,"Source Sink consuming command INFORM_KILLED. Name is "+target.getName());
			
			// Remove CodeLocator entry.
			//#J2ME_EXCLUDE_BEGIN
			codeLocator.removeAgent(target);
			//#J2ME_EXCLUDE_END

			// Remove the dead agent from the LADT of the container
			removeLocalAgent(target);
			
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
		
		private void handleKillContainer(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
			Object[] params = cmd.getParams();
			ContainerID cid = (ContainerID)params[0];
			
			if(myLogger.isLoggable(Logger.CONFIG)) {
				myLogger.log(Logger.CONFIG,"Source Sink consuming command KILL_CONTAINER. Container is "+cid.getName());
			}
			
			// Forward to the correct slice
			AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
			try {
				try {
					targetSlice.exitContainer();
				}
				catch(IMTPException imtpe) {
					// Try to get a newer slice and repeat...
					targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
					targetSlice.exitContainer();
				}
			}
			catch (NullPointerException npe) {
				// targetSlice not found --> The container does not exist
				throw new NotFoundException("Container "+cid.getName()+" not found");
			}
		}
		
		private void handleAddTool(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID tool = (AID)params[0];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				impl.toolAdded(tool);
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		private void handleRemoveTool(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID tool = (AID)params[0];
			
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				impl.toolRemoved(tool);
			}
			else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
			}
		}
		
		
	} // End of CommandSourceSink class
	
	
	private class CommandTargetSink implements Sink {
		
		public void consume(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if(name.equals(AgentManagementSlice.REQUEST_CREATE)) {
					handleRequestCreate(cmd);
				}
				else if(name.equals(AgentManagementSlice.REQUEST_KILL)) {
					handleRequestKill(cmd);
				}
				else if(name.equals(AgentManagementSlice.REQUEST_STATE_CHANGE)) {
					handleRequestStateChange(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_KILLED)) {
					handleInformKilled(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_STATE_CHANGED)) {
					handleInformStateChanged(cmd);
				}
				else if(name.equals(AgentManagementSlice.INFORM_CREATED)) {
					handleInformCreated(cmd);
				}
				else if(name.equals(AgentManagementSlice.KILL_CONTAINER)) {
					handleKillContainer(cmd);
				}
			}
			catch (Throwable t) {
				cmd.setReturnValue(t);
			}
		}
		
		
		// Vertical command handler methods
		
		private void handleRequestCreate(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, NameClashException, ServiceException {
			
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			String className = (String)params[1];
			Object[] arguments = (Object[])params[2];
			JADEPrincipal owner = (JADEPrincipal)params[3];
			Credentials initialCredentials = (Credentials)params[4];
			boolean startIt = ((Boolean) params[5]).booleanValue();
			
			//log("Target sink consuming command REQUEST_CREATE: Name is "+agentID.getName(), 2);
			if(myLogger.isLoggable(Logger.FINE)) {
				String ownerInfo = owner != null ? ", Owner = "+owner : "";
				myLogger.log(Logger.FINE,"Target sink consuming command REQUEST_CREATE: Name is "+agentID.getName()+ownerInfo);
			}
			
			createAgent(agentID, className, arguments, owner, initialCredentials, startIt);
		}
		
		private void handleRequestKill(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, ServiceException {
			
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			
			//log("Target sink consuming command REQUEST_KILL: Name is "+agentID.getName(), 2);
			if(myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE,"Target sink consuming command REQUEST_KILL: Name is "+agentID.getName());
			
			killAgent(agentID);
		}
		
		private void handleRequestStateChange(VerticalCommand cmd) throws IMTPException, JADESecurityException, NotFoundException, ServiceException {
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			int newState = ((Integer)params[1]).intValue();
			
			changeAgentState(agentID, newState);
		}
		
		private void handleInformCreated(VerticalCommand cmd) throws NotFoundException, NameClashException, ServiceException {
			Object[] params = cmd.getParams();
			
			AID agentID = (AID)params[0];
			ContainerID cid = (ContainerID)params[1];
			
			if(myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE,"Target sink consuming command INFORM_CREATED: Name is "+agentID.getName());
			}
			
			bornAgent(agentID, cid, cmd.getPrincipal(), cmd.getCredentials());
		}
		
		private void handleInformKilled(VerticalCommand cmd) throws NotFoundException, ServiceException {
			
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			
			//log("Target sink consuming command INFORM_KILLED: Name is "+agentID.getName(), 2);
			if(myLogger.isLoggable(Logger.FINE))
				myLogger.log(Logger.FINE,"Target sink consuming command INFORM_KILLED: Name is "+agentID.getName());
			
			deadAgent(agentID);
		}
		
		private void handleInformStateChanged(VerticalCommand cmd) throws NotFoundException {
			
			Object[] params = cmd.getParams();
			AID agentID = (AID)params[0];
			String newState = (String)params[1];
			String oldState = (String)params[2];
			
			if (newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
				suspendedAgent(agentID);
			}
			else if(newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
				resumedAgent(agentID);
			}
		}
		
		private void handleKillContainer(VerticalCommand cmd) {
			if(myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE,"Target sink consuming command KILL_CONTAINER");
			}
			exitContainer();
		}
		
		private void createAgent(AID agentID, String className, Object arguments[], JADEPrincipal owner, Credentials initialCredentials, boolean startIt) throws IMTPException, NotFoundException, NameClashException, JADESecurityException {
			Agent agent = null;
			try {
				//#J2ME_EXCLUDE_BEGIN
				// Try to load the agent using an agent loader
				agent = (Agent) ObjectManager.load(className, ObjectManager.AGENT_TYPE);
				//#J2ME_EXCLUDE_END
				
				if (agent == null) {
					agent = (Agent)Class.forName(className).newInstance();
				}
				
				agent.setArguments(arguments);
				
				myContainer.initAgent(agentID, agent, owner, initialCredentials);		
				
				if (startIt) {
					myContainer.powerUpLocalAgent(agentID);
				}
			}
			catch(ClassNotFoundException cnfe) {
				throw new IMTPException("Class " + className + " for agent " + agentID + " not found", cnfe);
			}
			catch(InstantiationException ie) {
				throw new IMTPException("Class " + className + " for agent " + agentID + " cannot be instantiated", ie);
			}
			catch(IllegalAccessException iae) {
				throw new IMTPException("Illegal access exception in createAgent()", iae);
			}
		}
		
		
		private void killAgent(AID agentID) throws IMTPException, NotFoundException {
			
			Agent a = myContainer.acquireLocalAgent(agentID);
			
			if(a == null)
				throw new NotFoundException("Kill-Agent failed to find " + agentID);
			a.doDelete();
			
			myContainer.releaseLocalAgent(agentID);
		}
		
		private void changeAgentState(AID agentID, int newState) throws IMTPException, NotFoundException {
			Agent a = myContainer.acquireLocalAgent(agentID);
			
			if(a == null)
				throw new NotFoundException("Change-Agent-State failed to find " + agentID);
			
			if(newState == Agent.AP_SUSPENDED) {
				a.doSuspend();
			}
			else if(newState == Agent.AP_WAITING) {
				a.doWait();
			}
			else if(newState == Agent.AP_ACTIVE) {
				int oldState = a.getState();
				if(oldState == Agent.AP_SUSPENDED) {
					a.doActivate();
				}
				else {
					a.doWake();
				}
			}
			
			myContainer.releaseLocalAgent(agentID);
		}
		
		private void bornAgent(AID name, ContainerID cid, JADEPrincipal principal, Credentials credentials) throws NameClashException, NotFoundException {
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
				try {
					// If the name is already in the GADT, throws NameClashException
					impl.bornAgent(name, cid, principal, ownership, false);
				}
				catch(NameClashException nce) {
					//#CUSTOMJ2SE_EXCLUDE_BEGIN
					try {
						ContainerID oldCid = impl.getContainerID(name);
						if (oldCid != null) {
							Node n = impl.getContainerNode(oldCid).getNode();
							
							// Perform a non-blocking ping to check...
							n.ping(false);
							
							// Ping succeeded: rethrow the NameClashException
							throw nce;
						}
						else {
							// The old agent is registered with the AMS, but does not live in the platform --> cannot check if it still exists
							throw nce;
						}
					}
					catch(NameClashException nce2) {
						// This is the re-thrown NameClashException --> let it through
						throw nce2;
					}
					catch(Exception e) {
						// Either the old agent disappeared in the meanwhile or the Ping failed: forcibly replace the old agent...
						impl.bornAgent(name, cid, principal, ownership, true);
					}
					//#CUSTOMJ2SE_EXCLUDE_END
					/*#CUSTOMJ2SE_INCLUDE_BEGIN
					 try {
					 //System.out.println("Replacing old agent "+name.getName());
					  if(myLogger.isLoggable(Logger.INFO))
					  myLogger.log(Logger.INFO,"Replacing old agent "+name.getName());
					  dyingAgents.add(name);
					  ((jade.core.AgentManager) impl).kill(name, principal, credentials);
					  waitUntilDead(name);
					  impl.bornAgent(name, cid, principal, ownership, false);
					  }
					  catch (Exception e) {
					  dyingAgents.remove(name);
					  impl.bornAgent(name, cid, principal, ownership, true);
					  }
					  #CUSTOMJ2SE_INCLUDE_END*/
				}
			}
		}
		
		/*#CUSTOMJ2SE_INCLUDE_BEGIN
		 private jade.util.leap.List dyingAgents = new jade.util.leap.ArrayList();
		 
		 private void waitUntilDead(AID id) {
		 synchronized (dyingAgents) {
		 while (dyingAgents.contains(id)) {
		 try {
		 dyingAgents.wait();
		 }
		 catch (Exception e) {}
		 }
		 }
		 }
		 
		 private void notifyDead(AID id) {
		 synchronized (dyingAgents) {
		 dyingAgents.remove(id);
		 dyingAgents.notifyAll();
		 }
		 }
		 #CUSTOMJ2SE_INCLUDE_END*/
		
		private void deadAgent(AID name) throws NotFoundException {
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				impl.deadAgent(name, false);
				/*#CUSTOMJ2SE_INCLUDE_BEGIN
				 notifyDead(name);
				 #CUSTOMJ2SE_INCLUDE_END*/
			}
		}
		
		private void suspendedAgent(AID name) throws NotFoundException {
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				impl.suspendedAgent(name);
			}
		}
		
		private void resumedAgent(AID name) throws NotFoundException {
			MainContainer impl = myContainer.getMain();
			if(impl != null) {
				impl.resumedAgent(name);
			}
		}
		
		private void exitContainer() {
			myContainer.shutDown();
		}

		
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
			return AgentManagementService.this;
		}
		
		public Node getNode() throws ServiceException {
			try {
				return AgentManagementService.this.getLocalNode();
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
					JADEPrincipal owner = (JADEPrincipal)params[3];
					Credentials initialCredentials = (Credentials)params[4];
					Boolean startIt = (Boolean) params[5];
					gCmd.addParam(agentID);
					gCmd.addParam(className);
					gCmd.addParam(arguments);
					gCmd.addParam(owner);
					gCmd.addParam(initialCredentials);
					gCmd.addParam(startIt);
					
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
					gCmd.addParam(agentID);
					gCmd.addParam(cid);
					
					JADEPrincipal owner = cmd.getPrincipal();
					if(myLogger.isLoggable(Logger.FINE)) {
						String ownerInfo = owner != null ? ", Owner = "+owner : "";
						myLogger.log(Logger.CONFIG,"Local slice processing H-command BORN_AGENT. Name is "+agentID.getName()+ownerInfo);
					}
					
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
					gCmd.addParam("*");
					
					result = gCmd;
				}
				else if(cmdName.equals(AgentManagementSlice.H_RESUMEDAGENT)) {
					GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_STATE_CHANGED, AgentManagementSlice.NAME, null);
					AID agentID = (AID)params[0];
					gCmd.addParam(agentID);
					gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE);
					gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED);
					
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
	
	
	
	private void initAgent(AID target, Agent instance, VerticalCommand vCmd) throws IMTPException, JADESecurityException, NameClashException, NotFoundException, ServiceException {
		//#J2ME_EXCLUDE_BEGIN
		// If the agent was loaded from a separate space, register it to the codeLocator 
		if (isLoadedFromSeparateSpace(instance)) {
			try {
				codeLocator.registerAgent(target, instance.getClass().getClassLoader());
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
		}
		//#J2ME_EXCLUDE_END
		
		// Connect the new instance to the local container
		Agent old = myContainer.addLocalAgent(target, instance);
		if (instance == old) {
			// This is a re-addition of an existing agent to a recovered main container (FaultRecoveryService)
			old = null;
		}
		
		try {
			// Notify the main container through its slice
			AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);
			
			// We propagate the class-name to the main, but we don't want to keep it in the actual agent AID. 
			AID cloned = (AID) target.clone();
			cloned.addUserDefinedSlot(AID.AGENT_CLASSNAME, instance.getClass().getName());
			try {
				mainSlice.bornAgent(cloned, myContainer.getID(), vCmd);
			}
			catch(IMTPException imtpe) {
				// Try to get a newer slice and repeat...
				mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
				mainSlice.bornAgent(cloned, myContainer.getID(), vCmd);
			}
			customize(instance);
		}
		catch(NameClashException nce) {
			removeLocalAgent(target);
			if(old != null) {
				myContainer.addLocalAgent(target, old);
			}
			throw nce;
		}
		catch(IMTPException imtpe) {
			removeLocalAgent(target);
			throw imtpe;
		}
		catch(NotFoundException nfe) {
			removeLocalAgent(target);
			throw nfe;
		}
		catch(JADESecurityException ae) {
			removeLocalAgent(target);
			throw ae;
		}
	}
	
	//#J2ME_EXCLUDE_BEGIN
	private boolean isLoadedFromSeparateSpace(Object obj) {
		try {
			Class c = obj.getClass();
			Class reloadedClass = Class.forName(c.getName(), true, getClass().getClassLoader());
			if (c == reloadedClass) {
				return false;
			}
		}
		catch (Throwable t) {
			// Just do nothing
		}
		return true;
	}
	//#J2ME_EXCLUDE_END
	
	private void customize(Agent agent) {
	}
	
	
	// The concrete agent container, providing access to LADT, etc.
	private AgentContainer myContainer;
	
	// The local slice for this service
	private final ServiceComponent localSlice = new ServiceComponent();
	
	// The command sink, source side
	private final CommandSourceSink senderSink = new CommandSourceSink();
	
	// The command sink, target side
	private final CommandTargetSink receiverSink = new CommandTargetSink();
	
	//#J2ME_EXCLUDE_BEGIN
	private String agentsPath = null;
	private CodeLocator codeLocator;
	//#J2ME_EXCLUDE_END
	
	// Work-around for PJAVA compilation
	protected Service.Slice getFreshSlice(String name) throws ServiceException {
		return super.getFreshSlice(name);
	}
}
