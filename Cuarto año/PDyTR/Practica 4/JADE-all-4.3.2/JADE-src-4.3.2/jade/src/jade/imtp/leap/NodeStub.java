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

package jade.imtp.leap;

import jade.core.*;
import jade.util.Logger;

/**
 This calss implements a stub to a remote LEAP Node.
 @author Giovanni Caire - TILAB
 @author Giovanni Rimassa - FRAMeTech s.r.l
 */
class NodeStub extends Stub implements Node {
	private String name;
	private boolean hasPM = false;
	
	public NodeStub() {
		super();
	}
	
	public NodeStub(int id, String platformName) {
		super(id, platformName);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasPlatformManager() {
		return hasPM;
	}
	
	void setPlatformManager(boolean b) {
		hasPM = b;
	}
	
	public void exportSlice(String serviceName, Service.Slice localSlice) {
		throw new RuntimeException("Trying to export a slice on a node stub");
	}
	
	public void unexportSlice(String serviceName) {
	}
	
	/**
	 Accepts a command to be forwarded to the remote node.
	 @param cmd The horizontal command to process.
	 @return The object that is the result of processing the command.
	 @throws IMTPException If a communication error occurs while
	 contacting the remote node.
	 */
	public Object accept(HorizontalCommand cmd) throws IMTPException {
		try {
			if (myLogger.isLoggable(Logger.FINER)) {
				myLogger.log(Logger.FINER, "Sending HC "+cmd.getName()+" to remote node "+name+" at addresses "+remoteTAs.toString());
			}
			Command leapCmd = new Command(Command.ACCEPT_COMMAND, remoteID);
			leapCmd.addParam(cmd);
			Command result = theDispatcher.dispatchCommand(remoteTAs, leapCmd);
			
			// Check whether an exception occurred in the remote container
			checkResult(result, new String[] { });
			
			return result.getParamAt(0);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	/**
	 Performs a ping operation on the remote node.
	 @param hang If <code>true</code>, the call hangs until the node
	 exits or is interrupted.
	 @return If the node is currently terminating, <code>true</code>
	 is returned, else <code>false</code>
	 */
	public boolean ping(boolean hang) throws IMTPException {
		Command cmd;
		if(hang) {
			cmd = new Command(Command.PING_NODE_BLOCKING, remoteID, true);
		}
		else {
			cmd = new Command(Command.PING_NODE_NONBLOCKING, remoteID, true);
		}
		cmd.addParam(new Boolean(hang));
		
		try {
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Pinging remote node "+name+" at addresses "+remoteTAs.toString());
			}
			
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			checkResult(result, new String[] { });
			
			Boolean b = (Boolean)result.getParamAt(0);
			if (myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE, "Ping to remote node "+name+" returned: "+b.booleanValue());
			}
			return b.booleanValue();
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void interrupt() throws IMTPException {
		Command cmd = new Command(Command.INTERRUPT_NODE, remoteID);
		
		try {
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			checkResult(result, new String[] { });
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void exit() throws IMTPException {
		Command cmd = new Command(Command.EXIT_NODE, remoteID);
		
		try {
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			checkResult(result, new String[] { });
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void platformManagerDead(String deadPMAddress, String notifyingPMAddress) throws IMTPException {
		Command cmd = new Command(Command.PLATFORM_MANAGER_DEAD, remoteID, true);
		cmd.addParam(deadPMAddress);
		cmd.addParam(notifyingPMAddress);
		
		try {
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			checkResult(result, new String[] { });
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public String toString() {
		String address = (remoteTAs != null && remoteTAs.size() > 0 ? remoteTAs.get(0) : "null").toString();
		return "["+name+", "+remoteID+", "+address+"]";
	}
}
