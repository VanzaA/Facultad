/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package examples.content.eco;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import examples.content.eco.elements.Costs;

/**
 * Ontology containing concepts related to buying/selling items.
 * 
 * @author Giovanni Caire - TILAB
 */
public class ECommerceOntology extends BeanOntology {
	private static final long serialVersionUID = 1L;

	// NAME
	public static final String ONTOLOGY_NAME = "E-Commerce-ontology";

	// The singleton instance of this ontology
	private static Ontology INSTANCE;

	public synchronized final static Ontology getInstance() throws BeanOntologyException {
		if (INSTANCE == null) {
			INSTANCE = new ECommerceOntology();
		}
		return INSTANCE;
	}

	/**
	 * Constructor
	 * 
	 * @throws BeanOntologyException
	 */
	private ECommerceOntology() throws BeanOntologyException {
		super(ONTOLOGY_NAME);

		String pkgname = Costs.class.getName();
		pkgname = pkgname.substring(0, pkgname.lastIndexOf("."));
		add(pkgname);
	}
}
