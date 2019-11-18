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

package jade.wrapper;

//#APIDOC_EXCLUDE_FILE

import jade.core.AID;
import jade.core.Location;
import jade.mtp.MTPDescriptor;

/**
   This interface is used by an AgentController or ContainerController
   to request action to the underlying implementation as if they
   where received from the main container.
   <br>
   <b>NOT available in MIDP</b>
   <br>
   @author Giovanni Caire - TILAB
 */
public interface ContainerProxy {
	void createAgent(AID id, String className, Object[] args) throws Throwable;	
	void killContainer() throws Throwable;
	MTPDescriptor installMTP(String address, String className) throws Throwable;
	void uninstallMTP(String address) throws Throwable;
	void suspendAgent(AID id) throws Throwable;
	void activateAgent(AID id) throws Throwable;
	void killAgent(AID id) throws Throwable;
	void moveAgent(AID id, Location where) throws Throwable;
	void cloneAgent(AID id, Location where, String newName) throws Throwable;
}
