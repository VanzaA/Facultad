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

package jade.core.faultRecovery;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.Profile;
import java.util.Map;

/**
   An implementation of this interface is used by the FaultRecoveryService 
   to save the information required to recover a platform after a fault
   of the Main Container.
   
   @author Giovanni Caire - TILAB
 */
public interface PersistentStorage {
	/** Initialize this persistent storage */
	void init(Profile p) throws Exception;
	/** Close this persistent storage performing all necessary clean-up operations */
	void close();
	/** Clear all information stored in this persistent storage */
	void clear(boolean clearPlatform) throws Exception;
	
	/** Store the Main Container local address */
	void storePlatformInfo(String platformName, String address) throws Exception;
	/** Retrieve the Platform Information i.e. Platform name and Main Ccntainer local address */
	String[] getPlatformInfo() throws Exception;
	
	/** Store a (possibly child) node */
	void storeNode(String name, boolean isChild, byte[] nn) throws Exception;
	/** Remove a (possibly child) reachable node */
	void removeNode(String name) throws Exception;
	/** Mark a previously stored node as unreachable */
	void setUnreachable(String name) throws Exception;
	/** Mark a previously stored node as reachable again */
	void resetUnreachable(String name) throws Exception;
	/** Retrieve all stored reachable nodes */
	Map getAllNodes(boolean children) throws Exception;
	/** Retrieve a stored unreachable node */
	byte[] getUnreachableNode(String name) throws Exception;

	/*void storeAgent(String name, byte[] aa) throws Exception;
	void removeAgent(String name) throws Exception;
	Map getAllAgents() throws Exception;*/
}