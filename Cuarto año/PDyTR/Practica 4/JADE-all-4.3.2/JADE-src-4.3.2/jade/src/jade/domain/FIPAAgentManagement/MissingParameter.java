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

import jade.content.Predicate;

/** 
* 
* @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date: 2003-03-10 16:38:45 +0100 (lun, 10 mar 2003) $ $Revision: 3775 $
*/

public class MissingParameter extends RefuseException implements Predicate {

  public MissingParameter() {
    this("Unkown-object", "Unknown-parameter-name"); 
  }
  
public MissingParameter(String objectName, String parameterName) {
  super("(missing-parameter "+objectName+" "+parameterName+")");
  s1=objectName;
  s2=parameterName;
}


/**
  @serial
  */
String s1,s2;
public void setObjectName(String a){s1=a; setMessage("(missing-parameter "+s1+" "+s2+")");}
public String getObjectName() {return s1;}
public void setParameterName(String a){s2=a; setMessage("(missing-parameter "+s1+" "+s2+")");}
public String getParameterName() {return s2;}


}