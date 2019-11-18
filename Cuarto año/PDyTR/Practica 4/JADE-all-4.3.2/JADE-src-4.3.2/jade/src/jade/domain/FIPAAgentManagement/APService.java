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


package jade.domain.FIPAAgentManagement;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.content.Concept;

/**
   This class implements the concept of the fipa-agent-management ontology
   representing the description of a platform service. 
   @author Fabio Bellifemine - CSELT
   @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $

 */
public class APService implements Concept {

  private List addresses = new ArrayList(); 
  private String name;
  private String type;

    /**
       Default constructor. Necessary for ontological classes.
    */
    public APService() {
    }
  
    /**
     * Constructor. Create a new APService where name and type get the same value (i.e.
     * the passed type parameter).
     **/
    public APService(String type, String[] addresses) {
	name=type;
	this.type=type;
	for (int i=0; i<addresses.length; i++)
	    this.addresses.add(addresses[i]);
    }

    /**
       Set the <code>name</code> slot of this object.
       @param n The string for the platform service name.
    */
    public void setName(String n) {
	name = n;
    }

    /**
       Retrieve the <code>name</code> slot of this object.
       @return The value of the <code>name</code> slot of this
       platform service description, or <code>null</code> if no value
       was set.
    */
    public String getName() {
	return name;
    }

    /**
       Set the <code>type</code> slot of this object.
       @param t The string for the platform service type.
    */
    public void setType(String t) {
	type = t;
    }

    /**
       Retrieve the <code>type</code> slot of this object.
       @return The value of the <code>type</code> slot of this
       platform service description, or <code>null</code> if no value
       was set.
    */
    public String getType() {
	return type;
    }

    /**
       Add a service to the <code>addresses</code> slot collection
       of this object.
       @param a The address to add to the collection.
    */
    public void addAddresses(String address) {
	addresses.add(address);
    }

    /**
       Remove a service from the <code>addresses</code> slot
       collection of this object.
       @param a The address to remove from the collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
    */
    public boolean removeAddresses(String address) {
	return addresses.remove(address);
    }

    /**
       Remove all addresses from the <code>addresses</code> slot
       collection of this object.
    */
    public void clearAllAddresses() {
	addresses.clear();
    }

    /**
       Access all addresses from the <code>addresses</code> slot
       collection of this object.
       @return An iterator over the addresses collection.
    */
    public Iterator getAllAddresses() {
	return addresses.iterator();
    }

    /**
     * Retrieve a string representation for this platform service
     * description.
     * @return an SL0-like String representation of this object 
     **/
    public String toString() {
	StringBuffer str = new StringBuffer("( ap-service ");
        if ((name!=null)&&(name.length()>0))
	    str.append(" :name " + name);
        if ((type!=null)&&(type.length()>0))
	    str.append(" :type " + type);
        String s;
        str.append(" :addresses (sequence");
        for (Iterator i=addresses.iterator(); i.hasNext(); ) {
            s=(String)(i.next());
            str.append(' ');
            str.append(s);
        }
	str.append("))");
	return str.toString();
    }


}
