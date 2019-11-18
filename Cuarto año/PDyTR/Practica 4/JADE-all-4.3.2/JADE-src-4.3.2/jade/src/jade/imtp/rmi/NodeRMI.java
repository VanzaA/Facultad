/* ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.imtp.rmi;

//#J2ME_EXCLUDE_FILE

import java.rmi.*;

import jade.core.HorizontalCommand;
import jade.core.IMTPException;

/**

  This RMI interface allows to dispatch JADE kernel-level commands
  across the network.

  @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
interface NodeRMI extends Remote {

	/**
       Accepts a command to be processed (from the implementor point
       of view).

       @param cmd The command to be processed.
       @return The return value of the remote operation represented by
       this horizontal command.
       @throws RemoteException If a network problem occurs.
	 */
	Object accept(HorizontalCommand cmd) throws RemoteException, IMTPException;

	/**
       Check whether this node is reachable.

       @param hang A boolean flag. When <code>false</code>, the method
       returns immediately; when <code>true</code>, the method blocks
       and will return only when unblocked from the remote end
       (through regular return or exception.
       @return If the node is exiting, <code>true</code> is returned,
       and <code>false</code> otherwise.
       @throws RemoteException If a network problem occurs.
	 */
	boolean ping(boolean hang) throws RemoteException;

	/**
       Shut down this node.
       @throws RemoteException If a network problem occurs.
	 */
	void exit() throws RemoteException;

	/**
       Release blocked ping() calls on this node.
       @throws RemoteException If a network problem occurs.       
	 */
	void interrupt() throws RemoteException;

	void platformManagerDead(String deadPmAddress, String notifyingPmAddr) throws RemoteException, IMTPException;
}
