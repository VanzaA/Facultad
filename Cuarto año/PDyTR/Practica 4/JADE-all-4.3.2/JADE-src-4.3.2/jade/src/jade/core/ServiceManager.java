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


import java.util.Vector;

import jade.security.JADESecurityException;


/**

   The <code>ServiceManager</code> interface serves as an access point
   for kernel-level service management. From a service manager object
   one can add and remove a service, or perform service management
   activities such as service suspension, resumption or parameter
   tuning.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.Service

*/
public interface ServiceManager {

    /**
       Retrieves the platform name from the platform <i>Service Manager</i>.

       @return The name of the platform, that can be used to compose
       the GUID of the resident agents.
       @throws IMTPException If an underlying network error forbids to
       fetch the name from the remote end.
    */
    String getPlatformName() throws IMTPException;

    /**
       Adds an address to the address list of this <i>Service Manager</i>.
       @param addr The address to add, as a stringified URL.
       @throws IMTPException If an underlying network error occurs.
    */
    void addAddress(String addr) throws IMTPException;

    /**
       Removes an address to the address list of this <i>Service Manager</i>.
       @param addr The address to remove, as a stringified URL.
       @throws IMTPException If an underlying network error occurs.
    */
    void removeAddress(String addr) throws IMTPException;


    /**
       Access the address exported by this copy of the <i>Service
       Manager</i>.
       @return The locally exported address.
       @throws IMTPException If an underlying network error occurs.
    */
    String getLocalAddress() throws IMTPException;

    /**
       Adds a new node to the distributed platform. The node
       abstraction can correspond to an agent container, but also to a
       different kind of networked component (command proxy server,
       persistent repository, etc.) of the JADE platform.

       @param desc The description of the new node to add, containing
       the node identifier and other node properties.
       @param services An array of <code>ServiceDescriptor</code>
       objects, describing the various kernel-level services that are
       to be activated on the newly created node.

       @throws IMTPException If an underlying network error forbids to
       tell whether the requested operation was possible. In that
       case, the operation is not executed.
       @throws ServiceException If the requested operation couldn't be
       executed (or an execution attempt failed) due to some condition
       on the remote end.
    */
    void addNode(NodeDescriptor desc, ServiceDescriptor[] services) throws IMTPException, ServiceException, JADESecurityException;

    /**
       Removes a node from the distributed platform. The node
       abstraction can correspond to an agent container, but also to a
       different kind of networked component (command proxy server,
       persistent repository, etc.) of the JADE platform.

       @param desc The description of the node to remove. At the very
       least, the description must contain a node identifier used as a
       key in node findng operations.

       @throws IMTPException If an underlying network error forbids to
       tell whether the requested operation was possible. In that
       case, the operation is not executed.
       @throws ServiceException If the requested operation couldn't be
       executed (or an execution attempt failed) due to some condition
       on the remote end.
    */
    void removeNode(NodeDescriptor desc) throws IMTPException, ServiceException;

    /**
       Activates a new service on the local agent container. Depending
       on whether the service is already active on other containers,
       the result can be the actual federation of this container and
       other containers service slices.

       @param desc The <code>ServiceDescriptor</code> object
       specifying the <code>Service</code> object implementing the
       service, along with the name and properties of the service.

       @throws IMTPException If an underlying network error forbids to
       tell whether the requested operation was possible. In that
       case, the operation is not executed.
       @throws ServiceException If the requested operation couldn't be
       executed (or an execution attempt failed) due to some condition
       on the remote end.

       @see jade.core.ServiceFinder
    */
    void activateService(ServiceDescriptor desc) throws IMTPException, ServiceException;

    /**
       Deactivates a service on the local container. Depending on
       whether the service is enabled also on other containers, this
       method can cause the complete service deactivation on the
       platform or just the detchmnent of the service slice
       corresponding to this container.

       @param name The name of the service to be deactivated. 

       @throws IMTPException If an underlying network error forbids to
       tell whether the requested operation was possible. In that
       case, the operation is not executed.
       @throws ServiceException If the requested operation couldn't be
       executed (or an execution attempt failed) due to some condition
       on the remote end.

       @see jade.core.ServiceFinder
    */
    void deactivateService(String name) throws IMTPException, ServiceException;

    /**
     * Retrieve the list of locally installed services as a Vector of ServiceDescriptor objects
     * @return the list of locally installed services as a Vector of ServiceDescriptor objects
     */
    Vector getLocalServices(); 
}
