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
import java.util.Date;
import jade.util.leap.Properties;

import jade.core.AID;
import jade.content.Concept;


/** 
 * This class models an envelope.
 * @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2009-08-26 08:56:09 +0200 (mer, 26 ago 2009) $ $Revision: 6183 $
 */
public class Envelope implements Concept, jade.util.leap.Serializable {

	private final static int EXPECTED_LIST_SIZE = 2;
	/**
  @serial
	 */
	private ArrayList to = new ArrayList(EXPECTED_LIST_SIZE);
	/**
  @serial
	 */
	private AID from;
	/**
  @serial
	 */
	private String comments;
	/**
  @serial
	 */
	private String aclRepresentation;
	/**
  @serial
	 */
	private Long payloadLength;
	/**
  @serial
	 */
	private String payloadEncoding;
	/**
  @serial
	 */
	private Date date;

	/**
  @serial
	 */
	private ArrayList intendedReceiver = new ArrayList(EXPECTED_LIST_SIZE);
	/**
  @serial
	 */
	private Properties transportBehaviour;

	/**
  @serial
	 */
	private ArrayList stamps = new ArrayList(EXPECTED_LIST_SIZE);

	/**
     @serial
	 */
	private ArrayList properties = new ArrayList(EXPECTED_LIST_SIZE);

	/**
	 * Default constructor. Initializes the payloadLength to -1.
	 **/
	public Envelope () {
		payloadLength = new Long(-1);
	}


	/**
       Add an agent identifier to the <code>to</code> slot collection
       of this object.
       @param id The agent identifier to add to the collection.
	 */
	public void addTo(AID id) {
		to.add(id);
	}

	/**
       Remove an agent identifier from the <code>to</code> slot
       collection of this object.
       @param id The agent identifierto remove from the collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
	 */
	public boolean removeTo(AID id) {
		return to.remove(id);
	}

	/**
       Remove all agent identifiers from the <code>to</code> slot
       collection of this object.
	 */
	public void clearAllTo() {
		to.clear();
	}

	/**
       Access all agent identifiers from the <code>to</code> slot
       collection of this object.
       @return An iterator over the agent identifiers collection.
	 */
	public Iterator getAllTo() {
		return to.iterator();
	}

	/**
       Set the <code>from</code> slot of this object.
       @param id The agent identifier for the envelope sender.
	 */
	public void setFrom(AID id) {
		from = id;
	}

	/**
       Retrieve the <code>from</code> slot of this object.
       @return The value of the <code>from</code> slot of this
       envelope, or <code>null</code> if no value was set.
	 */
	public AID getFrom() {
		return from;
	}

	/**
       Set the <code>comments</code> slot of this object.
       @param c The string for the envelope comments.
	 */
	public void setComments(String c) {
		comments = c;
	}

	/**
       Retrieve the <code>comments</code> slot of this object.
       @return The value of the <code>comments</code> slot of this
       envelope, or <code>null</code> if no value was set.
	 */
	public String getComments() {
		return comments;
	}

	/**
       Set the <code>acl-representation</code> slot of this object.
       @param r The string for the ACL representation.
	 */
	public void setAclRepresentation(String r) {
		aclRepresentation = r;
	}

	/**
       Retrieve the <code>acl-representation</code> slot of this
       object.
       @return The value of the <code>acl-representation</code> slot
       of this envelope, or <code>null</code> if no value was set.
	 */
	public String getAclRepresentation() {
		return aclRepresentation;
	}

	/**
       Set the <code>payload-length</code> slot of this object.
       @param l The payload length, in bytes.
	 */
	public void setPayloadLength(Long l) {
		payloadLength = l;
	}

	/**
       Retrieve the <code>payload-length</code> slot of this object.
       @return The value of the <code>payload-length</code> slot of
       this envelope, or <code>null</code> or a negative value if no value was set.
	 */
	public Long getPayloadLength() {
		return payloadLength;
	}

