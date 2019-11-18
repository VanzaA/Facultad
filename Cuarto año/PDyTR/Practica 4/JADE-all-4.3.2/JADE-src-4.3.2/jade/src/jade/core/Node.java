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
   This interface represents a node of a JADE platform (i.e. a
   component that can host a slice of a platform-level service).
   Concrete nodes for a platform are created by the concrete IMTP
   manager.

   @see jade.core.IMTPManager

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface Node extends Serializable {

    void setName(String name);
    String getName();
    
    boolean hasPlatformManager();

    void exportSlice(String serviceName, Service.Slice localSlice);
    void unexportSlice(String serviceName);

    /**
       Accepts a command. If this node is a proxy, the
       <code>accept()</code> method is a remote method, forwarding the
       command to the remote location it represents.
       @param cmd The horizontal command to process.
       @return The object that is the result of processing the command.
       @throws IMTPException If a communication error occurs while
       contacting the remote node.
    */
    Object accept(HorizontalCommand cmd) throws IMTPException;

    /**
       Performs a ping operation on this node, to check whether it is
       still alive.

       @param hang If <code>true</code>, the call hangs until the node
       exits or is interrupted.
       @return If the node is currently terminating, <code>true</code>
       is returned, else <code>false</code>
    */
    boolean ping(boolean hang) throws IMTPException;

    void interrupt() throws IMTPException;
    void exit() throws IMTPException;
    void platformManagerDead(String deadPmAddress, String notifyingPmAddr) throws IMTPException;
}
