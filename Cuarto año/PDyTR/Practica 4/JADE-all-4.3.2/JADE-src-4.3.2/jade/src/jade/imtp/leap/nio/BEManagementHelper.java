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

package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.core.ServiceHelper;

/**
   Helper interface to the BEManagementService.
   By means of this helper an agent can retrieve a number of properties 
   associated to the split containers attached to the underlying 
   BEManagementService.<br>
   In particular the <code>FRONT_END_HOST</code> key maps to 
   the IP address of the device where the front end is running.
   
   @author Giovanni Caire - TILAB
 */
public interface BEManagementHelper extends ServiceHelper {
	/**
	   Key to be specified to retrieve the IP address of the device
	   the front end of a given split container is running.
	 */
	public static final String FRONT_END_HOST = "frontendhost";
	public static final String CONNECTED = "connected";
	
	/**
	   @return a property associated to a given split container attached
	   to the underlying BEManagementService.
	 */
	String getProperty(String containerName, String key);
}
