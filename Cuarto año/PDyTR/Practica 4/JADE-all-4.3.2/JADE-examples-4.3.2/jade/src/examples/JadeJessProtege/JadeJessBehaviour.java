// Oliver Hoffmann 10 May 2001 oliver@hoffmann.org
// in collaboration with Fabio Bellifemine and Ernest Friedmann-Hill
package examples.JadeJessProtege;
import jess.*;
import java.io.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
public class JadeJessBehaviour extends CyclicBehaviour // tells JESS about incoming messages
  {
  private Agent agent; // the agent that started this behaviour
  private Rete jess; // the JESS engine attached to this behaviour
  public Rete getRete ()
    {
    return jess;
    }
  private int executedJessPasses = -1;
  private int maximumJessPasses = 0;
  private JadeJessBehaviour () // to prevent initialization without proper parameters
    {
    }
  public JadeJessBehaviour(Agent jadeJessAgent,Rete rete,int max) throws JessException
    {
    this (jadeJessAgent,rete);
    maximumJessPasses = max;
    } // end JadeJessBehaviour with maximum JESS passes
  public JadeJessBehaviour(Agent jadeJessAgent,Rete rete) throws JessException
    {
    if (jadeJessAgent == null)
      throw new JessException ("JadeJessBehaviour","no agent provided",0);
    else
      agent = jadeJessAgent;
    if (rete == null)
      throw new JessException ("JadeJessBehaviour","no JESS engine provided",0);
    else
      jess = rete; // take the JESS engine provided via constructor parameter
    } // end JadeJessBehaviour constructor without maximum JESS passes
  public JadeJessBehaviour(JadeJessAgent jadeJessAgent) throws JessException
  	{
  	this (jadeJessAgent,jadeJessAgent.getRete()); // if no JESS engine is provided, take the default engine as stored by the agent
  	}
  private ACLMessage receive ()
    {
    ACLMessage message = null;
    if (executedJessPasses < maximumJessPasses)
      {
      message = agent.blockingReceive();
      }
    else
      {
      message = agent.receive();
      }
    return message;
    } // end receive method
  private void tell (ACLMessage message) // make a JESS fact pointing to the JADE message
    {
    try
      {
      jess.store("MESSAGE",message); // store java pointer to message in JESS variable
      jess.executeCommand ("(assert (Message (fetch MESSAGE)))"); // make JESS fact with pointer to message
      }
    catch (JessException jessException)
      {
      jessException.printStackTrace(System.err);
      }
    } // end tell method
  private void run() // run the JESS engine, with an optional limit on the number of passes
    {
    try
      {
      if (maximumJessPasses > 0)
        executedJessPasses=jess.run(maximumJessPasses);
      else
        jess.run();
      }
    catch (JessException jessException)
      {
      jessException.printStackTrace(System.err);
      }
    } // end run method
  public void action() // executes agent behaviour in reaction to messages
    {
    ACLMessage message=receive(); // get a message from another agent
    if (message != null) // only do something if anything arrived
      {
      tell (message); // tell JESS about the message
      run(); // run the JESS engine on the new message
      }
    } // end behaviour action method
  } // end JadeJessBehaviour class
