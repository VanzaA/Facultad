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

//#MIDP_EXCLUDE_FILE

import jade.core.PlatformManager;
import jade.core.NodeDescriptor;
import jade.core.Node;
import jade.core.Service;
import jade.core.ServiceDescriptor;

import java.util.Vector;

/**

   The <code>ServiceManagerSkel</code> class is the remote
   adapter for JADE platform <i>Service Manager</i> and
   <i>Service Finder</i> components, running over LEAP transport layer.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
class PlatformManagerSkel extends Skeleton {

    private PlatformManager impl;
    private LEAPIMTPManager manager;

    public PlatformManagerSkel(PlatformManager pm, LEAPIMTPManager mgr) {
			impl = pm;
			manager = mgr;
    }

    public Command executeCommand(Command command) throws Throwable {

			switch (command.getCode()) {
		
			case PlatformManagerStub.GET_PLATFORM_NAME: {
			    // Execute command...
			    String name = impl.getPlatformName();
		
			    command.reset(Command.OK);
			    command.addParam(name);
			    break;
			} 
		
			case PlatformManagerStub.ADD_NODE: {
			    NodeDescriptor desc = (NodeDescriptor)command.getParamAt(0);
			    Vector services = (Vector)command.getParamAt(1);
			    boolean propagated = ((Boolean)command.getParamAt(2)).booleanValue();
		
			    // Execute command...
			    String name = impl.addNode(desc, services, propagated);
		
			    command.reset(Command.OK);
			    command.addParam(name);
			    break;
			} 
		
			case PlatformManagerStub.REMOVE_NODE: {
			    NodeDescriptor desc = (NodeDescriptor)command.getParamAt(0);
			    boolean propagated = ((Boolean)command.getParamAt(1)).booleanValue();
		
			    // Execute command...
			    impl.removeNode(desc, propagated);
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.ADD_SLICE: {
			    ServiceDescriptor svc = (ServiceDescriptor)command.getParamAt(0);
			    NodeDescriptor desc = (NodeDescriptor)command.getParamAt(1);
			    boolean propagated = ((Boolean)command.getParamAt(2)).booleanValue();
		
			    // Execute command...
			    impl.addSlice(svc, desc, propagated);
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.REMOVE_SLICE: {
			    String serviceKey = (String)command.getParamAt(0);
			    String sliceKey = (String)command.getParamAt(1);
			    boolean propagated = ((Boolean)command.getParamAt(2)).booleanValue();
		
			    // Execute command...
			    impl.removeSlice(serviceKey, sliceKey, propagated);
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.ADD_REPLICA: {
			    String pmAddr = (String)command.getParamAt(0);
			    boolean propagated = ((Boolean)command.getParamAt(1)).booleanValue();
		
			    // Execute command...
			    impl.addReplica(pmAddr, propagated);
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.REMOVE_REPLICA: {
			    String address = (String)command.getParamAt(0);
			    boolean propagated = ((Boolean)command.getParamAt(1)).booleanValue();
		
			    // Execute command...
			    impl.removeReplica(address, propagated);
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.ADOPT: {
				Node node = (Node)command.getParamAt(0);
				Node[] children = (Node[]) command.getParamAt(1);
		
			    // Execute command...
			    impl.adopt(node, children);
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.PING: {
			    // Execute command...
			    impl.ping();
		
			    command.reset(Command.OK);
			    break;
			} 
		
			case PlatformManagerStub.FIND_SLICE: {
			    String serviceKey = (String)command.getParamAt(0);
			    String sliceKey = (String)command.getParamAt(1);
		
			    // Execute command...
			    Service.Slice slice = impl.findSlice(serviceKey, sliceKey);
		
			    command.reset(Command.OK);
			    command.addParam(slice);
			    break;
			} 
		
			case PlatformManagerStub.FIND_ALL_SLICES: {
			    String serviceKey = (String)command.getParamAt(0);
		
			    // Execute command...
			    Vector v = impl.findAllSlices(serviceKey);
		
			    command.reset(Command.OK);
			    command.addParam(v);
			    break;
			} 		
			}
		
			return command;
    }
}
