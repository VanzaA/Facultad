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

package examples.inprocess;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.wrapper.*;

/**
   This class is an example of how you can embed JADE runtime
   environment within your applications.

   @author Giovanni Rimassa - Universita' di Parma

 */
public class InProcessTest {

  // Simple class behaving as a Condition Variable
  public static class CondVar {
    private boolean value = false;

    synchronized void waitOn() throws InterruptedException {
      while(!value) {
	wait();
      }
    }

    synchronized void signal() {
      value = true;
      notifyAll();
    }

  } // End of CondVar class


  // This class is a custom agent, accepting an Object through the
  // object-to-agent communication channel, and displying it on the
  // standard output.
  public static class CustomAgent extends jade.core.Agent {

    public void setup() {
      // Accept objects through the object-to-agent communication
      // channel, with a maximum size of 10 queued objects
      setEnabledO2ACommunication(true, 10);

      // Notify blocked threads that the agent is ready and that
      // object-to-agent communication is enabled
      Object[] args = getArguments();
      if(args.length > 0) {
	CondVar latch = (CondVar)args[0];
	latch.signal();
      }

      // Add a suitable cyclic behaviour...
      addBehaviour(new jade.core.behaviours.CyclicBehaviour() {

	public void action() {
	  // Retrieve the first object in the queue and print it on
	  // the standard output
	  Object obj = getO2AObject();
	  if(obj != null) {
	    System.out.println("Got an object from the queue: [" + obj + "]");
	  }
	  else 
	    block();
	}

      });
    }

    public void takeDown() {
      // Disables the object-to-agent communication channel, thus
      // waking up all waiting threads
      setEnabledO2ACommunication(false, 0);
    }

  } // End of CustomAgent class

  public static void main(String args[]) {

    try {

      // Get a hold on JADE runtime
      Runtime rt = Runtime.instance();

      // Exit the JVM when there are no more containers around
      rt.setCloseVM(true);

      // Check whether a '-container' flag was given
      if(args.length > 0) {
	if(args[0].equalsIgnoreCase("-container")) {
	  // Create a default profile
	  Profile p = new ProfileImpl(false);
	  //p.setParameter(Profile.MAIN, "false");

	  // Create a new non-main container, connecting to the default
	  // main container (i.e. on this host, port 1099)
      System.out.println("Launching the agent container ..."+p);
	  AgentContainer ac = rt.createAgentContainer(p);

	  // Create a new agent, a DummyAgent
	  AgentController dummy = ac.createNewAgent("inProcess", "jade.tools.DummyAgent.DummyAgent", new Object[0]);

	  // Fire up the agent
	  System.out.println("Starting up a DummyAgent...");
	  dummy.start();

	  // Wait for 10 seconds
	  Thread.sleep(10000);

	  // Kill the DummyAgent
	  System.out.println("Killing DummyAgent...");
	  dummy.kill();

	  // Create another peripheral container within the same JVM
	  // NB. Two containers CAN'T share the same Profile object!!! -->
	  // Create a new one.
	  p = new ProfileImpl(false);
	  //p.putProperty(Profile.MAIN, "false");
	  AgentContainer another = rt.createAgentContainer(p);

	  // Launch the Mobile Agent example
	  // and pass it 2 arguments: a String and an object reference
	  Object[] arguments = new Object[2];
	  arguments[0] = "Hello World!";
	  arguments[1]=dummy;
	  AgentController mobile = another.createNewAgent("Johnny", "examples.mobile.MobileAgent", arguments);
	  mobile.start();

	  return;
	}
      }

      // Launch a complete platform on the 8888 port
      // create a default Profile 
      Profile pMain = new ProfileImpl(null, 8888, null);

      System.out.println("Launching a whole in-process platform..."+pMain);
      AgentContainer mc = rt.createMainContainer(pMain);

      // set now the default Profile to start a container
      ProfileImpl pContainer = new ProfileImpl(null, 8888, null);
      System.out.println("Launching the agent container ..."+pContainer);
      AgentContainer cont = rt.createAgentContainer(pContainer);
      System.out.println("Launching the agent container after ..."+pContainer);

      System.out.println("Launching the rma agent on the main container ...");
      AgentController rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
      rma.start();

      // Launch a custom agent, taking an object via the
      // object-to-agent communication channel. Notice how an Object
      // is passed to the agent, to achieve a startup synchronization:
      // this Object is used as a POSIX 'condvar' or a Win32
      // 'EventSemaphore' object...

      CondVar startUpLatch = new CondVar();

      AgentController custom = mc.createNewAgent("customAgent", CustomAgent.class.getName(), new Object[] { startUpLatch });
      custom.start();

      // Wait until the agent starts up and notifies the Object
      try {
	startUpLatch.waitOn();
      }
      catch(InterruptedException ie) {
	ie.printStackTrace();
      }
	    

      // Put an object in the queue, asynchronously
      System.out.println("Inserting an object, asynchronously...");
      custom.putO2AObject("Message 1", AgentController.ASYNC);
      System.out.println("Inserted.");

      // Put an object in the queue, synchronously
      System.out.println("Inserting an object, synchronously...");
      custom.putO2AObject(mc, AgentController.SYNC);
      System.out.println("Inserted.");

    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

}
