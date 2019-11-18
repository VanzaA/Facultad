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

package jade.core.replication;

import jade.core.Service;
import jade.core.IMTPException;


/**
   The horizontal interface for the JADE kernel-level service
   distributing the <i>Service Manager</i> address list throughout the
   platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface AddressNotificationSlice extends Service.Slice {

    // Constants for the names of the service vertical commands

    /**
       The name of this service.
    */
    static final String NAME = "jade.core.replication.AddressNotification";

    /**
       This command name represents the notification of the addition
       of a new address to the <i>Service Manager</i> of the platform.
    */
    //static final String SM_ADDRESS_ADDED = "SM-Address-Added";

    /**
       This command name represents the notification of the removal of
       an address from the <i>Service Manager</i> of the platform.
    */
    //static final String SM_ADDRESS_REMOVED = "SM-Address-Removed";



    // Constants for the names of horizontal commands associated to methods
    static final String H_ADDSERVICEMANAGERADDRESS = "1";
    //static final String H_REMOVESERVICEMANAGERADDRESS = "2";
    static final String H_GETSERVICEMANAGERADDRESS = "3";

    void addServiceManagerAddress(String addr) throws IMTPException;
    //void removeServiceManagerAddress(String addr) throws IMTPException;
    String getServiceManagerAddress() throws IMTPException;


}
