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

import jade.security.JADESecurityException;
import java.util.Vector;


/**
 The <code>ServiceManagerStub</code> class is the remote
 proxy of JADE platform <i>Service Manager</i> and
 <i>Service Finder</i> components, running over LEAP transport layer.
 
 @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
class PlatformManagerStub extends Stub implements PlatformManager {
	
	static final int GET_PLATFORM_NAME = 1;
	static final int ADD_NODE = 2;
	static final int REMOVE_NODE = 3;
	static final int ADD_SLICE = 4;
	static final int REMOVE_SLICE = 5;
	static final int ADD_REPLICA = 6;
	static final int REMOVE_REPLICA = 7;
	static final int FIND_SLICE = 8;
	static final int FIND_ALL_SLICES = 9;
	static final int ADOPT = 10;
	static final int PING = 11;
	
	private static final String AUTH_EXCEPTION = "jade.security.JADESecurityException";
	private static final String SERVICE_EXCEPTION = "jade.core.ServiceException";
	
	protected PlatformManagerStub() {
		this(null); 
	}
	
	protected PlatformManagerStub(String platformName) {
		super(0, platformName); // The ID of a PlatformManager is always 0
	}
	
	
	public String getPlatformName() throws IMTPException {
		Command cmd = new Command(GET_PLATFORM_NAME, remoteID);
		
		try {
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			checkResult(result, null);
			
			return (String)result.getParamAt(0);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public String getLocalAddress() {
		if (remoteTAs.size() > 0) {
			return remoteTAs.get(0).toString(); 
		}
		else {
			return null;
		}
	}
	
	public void setLocalAddress(String addr) {
		// Should never be called
	}
	
	public String addNode(NodeDescriptor desc, Vector services, boolean propagated) throws IMTPException, ServiceException, JADESecurityException {
		try {		
			Command cmd = new Command(ADD_NODE, remoteID);
			
			cmd.addParam(desc);
			cmd.addParam(services);
			cmd.addParam(new Boolean(propagated));
			
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			switch (checkResult(result, new String[] {AUTH_EXCEPTION, SERVICE_EXCEPTION})) {
			case 1:
				throw new JADESecurityException((String) result.getParamAt(1));
			case 2:
				throw new ServiceException((String) result.getParamAt(1));
			}
			
			return (String)result.getParamAt(0);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void removeNode(NodeDescriptor desc, boolean propagate) throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(REMOVE_NODE, remoteID);
			cmd.addParam(desc);
			cmd.addParam(new Boolean(propagate));
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated)  throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(ADD_SLICE, remoteID);
			cmd.addParam(service);
			cmd.addParam(dsc);
			cmd.addParam(new Boolean(propagated));
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void removeSlice(String serviceKey, String sliceKey, boolean propagated)  throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(REMOVE_SLICE, remoteID);
			cmd.addParam(serviceKey);
			cmd.addParam(sliceKey);
			cmd.addParam(new Boolean(propagated));
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	
	public void addReplica(String newAddr, boolean propagated)  throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(ADD_REPLICA, remoteID);
			cmd.addParam(newAddr);
			cmd.addParam(new Boolean(propagated));
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void removeReplica(String address, boolean propagated)  throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(REMOVE_REPLICA, remoteID);
			cmd.addParam(address);
			cmd.addParam(new Boolean(propagated));
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void adopt(Node n, Node[] children) throws IMTPException {
		try {
			Command cmd = new Command(ADOPT, remoteID);
			cmd.addParam(n);
			cmd.addParam(children);
			
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			checkResult(result, null);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public void ping() throws IMTPException {
		try {
			Command cmd = new Command(PING, remoteID, true);
			
			Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			checkResult(result, null);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(FIND_SLICE, remoteID);
			cmd.addParam(serviceKey);
			cmd.addParam(sliceKey);
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
			
			return (Service.Slice) res.getParamAt(0);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}
	
	public Vector findAllSlices(String serviceKey) throws IMTPException, ServiceException {
		try {
			Command cmd = new Command(FIND_ALL_SLICES, remoteID);
			cmd.addParam(serviceKey);
			Command res = theDispatcher.dispatchCommand(remoteTAs, cmd);
			
			// Check whether an exception occurred in the remote container
			if (checkResult(res, new String[] {SERVICE_EXCEPTION}) > 0) {
				throw new ServiceException((String) res.getParamAt(1));
			}			    	
			
			return (Vector) res.getParamAt(0);
		}
		catch (DispatcherException de) {
			throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
			throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
	}    
}
