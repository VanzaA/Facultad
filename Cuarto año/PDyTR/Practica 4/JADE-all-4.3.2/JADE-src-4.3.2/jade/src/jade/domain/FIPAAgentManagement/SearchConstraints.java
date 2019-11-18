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
import jade.content.Concept;

import jade.content.Concept;

/**
* This class models a search constraint.
* @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date: 2013-06-16 12:43:57 +0200 (dom, 16 giu 2013) $ $Revision: 6677 $
*/
public class SearchConstraints implements Concept {

    private Long max_depth = null; 

    private Long max_results = null;

    private String search_id = null;
    private int cnt = 0;

    /**
     * Default constructor. Creates a new SearchConstraints by setting
     * default value, as defined by FIPA, for max_depth (i.e. 0 that
     * corresponds to no propagation of the search to the federated
     * DFs) and max_results (i.e. 1 result only to be returned).
     * Furthermore, a new globally unique identifier is created for
     * the value of search_id.  WARNING: When the same object is
     * reused for several searches, it is recommended to call the
     * method <code>renewSearchId</code> in order to create a new
     * globally unique identifier. Otherwise, the DF might reply with
     * a FAILURE having received already the same search.
     **/
    public SearchConstraints () {
    }
    
    /** Regenerate the value of search_id as a globally unique identifier.
     * This call is recommended in order to reuse the same object for several
     * searches. Otherwise, the DF might reply with a FAILURe having received
     * already the same search_id.
     **/
    public void renewSearchId() {
    	// We also use a counter since System.currentTimeMillis() is not precise --> 
    	// Two calls to renewSearchId() may result in the same search_id value
    	// if they occur very close.
        search_id = "s" + hashCode() + "_" + System.currentTimeMillis() + String.valueOf(cnt++);
        if (cnt >= 100) {
        	cnt = 0;
        }
    }


    /**
       Set the <code>search-id</code> slot of this object.
       @param searchId The unique identifier associated with this
       search operation.
    */
    public void setSearchId(String searchId) {
	search_id = searchId;
    }

    /**
       Retrieve the <code>search-id</code> slot of this object. This
       slot uniquely identifies a search operation.
       @return The value of the <code>search-id</code> slot of this
       object, or <code>null</code> if no value was set.
    */
    public String getSearchId() {
	return search_id;
    }


    /**
       Set the <code>max-depth</code> slot of this object.
       @param l The value of the maximum recursion depth of this
       search over the DF federation graph.
    */
    public void setMaxDepth(Long l) {
	max_depth = l;
    }

    /**
       Retrieve the <code>max-depth</code> slot of this object. This
       slot describes the maximum recursion depth of this search over
       the DF federation graph.
       @return The value of the <code>max-depth</code> slot of this
       envelope, or <code>null</code> if no value was set.
    */
    public Long getMaxDepth() {
	return max_depth;
    }

    /**
       Set the <code>max-results</code> slot of this object.
       @param l The maximum number of results to retrieve
       in response to this search operation.
    */
    public void setMaxResults(Long l) {
	max_results = l;
    }

    /**
       Retrieve the <code>max-results</code> slot of this object. This
       slot contains the maximum number of results to retrieve in
       response to this search operation.
       @return The value of the <code>max-results</code> slot of this
       SearchConstraints object, or <code>null</code> if no value was set.
    */
    public Long getMaxResults() {
	return max_results;
    }

}
