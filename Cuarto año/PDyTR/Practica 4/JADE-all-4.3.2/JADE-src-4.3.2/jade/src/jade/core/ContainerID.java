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

import jade.mtp.TransportAddress;

/**
   A class identifying an aget container in the JADE platform. This
   class is a simple descriptor of JADE containers, holding their name
   and a few other data about them.

   @author Giovanni Rimassa - Universita' di Parma
 */
public class ContainerID implements Location {

	/**
       String constant identifying the JADE default Internal Message
       Transport Protocol, connecting different containers within the
       same JADE platform.
	 */
	public static final String DEFAULT_IMTP ="JADE-IMTP"; 

	private String name;
	private String protocol = DEFAULT_IMTP;
	private String address = "<Unknown Host>";
	private String port;
	private Boolean main;


	/**
       The default costructor builds an uninitialized container ID.
	 */
	public ContainerID() {
	}

	/**
       Build a container ID with the given container name and
       transport address.
       @param n The name of the described container.
       @param a The network address of the node where the described
       container is deployed.
	 */
	public ContainerID(String n, TransportAddress a) {
		name = n;
		if(a != null){
			address = a.getHost();
			port = a.getPort();
			protocol = a.getProto();
		}
	}

	/**
       Set the name of the described container.
       @param n The name to give to the described container.
	 */
	public void setName(String n) {
		name = n;
	}

	/**
       Retrieve the name of the described container.
       @return The container name if one is set, or <code>null</code>
       otherwise.
	 */
	public String getName() {
		return name;
	}

	/**
       Set the IMTP protocol used to reach the described
       container.
       @param p The name of the chosen IMTP protocol.
	 */
	public void setProtocol(String p) {
		protocol = p;
	}

	/**
       Retrieves the IMTP protocol used to reach the described container.
       @return The name of the IMTP protocol used in the described container.
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
       Set the IP address (as a string) of the host, the described container is running on.
       @param a The string representation of the IP address of the host, the described container is running on.
	 */
	public void setAddress(String a) {
		address = a;
	}

	/**
       Retrieve the IP address of the host, the described container is running on.
       @return The string representation of the IP address of the host, the described container is running on.
	 */
	public String getAddress() {
		return address;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	/**
       Retrieve a string identifying the described container. The
       string is composed by the container name, the <i>@</i> symbol
       and the transport URL string.

       @return The ID string for the described container.
	 */
	public String getID() {
		return name + '@' + address;
	}

	/**
       Retrieve a string representation for this container ID. The
       string returned is the ID string of the described container.

       @return The string representation of this container ID.
	 */
	public String toString() {
		return getID();
	}

	/**
       Equality operation over container IDs. Two
       <code>ContainerID</code> objects are considered equal if and
       only if their name is the same (a case insensitive string
       comparison is used.

       @param obje The right hand side of the equality operation, the
       left hand side being the current object.
       @return If the <code>obj</code> parameter is an instance of
       <code>ContainerID</code> class and has the same name (case
       insensitively) as the current object, then <code>true</code> is
       returned. Otherwise, this method returns <code>false</code>.
	 */
	public boolean equals(Object obj) {
		try {
			ContainerID cid = (ContainerID) obj;
			return CaseInsensitiveString.equalsIgnoreCase(name, cid.getName());
		}
		catch (ClassCastException cce) {
			return false;
		}
	}

	/**
       Hash code operation, compliant with identity-by-name. This
       method returns an hash code for a container ID, so that two
       container IDs with the same name (case insensitively) have the
       same hash code.
	 */
	public int hashCode() {
		return name.toLowerCase().hashCode();
	}

	public void setMain(Boolean main) {
		this.main = main;

	}

	public Boolean getMain() {
		return main;
	}

}
