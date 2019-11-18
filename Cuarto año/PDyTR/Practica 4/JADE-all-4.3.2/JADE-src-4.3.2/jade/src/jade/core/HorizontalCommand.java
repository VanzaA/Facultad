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


import jade.util.leap.Serializable;


/**
   The <code>HorizontalCommand</code> interface represents those
   kernel-level commands that are exchanged among the slices of a
   given JADE service, and they are sent across the network by the
   platform IMTP.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public interface HorizontalCommand extends Command, Serializable {


    /**
       Access the service object this command belongs to. A command
       object belongs exactly to one single service.

       @return The name of a <code>Service</code> object, such that
       this command is one of the allowed commands for that service.
    */
    String getService();

    /**
       Query the interaction this command object is a part of. A
       command object is part of exactly one single interaction. Some
       services can generate interactions made by many commands, which
       can be grouped together because calling
       <code>getInteraction()</code> on them yields the same result.

       @return A <code>String</code> serving as a service-unique
       identifier for the interaction of this command object.
    */
    String getInteraction();

}
