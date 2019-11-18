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

package jade.core.nodeMonitoring;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.BaseService;
import jade.core.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.NodeFailureMonitor;


/**
   Base service for services that implements ad hoc node monitoring
   mechanisms such as the UDPNodeMonitoringService
 */
public abstract class NodeMonitoringService extends BaseService {
	/**
	   Vertical command issued on the Main Container 
	   when a peripheral node becomes unreachable
	 */
	public static final String NODE_UNREACHABLE = "Node-Unreachable";
	/**
	   Vertical command issued on the Main Container 
	   when a peripheral node returns reachable after being unreachable for a while
	 */
	public static final String NODE_REACHABLE = "Node-Unreachable";
	
	private static final String[] OWNED_COMMANDS = new String[] {
		NODE_UNREACHABLE,
		NODE_REACHABLE
	};
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		
		if (ac.getMain() != null) {
			NodeFailureMonitor.init(this);
		}
	}
	
	public String[] getOwnedCommands() {
		return OWNED_COMMANDS;
	}
	
	public abstract NodeFailureMonitor getFailureMonitor(); 	
}