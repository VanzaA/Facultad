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


import jade.core.Node;
import jade.core.HorizontalCommand;
import jade.core.PlatformManager;
import jade.util.Logger;

/**
   This calss implements a skeleton to a local LEAP Node.
   @author Giovanni Rimassa - FRAMeTech s.r.l.
   @author Giovanni Caire - TILAB
 */
class NodeSkel extends Skeleton {

	private Node myNode;
	private Logger myLogger = Logger.getJADELogger(getClass().getName());

	public NodeSkel(Node n) {
		myNode = n;
	}

	public Command executeCommand(Command command) throws Throwable {

		switch (command.getCode()) {
		case Command.ACCEPT_COMMAND: {
			HorizontalCommand cmd = (HorizontalCommand)command.getParamAt(0);
			Object result = myNode.accept(cmd);

			if (result instanceof Throwable) {
				if (myLogger.isLoggable(Logger.FINE)) {
					// If FINE logging, print the complete stack trace
					myLogger.log(Logger.WARNING, "Error serving H-Command "+cmd.getService()+'/'+cmd.getName(), (Throwable) result);
				}
				else {
					myLogger.log(Logger.WARNING, "Error serving H-Command "+cmd.getService()+'/'+cmd.getName()+": "+result);
				}
			}

			command.reset(Command.OK);
			command.addParam(result);
			break;
		}
		case Command.PING_NODE_BLOCKING:
		case Command.PING_NODE_NONBLOCKING: {
			Boolean hang = (Boolean)command.getParamAt(0);
			Boolean result = new Boolean(myNode.ping(hang.booleanValue()));

			command.reset(Command.OK);
			command.addParam(result);
			break;
		} 
		case Command.EXIT_NODE: {
			myNode.exit();
			command.reset(Command.OK);
			break;
		} 
		case Command.INTERRUPT_NODE: {
			myNode.interrupt();
			command.reset(Command.OK);
			break;
		}
		case Command.PLATFORM_MANAGER_DEAD: {
			String deadPMAddress = (String) command.getParamAt(0);
			String notifyingPMAddress = (String) command.getParamAt(1);
			myNode.platformManagerDead(deadPMAddress, notifyingPMAddress);

			command.reset(Command.OK);
			break;
		}
		}

		return command;
	}

	public String toString() {
		return getClass().getName()+"["+myNode.getName()+"]";
	}
}