	/**
       Set the <code>payload-encoding</code> slot of this object.
       This slot can be used to specify a different charset than 
       the standard one (US-ASCII) in order for instance to support
       accentuated characters in the content slot of the ACL message
       (e.g. setPayloadEncoding("UTF-8")).
       @param e The string for the payload encoding.
	 */
	public void setPayloadEncoding(String e) {
		payloadEncoding = e;
	}

	/**
       Retrieve the <code>payload-encoding</code> slot of this object.
       @return The value of the <code>payload-encoding</code> slot of
       this envelope, or <code>null</code> if no value was set.
	 */
	public String getPayloadEncoding() {
		return payloadEncoding;
	}

	/**
       Set the <code>date</code> slot of this object.
       @param d The envelope date.
	 */
	public void setDate(Date d) {
		date = d;
	}

	/**
       Retrieve the <code>date</code> slot of this object.
       @return The value of the <code>date</code> slot of this
       envelope, or <code>null</code> if no value was set.
	 */
	public Date getDate() {
		return date;
	}

	/**
       Add an agent identifier to the <code>intended-receiver</code>
       slot collection of this object.
       @param id The agent identifier to add to the collection.
	 */
	public void addIntendedReceiver(AID id) {
		intendedReceiver.add(id);
	}

	/**
       Remove an agent identifier from the
       <code>intended-receiver</code> slot collection of this object.
       @param id The agent identifier to remove from the collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
	 */
	public boolean removeIntendedReceiver(AID id) {
		return intendedReceiver.remove(id);
	}

	/**
       Remove all agent identifiers from the
       <code>intended-receiver</code> slot collection of this object.
	 */
	public void clearAllIntendedReceiver() {
		intendedReceiver.clear();
	}

	/**
       Access all agent identifiers from the <code>intended
       receiver</code> slot collection of this object.
       @return An iterator over the agent identifiers collection.
	 */
	public Iterator getAllIntendedReceiver() {
		return intendedReceiver.iterator();
	}

	/**
       Set the <code>received</code> slot of this object.
       @param ro The received object for the <code>received</code>
       slot.
	 */
	public void setReceived(ReceivedObject ro) {
		addStamp(ro);
	}

	/**
       Retrieve the <code>received</code> slot of this object.
       @return The value of the <code>received</code> slot of this
       envelope, or <code>null</code> if no value was set.
	 */
	public ReceivedObject getReceived() {
		if(stamps.isEmpty())
			return null;
		else
			return (ReceivedObject)stamps.get(stamps.size() - 1);
	}

	/**
       Add a <code>received-object</code> stamp to this message
       envelope. This method is used by the ACC to add a new stamp to
       the envelope at every routing hop.
       @param ro The <code>received-object</code> to add.
	 */
	public void addStamp(ReceivedObject ro) {
		stamps.add(ro);
	}

	/**
       Access the list of all the stamps. The
       <code>received-object</code> stamps are sorted according to the
       routing path, from the oldest to the newest.
	 */
	public ReceivedObject[] getStamps() {
		ReceivedObject[] ret = new ReceivedObject[stamps.size()];
		int counter = 0;

		for(Iterator it = stamps.iterator(); it.hasNext(); )
			ret[counter++] = (ReceivedObject)it.next();

		return ret;
	}


	/**
       Add a property to the <code>properties</code> slot collection
       of this object.
       @param p The property to add to the collection.
	 */
	public void addProperties(Property p) {
		properties.add(p);
	}

	/**
       Remove a property from the <code>properties</code> slot
       collection of this object.
       @param p The property to remove from the collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
	 */
	public boolean removeProperties(Property p) {
		return properties.remove(p);
	}

	/**
       Remove all properties from the <code>properties</code> slot
       collection of this object.
	 */
	public void clearAllProperties(){
		properties.clear();
	}

	/**
       Access all properties from the <code>properties</code> slot
       collection of this object.
       @return An iterator over the properties collection.
	 */
	public Iterator getAllProperties() {
		return properties.iterator();
	}

