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
package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * This is an example of recursive aggregation of composite agent behaviours.
 * A composite behaviour is created, composed of some Sequential Behaviours,
 * and OneShot behaviours.
 * @author Giovanni Rimassa - Università di Parma
 * @version  $Date: 2003-12-03 17:46:36 +0100 (mer, 03 dic 2003) $ $Revision: 4630 $
 */
public class ComplexBehaviourAgent extends Agent {
  class SingleStepBehaviour extends OneShotBehaviour {
    private String myStep;

    public SingleStepBehaviour(Agent a, String step) {
      super(a);
      myStep = step;
    }

    public void action() {
      System.out.println("Agent "+getName()+": Step "+myStep);
    } 

  }

  protected void setup() {
    SequentialBehaviour myBehaviour1 = new SequentialBehaviour(this) {
      public int onEnd() {
        reset();
        return super.onEnd();
      } 

    };
    SequentialBehaviour myBehaviour2 = new SequentialBehaviour(this);
    SequentialBehaviour myBehaviour2_1 = new SequentialBehaviour(this);
    SequentialBehaviour myBehaviour2_2 = new SequentialBehaviour(this);

    myBehaviour2_1.addSubBehaviour(new SingleStepBehaviour(this, "2.1.1"));
    myBehaviour2_1.addSubBehaviour(new SingleStepBehaviour(this, "2.1.2"));
    myBehaviour2_1.addSubBehaviour(new SingleStepBehaviour(this, "2.1.3"));
    myBehaviour2_2.addSubBehaviour(new SingleStepBehaviour(this, "2.2.1"));
    myBehaviour2_2.addSubBehaviour(new SingleStepBehaviour(this, "2.2.2"));

    Behaviour b = new SingleStepBehaviour(this, "2.2.3");

    myBehaviour2_2.addSubBehaviour(b);
    myBehaviour1.addSubBehaviour(new SingleStepBehaviour(this, "1.1"));
    myBehaviour1.addSubBehaviour(new SingleStepBehaviour(this, "1.2"));
    myBehaviour1.addSubBehaviour(new SingleStepBehaviour(this, "1.3"));
    myBehaviour2.addSubBehaviour(myBehaviour2_1);
    myBehaviour2.addSubBehaviour(myBehaviour2_2);
    myBehaviour2.addSubBehaviour(new SingleStepBehaviour(this, "2.3"));
    myBehaviour2.addSubBehaviour(new SingleStepBehaviour(this, "2.4"));
    myBehaviour2.addSubBehaviour(new SingleStepBehaviour(this, "2.5"));
    addBehaviour(myBehaviour1);
    addBehaviour(myBehaviour2);
  } 
}

