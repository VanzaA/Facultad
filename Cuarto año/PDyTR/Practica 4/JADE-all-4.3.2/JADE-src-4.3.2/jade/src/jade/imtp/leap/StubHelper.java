/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.imtp.leap;

import jade.core.IMTPException;
import jade.core.UnreachableException;
import jade.mtp.TransportAddress;
import jade.util.leap.List;

/**
 * @author Giovanni Caire - Telecom Italia LAB
 */
interface StubHelper {
  /**
   * Build a stub for an already remotized object.
   * 
   * @param remoteObject the remote object the stub depends on.
   * @return a new stub depending on the specified remote object.
   * @throws IMTPException if the stub cannot be created.
   */
	Stub buildLocalStub(Object obj) throws IMTPException;
	
  /**
   * This method dispatches the specified command to the first address
   * (among those specified) to which dispatching succeeds.
   * 
   * @param destTAs a list of transport addresses where the command
   * dispatcher should try to dispatch the command.
   * @param command the command that is to be dispatched.
   * @return a response command from the receiving container.
   * @throws DispatcherException if an error occurs during dispatching.
   * @throws UnreachableException if none of the destination addresses
   * is reachable.
   */
  public Command dispatchCommand(List destTAs, 
                                 Command command) throws DispatcherException, UnreachableException;
	
}

