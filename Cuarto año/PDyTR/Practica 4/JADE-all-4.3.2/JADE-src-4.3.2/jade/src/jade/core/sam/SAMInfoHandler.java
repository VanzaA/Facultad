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

package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

import jade.core.Profile;

import java.util.Date;

/**
 * The interface to be implemented by classes that can be used to handle 
 * SAM information collected by the SAM Service.
 * The default implementation used by the SAM Service simply writes a csv file for each 
 * monitored entity and counter.
 * Developers can provide their own implementation to handle SAM information in an
 * application specific way (e.g. storing SAM information in a DB)
 */
public interface SAMInfoHandler {
	void initialize(Profile p) throws Exception;
	void shutdown();
	void handle(Date timeStamp, SAMInfo info);
}
