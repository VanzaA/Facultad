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

package jade.domain.introspection;

import jade.core.ContainerID;

/**
   This class represents the <code>added-container</code> concept in the
   <code>jade-introspection</code> ontology.

   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2005-08-24 17:21:34 +0200 (mer, 24 ago 2005) $ $Revision: 5758 $
 */
public class AddedContainer implements Event {

    /**
       A string constant for the name of this event.
    */
    public static final String NAME = "Added-Container";

    private ContainerID container;
    private String ownership;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public AddedContainer() {
    }

    /**
       Retrieve the name of this event.
       @return A constant value for the event name.
    */
    public String getName() {
	return NAME;
    }

    /**
       Set the <code>container</code> of this event.
       @param id The container identifier of the newly added
       container.
    */
    public void setContainer(ContainerID id) {
	container = id;
    }

    /**
       Retrieve the value of the <code>container</code> slot of this
       event, containing the container identifier of the newly added
       container.
       @return The value of the <code>container</code> slot, or
       <code>null</code> if no value was set.
    */
    public ContainerID getContainer() {
	return container;
    }

    /**
       Set the <code>ownership</code> of this event.
       @param o The name of the entity owning the newly added
       container.
    */
    public void setOwnership(String o) {
	ownership = o;
    }

    /**
       Retrieve the value of the <code>ownership</code> slot of this
       event, containing the name of the entity owning the newly added
       container.
       @return The value of the <code>ownership</code> slot, or
       <code>null</code> if no value was set.
    */
    public String getOwnership() {
	return ownership;
    }

}
