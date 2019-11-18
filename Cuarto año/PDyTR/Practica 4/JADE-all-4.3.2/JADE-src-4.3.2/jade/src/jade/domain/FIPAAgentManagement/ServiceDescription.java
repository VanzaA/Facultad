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
import jade.util.leap.*;
import jade.content.Concept;

/** 
 * This class models a service data type.
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2006-12-14 17:26:48 +0100 (gio, 14 dic 2006) $ $Revision: 5916 $
 * 
 */
public class ServiceDescription implements Concept {

	private String name;
	private String type;
	private String ownership;
	private List interactionProtocols = new ArrayList();
	private List ontology = new ArrayList();
	private List language = new ArrayList();
	private List properties = new ArrayList();

	/**
       Default constructor. A default constructor is necessary for
       JADE ontological classes.
	 */
	public ServiceDescription() {
	}

	/**
       Set the <code>name</code> slot of this object.
       @param n The name of the described service.
	 */
	public void setName(String n) {
		name = n;
	}

	/**
       Retrieve the <code>name</code> slot of this object.
       @return The value of the <code>name</code> slot of this service
       description, or <code>null</code> if no value was set.
	 */
	public String getName() {
		return name;
	}

	/**
       Set the <code>type</code> slot of this object.
       @param t The type of the described service.
	 */
	public void setType(String t) {
		type = t;
	}

	/**
       Retrieve the <code>type</code> slot of this object.
       @return The value of the <code>type</code> slot of this service
       description, or <code>null</code> if no value was set.
	 */
	public String getType() {
		return type;
	}

	/**
       Add a protocol name to the <code>protocols</code> slot
       collection of this object.
       @param ip The protocol name to add to the collection.
	 */
	public void addProtocols(String ip) {
		interactionProtocols.add(ip);
	}

	/**
       Remove a protocol name from the <code>protocols</code> slot
       collection of this object.
       @param ip The protocol name to remove from the collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
	 */
	public boolean removeProtocols(String ip) {
		return interactionProtocols.remove(ip);
	}

	/**
       Remove all protocol names from the <code>protocols</code> slot
       collection of this object.
	 */
	public void clearAllProtocols() {
		interactionProtocols.clear();
	}

	/**
       Access all protocol names from the <code>protocols</code> slot
       collection of this object.
       @return An iterator over the protocol names collection.
	 */
	public Iterator getAllProtocols() {
		return interactionProtocols.iterator();
	}

	/**
       Add an ontology name to the <code>ontologies</code> slot
       collection of this object.
       @param o The ontology name to add to the collection.
	 */
	public void addOntologies(String o) {
		ontology.add(o);
	}

	/**
       Remove an ontology name from the <code>ontologies</code> slot
       collection of this object.
       @param o The ontology name to remove from the collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
	 */
	public boolean removeOntologies(String o) {
		return ontology.remove(o);
	}

	/**
       Remove all ontology names from the <code>ontologies</code> slot
       collection of this object.
	 */
	public void clearAllOntologies() {
		ontology.clear();
	}

	/**
       Access all ontology names from the <code>ontologies</code> slot
       collection of this object.
       @return An iterator over the ontology names collection.
	 */
	public Iterator getAllOntologies() {
		return ontology.iterator();
	}

	/**
       Add a content language name to the <code>languages</code> slot
       collection of this object.
       @param l The content language name to add to the collection.
	 */
	public void addLanguages(String l) {
		language.add(l);
	}

	/**
       Remove a content language name from the <code>languages</code>
       slot collection of this object.
       @param l The content language name to remove from the
       collection.
       @return A boolean, telling whether the element was present in
       the collection or not.
	 */
	public boolean removeLanguages(String l) {
		return language.remove(l);
	}

	/**
       Remove all content language names from the
       <code>languages</code> slot collection of this object.
	 */
	public void clearAllLanguages() {
		language.clear();
	}

	/**
       Access all content language names from the
       <code>languages</code> slot collection of this object.
       @return An iterator over the content language names collection.
	 */
	public Iterator getAllLanguages() {
		return language.iterator();
	}

	/**
       Set the <code>ownership</code> slot of this object.
       @param o The name of the entity owning the described service.
	 */
	public void setOwnership(String o) {
		ownership = o;
	}

	/**
       Retrieve the <code>ownership</code> slot of this object.
       @return The value of the <code>ownership</code> slot of this
       service description, or <code>null</code> if no value was set.
	 */
	public String getOwnership() {
		return ownership;
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

} 
