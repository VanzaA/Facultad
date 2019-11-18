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


/**
   This interface is used to notify the JADE kernel of important
   events related to the platform nodes.
   <ul>
   <li>Node addition.</li>
   <li>Node removal.</li>
   <li>An existing node has become suddenly unreachable.</li>
   <li>An existing node has become reachable again.</li>
   </ul>

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.ServiceManagerImpl
*/
public interface NodeEventListener {

    /**
       This method is invoked when a new node has joined the platform.

       @param n The newly added platform node.
    */
    void nodeAdded(Node n);

    /**
       This method is invoked when a node has left the platform.

       @param n The node that has just left.
    */
    void nodeRemoved(Node n);

    /**
       This method is invoked when the local node discovers that a
       platform node is not reachable anymore. The discovery is due to
       some IMTP-specific network mechanism, and <em>does not</em>
       mean that the remote node has failed. A subsequent
       <code>nodeReachable()</code> call might be issued when the IMTP
       connection becomes working again.

       @param n The node that has become unreachable.
    */
    void nodeUnreachable(Node n);

    /**
       This method is invoked when the local node discovers that a
       platform node, that was previously unreachable, has become
       reachable again.

       @param n The node that has become reachable again.
    */
    void nodeReachable(Node n);

}
