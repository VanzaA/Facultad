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

package jade.imtp.rmi;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import java.rmi.Remote;
import java.rmi.RemoteException;

import jade.core.Service;
import jade.core.ServiceDescriptor;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.ServiceException;

import jade.security.JADESecurityException;

import java.util.Vector;

/**
   @author Giovanni Rimassa - FRAMeTech s.r.l
   @version $Date: 2010-04-08 15:54:18 +0200 (gio, 08 apr 2010) $ $Revision: 6298 $
 */
// Notice that this interface (and the method getPlatformName)
// are declared public just for the purpose of 
// the program jade.misc.JADEPlatformTest
public interface ServiceManagerRMI extends Remote {

	// Proper ServiceManager-like methods
	public String getPlatformName() throws RemoteException;
	String addNode(NodeDescriptor dsc, Vector nodeServices, boolean propagated) throws RemoteException, ServiceException, JADESecurityException;
	void removeNode(NodeDescriptor dsc, boolean propagated) throws RemoteException, ServiceException;
	void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated)  throws RemoteException, ServiceException;
	void removeSlice(String serviceKey, String sliceKey, boolean propagated)  throws RemoteException, ServiceException;
	void addReplica(String newAddr, boolean propagated)  throws RemoteException, ServiceException;
	void removeReplica(String address, boolean propagated)  throws RemoteException, ServiceException;

	Service.Slice findSlice(String serviceKey, String sliceKey) throws RemoteException, ServiceException;
	Vector findAllSlices(String serviceKey) throws RemoteException, ServiceException;

	void adopt(Node n, Node[] children) throws RemoteException;
	void ping() throws RemoteException;    
}
