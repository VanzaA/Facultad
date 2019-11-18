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
package examples.content.mso;

import jade.content.onto.OntologyUtils;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import examples.content.eco.ECommerceOntology;
import examples.content.mso.elements.CD;
import examples.content.mso.elements.Single;
import examples.content.mso.elements.Track;

/**
 * Ontology containing music related concepts.
 * 
 * @author Giovanni Caire - TILAB
 */
public class MusicShopOntology extends BeanOntology {
	private static final long serialVersionUID = 1L;

	// NAME
	public static final String ONTOLOGY_NAME = "Music-shop-ontology";

	// The singleton instance of this ontology
	private static Ontology INSTANCE;

	public synchronized final static Ontology getInstance() throws BeanOntologyException {
		if (INSTANCE == null) {
			INSTANCE = new MusicShopOntology();
		}
		return INSTANCE;
	}

	/**
	 * Constructor
	 * 
	 * @throws BeanOntologyException
	 */
	private MusicShopOntology() throws BeanOntologyException {
		super(ONTOLOGY_NAME, ECommerceOntology.getInstance());

		add(Track.class);
		add(CD.class);
		add(Single.class);
	}

	public static void main(String[] args) throws Exception {
		Ontology onto = getInstance();
		OntologyUtils.exploreOntology(onto);
		onto = ECommerceOntology.getInstance();
		OntologyUtils.exploreOntology(onto);
	}
}
