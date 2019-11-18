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

// Created: Jun 7, 2004

package jade.core;

import jade.domain.FIPANames;

/**
 * Description here
 * 
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller Esquerra</a>
 * @author Carles Garrigues
 * @author <a href="mailto:Jordi.Cucurull@uab.es">Jordi Cucurull Juan</a>
 * 
 */
public class PlatformID implements Location {
	private static final String NO_NAME = "__NO_NAME__";
	
	public PlatformID() {	
		_amsAID = new AID(FIPANames.AMS+'@'+NO_NAME, AID.ISGUID);
	}
	
	public PlatformID(AID amsAID){
		_amsAID = amsAID;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Location#getID()
	 */
	public String getID() {
		return _amsAID.getName();
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Location#getName()
	 */
	public String getName() {
		String name = _amsAID.getName();
		return name.substring(name.lastIndexOf('@') + 1);
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Location#getProtocol()
	 */
	public String getProtocol() {
		String protocol = null;
		String address = getAddress();
		if (address != null) {
			int index = address.indexOf(':');
			if (index > 0) {
				protocol = address.substring(0, index);
			}
		}
		return protocol;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Location#getAddress()
	 */
	public String getAddress() {
		return (String)_amsAID.getAllAddresses().next();
	}
	
	public AID getAmsAID(){
		return _amsAID;
	}
	
	public void setID(String id) {
		_amsAID.setName(id);
	}
	
	public void setName(String name) {
		_amsAID.setName(FIPANames.AMS+'@'+name);
	}
	
	public void setProtocol(String protocol) {
		// Do nothing: this is just for ontology support
	}
	
	public void setAddress(String address) {
		_amsAID.clearAllAddresses();
		_amsAID.addAddresses(address);
	}
	
	public void setAmsAID(AID amsAID) {
		_amsAID = amsAID;
	}
	
	private AID _amsAID;
}
