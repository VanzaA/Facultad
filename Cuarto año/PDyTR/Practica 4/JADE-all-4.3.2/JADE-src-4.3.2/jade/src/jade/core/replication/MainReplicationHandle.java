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

import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.ServiceFinder;
import jade.core.ServiceNotActiveException;
import jade.util.Logger;

/**
 * This class allows other services to exploit the MainReplicationService to keep 
 * local information in synch among slices on replicated Main Containers 
 */
public class MainReplicationHandle {
	//#J2ME_EXCLUDE_BEGIN
	private String myService;
	private MainReplicationService replicationService;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	//#J2ME_EXCLUDE_END
	
	public MainReplicationHandle(Service svc, ServiceFinder sf) {
		//#J2ME_EXCLUDE_BEGIN
		myService = svc.getName();
		try {
			replicationService = (MainReplicationService) sf.findService(MainReplicationSlice.NAME);
		}
		catch (ServiceNotActiveException snat) {
			// MainReplicationService not active --> just do nothing
		}
		catch (Exception e) {
			// Should never happen
			myLogger.log(Logger.WARNING, "Error accessing the local MainReplicationService.", e);
		}
		//#J2ME_EXCLUDE_END
	}
	
	public void invokeReplicatedMethod(String methodName, Object[] params) {
		//#J2ME_EXCLUDE_BEGIN
		if (replicationService != null) {
			GenericCommand cmd = new GenericCommand(MainReplicationSlice.H_INVOKESERVICEMETHOD, MainReplicationSlice.NAME, null);
			cmd.addParam(myService);
			cmd.addParam(methodName);
			cmd.addParam(params);
			try {
				replicationService.broadcastToReplicas(cmd, false);
			}
			catch (Exception e) {
				// Should never happen as real exceptions are logged inside broadcastToReplicas()
				myLogger.log(Logger.WARNING, "Error propagating H-command " + cmd.getName() +" to replicas. Method to invoke on replicas was "+methodName, e);
			}
		}
		//#J2ME_EXCLUDE_END
	}

}