	//#MIDP_EXCLUDE_BEGIN
	/**
	 * Retrieve a string representation for this platform description.
	 * @return an SL0-like String representation of this object 
	 **/
	public String toString() {
		String s = "(Envelope ";
		Iterator i = getAllTo();
		if (i.hasNext()) {
			s = s + " :to (sequence ";
			for (Iterator ii=i; ii.hasNext(); ) 
				s = s+" "+ii.next().toString();
			s = s + ") ";
		}
		if (getFrom() != null)
			s = s + " :from " + getFrom().toString();
		if (getComments() != null) 
			s = s + " :comments " + getComments(); 
		if (getAclRepresentation() != null) 
			s = s + " :acl-representation " + getAclRepresentation(); 
		if (getPayloadLength() != null) 
			s = s + " :payload-length " + getPayloadLength().toString(); 
		if (getPayloadEncoding() != null) 
			s = s + " :payload-encoding " + getPayloadEncoding();
		if (getDate() != null)
			s = s + " :date " + getDate().toString();
		i = getAllIntendedReceiver();
		if (i.hasNext()) {
			s = s + " :intended-receiver (sequence ";
			for (Iterator ii=i; ii.hasNext(); ) 
				s = s+" "+ ii.next().toString();
			s = s + ") ";
		}
		ReceivedObject[] ro = getStamps();
		if (ro.length > 0 ) {
			s = s + " :received-object (sequence ";
			for (int j=0; j<ro.length; j++) {
				if (ro[j] != null) {
					s = s + " "+ ro[j].toString(); 
				}
			}
			s = s + ") ";
		}
		if (properties.size() > 0) {
			s = s + " :properties (set";
			for (int j=0; j<properties.size(); j++) {
				Property p = (Property)properties.get(j);
				s = s + " " + p.getName() + " " + p.getValue();
			}
			s = s + ")";
		}
		return s+")";
	}
	//#MIDP_EXCLUDE_END

	//#APIDOC_EXCLUDE_BEGIN
	public Object clone(){
		Envelope env = new Envelope();

		// Deep clone
		env.to = new ArrayList(to.size());
		for(int i = 0; i < to.size(); i++) {
			AID id = (AID)to.get(i);
			env.to.add(id.clone());	
		}

		// Deep clone
		env.intendedReceiver = new ArrayList(intendedReceiver.size());
		for(int i = 0; i < intendedReceiver.size(); i++) {
			AID id = (AID)intendedReceiver.get(i);
			env.intendedReceiver.add(id.clone());
		}

		env.stamps = (ArrayList)stamps.clone();

		if (from != null) {
			env.from = (AID)from.clone();
		}
		env.comments = comments;
		env.aclRepresentation = aclRepresentation;
		env.payloadLength = payloadLength;
		env.payloadEncoding = payloadEncoding;
		env.date = date;
		env.transportBehaviour = transportBehaviour;
		
		// Deep clone. Particularly important when security is enabled as SecurityObject-s (that are stored as 
		// Envelope properties) are modified in the encryption process
		env.properties = new ArrayList(properties.size());
		for(int i = 0; i < properties.size(); i++) {
			Property p = (Property)properties.get(i);
			env.properties.add(p.clone());
		}
		
		return env;
	}
	//#APIDOC_EXCLUDE_END



	//#MIDP_EXCLUDE_BEGIN


	// For persistence service
	private void setTo(ArrayList al) {
		to = al;
	}

	// For persistence service
	private ArrayList getTo() {
		return to;
	}

	// For persistence service
	private void setIntendedReceivers(ArrayList al) {
		intendedReceiver = al;
	}

	// For persistence service
	private ArrayList getIntendedReceivers() {
		return intendedReceiver;
	}

	// For persistence service
	private void setProperties(ArrayList al) {
		properties = al;
	}

	// For persistence service
	private ArrayList getProperties() {
		return properties;
	}

	//#MIDP_EXCLUDE_END

}
