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

import java.rmi.RemoteException;

import jade.core.BaseNode;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;


/**
   This class wraps an RMI endpoint representing the local platform
   node. When this node is sent over the network, the RMI stub is
   transferred, too.

   @author Giovanni Rimassa - FRAMeTech s.r.l

 */
class NodeAdapter extends BaseNode {

	public NodeAdapter(String name, boolean hasSM, int port, RMIIMTPManager mgr) throws RemoteException {
		super(name, hasSM);
		try {
			adaptee = new NodeRMIImpl(this, port, mgr);
		}
		catch(Exception e) {
			adaptee = new NodeRMIImpl(this, 0, mgr);
		}
	}

	public Object accept(HorizontalCommand cmd) throws IMTPException {
		try {
			return adaptee.accept(cmd);
		}
		catch(RemoteException re) {
			throw new IMTPException("An RMI error occurred", re);
		}
	}

	public NodeRMI getRMIStub() {
		return adaptee;
	}

	public boolean ping(boolean hang) throws IMTPException {
		try {
			return adaptee.ping(hang);
		}
		catch(RemoteException re) {
			throw new IMTPException("RMI exception", re);
		}
	}

	public void exit() throws IMTPException {
		try {
			adaptee.exit();
		}
		catch(RemoteException re) {
			throw new IMTPException("RMI exception", re);
		}
	}

	public void interrupt() throws IMTPException {
		try {
			adaptee.interrupt();
		}
		catch(RemoteException re) {
			throw new IMTPException("RMI exception", re);
		}
	}

	public void platformManagerDead(String deadPmAddress, String notifyingPmAddr) throws IMTPException {
		if (myServiceManager != null) {
			// Local call
			super.platformManagerDead(deadPmAddress, notifyingPmAddr);
		}
		else {
			// Remote call
			try {
				adaptee.platformManagerDead(deadPmAddress, notifyingPmAddr);
			}
			catch(RemoteException re) {
				throw new IMTPException("RMI exception", re);
			}
		}
	}

	private NodeRMI adaptee;

}
