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
package jade.content.onto;

//#APIDOC_EXCLUDE_FILE

import jade.content.abs.AbsObject;

/** 
   This interface must be implemented by ontological classes that
   belong to an ontology using the <code>MicroIntrospector</code>.
   It includes methods by means of which an object can be converted
   into/from an abstract descriptor.
   @see jade.content.onto.MicroIntrospector
   @author Giovanni Caire - TILAB
 */
public interface Introspectable {

    /**
     * Externalise this object into the given abstract descriptor
     * @param abs The abstract descriptor this object must externalise 
     * itself into.
     * @param onto The reference ontology 
     * @throws OntologyException If some error occurs during the externalisation
     */
    public void externalise(AbsObject abs, Ontology onto) throws OntologyException; 

    /**
     * Internalise this object from a given abstract descriptor 
     * @param abs The abstract descriptor this object must internalise 
     * itself from
     * @param onto The reference ontology 
     * @throws UngroundedException If the abstract descriptor  
     * contains a variable
     * @throws OntologyException If some error occurs during the internalisation
     */
    public void internalise(AbsObject abs, Ontology onto) throws UngroundedException, OntologyException; 
}
