/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.domain;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.KBManagement.*;

/**
 * @author Elisabetta Cortese - TILab
 *
 */
public class DFMemKB extends MemKB {

	boolean entriesToDelete = false; // gets true if there's at least one entry to delete for the method clean

	/**
	 * Constructor
	 * @param maxResultLimit JADE internal limit for maximum number of search results
	 */
	public DFMemKB(int maxResultLimit){
		super(maxResultLimit);
		clean();
	}


	protected Object insert(Object name, Object fact) {
		DFAgentDescription desc = (DFAgentDescription)fact;
		if (desc.getLeaseTime() != null ) {
			entriesToDelete = true;
		}
		return super.insert(name, fact);
	}

	/**
	   Scan the facts and remove those whose lease time has expired.
	 */
	protected void clean(){

		if (entriesToDelete) { 
			ArrayList toBeRemoved = new ArrayList();
			Iterator iter = facts.values().iterator();		
			while(iter.hasNext()){
				DFAgentDescription dfd = (DFAgentDescription) iter.next();
				if(dfd.checkLeaseTimeExpired()) {
					toBeRemoved.add(dfd.getName());
				}
			}
			iter = toBeRemoved.iterator();
			while (iter.hasNext()) {
				facts.remove((AID) iter.next());
			}
		}
	}

	// match
	public final boolean match(Object template, Object fact) {
		return compare(template, fact);
	}

	public static final boolean compare(Object template, Object fact) {

		try {
			DFAgentDescription templateDesc = (DFAgentDescription)template;
			DFAgentDescription factDesc = (DFAgentDescription)fact;
			// We must not return facts whose lease time has expired (no 
			// matter if they match)
			if(factDesc.checkLeaseTimeExpired())
				return false;

			// Match name
			AID id1 = templateDesc.getName();
			if(id1 != null) {
				AID id2 = factDesc.getName();
				if((id2 == null) || (!matchAID(id1, id2)))
					return false;
			}

			// Match protocol set
			Iterator itTemplate = templateDesc.getAllProtocols();
			while(itTemplate.hasNext()) {
				String templateProto = (String)itTemplate.next();
				boolean found = false;
				Iterator itFact = factDesc.getAllProtocols();
				while(!found && itFact.hasNext()) {
					String factProto = (String)itFact.next();
					found = templateProto.equalsIgnoreCase(factProto);
				}
				if(!found)
					return false;
			}

			// Match ontologies set
			itTemplate = templateDesc.getAllOntologies();
			while(itTemplate.hasNext()) {
				String templateOnto = (String)itTemplate.next();
				boolean found = false;
				Iterator itFact = factDesc.getAllOntologies();
				while(!found && itFact.hasNext()) {
					String factOnto = (String)itFact.next();
					found = templateOnto.equalsIgnoreCase(factOnto);
				}
				if(!found)
					return false;
			}

			// Match languages set
			itTemplate = templateDesc.getAllLanguages();
			while(itTemplate.hasNext()) {
				String templateLang = (String)itTemplate.next();
				boolean found = false;
				Iterator itFact = factDesc.getAllLanguages();
				while(!found && itFact.hasNext()) {
					String factLang = (String)itFact.next();
					found = templateLang.equalsIgnoreCase(factLang);
				}
				if(!found)
					return false;
			}

			// Match services set
			itTemplate = templateDesc.getAllServices();
			while(itTemplate.hasNext()) {
				ServiceDescription templateSvc = (ServiceDescription)itTemplate.next();
				boolean found = false;
				Iterator itFact = factDesc.getAllServices();
				while(!found && itFact.hasNext()) {
					ServiceDescription factSvc = (ServiceDescription)itFact.next();
					found = compareServiceDesc(templateSvc, factSvc);
				}
				if(!found)
					return false;
			}

			return true;
		}
		catch(ClassCastException cce) {
			return false;
		}
	}

	// Helper method to compare two Service Description objects
	public static final boolean compareServiceDesc(ServiceDescription template, ServiceDescription fact) {

		// Match name
		String n1 = template.getName();
		if(n1 != null) {
			String n2 = fact.getName();
			if((n2 == null) || (!n1.equalsIgnoreCase(n2)))
				return false;
		}

		// Match type
		String t1 = template.getType();
		if(t1 != null) {
			String t2 = fact.getType();
			if((t2 == null) || (!t1.equalsIgnoreCase(t2)))
				return false;
		}

		// Match ownership
		String o1 = template.getOwnership();
		if(o1 != null) {
			String o2 = fact.getOwnership();
			if((o2 == null) || (!o1.equalsIgnoreCase(o2)))
				return false;
		}

		// Match ontologies set
		Iterator itTemplate = template.getAllOntologies();
		while(itTemplate.hasNext()) {
			String templateOnto = (String)itTemplate.next();
			boolean found = false;
			Iterator itFact = fact.getAllOntologies();
			while(!found && itFact.hasNext()) {
				String factOnto = (String)itFact.next();
				found = templateOnto.equalsIgnoreCase(factOnto);
			}
			if(!found)
				return false;
		}

		// Match languages set
		itTemplate = template.getAllLanguages();
		while(itTemplate.hasNext()) {
			String templateLang = (String)itTemplate.next();
			boolean found = false;
			Iterator itFact = fact.getAllLanguages();
			while(!found && itFact.hasNext()) {
				String factLang = (String)itFact.next();
				found = templateLang.equalsIgnoreCase(factLang);
			}
			if(!found)
				return false;
		}

		// Match protocols set
		itTemplate = template.getAllProtocols();
		while(itTemplate.hasNext()) {
			String templateProto = (String)itTemplate.next();
			boolean found = false;
			Iterator itFact = fact.getAllProtocols();
			while(!found && itFact.hasNext()) {
				String factProto = (String)itFact.next();
				found = templateProto.equalsIgnoreCase(factProto);
			}
			if(!found)
				return false;
		}

		// Match properties set
		itTemplate = template.getAllProperties();
		while(itTemplate.hasNext()) {
			Property templateProp = (Property)itTemplate.next();
			boolean found = false;
			Iterator itFact = fact.getAllProperties();
			while(!found && itFact.hasNext()) {
				Property factProp = (Property)itFact.next();
				found = templateProp.match(factProp);
				/*if (templateProp.getName().equals(factProp.getName())) {
					// The property name matches. Check the value
					Object templateValue = templateProp.getValue(); 
					if (templateValue == null) {
						found = true;
					}
					else if (templateValue instanceof String) {
						found = ((String)templateValue).equalsIgnoreCase(factProp.getValue().toString());
					}
					else {
						found = templateValue.equals(factProp.getValue());
					}
				}*/
			}
			if(!found)
				return false;
		}

		return true;
	}
}
