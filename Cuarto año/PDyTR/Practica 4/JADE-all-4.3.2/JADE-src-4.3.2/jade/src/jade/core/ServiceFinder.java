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


/**

   The <code>ServiceFinder</code> interface serves as an access point
   for kernel-level service discovery. From a service finder object
   one can look up a given service by name, or can directly ask for a
   slice of the given service.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.Service

*/
public interface ServiceFinder {

    static final String MAIN_SLICE = "$$$Main-Slice$$$";
    static final String THIS_SLICE = "$$$This-Slice$$$";

    /**
       Looks up a platform service by name.

       @param key The name of the service. Concrete syntax for service
       names is left up to concrete services.
    */
    Service findService(String key) throws IMTPException, ServiceException;

    /**
       Looks up a specific service slice by name.

       @param key A structured name identifying both a service and a
       slice within it. Concrete syntax for service names is left up
       to concrete services.
    */
    Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException;

    /**
       Retrieves all the slices of a service currently active on this platform.
       @param serviceKey The name of the service. Concrete syntax for
       service names is left up to concrete services.
       @return An array of <code>Service.Slice</code> objects,
       containing all the slices of the requested service.
    */
    Service.Slice[] findAllSlices(String serviceKey) throws IMTPException, ServiceException;

}
