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
package jade.core.resource;

import jade.core.NotFoundException;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

/**
 * The ResourceManagementHelper provides methods that allows to access 
 * resources available on the main or other containers.
 */
public interface ResourceManagementHelper extends ServiceHelper {

	/**
	 * Name of the service
	 */
	public static final String SERVICE_NAME = "jade.core.resource.ResourceManagement";
	
	/**
	 * System property key to define the folder root of shared resources
	 */
	public static final String SHARED_RESOURCES_FOLDER_KEY = "shared-resources-folder";
	
	/**
	 * This constant represents the possible ways to fetch the resources
	 */
	public static final int CLASSPATH_RESOURCES = 1;
	public static final int FILE_SYSTEM_RESOURCES = 2;
	public static final int SHARED_RESOURCES = 3;
	public static final int ALL_RESOURCES = 4;
	
	/**
	 * Get remote resource
	 * @param name resource name
	 * @return resource as byte array
	 */
	byte[] getResource(String name) throws ServiceException, NotFoundException;
	
	/**
	 * Get remote resource
	 * @param name resource name
	 * @param fetchMode resource fetch mode
	 * @return resource as byte array
	 */
	byte[] getResource(String name, int fetchMode) throws ServiceException, NotFoundException;
	
	/**
	 * Get remote resource
	 * @param name resource name
	 * @param containerName name of container containing the resource
	 * @return resource as byte array
	 */
	byte[] getResource(String name, String containerName) throws ServiceException, NotFoundException;
	
	/**
	 * Get remote resource
	 * @param name resource name
	 * @param fetchMode resource fetch mode
	 * @param containerName name of container containing the resource
	 * @return resource as byte array
	 */
	byte[] getResource(String name, int fetchMode, String containerName) throws ServiceException, NotFoundException;
}
