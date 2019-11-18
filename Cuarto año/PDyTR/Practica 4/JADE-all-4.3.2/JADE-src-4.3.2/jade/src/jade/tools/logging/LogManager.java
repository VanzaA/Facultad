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
package jade.tools.logging;

import jade.util.leap.List;

/**
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public interface LogManager {
	
	/**
	 * Returns a list of <code>LoggerInfo</code> objects each one describing a logger
	 * active in the local JVM.
	 * @return a List of LogInfo
	 */
	public List getAllLogInfo();
	
	public void setLogLevel(String name, int level);
	
	public void setFile(String name, String  fileHandler);
	
	/**
	 * Returns  a list of <code>LevelInfo</code> object each one describing a level valid for the selected logging system. 
	 * @return a list of <code>LevelInfo</code> object each one describing a level valid for the selected logging system.
	 */
	public List getLogLevels();
	
	/**
	 * Returns a user friendly name of the logging system
	 * @return user friendly name of the logging system
	 */
	public String getName();
	
}
