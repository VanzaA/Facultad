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


import jade.util.leap.Properties;

/**
@author Giovanni Caire - TILAB
 */

public interface FEConnectionManager {
	/**
	   Set up a permanent connection to a remote BackEnd and return a stub of it.
	   @param fe The FrontEnd container that is requesting the 
	   connection setup.
	   @param props Additional (implementation dependent) connection 
	   configuration properties.
	   @return A stub of the remote BackEnd. 
	 */
	BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException;

	/**
	   Shut down the permanent connection to the remote BackEnd
	 */
	void shutdown();
}

