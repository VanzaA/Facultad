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

package jade.core.event;

//#J2ME_EXCLUDE_FILE


import jade.core.Node;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.SliceProxy;
import jade.core.ServiceException;
import jade.core.AID;
import jade.core.IMTPException;


/**
   The remote proxy for the JADE kernel-level service managing
   the event notification subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class NotificationProxy extends SliceProxy implements NotificationSlice {


	public void sniffOn(AID snifferName, AID targetName) throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_SNIFFON, NotificationSlice.NAME, null);
			cmd.addParam(snifferName);
			cmd.addParam(targetName);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void sniffOff(AID snifferName, AID targetName) throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_SNIFFOFF, NotificationSlice.NAME, null);
			cmd.addParam(snifferName);
			cmd.addParam(targetName);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void debugOn(AID introspectorName, AID targetName) throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_DEBUGON, NotificationSlice.NAME, null);
			cmd.addParam(introspectorName);
			cmd.addParam(targetName);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

	public void debugOff(AID introspectorName, AID targetName) throws IMTPException {
		try {
			GenericCommand cmd = new GenericCommand(H_DEBUGOFF, NotificationSlice.NAME, null);
			cmd.addParam(introspectorName);
			cmd.addParam(targetName);


			Node n = getNode();
			Object result = n.accept(cmd);
			if((result != null) && (result instanceof Throwable)) {
				if(result instanceof IMTPException) {
					throw (IMTPException)result;
				}
				else {
					throw new IMTPException("An undeclared exception was thrown", (Throwable)result);
				}
			}
		}
		catch(ServiceException se) {
			throw new IMTPException("Unable to access remote node", se);
		}
	}

}
