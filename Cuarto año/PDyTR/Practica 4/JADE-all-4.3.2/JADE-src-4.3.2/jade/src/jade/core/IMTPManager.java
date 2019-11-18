/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

//#APIDOC_EXCLUDE_FILE


import jade.mtp.TransportAddress;
import jade.util.leap.List;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public interface IMTPManager {

    public static final String SERVICE_MANAGER_NAME = "ServiceManager";


    /**
     * Initialize this IMTPManager
     */
    void initialize(Profile p) throws IMTPException;

    /**
     * Release all resources of this IMTPManager
     */
    void shutDown();

    /**
       Access the node that represents the local JVM.

       @return A <code>Node</code> object, representing the local node
       of this platform.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    Node getLocalNode() throws IMTPException;

	  //#MIDP_EXCLUDE_BEGIN
    /**
       Makes the platform <i>Service Manager</i> available through
       this IMTP.
       @param mgr The <code>ServiceManager</code> implementation that
       is to be made available across the network.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    void exportPlatformManager(PlatformManager mgr) throws IMTPException;


    /**
       Stops making the platform <i>Service Manager</i> available
       through this IMTP.
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    void unexportPlatformManager(PlatformManager sm) throws IMTPException;
	  //#MIDP_EXCLUDE_END
    
    /**
       Retrieve a proxy to the PlatformManager specified in the local 
       Profile
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    PlatformManager getPlatformManagerProxy() throws IMTPException;
    
    /**
       Retrieve a proxy to the PlatformManager listening at a given address
       @throws IMTPException If something goes wrong in the underlying
       network transport.
    */
    PlatformManager getPlatformManagerProxy(String addr) throws IMTPException;

    /**
       Inform the local IMTPManager that this node is now connected to 
       the given PlatformManager
     */
    void reconnected(PlatformManager pm);
    
    /**
       Builds a proxy object for a remote service slice.

       @param itfs The array of all the interfaces that have to be
       implemented by the returned proxy. The first element of the
       array must be an interface derived from
       <code>Service.Slice</code>.
       @return A proxy object that can be safely casted to any of the
       interfaces in the <code>itfs</code> array.
       @throws IMTPException If something goes wrong in the underlying
       network transport.

       @see jade.core.Service
    */
    Service.Slice createSliceProxy(String serviceName, Class itf, Node where) throws IMTPException;

    /**
       Return the the List of TransportAddress where this IMTP is 
       waiting for intra-platform remote calls.
     */
    List getLocalAddresses() throws IMTPException;
    
    /**
     */
    TransportAddress stringToAddr(String addr) throws IMTPException;
}

