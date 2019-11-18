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
   The <code>Sink</code> interface has to be implemented by all the
   components that process JADE kernel-level commands in an exclusive
   and terminal way.

   For each kind of vertical command, there must be at most one sink
   that is registered for that command.  Instead, many command filters
   can be applied to a single command.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.Filter
*/
public interface Sink {

    static final boolean COMMAND_SOURCE = false;
    static final boolean COMMAND_TARGET = true;

    /**
       Definitely consume a command object. This method is invoked by
       the kernel when all incoming filters have been applied to the
       incoming vertical command.

       @param cmd A <code>VerticalCommand</code> describing what
       operation has been requested from previous layers (that can be
       the actual prime source of the command or members of the filter
       chain).
    */
    void consume(VerticalCommand cmd);

}
