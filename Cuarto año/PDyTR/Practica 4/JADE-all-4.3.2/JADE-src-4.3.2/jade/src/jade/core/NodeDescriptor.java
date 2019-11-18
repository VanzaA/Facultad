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


import jade.util.leap.Serializable;
import jade.security.JADEPrincipal;
import jade.security.Credentials;

/**

   The <code>NodeDescriptor</code> class serves as a meta-level
   description of a kernel-level service.
   Instances of this class contain a <code>Node</code> object,
   along with its name and properties, and are used in service
   management operations, as well as in agent-level introspection of
   platform-level entities.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.Node

*/
public class NodeDescriptor implements Serializable {

    /**
       Builds a new node descriptor, describing the given node with
       the given name and properties.

       @param nn The name of the described node.
       @param node The described <code>Node</code> object.
    */
    public NodeDescriptor(Node node) {
	myName = node.getName();
	myNode = node;
    }

    /**
       Builds a node descriptor for a node hosting an agent container.

       @param cid The container ID for the hosted container.
       @param node The described <code>Node</code> object.
       @param principal The principal of the node owner.
       @param credentials The credentials of the node owner.
    */
    public NodeDescriptor(ContainerID cid, Node node) {
	myName = cid.getName();
	myNode = node;
	myContainer = cid;
    }

    /**
       Builds an uninitialized node descriptor.

       @see jade.core.NodeDescriptor#setName(String sn)
       @see jade.core.NodeDescriptor#setNode(Node node)
    */
    public NodeDescriptor() {
    }

    /**
       Change the name (if any) of the described node.

       @param nn The name to assign to the described node.
    */
    public void setName(String nn) {
	myName = nn;
    }

    /**
       Retrieve the name (if any) of the described node.

       @return The name of the described node, or <code>null</code>
       if no name was set.
    */
    public String getName() {
	return myName;
    }

    /**
       Change the described node (if any).

       @param node The <code>Node</code> object that is to be
       described by this node descriptor.
    */
    public void setNode(Node node) {
	myNode = node;
    }

    /**
       Retrieve the described node.

       @return The <code>Node</code> object described by this
       node descriptor, or <code>null</code> if no node was set.
    */
    public Node getNode() {
	return myNode;
    }

    /**
       Retrieve the ID of the container (if any) hosted by the
       described node.

       @return The <code>ContainerID</code> of the hosted container,
       or <code>null</code> if no such container was set.
    */
    public ContainerID getContainer() {
	return myContainer;
    }

    public void setParentNode(Node n) {
    	parentNode = n;
    }
    
    public Node getParentNode() {
    	return parentNode;
    }
    
    /**
       Set the username of the owner of the described node
     */
    public void setUsername(String username) {
			this.username = username;
    }

    /**
       Retrieve the username of the owner of the described node
     */
    public String getUsername() {
			return username;
    }

    /**
       Set the password of the owner of the described node
     */
    public void setPassword(byte[] password) {
	this.password = password;
    }

    /**
       Retrieve the password of the owner of the described node
     */
    public byte[] getPassword() {
			return password;
    }
    
    /**
       Set the principal of the described node
     */
    public void setPrincipal(JADEPrincipal principal) {
			myPrincipal = principal;
    }

    /**
       Retrieve the principal of the described node
     */
    public JADEPrincipal getPrincipal() {
			return myPrincipal;
    }
    
    /**
       Set the principal of the owner of this node
     */
    public void setOwnerPrincipal(JADEPrincipal principal) {
			ownerPrincipal = principal;
    }

    /**
       Retrieve the principal of the owner of this node (if any)

       @return The principal of the owner of this node, or
       <code>null</code> if no principal was set.
    */
    public JADEPrincipal getOwnerPrincipal() {
			return ownerPrincipal;
    }

    /**
       Set the credentials of the owner of this node
     */
    public void setOwnerCredentials(Credentials credentials) {
			ownerCredentials = credentials;
    }
    
    /**
       Retrieve the credentials of the owner of this node (if any)

       @return The credentials of the owner of this node, or
       <code>null</code> if no credentials were set.
    */
    public Credentials getOwnerCredentials() {
			return ownerCredentials;
    }

    private String myName;
    private Node myNode;
    
    private Node parentNode;

    private ContainerID myContainer;
    private String username;
    private byte[] password;
    private JADEPrincipal myPrincipal;
    private JADEPrincipal ownerPrincipal;
    private Credentials ownerCredentials;

}
