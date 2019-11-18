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

package demo.MeetingScheduler.Ontology;

import jade.core.AID;
import jade.content.Concept;
/**

@author Fabio Bellifemine - CSELT S.p.A
@version $Date: 2003-03-19 16:07:33 +0100 (mer, 19 mar 2003) $ $Revision: 3843 $
*/

public class Person implements Concept
{
    String name;   // name of the person
    AID dfName; // name of the DF with which this person is known
  AID aid;   // aid of the agent
    
  // used by the Ontology support //
public Person() { }

public Person(String userName){
  this(userName, new AID(userName, AID.ISLOCALNAME), new AID("unkwnown", AID.ISLOCALNAME));
}

public Person(String userName, AID agentName,  AID dfName) {
  name=userName;
  this.dfName=dfName;
  aid = agentName;
}

    public String getName() {
        return name;
    }

public void setName(String n) {
  name = n;
}



public void setDFName(AID df){ dfName = df; }
public AID getDFName() { return dfName; }
  /**
   * This method returns the AID of the agent corresponding to this person
   **/
public AID getAID(){
    return aid; 
}

public void setAID(AID n){ aid=n; }
	
public String toString() {
  return "Mr./Mrs. "+ name + " - "+aid.toString()+" registered with DF " + dfName.getName();
}

}
