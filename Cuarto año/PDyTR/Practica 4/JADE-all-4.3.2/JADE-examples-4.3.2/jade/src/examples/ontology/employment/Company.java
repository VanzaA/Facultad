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

package examples.ontology.employment;

import jade.content.Concept;

/**
* @author Angelo Difino - CSELT S.p.A
* @version $Date: 2002-07-31 17:27:34 +0200 (mer, 31 lug 2002) $ $Revision: 3315 $
*/
public class Company implements Concept {
	
	private String 	_name;						//Company's name
	private Address	_address;					//Headquarter's address
	
	// Methods required to use this class to represent the COMPANY role
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}
	public void setAddress(Address address) {
		_address=address;
	}
	public Address getAddress() {
		return _address;
	}
	
	// Other application specific methods
	public boolean equals(Company c){
		return (_name.equalsIgnoreCase(c.getName()));
	}
}