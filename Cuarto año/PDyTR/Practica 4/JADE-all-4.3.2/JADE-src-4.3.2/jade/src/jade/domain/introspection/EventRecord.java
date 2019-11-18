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

//#APIDOC_EXCLUDE_FILE

import java.util.Date;

import jade.core.Location;
import jade.content.Concept;

/**
   This class represents the <code>event-record</code> concept in the
   <code>jade-introspection</code> ontology.

   @author Giovanni Rimassa - Universita' di Parma
   @version $Date: 2005-02-16 18:18:28 +0100 (mer, 16 feb 2005) $ $Revision: 5552 $
 */
public class EventRecord implements Concept {

    private Event what;
    private Date when;
    private Location where;

    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public EventRecord() {
    }

    /**
       Construct an event record, attaching a location and a timestamp
       to an event object.
       @param evt The occurred event.
       @param l The location where the event occurred.
    */
    public EventRecord(Event evt, Location l) {
	what = evt;
	when = new Date();
	where = l;
    }

    /**
       Set the <code>what</code> slot of this event.
       @param id The occurred event.
    */
    public void setWhat(Event evt) {
	what = evt;
    }

    /**
       Retrieve the value of the <code>what</code> slot of this
       event, containing the occurred event.
       @return The value of the <code>what</code> slot, or
       <code>null</code> if no value was set.
    */
    public Event getWhat() {
	return what;
    }

    /**
       Set the <code>when</code> slot of this event.
       @param d The time instant when the event occurred.
    */
    public void setWhen(Date d) {
	when = d;
    }

    /**
       Retrieve the value of the <code>when</code> slot of this event,
       containing the time instant when the event occurred.
       @return The value of the <code>when</code> slot, or
       <code>null</code> if no value was set.
    */
    public Date getWhen() {
	return when;
    }

    /**
       Set the <code>where</code> slot of this event.
       @param l The location where the event occurred.
    */
    public void setWhere(Location l) {
	where = l;
    }

    /**
       Retrieve the value of the <code>where</code> slot of this
       event, containing the location where the event occurred.
       @return The value of the <code>where</code> slot, or
       <code>null</code> if no value was set.
    */
    public Location getWhere() {
	return where;
    }

}
