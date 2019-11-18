
/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package examples.jess;

import jade.core.Agent;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date: 2000-09-12 15:24:08 +0200 (mar, 12 set 2000) $ $Revision: 1857 $
*/

/**
 * This is a simple sample JADE Agent that embeds a Jess engine.
 * It instantiates and adds only one behaviour. This behaviour is BasicJessBehaviour
 * that only asserts messages when they arrive and use Jess as a reasoning tool
 * This agent executes the Jess code in the file examples/jess/JadeAgent.clp
 */
public class JessAgent extends Agent {

  /** 
   * adds the JessBehaviour and that's all.
   */
  protected void setup() {
    // add the behaviour
    // 1 is the number of steps that must be executed at each run of
    // the Jess engine before giving back the control to the Java code
    addBehaviour(new BasicJessBehaviour(this,"examples/jess/JadeAgent.clp",1)); 
  }
}

