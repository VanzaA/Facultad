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

import jade.content.Predicate;

/**
* @author Giovanni Caire - CSELT S.p.A
* @version $Date: 2002-07-31 17:27:34 +0200 (mer, 31 lug 2002) $ $Revision: 3315 $
*/
public class WorksFor implements Predicate {
	
	private Company	_company;							//Company employer
	private Person	_person;							//Person employee
	
	//These methods are used by the JADE-framework
	public void setPerson(Person person) {
		_person=person;
	}
	public Person getPerson() {
		return _person;
	}
	
	public void setCompany(Company company) {
		_company=company;
	}
	public Company getCompany() {
		return _company;
	}

}