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

package jade.core;

//#APIDOC_EXCLUDE_FILE

import jade.security.JADESecurityException;

import java.util.Vector;

/**
 @author Giovanni Caire - TILAB
 */
public interface PlatformManager {
	static final String NO_NAME = "No-Name";
	static final String AUX_NODE_NAME = "Aux-Node";
	
	String getPlatformName() throws IMTPException;
	
	String getLocalAddress();	
	void setLocalAddress(String addr);
	
	/**
	 @param dsc The Descriptor of the new Node
	 @param services The services currently installed on the new Node
	 @param propagated Flag indicating whether the new-node event 
	 was a propagated event within the replication mechanism
	 */
	String addNode(NodeDescriptor dsc, Vector nodeServices, boolean propagated) throws IMTPException, ServiceException, JADESecurityException;
	void removeNode(NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException;
	void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated)  throws IMTPException, ServiceException;
	void removeSlice(String serviceKey, String sliceKey, boolean propagated)  throws IMTPException, ServiceException;
	void addReplica(String newAddr, boolean propagated)  throws IMTPException, ServiceException;
	void removeReplica(String address, boolean propagated)  throws IMTPException, ServiceException;
	
	Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException;
	Vector findAllSlices(String serviceKey) throws IMTPException, ServiceException;
	
	void adopt(Node n, Node[] children) throws IMTPException;
	void ping() throws IMTPException;
}
